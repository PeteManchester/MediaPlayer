package org.rpi.songcast.ohu;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ChannelFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.InternetProtocolFamily;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.songcast.ohz.OHZLeaveRequest;

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
	private OHURequestListen listen = null;

	private DatagramChannel ch = null;
	private long started = System.nanoTime();
	private boolean isMulticast = false;

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
		listen = new OHURequestListen();
	}

	public void run() throws Exception {

		try {
			PlayManager.getInstance().setStatus("Playing", "SONGCAST");
			remoteInetSocket = new InetSocketAddress(remoteInetAddr, remotePort);
			localInetSocket = new InetSocketAddress(remotePort);
			NetworkInterface nic = NetworkInterface.getByInetAddress(localInetAddr);

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
			//b.option(ChannelOption.SO_RCVBUF, 3 * 1024);
			b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator( 1024 * 5 ));
			//b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			//b.option(ChannelOption.IP_MULTICAST_IF, nic);
			b.handler(new OHUChannelInitializer());

			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			isMulticast = remoteInetAddr.isMulticastAddress();
			if (isMulticast) {
				ChannelFuture future = ch.joinGroup(remoteInetSocket, nic);
				log.debug("Result of Join: " + future.toString());
			}
			OHURequestJoin join = new OHURequestJoin();
			//ByteBuf buffer = Unpooled.copiedBuffer(join.data);
			DatagramPacket packet = new DatagramPacket(join.getBuffer(), remoteInetSocket, localInetSocket);
			sendMessage(packet);

			group.scheduleAtFixedRate(new Runnable() {

				@Override
				public void run() {
					if (!isMulticast) {
						try {
							//ByteBuf lBuffer = Unpooled.copiedBuffer(listen.data);
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
			ch.writeAndFlush(packet).sync();
		} catch (Exception e) {
			log.error("Error Listen Keep Alive", e);
		}
	}

	public void stop() {

		try {
			if (ch != null) {
				try
				{
					OHZLeaveRequest leave = new OHZLeaveRequest();
					ByteBuf buffer = Unpooled.copiedBuffer(leave.getBuffer());
					DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);
					log.debug("Sending : " + packet.toString());
					ch.writeAndFlush(packet).sync();
					log.debug("Sent Leave Message");
				}
				catch(Exception e)
				{
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
			log.error("Error ShuutingDown", e);
		}
	}

}
