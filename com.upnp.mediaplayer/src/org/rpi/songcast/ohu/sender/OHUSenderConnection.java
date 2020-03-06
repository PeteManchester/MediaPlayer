package org.rpi.songcast.ohu.sender;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.songcast.ohu.receiver.OHUChannelInitializer;
import org.rpi.songcast.ohu.receiver.OHURequestJoin;
import org.rpi.songcast.ohu.receiver.OHURequestListen;
import org.rpi.songcast.ohz.common.OHZLeaveRequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class OHUSenderConnection {
	
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

	public OHUSenderConnection(String zoneID, InetAddress localInetAddr) {
		log.debug("Create OHUSenderConnector: " + localInetAddr.getHostAddress());

		this.localInetAddr = localInetAddr;
		this.zoneID = zoneID;
	}

	public String run() throws Exception {
		String uri = "";
		try {
			log.debug("Start OHUConnector: " + localInetAddr.getHostName());

			localInetSocket = new InetSocketAddress(0);
			NetworkInterface nic = NetworkInterface.getByInetAddress(localInetAddr);

			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioDatagramChannel.class);
			
			b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator( 1024 * 5 ));
			b.handler(new OHUChannelInitializer());
			log.debug("OHU Bind to Socket: " + localInetSocket.getHostName());
			ch = (DatagramChannel) b.bind(localInetSocket).sync().channel();
			log.debug(ch.localAddress());
			uri = localInetAddr.getHostAddress() + ":" + ch.localAddress().getPort();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {

		}		
		log.debug("MyURI: " + uri);
		return uri;
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
			log.debug("Attempt to Stop Songcast Playback");
			if (ch != null) {
				try
				{
					OHZLeaveRequest leave = new OHZLeaveRequest();
					ByteBuf buffer = Unpooled.copiedBuffer(leave.getBuffer());
					DatagramPacket packet = new DatagramPacket(buffer, remoteInetSocket, localInetSocket);
					log.debug("Sending : " + packet.toString());
					ch.writeAndFlush(packet).sync();
					log.debug("Sent Leave Message");
					PlayManager.getInstance().setStatus("Stopped", "SONGCAST");
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
			log.error("Error ShuttingDown", e);
		}
	}

}
