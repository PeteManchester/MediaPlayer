package org.rpi.airplay;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.rtsp.RtspRequestDecoder;
import org.jboss.netty.handler.codec.rtsp.RtspResponseEncoder;

public class TimingChannelPipelineFactory implements ChannelPipelineFactory {

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = pipeline();
	    pipeline.addLast("executionHandler", AirPlayThread.ChannelExecutionHandler);
		pipeline.addLast("closeOnShutdownHandler", AirPlayThread.CloseChannelOnShutdownHandler);
	    pipeline.addLast("decoder", new RtspRequestDecoder());
	    pipeline.addLast("encoder", new RtspResponseEncoder());
	    pipeline.addLast("handler", new TiminigChannelRequestHandler());
	    return pipeline;
	}

}
