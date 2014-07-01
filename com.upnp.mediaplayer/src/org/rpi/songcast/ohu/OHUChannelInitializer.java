package org.rpi.songcast.ohu;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class OHUChannelInitializer extends ChannelInitializer<NioDatagramChannel> {
	
	private ConcurrentHashMap<String, Slave> endpoints = new ConcurrentHashMap<String, Slave>();

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		p.addLast("OHUDecoder",new OHUMessageDecoder(this));
		p.addLast("OHUSlaveForwarder",new OHUSlaveForwarder(this));
		p.addLast("OHUAudioHandler", new OHUMessageAudioHandler());
		p.addLast("OHMTrackHandler", new OHUMessageTrackHandler());
		p.addLast("OHMMessageMetaTextHandler", new OHUMessageMetaTextHandler());
		p.addLast("OHUSlaveHandler", new OHUMessageSlaveHandler(this));
		//p.addLast("OHULeakCatcher", new OHULeakCatcher());
		
	}

	/**
	 * Set the Slave Endpoints
	 * @param endpoints
	 */
	public void setEndpoints(ConcurrentHashMap<String, Slave> endpoints) {
		this.endpoints = endpoints;
		
	}

	/**
	 * Do we have any Slaves
	 * @return
	 */
	public boolean hasSlaves() {
		return endpoints.size()>0;
	}
}
