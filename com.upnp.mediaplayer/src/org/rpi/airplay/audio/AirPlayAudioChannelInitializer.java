package org.rpi.airplay.audio;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.rpi.java.sound.IJavaSoundPlayer;




public class AirPlayAudioChannelInitializer extends ChannelInitializer<NioDatagramChannel> {
	
	private IJavaSoundPlayer audioQueue = null;
	
	public AirPlayAudioChannelInitializer(IJavaSoundPlayer audioQueue)
	{
		this.audioQueue = audioQueue;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {	
		ChannelPipeline p = ch.pipeline();
		//EventExecutorGroup e1 = new DefaultEventExecutorGroup(1);
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		p.addLast("Audio Decrypter", new AudioDecrpyt());
		p.addLast("AudioBuffer", new AudioBuffer());
		p.addLast("Audio Deocder", new AudioALACDecode(audioQueue));
		p.addLast("Audio Handler",new AudioChannelRequestHandler());
	}

}
