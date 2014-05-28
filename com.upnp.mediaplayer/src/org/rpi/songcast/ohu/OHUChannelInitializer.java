package org.rpi.songcast.ohu;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class OHUChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		p.addLast("OHUDecoder",new OHUMessageDecoder());
		p.addLast("OHUSlaveForwarder",new OHUSlaveForwarder());
		p.addLast("OHUAudioHandler", new OHUMessageAudioHandler());
		p.addLast("OHMTrackHandler", new OHUMessageTrackHandler());
		p.addLast("OHMMessageMetaTextHandler", new OHUMessageMetaTextHandler());
		p.addLast("OHUSlaveHandler", new OHUMessageSlaveHandler());
		//p.addLast("OHULeakCatcher", new OHULeakCatcher());
		
	}
}
