package org.rpi.songcast.ohz.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.OHULeakCatcher;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/***
 * Initialise the Netty ChannelPipe for OHZ
 * 
 * @author phoyle
 *
 */

public class OHZChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

	private Logger log = Logger.getLogger(this.getClass());
	private InetAddress localInetAddr = null;
	private InetSocketAddress remoteInetSocketAddr = null;
	private InetSocketAddress localInetSocketAddr = null;

	public OHZChannelInitializer(InetAddress localInetAddr, InetSocketAddress remoteInetSocketAddr, InetSocketAddress localInetSocketAddress) {
		this.localInetAddr = localInetAddr;
		this.remoteInetSocketAddr = remoteInetSocketAddr;
		this.localInetSocketAddr = localInetSocketAddress;
	}

	@Override
	protected void initChannel(NioDatagramChannel ch) throws Exception {
		try {
			log.debug("Start of OHZChannelInitializer");
			ChannelPipeline p = ch.pipeline();
			p.addLast(new LoggingHandler(LogLevel.DEBUG));
			p.addLast("OHZDecoder", new OHZMessageDecoder());
			p.addLast("OHZLogicHandler", new OHZLogicHandler(localInetAddr, remoteInetSocketAddr, localInetSocketAddr));
			p.addLast("OHZLeakCatcher", new OHULeakCatcher());
			log.debug("End of OHZChannelInitializer");
		} catch (Exception e) {
			log.error("Error ChannelInitializer", e);
		}
	}
}