package org.rpi.airplay;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.Base64;
import org.rpi.utils.SecUtils;

public class RtspRequestHandlerSimple extends SimpleChannelInboundHandler<FullHttpRequest> implements Observer {

	private static final String RSAAESKEY = "a=rsaaeskey:";
	private static final String AESIV = "a=aesiv:";
	private static final String FMTP = "a=fmtp:";
	private static final String VOLUME = "volume:";
	private AudioServer audioServer = null;
	private Logger log = Logger.getLogger(this.getClass());
	private boolean disconnectChannel = false;
	private String client_name = "iTunes";
	private String clientInstance = "";
	private ChannelAirPlay channel = null;
	private boolean ignore = false;

	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'>AirPlay</dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>AirPlay</upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	public RtspRequestHandlerSimple() {
		PlayManager.getInstance().observeAirPlayEvents(this);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (disconnectChannel) {
			try {
				ctx.channel().close();
			} catch (Exception e) {
				log.error("Error Disconnecting Channel");
			} finally {
				disconnectChannel = false;
			}
		}

		if (request instanceof FullHttpRequest) {
			// log.debug("FullRequest " + request.content().capacity());

			String content = request.toString();
			// log.debug(content);
			if (!RtspVersions.RTSP_1_0.equals(request.getProtocolVersion())) {
				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
				response.headers().add("Connection", "close");
				ctx.writeAndFlush(response);
				ctx.disconnect();
				return;
			}
			// HttpMethod methods = request.getMethod();
			FullHttpResponse response = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
			response.headers().clear();

			String cSeq = request.headers().get("CSeq");
			if (cSeq != null) {
				response.headers().add("CSeq", cSeq);
			}

			response.headers().add("Audio-Jack-Status", "connected; type=analog");

			String challenge = request.headers().get("Apple-Challenge");
			if (challenge != null) {
				SocketAddress remoteAddress = ctx.channel().localAddress();
				response.headers().add("Apple-Response", Utils.getChallengeResponse(challenge, ((InetSocketAddress) remoteAddress).getAddress(), AudioSessionHolder.getInstance().getHardWareAddress()));
			}

			// String clientInstance = request.headers().get("DACP-ID");
			clientInstance = request.headers().get("DACP-ID");
			if (org.rpi.utils.Utils.isEmpty(clientInstance)) {
				// throw new RuntimeException("No Client Instance given");
				try {
					clientInstance = RaopSessionManager.getFirstSession();
				} catch (Exception e) {
					log.error("Did not get DACP_ID, attempted to get first session id", e);
				}
			}

			HttpMethod method = request.getMethod();
			if (RtspMethods.ANNOUNCE.equals(method)) {
				log.debug("ANNOUNCE \r\n" + request.toString() + content + "\r\n");
				disconnectChannel = false;
				String client_name = "iTunes";
				if (request.headers().contains("X-Apple-Client-Name")) {
					client_name = URLDecoder.decode(request.headers().get("X-Apple-Client-Name"), "UTF-8");
				}
				channel = new ChannelAirPlay("", metaData, 1, client_name, Config.getInstance().getResourceURIPrefix() + "org/rpi/image/AirPlay.png");
				PlayManager.getInstance().playAirPlayer(channel);

				String s = request.content().toString(Charset.defaultCharset());
				BufferedReader reader = new BufferedReader(new StringReader(s));
				String line;
				String rsaAesKey = null;
				String aesIv = null;
				String fmtp = null;
				// Get attributes of Service-Discovery-Protocol
				while ((line = reader.readLine()) != null) {
					log.debug(line);
					if (line.startsWith(RSAAESKEY)) {
						rsaAesKey = line.substring(RSAAESKEY.length());
					} else if (line.startsWith(AESIV)) {
						aesIv = line.substring(AESIV.length());
					} else if (line.startsWith(FMTP)) {
						fmtp = line.substring(FMTP.length());
					}
				}

				if (org.rpi.utils.Utils.isEmpty(aesIv)) {
					// throw new RuntimeException("No AES Iv");
					return;
				}

				if (org.rpi.utils.Utils.isEmpty(rsaAesKey)) {
					// throw new RuntimeException("No RSA AES Key");
					return;
				}
				AudioSession session = RaopSessionManager.getSession(clientInstance);
				if (session == null) {
					// session = new AudioSession(Base64.decode(aesIv),
					session = new AudioSession(Base64.decode(aesIv), SecUtils.decryptRSA(Base64.decode(rsaAesKey)), fmtp, 0, 0);
					RaopSessionManager.addSession(clientInstance, session);
					log.debug("Audio Session being created");
					AudioSessionHolder.getInstance().setSession(session);
				} else {
					log.debug("Audio Session being updated");
					session.setAESIV(Base64.decode(aesIv));
					//
					session.setAESKEY(SecUtils.decryptRSA(Base64.decode(rsaAesKey)));
					session.setFmtp(fmtp);
					AudioSessionHolder.getInstance().setSession(session);
				}

			} else if (RtspMethods.SETUP.equals(method)) {
				log.debug("SETUP \r\n" + request.toString() + content + "\r\n");
				tearDown();
				RaopSessionManager.shutdownSession(clientInstance);
				disconnectChannel = false;
				AudioSession session = RaopSessionManager.getSession(clientInstance);
				if (session == null) {
					throw new RuntimeException("No Session " + clientInstance);
				}
				String transport = request.headers().get("Transport");
				setPorts(session, transport);
				session.setLocalAddress((InetSocketAddress) ctx.channel().localAddress());
				session.setRemoteAddress((InetSocketAddress) ctx.channel().remoteAddress());
				audioServer = new AudioServer(session);
				PlayManager.getInstance().setStatus("Playing", "AIRPLAY");
				//PluginGateWay.getInstance().setSourceId("AirPlay", "AirPlay");
				PluginGateWay.getInstance().setSourceByname("AirPlay");
				EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
				ev.setTitle(client_name);
				ev.setArtist("AirPlay");
				PlayManager.getInstance().updateTrackInfo(ev);
				response.headers().add("Transport", request.headers().get("Transport") + ";server_port=" + session.getControlPort());
				log.debug("SetUp Response: " + response.toString() + "\r\n");
			} else if (RtspMethods.RECORD.equals(method)) {
				log.debug("RECORD \r\n" + request.toString() + content + "\r\n");
				// ignore
			} else if ("FLUSH".equalsIgnoreCase(method.name())) {
				log.debug("FLUSH \r\n" + request.toString() + content + "\r\n");
				audioServer.flush();
			} else if (RtspMethods.TEARDOWN.equals(method)) {
				log.debug("TEARDOWN \r\n" + request.toString() + content + "\r\n");
				tearDown();
				response.headers().set("Connection", "close");
				ctx.writeAndFlush(response);
				RaopSessionManager.shutdownSession(clientInstance);
				ctx.disconnect();
				PlayManager.getInstance().setStatus("Stopped", "AIRPLAY");
			}
			else if (RtspMethods.GET_PARAMETER.equals(method)) {
				log.debug("GET_PARAMETER" + "\r\n");
			} else if ("DENIED".equalsIgnoreCase(method.name())) {
				log.debug("DENIED");
			} else {
				log.info("Unknown RSTPMethod: " + method.name() + "\r\n");
			}

			boolean keepAlive = isKeepAlive(request);

			if (keepAlive) {
				response.headers().add("Content-Length", response.content().readableBytes());
			}
			response.headers().add("Session", "WENEEDASSESSION");
			// log.debug("Respone: " + response.toString());
			if (!ignore) {
				ChannelFuture future = ctx.writeAndFlush(response);
				if (!keepAlive) {
					future.addListener(ChannelFutureListener.CLOSE);
				}
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Exception Caught: " + cause);
		ctx.close();
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

		// If iTunes is setting up the session we need to set the ports and let
		// iTunes know..
		if (controlPort == 0) {
			controlPort = Config.getInstance().getAirPlayPort() + 1;
		} else if (timingPort == 0) {
			timingPort = Config.getInstance().getAirPlayPort() + 2;
		}

		session.setControlPort(controlPort);
		session.setTimingPort(timingPort);
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

	public String getChallengeResponse(String challenge, InetAddress address, byte[] hwAddress) {
		{
			// BASE64 DECODE
			byte[] decoded = Base64.decode(challenge);

			byte[] ip = address.getAddress();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// Challenge
			try {
				out.write(decoded);
				// IP-Address
				out.write(ip);
				// HW-Addr
				out.write(hwAddress);

				// Pad to 32 Bytes
				int padLen = 32 - out.size();
				for (int i = 0; i < padLen; ++i) {
					out.write(0x00);
				}

			} catch (Exception e) {
				// log.error(e);
			}

			// RSA
			byte[] crypted = SecUtils.encryptRSA(out.toByteArray());

			// Encode64
			String ret = Base64.encode(crypted);

			// On retire les ==
			return ret = ret.replace("=", "").replace("\r", "").replace("\n", "");

			// Write
			// response.append("Apple-Response", ret);
		}
	}

	public String byteToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
}
