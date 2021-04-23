package org.rpi.songcast.ohz.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.ohu.receiver.OHUConnector;

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
import io.netty.util.concurrent.DefaultThreadFactory;

public class OHZConnector {

	private int remotePort;
	// ohz://239.255.255.250:51972/b33f69011e38827aa138adc6d00cb23e
	// private String zoneId = "adb3ff3c41b7ebd669a49e35d54222ae";
	// private String zoneID = "b33f69011e38827aa138adc6d00cb23e";
	private String zoneID = "";
	private Logger log = Logger.getLogger(this.getClass());

	private InetAddress remoteInetAddr = null;
	private InetSocketAddress remoteInetSocket = null;
	private InetAddress localInetAddr = null;
	private InetSocketAddress localInetSocket = null;

	private DatagramChannel ch = null;
	private EventLoopGroup group = null;

	private static OHZConnector instance = null;

	public static OHZConnector getInstance() {
		if (instance == null) {
			instance = new OHZConnector();
		}
		return instance;
	}

	private OHZConnector() {
		ThreadFactory threadFactory = new DefaultThreadFactory("OHZEventLoopGroupThread");
		group = new NioEventLoopGroup(4, threadFactory);
	}

	public void run(String uri, String zoneID, Inet4Address localInetAddr) throws Exception {

		log.debug("Run OHZConnector: ZoneID: " + zoneID + " LocalAddress: " + localInetAddr.getHostAddress());

		log.debug("Creating OHZConnector: " + uri + " " + zoneID);
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

		try {

			remoteInetSocket = new InetSocketAddress(remoteInetAddr, remotePort);
			localInetSocket = new InetSocketAddress(remotePort);
			NetworkInterface nic = NetworkInterface.getByInetAddress(localInetAddr);
			log.info("OHZ is using NIF: " + nic.getDisplayName());
			Bootstrap b = new Bootstrap();
			b.group(group);
			// b.channel(NioDatagramChannel.class);

			b.channelFactory(new ChannelFactory<Channel>() {
				@Override
				public Channel newChannel() {
					return new NioDatagramChannel(InternetProtocolFamily.IPv4);
				}
			});

			b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.IP_MULTICAST_ADDR, localInetAddr);

			/*
			 * Enumeration<InetAddress> addresses = nic.getInetAddresses();
			 * 
			 * while( addresses.hasMoreElements() ) { InetAddress addr =
			 * addresses.nextElement(); if( addr instanceof Inet4Address &&
			 * !addr.isLoopbackAddress() ) {
			 * 
			 * log.debug("Inet4Address" + addr); break; } }
			 */

			// b.option(ChannelOption.SO_RCVBUF, 2048);
			// b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			// b.option(ChannelOption.IP_MULTICAST_IF, nic);

			b.handler(new OHZChannelInitializer(localInetAddr, remoteInetSocket, localInetSocket));

			boolean isRoot = System.getProperty("user.name").equals("root");
			log.debug("Am I Logged on as ROOT: " + isRoot);
			// log.debug("Am I Logged on as ROOT: " +
			// PlatformDependent.isRoot());
			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			if (remoteInetAddr.isMulticastAddress()) {
				ChannelFuture future = ch.joinGroup(remoteInetSocket, nic);
				log.debug("Result of JoinGroup: " + future.toString());
			}

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}
	}
	
	public void sendMessage(ByteBuf buf) {
		try	{
			DatagramPacket packet = new DatagramPacket(buf, remoteInetSocket, localInetSocket);
			log.debug("Sending Request Request : " + packet.toString());
			//ch.writeAndFlush(packet).sync();
			ch.writeAndFlush(packet);
			log.debug("Sent Request");
		}catch(Exception e) {
			log.error("Error sendMessage",e);
		}
	}

	/***
	 * Send a ZoneQuery Request for the ZoneID
	 * @param remoteZoneID
	 * @throws Exception
	 */
	public void sendZoneQueryRequest(String remoteZoneID) throws Exception {
		// Create Message
		// OHZRequestJoin joinOLD = new OHZRequestJoin(zoneID);
		OHZZoneQueryRequest zoneQuery = new OHZZoneQueryRequest(remoteZoneID);
		// ByteBuf buffer = Unpooled.copiedBuffer(joinOLD.data);
		//DatagramPacket packet = new DatagramPacket(zoneQuery.getBuffer(), remoteInetSocket, localInetSocket);
		// DatagramPacket packets = new DatagramPacket(buffer,
		// remoteInetSocket, localInetSocket);

		log.debug("Sending ZoneQuery Request : " + zoneQuery.toString());
		//ch.writeAndFlush(zoneQuery).sync();
		ch.writeAndFlush(zoneQuery);
		log.debug("Sent ZoneQuery Request");
		//zoneQuery.getBuffer().release();
	}

	/***
	 * Send a Leave Request for the ZoneID
	 * @param remoteZoneID
	 */
	public void sendLeaveRequest(String remoteZoneID) {
		try {
			log.debug("Send Leave Request: " + remoteZoneID);
			OHZLeaveRequest leave = new OHZLeaveRequest();
			ByteBuf buffer = Unpooled.copiedBuffer(leave.getBuffer());
			DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);
			log.debug("Sending : " + packet.toString());
			//ch.writeAndFlush(packet).sync();
			ch.writeAndFlush(packet);
			log.debug("Sent Leave Message");

		} catch (Exception e) {
			log.error("Error Sending Leave Message", e);
		}
	}

	public void stop() {
		log.info("Stopping OHZConnector");
		try {
			
			// OHZLeaveRequest leave = new OHZLeaveRequest();
			// ByteBuf buffer = Unpooled.copiedBuffer(leave.getBuffer());
			// DatagramPacket packet = new DatagramPacket(buffer,
			// remoteInetSocket, localInetSocket);
			// log.debug("Sending : " + packet.toString());
			// ch.writeAndFlush(packet).sync();
			// log.debug("Sent Leave Message");
		} catch (Exception e) {
			log.error("Error Sending Leave Message", e);
		}
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
			log.error("Error ShuttingDown", e);
		}
	}
}