package org.rpi.songcast.ohu.sender;

import org.rpi.songcast.ohu.receiver.OHULeakCatcher;
import org.rpi.songcast.ohu.sender.handlers.OHUSenderLogicHandler;
import org.rpi.songcast.ohu.sender.handlers.OHUSenderMessageDecoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class OHUSenderChannelInitialiser extends ChannelInitializer<NioDatagramChannel> {

	OHUSenderConnection ohuSenderConnection = null;

	public OHUSenderChannelInitialiser(OHUSenderConnection ohuSenderConnection) {
		this.ohuSenderConnection = ohuSenderConnection;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		p.addLast("OHUDecoder", new OHUSenderMessageDecoder());
		p.addLast("OHUSenderLogic", new OHUSenderLogicHandler());
		p.addLast("OHULeakCatcher", new OHULeakCatcher());
	}

}
