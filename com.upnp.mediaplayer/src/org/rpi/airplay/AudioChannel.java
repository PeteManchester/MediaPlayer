package org.rpi.airplay;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;


public class AudioChannel {

	private Logger log = Logger.getLogger(this.getClass());
	EventLoopGroup workerGroup = new NioEventLoopGroup(10);
	Bootstrap bootstrap = new Bootstrap();
	ChannelFuture channel = null;

	

	public AudioChannel(InetSocketAddress local, InetSocketAddress remote, int remotePort, AudioEventQueue audioQueue) {
		initialize(local, remote, remotePort, audioQueue);
	}

	private void initialize(InetSocketAddress local, InetSocketAddress remote, int remotePort, AudioEventQueue audioQueue) {
		try {
			bootstrap.group(workerGroup);
			bootstrap.channel(NioDatagramChannel.class);
			bootstrap.handler(new AudioChannelPipelineFactory(audioQueue));
			InetSocketAddress localAddr = new InetSocketAddress(local.getAddress().getHostAddress(), remotePort);
			InetSocketAddress remoteAddr = new InetSocketAddress(remote.getAddress().getHostAddress(), 0);
			log.debug("LocalAddress: " + localAddr.toString());
			channel = bootstrap.bind(localAddr);
			if (remote != null) {
				log.debug("RemoteAddress: " + remoteAddr.toString());
				bootstrap.connect(remoteAddr);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void close() {
		
		try
		{
			if(channel !=null)
			{
				channel.channel().close().sync();
			}
		}
		catch(Exception e)
		{
			log.error("Error Closing AudioChannel ",e);
		}
		try
		{
			workerGroup.shutdownGracefully(10, 100, TimeUnit.MILLISECONDS);
		}
		catch(Exception e)
		{
			log.error("Error Stop AudioChannel" ,e);
		}
		
		try
		{
			workerGroup.terminationFuture().sync();
		}
		catch(Exception e)
		{
			log.debug("Error Closing Audio ChannelFuture",e );
		}
	}
}
