package org.rpi.airplay;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.rtsp.RtspRequestDecoder;
import io.netty.handler.codec.rtsp.RtspResponseEncoder;

public class RtspServerInitializer extends ChannelInitializer<NioSocketChannel> {

@Override
protected void initChannel(NioSocketChannel ch) throws Exception {
	ChannelPipeline p = ch.pipeline();
	//p.addLast(new LoggingHandler(LogLevel.DEBUG));	
	p.addLast(new HttpRequestDecoder());
	p.addLast(new HttpObjectAggregator(1024*1024*1024));	
	p.addLast(new RtspRequestDecoder());
	p.addLast(new RtspResponseEncoder());
	p.addLast(new RtspRequestHandlerVerifyRequest());
	p.addLast(new RtspRequestHandlerOptions());
	p.addLast(new RtspRequestHandlerSetParams());
	p.addLast(new RtspRequestHandlerSimple());
	
}
}
