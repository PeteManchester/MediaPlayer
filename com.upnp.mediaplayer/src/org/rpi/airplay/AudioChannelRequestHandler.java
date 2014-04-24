package org.rpi.airplay;

import org.apache.log4j.Logger;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class AudioChannelRequestHandler extends SimpleChannelUpstreamHandler {

	private AudioEventQueue audioQueue = null;
	private Logger log = Logger.getLogger(this.getClass());


	public AudioChannelRequestHandler(AudioEventQueue audioQueue) {
		this.audioQueue = audioQueue;
	}
	

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		try {
			byte[] audio = (byte[]) e.getMessage();
			//
			if (audioQueue != null) {
				audioQueue.put(audio);
			}
		} catch (Exception ex) {
			log.error("Error ", ex);
		}
		super.messageReceived(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() != null) {
			log.error("Error AudioChannelHandler", e.getCause());
		}
		e.getChannel().close();
	}

	@Override
	public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent evt) throws Exception {
		log.debug("AudioChannel Opened");
		super.channelOpen(ctx, evt);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.debug("AudioChannel Connected");
		super.channelConnected(ctx, e);
	};

	@Override
	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent evt) throws Exception {
		log.debug("AudioChannel Closed");
	}
}
