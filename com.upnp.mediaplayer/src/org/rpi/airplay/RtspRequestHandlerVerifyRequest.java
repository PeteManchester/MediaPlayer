package org.rpi.airplay;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.rtsp.RtspVersions;

public class RtspRequestHandlerVerifyRequest extends SimpleChannelInboundHandler<FullHttpRequest> {
	
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
		if (!RtspVersions.RTSP_1_0.equals(request.getProtocolVersion())) {
			log.error("Not an RTSP_1_0 Request");
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			response.headers().add("Connection", "close");
			ctx.writeAndFlush(response);
			ctx.disconnect();
			return;
		}	
		else
		{
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
