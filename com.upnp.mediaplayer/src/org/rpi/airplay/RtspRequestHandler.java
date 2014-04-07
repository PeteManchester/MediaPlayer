package org.rpi.airplay;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;

import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.handler.codec.rtsp.RtspVersions;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.SecUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RtspRequestHandler extends SimpleChannelUpstreamHandler implements Observer {
	private static final String RSAAESKEY = "a=rsaaeskey:";
	private static final String AESIV = "a=aesiv:";
	private static final String FMTP = "a=fmtp:";
	private static final String VOLUME = "volume:";
	private AudioServer audioServer = null;
	private Logger log = Logger.getLogger(this.getClass());
	private boolean closeChannel = false;

	public RtspRequestHandler() {
		PlayManager.getInstance().observeAirPlayEvents(this);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		// log.debug(request.toString());

		if (!RtspVersions.RTSP_1_0.equals(request.getProtocolVersion())) {
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			response.addHeader("Connection", "close");
			e.getChannel().write(response);
			e.getChannel().disconnect();
			return;
		}
		HttpMethod methods = request.getMethod();
		HttpResponse response = new DefaultHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
		response.clearHeaders();

		String cSeq = request.getHeader("CSeq");
		if (cSeq != null) {
			response.addHeader("CSeq", cSeq);
		}

		response.addHeader("Audio-Jack-Status", "connected; type=analog");

		String challenge = request.getHeader("Apple-Challenge");
		if (challenge != null) {
			// SocketAddress remoteAddress = e.getRemoteAddress();
			SocketAddress remoteAddress = e.getChannel().getLocalAddress();
			response.addHeader("Apple-Response", Utils.getChallengeResponse(challenge, ((InetSocketAddress) remoteAddress).getAddress(), AudioSessionHolder.getInstance().getHardWareAddress()));
		}

		// String clientInstance = request.getHeader("DACP-ID");
		String clientInstance = request.getHeader("DACP-ID");
		if (org.rpi.utils.Utils.isEmpty(clientInstance)) {
			throw new RuntimeException("No Client Instance given");
		}

		HttpMethod method = request.getMethod();
		if (RtspMethods.OPTIONS.equals(method)) {
			response.addHeader("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
		} else if (RtspMethods.ANNOUNCE.equals(method)) {
			String content = request.getContent().toString(Charset.forName("UTF-8"));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				request.getContent().readBytes(out, request.getContent().readableBytes());
				BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
				String line;
				String rsaAesKey = null;
				String aesIv = null;
				String fmtp = null;
				// Get attributes of Service-Discovery-Protocol
				while ((line = reader.readLine()) != null) {
					if (line.startsWith(RSAAESKEY)) {
						rsaAesKey = line.substring(RSAAESKEY.length());
					} else if (line.startsWith(AESIV)) {
						aesIv = line.substring(AESIV.length());
					} else if (line.startsWith(FMTP)) {
						fmtp = line.substring(FMTP.length());
					}
				}

				if (org.rpi.utils.Utils.isEmpty(aesIv)) {
					throw new RuntimeException("No AES Iv");
				}

				if (org.rpi.utils.Utils.isEmpty(rsaAesKey)) {
					throw new RuntimeException("No RSA AES Key");
				}

				AudioSession session = RaopSessionManager.getSession(clientInstance);
				if (session == null) {
					session = new AudioSession(Base64.decode(aesIv), SecUtils.decryptRSA(Base64.decode(rsaAesKey)), fmtp, 0, 0);
					RaopSessionManager.addSession(clientInstance, session);
				} else {
					session.setAESIV(Base64.decode(aesIv));
					session.setAESKEY(SecUtils.decryptRSA(Base64.decode(rsaAesKey)));
					session.setFmtp(fmtp);
				}
				String client_name = URLDecoder.decode(request.getHeader("X-Apple-Client-Name"),"UTF-8");
				String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";
				ChannelAirPlay channel = new ChannelAirPlay("", metaData, 1, client_name);
				PlayManager.getInstance().playAirPlayer(channel);
				EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
				ev.setTitle(client_name);
				ev.setArtist("AirPlay");
				PlayManager.getInstance().updateTrackInfo(ev);
				// TODO Maybe Don't need to do this, with better refactoring.
				AudioSessionHolder.getInstance().setSession(session);
			} finally {
				out.close();
			}
		} else if (RtspMethods.SETUP.equals(method)) {
			AudioSession session = RaopSessionManager.getSession(clientInstance);
			if (session == null) {
				throw new RuntimeException("No Session " + clientInstance);
			}
			String transport = request.getHeader("Transport");
			setPorts(session, transport);
			audioServer = new AudioServer(session);
			PlayManager.getInstance().setStatus("Playing", "AIRPLAY");
			PluginGateWay.getInstance().setSourceId("AirPlay", "AirPlay");
			response.setHeader("Transport", request.getHeader("Transport") + ";server_port=" + session.getControlPort());
		} else if (RtspMethods.RECORD.equals(method)) {
			// ignore
		} else if ("FLUSH".equalsIgnoreCase(method.getName())) {
			log.debug("FLUSH");
			audioServer.flush();
		} else if (RtspMethods.TEARDOWN.equals(method)) {
			tearDown();
			response.setHeader("Connection", "close");
			e.getChannel().write(response);
			RaopSessionManager.shutdownSession(clientInstance);
			e.getChannel().disconnect();
			return;
		} else if (RtspMethods.SET_PARAMETER.equals(method)) {
			String content = request.getContent().toString(Charset.forName("UTF-8"));
			if(content.startsWith(VOLUME))
			{
				String vol = content.substring(VOLUME.length());
				try
				{
					Double dVol = Double.parseDouble(vol.trim());
					dVol = dVol + 144;
					dVol = dVol*1000;
					//dVol += 100;
					int iVol = dVol.intValue();
					if(iVol !=0)
					{
					iVol = iVol/1000;
					}
					iVol = iVol -44;
					log.debug("Vol: " + iVol);
					//Not very good at maths so use this dodgy method to convert from db in the range -144-0 to long in the range 0-100
					if(iVol> 100)
						iVol = 100;
					if(iVol<0)
						iVol = 0;
					PlayManager.getInstance().setVolume(iVol);
				}
				catch(Exception ex)
				{
					log.error(ex);
				}
			}
		} else if (RtspMethods.GET_PARAMETER.equals(method)) {
			log.debug("GET_PARAMETER");
		} else if ("DENIED".equalsIgnoreCase(method.getName())) {
			log.debug("DENIED");
		} else {
			throw new RuntimeException("Unknown Method: " + method.getName());
		}

		boolean keepAlive = isKeepAlive(request);
		if (keepAlive) {
			response.setHeader("Content-Length", response.getContent().readableBytes());
		}
		// log.debug("Respone: " + response.toString());
		ChannelFuture future = e.getChannel().write(response);

		if (closeChannel) {
			response.setHeader("Connection", "close");
			e.getChannel().write(response);
			RaopSessionManager.shutdownSession(clientInstance);
			e.getChannel().disconnect();
		}

		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	private void setPorts(AudioSession session, String transport) {
		int controlPort = 0;
		int timingPort = 0;

		Pattern controlPortPattern = Pattern.compile(".*control_port=(\\d+).*");
		Matcher controlPortMatcher = controlPortPattern.matcher(transport);
		if (controlPortMatcher.matches()) {
			controlPort = Integer.parseInt(controlPortMatcher.group(1));
		}

		Pattern timingPortPattern = Pattern.compile(".*timing_port=(\\d+).*");
		Matcher timingPortMatcher = timingPortPattern.matcher(transport);
		if (timingPortMatcher.matches()) {
			timingPort = Integer.parseInt(timingPortMatcher.group(1));
		}

		if (controlPort == 0) {
			throw new RuntimeException("no control port");
		} else if (timingPort == 0) {
			throw new RuntimeException("no timing port");
		}

		session.setControlPort(controlPort);
		session.setTimingPort(timingPort);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() != null) {
			log.error("Error ChannelHandler", e.getCause());
		}
		e.getChannel().close();
	}

	private void tearDown() {
		closeChannel = true;
		try {
			if (audioServer != null) {
				audioServer.stop();
				audioServer = null;
			}
		} catch (Exception e) {
			log.error("Error TearDown", e);
		}

	}

	/**
	 * Used to indicate that we need to stop playing
	 */
	@Override
	public void update(Observable o, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTAIRPLAYERSTOP:
			tearDown();
			break;
		}
	}

}
