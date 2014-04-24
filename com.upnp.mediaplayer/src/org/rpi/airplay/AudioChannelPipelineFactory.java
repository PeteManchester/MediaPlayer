package org.rpi.airplay;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;


public class AudioChannelPipelineFactory implements ChannelPipelineFactory {
	
	private AudioEventQueue audioQueue = null;
	
	public AudioChannelPipelineFactory(AudioEventQueue audioQueue)
	{
		this.audioQueue = audioQueue;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
	    //pipeline.addLast("executionHandler", AirPlayThread.ChannelExecutionHandler);
		//pipeline.addLast("closeOnShutdownHandler", AirPlayThread.CloseChannelOnShutdownHandler);
	    pipeline.addLast("decrypter", new AudioDecrpyt());
	    pipeline.addLast("deocder", new AudioALACDecode());
	    pipeline.addLast("handler", new AudioChannelRequestHandler(audioQueue));
	    return pipeline;
	}

}
