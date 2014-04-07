package org.rpi.airplay;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RtspServer implements Runnable {
	private int port;
	private Logger log = Logger.getLogger(this.getClass());

	public RtspServer(int port) {
		this.port = port;
	}

	@Override
	public void run() {
		log.debug("Listening on Port " + port);
		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new RtspServerPipelineFactory());
		bootstrap.bind(new InetSocketAddress(port));
		while (!Thread.interrupted()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// ignore
			}
		}

		log.debug("RTSP-Server shutdown");
	}
}
