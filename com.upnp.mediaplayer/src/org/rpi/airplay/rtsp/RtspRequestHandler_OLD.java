package org.rpi.airplay.rtsp;

import org.apache.log4j.Logger;
import org.rpi.airplay.AudioServer;
import org.rpi.airplay.AudioSession;
import org.rpi.airplay.AudioSessionHolder;
import org.rpi.airplay.DecodeDMAP;
import org.rpi.airplay.RaopSessionManager;
import org.rpi.airplay.Utils;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.Base64;
import org.rpi.utils.SecUtils;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
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

public class RtspRequestHandler_OLD extends ChannelInboundHandlerAdapter implements Observer {
	private static final String RSAAESKEY = "a=rsaaeskey:";
	private static final String AESIV = "a=aesiv:";
	
	private static final String FMTP = "a=fmtp:";
	private static final String VOLUME = "volume:";
	private AudioServer audioServer = null;
	private Logger log = Logger.getLogger(this.getClass());
	private boolean disconnectChannel = false;
	private String client_name = "iTunes";
	private boolean bAnnounced = false;
	private boolean bSetParameter = false;
	private String clientInstance = "";
	private ChannelAirPlay channel = null;

	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'>AirPlay</dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>AirPlay</upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	public RtspRequestHandler_OLD() {
		PlayManager.getInstance().observeAirPlayEvents(this);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Exception Caught: " + cause);
		ctx.close();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// The http message and content are new received as separate events in
		// Netty4

