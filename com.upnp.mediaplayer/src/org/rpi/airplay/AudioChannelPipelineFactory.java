package org.rpi.airplay;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;




public class AudioChannelPipelineFactory extends ChannelInitializer<NioDatagramChannel> {
	
	private AudioEventQueue audioQueue = null;
	
	public AudioChannelPipelineFactory(AudioEventQueue audioQueue)
	{
		this.audioQueue = audioQueue;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {	
		ChannelPipeline p = ch.pipeline();
		//EventExecutorGroup e1 = new DefaultEventExecutorGroup(1);
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		p.addLast("Audio Decrypter", new AudioDecrpyt());
		p.addLast("Audio Deocder", new AudioALACDecode(audioQueue));
		p.addLast("Audio Handler",new AudioChannelRequestHandler());
	}

}
