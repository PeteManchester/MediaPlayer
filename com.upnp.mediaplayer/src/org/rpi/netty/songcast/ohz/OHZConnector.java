package org.rpi.netty.songcast.ohz;

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
import io.netty.util.internal.PlatformDependent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.songcast.ohm.OHMRequestLeave;
import org.rpi.songcast.ohz.OHZRequestJoin;

public class OHZConnector {

	private final int remotePort;
	// ohz://239.255.255.250:51972/b33f69011e38827aa138adc6d00cb23e
	// private String zoneId = "adb3ff3c41b7ebd669a49e35d54222ae";
	//private String zoneID = "b33f69011e38827aa138adc6d00cb23e";
	private String zoneID ="";
	private Logger log = Logger.getLogger(this.getClass());

	private InetAddress remoteInetAddr = null;
	private InetSocketAddress remoteInetSocket = null;
	private InetAddress localInetAddr = null;
	private InetSocketAddress localInetSocket = null;
	
	private DatagramChannel ch = null;
	private EventLoopGroup group = new NioEventLoopGroup();

	public OHZConnector(String uri, String zoneID, InetAddress localInetAddr) {
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
	}

	public void run() throws Exception {
		log.debug("Run OHZConnector");
		try {
			PlayManager.getInstance().setStatus("Buffering","SONGCAST");
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
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, true);
			b.option(ChannelOption.SO_RCVBUF, 2048);
			b.option(ChannelOption.IP_MULTICAST_TTL, 255);
			b.option(ChannelOption.IP_MULTICAST_IF, nic);

			b.handler(new OHZChannelInitializer());
			log.debug("Am I Logged on as ROOT: " + PlatformDependent.isRoot());
			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			if (remoteInetAddr.isMulticastAddress()) {
				ChannelFuture future = ch.joinGroup(remoteInetSocket,nic);
				log.debug("Result of Join: " + future.toString());
			}
			// Create Message
			OHZRequestJoin join = new OHZRequestJoin(zoneID);
			ByteBuf buffer = Unpooled.copiedBuffer(join.data);
			DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);

			log.debug("Sending : " + packet.toString());
			ch.writeAndFlush(packet).sync();
			log.debug("Sent Join Message");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}
	}

	public void stop(String zoneID) {
		try
		{
			OHMRequestLeave leave = new OHMRequestLeave(zoneID);
			ByteBuf buffer = Unpooled.copiedBuffer(leave.data);
			DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);
			log.debug("Sending : " + packet.toString());
			ch.writeAndFlush(packet).sync();
			log.debug("Sent Leave Message");
		}
		catch(Exception e)
		{
			log.error("Error Sending Leave Message" ,e );			
		}
		try {
			if(ch!=null)
			{
				try{
					ch.close();
				}
				catch(Exception e)
				{
					log.error("Error Closing Channel" ,e );
				}
			}
			group.shutdownGracefully();
		} catch (Exception e) {
			log.error("Error ShuutingDown", e);
		}
	}
}