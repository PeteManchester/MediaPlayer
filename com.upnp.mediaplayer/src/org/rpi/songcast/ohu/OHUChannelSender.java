package org.rpi.songcast.ohu;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

public class OHUChannelSender {

	private Logger log = Logger.getLogger(this.getClass());
	private InetAddress localInetAddr = null;
	private EventLoopGroup group = new NioEventLoopGroup(1);
	private DatagramChannel ch = null;

	public OHUChannelSender(InetAddress localInetAddr) {
		this.localInetAddr = localInetAddr;
	}

	public void run() throws Exception {

		try {

			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channelFactory(new ChannelFactory<Channel>() {
				@Override
				public Channel newChannel() {
					return new NioDatagramChannel(InternetProtocolFamily.IPv4);
				}
			});

			InetSocketAddress localInetSocket = new InetSocketAddress(localInetAddr, 57667);
			//b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
			b.option(ChannelOption.SO_RCVBUF, 2 * 1024);
			b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			//b.handler(new LoggingHandler(LogLevel.WARN));
			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void sendMessage(DatagramPacket packet) {
		try {
			if (ch != null) {
				ch.writeAndFlush(packet).sync();
			}
		} catch (Exception e) {
			log.error("Erorr Sending Packet", e);
		}
	}
	
	public void stop() {
		try {
			if (ch != null) {
				try {
					ch.close();
				} catch (Exception e) {
					log.error("Error Closing Channel", e);
				}
			}
			group.shutdownGracefully();
		} catch (Exception e) {
			log.error("Error ShuutingDown", e);
		}
	}

}
