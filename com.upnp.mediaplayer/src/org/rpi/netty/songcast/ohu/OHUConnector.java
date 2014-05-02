package org.rpi.netty.songcast.ohu;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.songcast.ohm.OHMRequestJoin;
import org.rpi.songcast.ohm.OHMRequestListen;

public class OHUConnector {

	private final int remotePort;
	// ohz://239.255.255.250:51972/b33f69011e38827aa138adc6d00cb23e
	// private String zoneId = "adb3ff3c41b7ebd669a49e35d54222ae";
	// private String zoneID = "b33f69011e38827aa138adc6d00cb23e";

	private String zoneID = "";
	private Logger log = Logger.getLogger(this.getClass());

	private InetAddress remoteInetAddr = null;
	private InetSocketAddress remoteInetSocket = null;
	private InetAddress localInetAddr = null;
	private InetSocketAddress localInetSocket = null;

	private EventLoopGroup group = new NioEventLoopGroup(1);

	private DatagramChannel ch = null;
	private long started = System.nanoTime();

	public OHUConnector(String uri, String zoneID, InetAddress localInetAddr) {
		int lastColon = uri.lastIndexOf(":");
		int lastSlash = uri.lastIndexOf("/");
		String host = uri.substring(lastSlash + 1, lastColon);
		String sPort = uri.substring(lastColon + 1);
		try {
			remoteInetAddr = InetAddress.getByName(host);

		} catch (Exception e) {
			log.error("Error Getting RemoteHost: " + host, e);
		}
		this.localInetAddr = localInetAddr;
		remotePort = Integer.parseInt(sPort);
		this.zoneID = zoneID;
	}

	public void run() throws Exception {

		try {

			remoteInetSocket = new InetSocketAddress(remoteInetAddr, remotePort);
			localInetSocket = new InetSocketAddress(localInetAddr, remotePort);

			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channelFactory(new ChannelFactory<Channel>() {
				@Override
				public Channel newChannel() {
					return new NioDatagramChannel(InternetProtocolFamily.IPv4);
				}
			});
			b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.SO_RCVBUF, 10240);
			b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			b.handler(new OHUChannelInitializer());

			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			if (remoteInetAddr.isMulticastAddress()) {
				ChannelFuture future = ch.joinGroup(remoteInetAddr);
			}

			OHMRequestJoin join = new OHMRequestJoin(zoneID);
			ByteBuf buffer = Unpooled.copiedBuffer(join.data);
			DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);
			sendMessage(packet);
			PlayManager.getInstance().setStatus("Playing","SONGCAST");

			group.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					OHMRequestListen listen = new OHMRequestListen(zoneID);
					ByteBuf lBuffer = Unpooled.copiedBuffer(listen.data);
					DatagramPacket pListen = new DatagramPacket(lBuffer, remoteInetSocket, localInetSocket);
					sendMessage(pListen);
					try {
						long now = System.nanoTime();
						long time = (now - started) / 1000000000;
						if (time >= 0) {
							 EventTimeUpdate e = new EventTimeUpdate();
							 e.setTime(time);
							 PlayManager.getInstance().updateTime(e);
						}
					} catch (Exception e) {

					}
				}
			}, 1, 1, TimeUnit.SECONDS);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}
	}

	private void sendMessage(DatagramPacket packet) {
		try {
			ch.writeAndFlush(packet).sync();
		} catch (Exception e) {
			log.error("Error Listen Keep Alive", e);
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
