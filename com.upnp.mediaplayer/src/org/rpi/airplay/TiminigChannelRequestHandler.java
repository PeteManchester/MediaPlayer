package org.rpi.airplay;

import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class TiminigChannelRequestHandler extends SimpleChannelUpstreamHandler {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();
		String content = request.getContent().toString(Charset.forName("UTF-8"));
		log.debug("TIMING### \r\n" + content);
		super.messageReceived(ctx, e);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() != null) {
			log.error("Error ChannelHandler", e.getCause());
		}
		e.getChannel().close();
	}
	
	@Override
	public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent evt)
		throws Exception
	{
		log.debug("TIMING### Opened");
		super.channelOpen(ctx, evt);
	}
	
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.debug("TIMING### Connected");
		super.channelConnected(ctx, e);
	};
	
	@Override
	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent evt)
		throws Exception
	{
		log.debug("TIMING### Closed");
	}

}
