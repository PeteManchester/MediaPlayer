package org.rpi.songcast.ohu.sender;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.rpi.songcast.ohu.receiver.OHULeakCatcher;
import org.rpi.songcast.ohu.receiver.SlaveInfo;
import org.rpi.songcast.ohu.receiver.handlers.OHUMessageAudioHandler;
import org.rpi.songcast.ohu.receiver.handlers.OHUMessageDecoder;
import org.rpi.songcast.ohu.receiver.handlers.OHUMessageMetaTextHandler;
import org.rpi.songcast.ohu.receiver.handlers.OHUMessageSlaveHandler;
import org.rpi.songcast.ohu.receiver.handlers.OHUMessageTrackHandler;
import org.rpi.songcast.ohu.receiver.handlers.OHUSlaveForwarder;
import org.rpi.songcast.ohu.sender.handlers.OHUSenderLogicHandler;
import org.rpi.songcast.ohu.sender.handlers.OHUSenderMessageDecoder;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;



public class OHUSenderChannelInitialiser extends ChannelInitializer<NioDatagramChannel> {
	
	private ConcurrentHashMap<String, SlaveInfo> endpoints = new ConcurrentHashMap<String, SlaveInfo>();
	
	private InetSocketAddress remoteAddress = null;
	private OHUSenderConnection ohuSenderConnection = null; 

	public OHUSenderChannelInitialiser(OHUSenderConnection ohuSenderConnection) {
		this.ohuSenderConnection = ohuSenderConnection;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		ChannelPipeline p = ch.pipeline();
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		//p.addLast(new LoggingHandler(LogLevel.DEBUG));
		p.addLast("OHUDecoder",new OHUSenderMessageDecoder(this));
		p.addLast("OHUSenderLogic", new OHUSenderLogicHandler());
		//p.addLast("OHUSlaveForwarder",new OHUSlaveForwarder(this));
		//p.addLast("OHUAudioHandler", new OHUMessageAudioHandler());
		//p.addLast("OHMTrackHandler", new OHUMessageTrackHandler());
		//p.addLast("OHMMessageMetaTextHandler", new OHUMessageMetaTextHandler());
		//p.addLast("OHUSlaveHandler", new OHUMessageSlaveHandler(this));
		p.addLast("OHULeakCatcher", new OHULeakCatcher());
		
	}

	/**
	 * Set the Slave Endpoints
	 * @param endpoints
	 */
	public void setEndpoints(ConcurrentHashMap<String, SlaveInfo> endpoints) {
		this.endpoints = endpoints;
		
	}

	/**
	 * Do we have any Slaves
	 * @return
	 */
	public boolean hasSlaves() {
		return endpoints.size()>0;
	}

	public void setRemoteAddress(InetSocketAddress sender) {
		this.remoteAddress =sender;	
		this.ohuSenderConnection.setRemoteAddress(sender);
	}
	
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}
}

