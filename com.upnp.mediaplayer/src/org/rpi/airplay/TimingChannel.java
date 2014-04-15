package org.rpi.airplay;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;

public class TimingChannel {

	private Logger log = Logger.getLogger(this.getClass());

	public TimingChannel(InetSocketAddress local, InetSocketAddress remote, int remotePort) {
		initialize(local, remote, remotePort);
	}

	private void initialize(InetSocketAddress local, InetSocketAddress remote, int remotePort) {
		try {
			ConnectionlessBootstrap bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(Executors.newCachedThreadPool()));
			bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(1500));
			bootstrap.setOption("receiveBufferSize", 1048576);
			bootstrap.setPipelineFactory(new TimingChannelPipelineFactory());
			Channel channel = null;
			InetSocketAddress localAddr = new InetSocketAddress(local.getAddress().getHostAddress(), 0);
			InetSocketAddress remoteAddr = new InetSocketAddress(remote.getAddress().getHostAddress(), remotePort);
			channel = bootstrap.bind(localAddr);
			if (remote != null) {
				channel.connect(remoteAddr);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}
}
