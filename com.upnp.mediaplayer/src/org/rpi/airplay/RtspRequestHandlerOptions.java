package org.rpi.airplay;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

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

public class RtspRequestHandlerOptions extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {

		HttpMethod method = request.getMethod();
		if (RtspMethods.OPTIONS.equals(method)) {
			//log.debug("OPTIONS REQUEST");
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

			response.headers().add("Audio-Jack-Status", "connected; type=analog");
			response.headers().add("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");
			boolean keepAlive = isKeepAlive(request);

			if (keepAlive) {
				response.headers().add("Content-Length", response.content().readableBytes());
			}
			//response.headers().add("Session", "WENEEDASSESSION");
			response.headers().add("Server","AirTunes/130.14");

			ChannelFuture future = ctx.writeAndFlush(response);
			if (!keepAlive) {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		}
		else
		{
			ctx.fireChannelRead(request.retain());
			return;
		}
	}

}
