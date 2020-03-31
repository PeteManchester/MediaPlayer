package org.rpi.airplay.audio;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;




public class AirPlayAudioChannelInitializer extends ChannelInitializer<NioDatagramChannel> {
	
	private ProcessAirplayAudio processQueue = null;
	
	public AirPlayAudioChannelInitializer(ProcessAirplayAudio processQueue)
	{
		this.processQueue = processQueue;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {	
		ChannelPipeline p = ch.pipeline();
		//EventExecutorGroup e1 = new DefaultEventExecutorGroup(1);
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));

		p.addLast("Audio Decrypter", new AudioDecrpyt(processQueue));
		//p.addLast("AudioBuffer", new AudioBuffer());
		//p.addLast("Audio Decoder", new AudioALACDecode(audioQueue));
		p.addLast("Audio Handler",new AudioChannelRequestHandler());
	}

}
