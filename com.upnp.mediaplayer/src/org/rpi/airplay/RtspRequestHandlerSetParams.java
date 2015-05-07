package org.rpi.airplay;

/**
 * Handler for the RTSP SET_PARAMETERS
 * Used for volume changes, meta data changes and progress
 */

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.rtsp.RtspMethods;
import io.netty.handler.codec.rtsp.RtspVersions;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackMetaText;

public class RtspRequestHandlerSetParams extends SimpleChannelInboundHandler<FullHttpRequest> {

	private Logger log = Logger.getLogger(this.getClass());
	private final String VOLUME = "volume:";

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

		HttpMethod method = request.getMethod();
		if (RtspMethods.SET_PARAMETER.equals(method)) {
			log.debug("SET PARAMETERS ###### " + request.toString());
			FullHttpResponse response = new DefaultFullHttpResponse(RtspVersions.RTSP_1_0, HttpResponseStatus.OK);
			response.headers().clear();

			String cSeq = request.headers().get("CSeq");
			if (cSeq != null) {
				response.headers().add("CSeq", cSeq);
			}

			String challenge = request.headers().get("Apple-Challenge");
			if (challenge != null) {
				SocketAddress remoteAddress = ctx.channel().localAddress();
				response.headers().add("Apple-Response", Utils.getChallengeResponse(challenge, ((InetSocketAddress) remoteAddress).getAddress(), AudioSessionHolder.getInstance().getHardWareAddress()));
			}

			String contentType = request.headers().get("Content-Type");
			if (contentType.equalsIgnoreCase("text/parameters")) {
				String s = request.content().toString(Charset.defaultCharset());
				// log.debug("SETPARAMTER: \r\n" + request.toString() + "\r\n" +
				// length + "\r\n");

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
						PlayManager.getInstance().setAirplayVolume(iVol);
					} catch (Exception ex) {
						log.error(ex);
					}
				} else if (s.startsWith("progress")) {
					log.debug("Progress: " + s);
				}
			} else {
				ByteBuf buf = request.content();
				// log.debug(content.content().toString(Charset.forName("UTF-8")));
				DecodeDMAP dmap = new DecodeDMAP(buf);
				String album = dmap.getValue("asal");
				String artist = dmap.getValue("asar");
				String title = dmap.getValue("minm");
				if (!artist.equalsIgnoreCase("") && !title.equalsIgnoreCase("")) {
					log.debug("MetaData Changed :" + title + " : " + artist + " : " + album);
					EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
					ChannelBase channel = PlayManager.getInstance().getCurrentTrack();
					if (channel instanceof ChannelAirPlay) {
						String metatext = channel.updateTrack(title, artist);
						ev.setTitle(title);
						ev.setArtist(artist);
						ev.setMetaText(metatext);
						PlayManager.getInstance().updateTrackInfo(ev);
					}
				}
				log.debug("Album: " + album + " Artist: " + artist + " title: " + title);
			}

			boolean keepAlive = isKeepAlive(request);

			if (keepAlive) {
				response.headers().add("Content-Length", response.content().readableBytes());
			}
			// response.headers().add("Session", "WENEEDASSESSION");
			response.headers().add("Server", "AirTunes/130.14");
			// log.debug("Respone: " + response.toString());
			ChannelFuture future = ctx.writeAndFlush(response);
			if (!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} else {
			ctx.fireChannelRead(request.retain());
			return;
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Exception Caught: " + cause);
		ctx.close();
	}
}
