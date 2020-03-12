package org.rpi.songcast.ohu.sender;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.requests.OHURequestListen;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
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
	private int frameCount = 0;

	private Thread ohuSenderThread = null;
	OHUSenderThread1 ohuSender =null;

	private int lastTab = 0;

	private boolean bConnected = false;

	public OHUSenderConnection(String zoneID, InetAddress localInetAddr) {
		log.debug("Create OHUSenderConnector: " + localInetAddr.getHostAddress());

		this.localInetAddr = localInetAddr;
		this.zoneID = zoneID;

		ohuSender = new OHUSenderThread1(this);
		ohuSenderThread = new Thread(ohuSender, "OHUSenderThread");
		ohuSenderThread.start();
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

			// b.option(ChannelOption.SO_BROADCAST, true);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(1024 * 5));
			b.handler(new OHUSenderChannelInitialiser(this));
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

	public void sendMessage(DatagramPacket packet) throws Exception {
		try {
			// log.debug("SendMessage");
			ch.writeAndFlush(packet).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						//log.debug("Write successful");
					} else {
						log.error("Error writing message to Raspi host");
					}
				}
			});
		} catch (Exception e) {
			log.error("SendMessage", e);
			throw e;
		}
	}

	public void stop() {

		log.debug("Attempt to Stop Songcast Playback");

		try {
			
			if (ohuSender != null) {
				ohuSender.stop();
				ohuSender = null;
				ohuSenderThread = null;				
			}
		} catch (Exception e) {
			log.error("Error Closing OHUSenderThread", e);
		}
		try {
			group.shutdownGracefully();
		} catch (Exception e) {
			log.error("Error ShuttingDown", e);
		}
	}

	public void setRemoteAddress(InetSocketAddress sender) {
		log.debug("Remote Socket Address is being set: " + sender);
		this.remoteInetSocket = sender;
		if (remoteInetSocket == null) {
			log.error("This is NUL NOT GOOD");
			bConnected = false;
		} else {
			bConnected = true;
		}

	}

	public void sendMessage(OHUSenderAudioResponse r) throws Exception {
		if (remoteInetSocket == null) {
			return;
		}
		DatagramPacket packet = new DatagramPacket(r.getBuffer(), remoteInetSocket, localInetSocket);
		sendMessage(packet);
	}

}
