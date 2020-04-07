package org.rpi.songcast.ohu.receiver;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventReceiverStatusChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.providers.IDisposableDevice;
import org.rpi.songcast.ohu.receiver.requests.OHURequestJoin;
import org.rpi.songcast.ohu.receiver.requests.OHURequestListen;
import org.rpi.songcast.ohz.common.OHZLeaveRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultMaxMessagesRecvByteBufAllocator;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

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

	private EventLoopGroup group = null;
	private OHURequestListen listen = null;

	private DatagramChannel ch = null;
	private long started = System.nanoTime();
	private boolean isMulticast = false;

	public OHUConnector(String uri, String zoneID, InetAddress localInetAddr) {
		ThreadFactory threadFactory = new DefaultThreadFactory("OHUReceiverEventLoopGroupThread");
		group = new NioEventLoopGroup(1, threadFactory);
		log.debug("Create OHUConnector: " + uri);
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
		listen = new OHURequestListen();
	}

	public void run() throws Exception {

		try {
			log.debug("Start OHUConnector: " + localInetAddr.getHostName());

			// https:stackoverflow.com/questions/9637436/lot-of-udp-requests-lost-in-udp-server-with-netty

			remoteInetSocket = new InetSocketAddress(remoteInetAddr, remotePort);
			localInetSocket = new InetSocketAddress(localInetAddr, remotePort);
			NetworkInterface nic = NetworkInterface.getByInetAddress(localInetAddr);

			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioDatagramChannel.class);

			// b.option(ChannelOption.SO_BROADCAST, true);
			int byteBuffer = 10240;
			// int byteBuffer = 1310720;
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_RCVBUF, byteBuffer);
			b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(byteBuffer * 4));
			//b.option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(64,byteBuffer , 65536));
			b.option(ChannelOption.SO_SNDBUF, byteBuffer);
			// b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			// b.option(ChannelOption.IP_MULTICAST_IF, nic);
			b.handler(new OHUChannelInitializer());
			log.debug("OHU Bind to Socket: " + localInetSocket.getHostName());
			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			isMulticast = remoteInetAddr.isMulticastAddress();
			if (isMulticast) {
				log.debug("OHU is Multicast");
				ChannelFuture future = ch.joinGroup(remoteInetSocket, nic);
				log.debug("Result of Join: " + future.toString());
			} else {
				log.debug("OHU is not Multicast");
			}
			OHURequestJoin join = new OHURequestJoin();
			// ByteBuf buffer = Unpooled.copiedBuffer(join.data);
			DatagramPacket packet = new DatagramPacket(join.getBuffer(), remoteInetSocket, localInetSocket);
			log.debug("Send OHU Join Message: " + join.toString());
			sendMessage(packet);
			log.debug("Sent OHU Join Message: " + join.toString());

			group.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					// log.debug("OHU Time to Listen");
					if (!isMulticast) {
						try {
							// ByteBuf lBuffer =
							// Unpooled.copiedBuffer(listen.data);
							DatagramPacket pListen = new DatagramPacket(listen.getBuffer().retain(), remoteInetSocket, localInetSocket);
							sendMessage(pListen);
						} catch (Exception e) {
							log.error("Error Sending Listen", e);
						}
					}
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
			// log.debug("SendMessage");
			// ch.writeAndFlush(packet).sync();
			ch.writeAndFlush(packet);
		} catch (Exception e) {
			log.error("Error Listen Keep Alive", e);
		}
	}

	public void stop() {

		try {
			log.debug("Attempt to Stop Songcast Playback");
			if (ch != null) {
				try {
					OHZLeaveRequest leave = new OHZLeaveRequest();
					// ByteBuf buffer =
					// Unpooled.copiedBuffer(leave.getBuffer());
					DatagramPacket packet = new DatagramPacket(leave.getBuffer().retain(), remoteInetSocket, localInetSocket);
					log.debug("Sending : " + packet.toString());
					// ch.writeAndFlush(packet).sync();
					ch.writeAndFlush(packet);
					log.debug("Sent Leave Message");
					PlayManager.getInstance().setStatus("Stopped", "SONGCAST");
				} catch (Exception e) {
					log.error("Error Sending Leave", e);
				}
				try {
					ch.close();
				} catch (Exception e) {
					log.error("Error Closing Channel", e);
				}
			}
			group.shutdownGracefully();
		} catch (Exception e) {
			log.error("Error ShuttingDown", e);
		}
	}
}
