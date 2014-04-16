package org.rpi.airplay;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;

import static org.jboss.netty.handler.codec.http.HttpHeaders.*;

import org.jboss.netty.handler.codec.rtsp.RtspMethods;
import org.jboss.netty.handler.codec.rtsp.RtspVersions;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.Base64;
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
	private boolean disconnectChannel = false;
	private String client_name = "iTunes";
	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'>AirPlay</dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>AirPlay</upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	public RtspRequestHandler() {
		PlayManager.getInstance().observeAirPlayEvents(this);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (disconnectChannel) {
			try {
				e.getChannel().disconnect();
				return;
			} catch (Exception ex) {
				log.error("Error Disconnecting Channel", ex);
			} finally {
				disconnectChannel = false;
			}
		}
		HttpRequest request = (HttpRequest) e.getMessage();
		String content = request.getContent().toString(Charset.forName("UTF-8"));
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
			// log.debug("OPTIONS \r\n" + request.toString() + content);
		} else if (RtspMethods.ANNOUNCE.equals(method)) {
			log.debug("ANNOUNCE \r\n" + request.toString() + content);
			disconnectChannel = false;
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
					log.debug("Audio Session being created");
					AudioSessionHolder.getInstance().setSession(session);
				} else {
					log.debug("Audio Session being updated");
					session.setAESIV(Base64.decode(aesIv));
					session.setAESKEY(SecUtils.decryptRSA(Base64.decode(rsaAesKey)));
					session.setFmtp(fmtp);
					AudioSessionHolder.getInstance().setSession(session);
				}
				if(request.containsHeader("X-Apple-Client-Name"))
				{
					client_name = URLDecoder.decode(request.getHeader("X-Apple-Client-Name"), "UTF-8");
				}
				ChannelAirPlay channel = new ChannelAirPlay("", metaData, 1, client_name,Config.getInstance().getResourceURIPrefix()+"org/rpi/image/AirPlay.png");
				PlayManager.getInstance().playAirPlayer(channel);
			} finally {
				out.close();
			}
		} else if (RtspMethods.SETUP.equals(method)) {
			log.debug("SETUP \r\n" + request.toString() + content);
			disconnectChannel = false;
			AudioSession session = RaopSessionManager.getSession(clientInstance);
			if (session == null) {
				throw new RuntimeException("No Session " + clientInstance);
			}
			String transport = request.getHeader("Transport");
			setPorts(session, transport);
			session.setLocalAddress((InetSocketAddress)ctx.getChannel().getLocalAddress());
			session.setRemoteAddress((InetSocketAddress)ctx.getChannel().getRemoteAddress());
			audioServer = new AudioServer(session);
			PlayManager.getInstance().setStatus("Playing", "AIRPLAY");
			PluginGateWay.getInstance().setSourceId("AirPlay", "AirPlay");
			EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
			ev.setTitle(client_name);
			ev.setArtist("AirPlay");
			PlayManager.getInstance().updateTrackInfo(ev);			
			response.setHeader("Transport", request.getHeader("Transport") + ";server_port=" + session.getControlPort());
			log.debug("SetUp Response: " + response.toString());
		} else if (RtspMethods.RECORD.equals(method)) {
			log.debug("RECORD \r\n" + request.toString() + content);
			// ignore
		} else if ("FLUSH".equalsIgnoreCase(method.getName())) {
			log.debug("FLUSH \r\n" + request.toString() + content);
			audioServer.flush();
		} else if (RtspMethods.TEARDOWN.equals(method)) {
			log.debug("TEARDOWN \r\n" + request.toString() + content);
			tearDown();
			response.setHeader("Connection", "close");
			e.getChannel().write(response);
			RaopSessionManager.shutdownSession(clientInstance);
			e.getChannel().disconnect();
			PlayManager.getInstance().setStatus("Stopped", "AIRPLAY");
			return;
		} else if (RtspMethods.SET_PARAMETER.equals(method)) {
			log.debug("SETPARAMTER: \r\n" + request.toString() + "\r\n" + content);
			// String content =
			// request.getContent().toString(Charset.forName("UTF-8"));
			if (content.startsWith(VOLUME)) {
				String vol = content.substring(VOLUME.length());
				try {
					Double dVol = Double.parseDouble(vol.trim());
					dVol = dVol + 30;
					dVol = dVol * 1000;

					try {
						dVol = dVol / 300;
					} catch (Exception ex) {
						log.debug(ex);
					}
					long iVol = dVol.longValue();
					if (iVol > 100)
						iVol = 100;
					if (iVol < 0)
						iVol = 0;
					PlayManager.getInstance().setVolume(iVol);
				} catch (Exception ex) {
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
		response.addHeader("Session", "WENEEDASSESSION");
		// log.debug("Respone: " + response.toString());
		ChannelFuture future = e.getChannel().write(response);

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

		//If iTunes is setting up the session we need to set the ports and let iTunes know..
		if (controlPort == 0) {
			controlPort = Config.getInstance().getAirPlayPort() + 1;
		} else if (timingPort == 0) {
			timingPort = Config.getInstance().getAirPlayPort() + 2;
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
		log.debug("Stopping Audio Playing");
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
			log.debug("Stop Airplayer");
			disconnectChannel = true;
			tearDown();
			break;
		}
	}

}
