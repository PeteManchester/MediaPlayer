package org.rpi.songcast.ohu.sender.mpd;

import org.apache.log4j.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpResponse;

public class MPDStreamerDefaultHttpResponseHandler extends SimpleChannelInboundHandler<DefaultHttpResponse> {

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DefaultHttpResponse msg) throws Exception {
		log.debug("DefaultHttpResponse: " + msg.toString());
	}

}