		if (disconnectChannel) {
			try {
				ctx.channel().close();
			} catch (Exception e) {
				log.error("Error Disconnecting Channel");
			} finally {
				disconnectChannel = false;
			}
		}
		
		
		if(msg instanceof FullHttpRequest)
		{
			FullHttpRequest request = (FullHttpRequest)msg;
			//log.debug("FullRequest " + request.content().capacity());
			
			
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
			if (RtspMethods.OPTIONS.equals(method)) {
				response.headers().add("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
				// log.debug("OPTIONS \r\n" + request.toString() + content);
			} else if (RtspMethods.ANNOUNCE.equals(method)) {
				log.debug("ANNOUNCE \r\n" + request.toString() + content + "\r\n");
				disconnectChannel = false;
				bAnnounced = true;
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
				bAnnounced = false;
				AudioSession session = RaopSessionManager.getSession(clientInstance);
				if (session == null) {
					// session = new AudioSession(Base64.decode(aesIv),
					session = new AudioSession(Base64.decode(aesIv), SecUtils.getInstance().decryptRSA(Base64.decode(rsaAesKey)), fmtp, 0, 0);
					RaopSessionManager.addSession(clientInstance, session);
					log.debug("Audio Session being created");
					AudioSessionHolder.getInstance().setSession(session);
				} else {
					log.debug("Audio Session being updated");
					session.setAESIV(Base64.decode(aesIv));
					//
					session.setAESKEY(SecUtils.getInstance().decryptRSA(Base64.decode(rsaAesKey)));
					session.setFmtp(fmtp);
					AudioSessionHolder.getInstance().setSession(session);
				}
				
				
			} else if (RtspMethods.SETUP.equals(method)) {
				log.debug("SETUP \r\n" + request.toString() + content + "\r\n");
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
			} else if (RtspMethods.SET_PARAMETER.equals(method)) {
				log.debug("FullRequest: " + request.toString());
				bSetParameter = true;
				request.headers();
				long length = request.content().capacity();
				String s = request.content().toString(Charset.defaultCharset());
				log.debug("SETPARAMTER: \r\n" + request.toString() + "\r\n" + length + "\r\n");
				
				if (s.startsWith(VOLUME)) {
					String vol = s.substring(VOLUME.length());
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
				
				if (!s.equalsIgnoreCase("")) {
					ByteBuf buf = request.content();
					//log.debug(content.content().toString(Charset.forName("UTF-8")));
					DecodeDMAP dmap = new DecodeDMAP(buf);
					String album = dmap.getValue("asal");
					String artist =dmap.getValue("asar");
					String title  = dmap.getValue("minm");					
					if(!artist.equalsIgnoreCase("") && ! title.equalsIgnoreCase(""))
					{
						EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
						String metatext = channel.updateTrack(title, artist);
						ev.setTitle(title);
						ev.setArtist(artist);
						ev.setMetaText(metatext);
						PlayManager.getInstance().updateTrackInfo(ev);
					}					
					log.debug("Album: " + album + " Artist: " + artist + " title: " + title);
				}
				// String contents =
				// request.getContent().toString(Charset.forName("UTF-8"));
			} else if (RtspMethods.GET_PARAMETER.equals(method)) {
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
			ChannelFuture future = ctx.writeAndFlush(response);
			if (!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
			
			
		}

		// Get the Content
		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			//try {
			//	ByteBuf buff = content.content();
			//	if (buff.hasArray()) {
			//		String hexString = byteToHexString(buff.array());
			//		log.info("HexString:" + hexString);
			//	}
			//} catch (Exception e) {
			//	log.error(e);
			//}
			String s = content.content().toString(Charset.defaultCharset());
			if (s.equalsIgnoreCase("EmptyLastHttpContent")) {
				//content.release();
				//return;
			}

//			if (s.startsWith(VOLUME)) {
//				String vol = s.substring(VOLUME.length());
//				try {
//					Double dVol = Double.parseDouble(vol.trim());
//					dVol = dVol + 30;
//					dVol = dVol * 1000;
//
//					try {
//						dVol = dVol / 300;
//					} catch (Exception ex) {
//						log.debug(ex);
//					}
//					long iVol = dVol.longValue();
//					if (iVol > 100)
//						iVol = 100;
//					if (iVol < 0)
//						iVol = 0;
//					PlayManager.getInstance().setVolume(iVol);
//				} catch (Exception ex) {
//					log.error(ex);
//				}
//			}

//			if (bSetParameter) {
//				if (!s.equalsIgnoreCase("")) {
//					ByteBuf buf = content.content();
//					log.debug(content.content().toString(Charset.forName("UTF-8")));
//					DecodeDMAP dmap = new DecodeDMAP(buf);
//					String album = dmap.getValue("asal");
//					String artist =dmap.getValue("asar");
//					String title  = dmap.getValue("minm");					
//					if(!artist.equalsIgnoreCase("") && ! title.equalsIgnoreCase(""))
//					{
//						EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
//						String metatext = channel.updateTrack(title, artist);
//						ev.setTitle(title);
//						ev.setArtist(artist);
//						ev.setMetaText(metatext);
//						PlayManager.getInstance().updateTrackInfo(ev);
//					}					
//					log.debug("Album: " + album + " Artist: " + artist + " title: " + title);
//				}
//				bSetParameter = false;
//			}

			if (bAnnounced) {
				try {
//					BufferedReader reader = new BufferedReader(new StringReader(s));
//					String line;
//					String rsaAesKey = null;
//					String aesIv = null;
//					String fmtp = null;
//					// Get attributes of Service-Discovery-Protocol
//					while ((line = reader.readLine()) != null) {
//						log.debug(line);
//						if (line.startsWith(RSAAESKEY)) {
//							rsaAesKey = line.substring(RSAAESKEY.length());
//						} else if (line.startsWith(AESIV)) {
//							aesIv = line.substring(AESIV.length());
//						} else if (line.startsWith(FMTP)) {
//							fmtp = line.substring(FMTP.length());
//						}
//					}
//
//					if (org.rpi.utils.Utils.isEmpty(aesIv)) {
//						// throw new RuntimeException("No AES Iv");
//						return;
//					}
//
//					if (org.rpi.utils.Utils.isEmpty(rsaAesKey)) {
//						// throw new RuntimeException("No RSA AES Key");
//						return;
//					}
//					bAnnounced = false;
//					AudioSession session = RaopSessionManager.getSession(clientInstance);
//					if (session == null) {
//						// session = new AudioSession(Base64.decode(aesIv),
//						session = new AudioSession(Base64.decode(aesIv), SecUtils.decryptRSA(Base64.decode(rsaAesKey)), fmtp, 0, 0);
//						RaopSessionManager.addSession(clientInstance, session);
//						log.debug("Audio Session being created");
//						AudioSessionHolder.getInstance().setSession(session);
//					} else {
//						log.debug("Audio Session being updated");
//						session.setAESIV(Base64.decode(aesIv));
//						//
//						session.setAESKEY(SecUtils.decryptRSA(Base64.decode(rsaAesKey)));
//						session.setFmtp(fmtp);
//						AudioSessionHolder.getInstance().setSession(session);
//					}
				} finally {
					//content.release();
				}
			}
		}
		
		if(msg instanceof LastHttpContent)
		{
			//log.debug("End of Content");
		}

//		if (msg instanceof DefaultHttpRequest) {
//			DefaultHttpRequest request = (DefaultHttpRequest) msg;
//			// String content =
//			// request.getContent().toString(Charset.forName("UTF-8"));
//			String content = request.toString();
//			// log.debug(content);
//			if (!RtspVersions.RTSP_1_0.equals(request.getProtocolVersion())) {
//				HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
//				response.headers().add("Connection", "close");
//				ctx.writeAndFlush(response);
//				ctx.disconnect();
//				return;
//			}
//			// HttpMethod methods = request.getMethod();
//			FullHttpResponse response = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
//			response.headers().clear();
//
//			String cSeq = request.headers().get("CSeq");
//			if (cSeq != null) {
//				response.headers().add("CSeq", cSeq);
//			}
//
//			response.headers().add("Audio-Jack-Status", "connected; type=analog");
//
//			String challenge = request.headers().get("Apple-Challenge");
//			if (challenge != null) {
//				SocketAddress remoteAddress = ctx.channel().localAddress();
//				response.headers().add("Apple-Response", Utils.getChallengeResponse(challenge, ((InetSocketAddress) remoteAddress).getAddress(), AudioSessionHolder.getInstance().getHardWareAddress()));
//			}
//
//			// String clientInstance = request.headers().get("DACP-ID");
//			clientInstance = request.headers().get("DACP-ID");
//			if (org.rpi.utils.Utils.isEmpty(clientInstance)) {
//				// throw new RuntimeException("No Client Instance given");
//				try {
//					clientInstance = RaopSessionManager.getFirstSession();
//				} catch (Exception e) {
//					log.error("Did not get DACP_ID, attempted to get first session id", e);
//				}
//			}
//
//			HttpMethod method = request.getMethod();
//			if (RtspMethods.OPTIONS.equals(method)) {
//				response.headers().add("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
//				// log.debug("OPTIONS \r\n" + request.toString() + content);
//			} else if (RtspMethods.ANNOUNCE.equals(method)) {
//				log.debug("ANNOUNCE \r\n" + request.toString() + content + "\r\n");
//				disconnectChannel = false;
//				bAnnounced = true;
//				String client_name = "iTunes";
//				if (request.headers().contains("X-Apple-Client-Name")) {
//					client_name = URLDecoder.decode(request.headers().get("X-Apple-Client-Name"), "UTF-8");
//				}
//				channel = new ChannelAirPlay("", metaData, 1, client_name, Config.getInstance().getResourceURIPrefix() + "org/rpi/image/AirPlay.png");
//				PlayManager.getInstance().playAirPlayer(channel);
//			} else if (RtspMethods.SETUP.equals(method)) {
//				log.debug("SETUP \r\n" + request.toString() + content + "\r\n");
//				disconnectChannel = false;
//				AudioSession session = RaopSessionManager.getSession(clientInstance);
//				if (session == null) {
//					throw new RuntimeException("No Session " + clientInstance);
//				}
//				String transport = request.headers().get("Transport");
//				setPorts(session, transport);
//				session.setLocalAddress((InetSocketAddress) ctx.channel().localAddress());
//				session.setRemoteAddress((InetSocketAddress) ctx.channel().remoteAddress());
//				audioServer = new AudioServer(session);
//				PlayManager.getInstance().setStatus("Playing", "AIRPLAY");
//				PluginGateWay.getInstance().setSourceId("AirPlay", "AirPlay");
//				EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
//				ev.setTitle(client_name);
//				ev.setArtist("AirPlay");
//				PlayManager.getInstance().updateTrackInfo(ev);
//				response.headers().add("Transport", request.headers().get("Transport") + ";server_port=" + session.getControlPort());
//				log.debug("SetUp Response: " + response.toString() + "\r\n");
//			} else if (RtspMethods.RECORD.equals(method)) {
//				log.debug("RECORD \r\n" + request.toString() + content + "\r\n");
//				// ignore
//			} else if ("FLUSH".equalsIgnoreCase(method.name())) {
//				log.debug("FLUSH \r\n" + request.toString() + content + "\r\n");
//				audioServer.flush();
//			} else if (RtspMethods.TEARDOWN.equals(method)) {
//				log.debug("TEARDOWN \r\n" + request.toString() + content + "\r\n");
//				tearDown();
//				response.headers().set("Connection", "close");
//				ctx.writeAndFlush(response);
//				RaopSessionManager.shutdownSession(clientInstance);
//				ctx.disconnect();
//				PlayManager.getInstance().setStatus("Stopped", "AIRPLAY");
//			} else if (RtspMethods.SET_PARAMETER.equals(method)) {
//				bSetParameter = true;
//				request.headers();
//				long length = HttpHeaders.getContentLength(request);
//				log.debug("SETPARAMTER: \r\n" + request.toString() + "\r\n" + length + "\r\n");
//				// String contents =
//				// request.getContent().toString(Charset.forName("UTF-8"));
//			} else if (RtspMethods.GET_PARAMETER.equals(method)) {
//				log.debug("GET_PARAMETER" + "\r\n");
//			} else if ("DENIED".equalsIgnoreCase(method.name())) {
//				log.debug("DENIED");
//			} else {
//				log.info("Unknown RSTPMethod: " + method.name() + "\r\n");
//			}
//
//			boolean keepAlive = isKeepAlive(request);
//
//			if (keepAlive) {
//				response.headers().add("Content-Length", response.content().readableBytes());
//			}
//			response.headers().add("Session", "WENEEDASSESSION");
//			// log.debug("Respone: " + response.toString());
//			ChannelFuture future = ctx.writeAndFlush(response);
//			if (!keepAlive) {
//				future.addListener(ChannelFutureListener.CLOSE);
//			}
//		}

		// super.channelRead(ctx, msg);
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
			byte[] crypted = SecUtils.getInstance().encryptRSA(out.toByteArray());

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
