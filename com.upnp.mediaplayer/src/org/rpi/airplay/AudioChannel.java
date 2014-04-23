package org.rpi.airplay;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

public class AudioChannel {

	private Logger log = Logger.getLogger(this.getClass());
	private Channel channel = null;

public AudioChannel(InetSocketAddress local, InetSocketAddress remote, int remotePort,AudioEventQueue audioQueue) {
		initialize(local, remote, remotePort,audioQueue);
	}

	private void initialize(InetSocketAddress local, InetSocketAddress remote, int remotePort,AudioEventQueue audioQueue) {
		try {
			ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(Executors.newCachedThreadPool()));
			bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(2408));
			bootstrap.setOption("receiveBufferSize", 1048576);
			bootstrap.setPipelineFactory(new AudioChannelPipelineFactory(audioQueue));
			
			InetSocketAddress localAddr = new InetSocketAddress(local.getAddress().getHostAddress(), remotePort);
			InetSocketAddress remoteAddr = new InetSocketAddress(remote.getAddress().getHostAddress(), 0);
			log.debug("LocalAddress: " + localAddr.toString());
			channel = bootstrap.bind(localAddr);
			if (remote != null) {
				log.debug("RemoteAddress: " + remoteAddr.toString());
				channel.connect(remoteAddr);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	public void close()
	{
		if(channel!=null)
		{
			channel.close();
			channel = null;
		}
	}
}
