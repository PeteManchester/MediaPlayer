package org.rpi.songcast.ohu.sender;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderMetaTextResponse;
import org.rpi.songcast.ohu.sender.response.OHUSenderTrackResponse;
import org.rpi.utils.Utils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

public class OHUSenderConnection {

	private Logger log = Logger.getLogger(this.getClass());

	private InetSocketAddress remoteInetSocket = null;
	private InetAddress localInetAddr = null;
	private InetSocketAddress localInetSocket = null;

	private EventLoopGroup group = null;

	private DatagramChannel ch = null;

	private String myURI = "";

	private boolean enabled = true;

	private OHUSenderTrackResponse ohuTrackResponse = null;
	private OHUSenderMetaTextResponse ohuMetaTextResponse = null;

	public OHUSenderConnection(InetAddress localInetAddr) {
		ThreadFactory threadFactory = new DefaultThreadFactory("OHUSenderEventLoopGroupThread");
		group = new NioEventLoopGroup(4, threadFactory);
		log.debug("Create OHUSenderConnector: " + localInetAddr.getHostAddress());
		this.localInetAddr = localInetAddr;
		log.debug("Created OHUSenderConnector: " + localInetAddr.getHostAddress());
	}

	public String run() throws Exception {
		String uri = "";
		try {
			log.debug("Start OHUConnector: " + localInetAddr.getHostName());

			localInetSocket = new InetSocketAddress(0);

			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioDatagramChannel.class);

			int byteBuffer = 10240;
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.IP_MULTICAST_LOOP_DISABLED, false);
			b.option(ChannelOption.SO_RCVBUF, byteBuffer);
			b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(byteBuffer * 4));
			b.option(ChannelOption.SO_SNDBUF, byteBuffer);

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

	/***
	 * Send a Datagram Packet
	 * 
	 * @param packet
	 * @throws Exception
	 */
	public void sendMessage(DatagramPacket packet) throws Exception {
		try {
			ch.writeAndFlush(packet);
			/*
			 * ch.writeAndFlush(packet).addListener(new ChannelFutureListener()
			 * {
			 * 
			 * @Override public void operationComplete(ChannelFuture future)
			 * throws Exception { if (!future.isSuccess()) {
			 * log.error("Error writing Datagram Packet for RemoteHost"); } }
			 * });
			 */

		} catch (Exception e) {
			log.error("SendMessage", e);
			throw e;
		}
	}

	public void stop() {

		if (ch != null) {
			try {
				ch.close().sync();
				ch = null;
			} catch (Exception e) {

			}
		}

		try {
			group.shutdownGracefully();
		} catch (Exception e) {
			log.error("Error ShuttingDown", e);
		}
	}

	/***
	 * 
	 * @param sender
	 */
	public void setRemoteAddress(InetSocketAddress sender) {
		log.debug("Remote Socket Address is being set: " + sender);
		this.remoteInetSocket = sender;
		if (remoteInetSocket == null) {
			log.error("This is NUL. Not Connected");
		}
	}

	/***
	 * 
	 * @return
	 */
	public InetSocketAddress getRemoteAddress() {
		return remoteInetSocket;
	}

	/***
	 * 
	 */
	public String getRemoteHostString() {

		if (remoteInetSocket == null) {
			return "";
		}
		return remoteInetSocket.getHostString();

	}

	/***
	 * Send An OHUSenderAudioResponse to the main OHUListener.
	 * 
	 * @param r
	 * @throws Exception
	 */
	public void sendMessage(OHUSenderAudioResponse r) throws Exception {
		if (remoteInetSocket == null) {
			return;
		}
		DatagramPacket packet = new DatagramPacket(r.getBuffer(), remoteInetSocket, localInetSocket);
		// r.getBuffer().release();
		sendMessage(packet);
	}

	// TODO make just one method to send all Songcast responses..
	public void sendMessage(OHUSenderTrackResponse ohuTrackResponse) throws Exception {
		this.ohuTrackResponse = ohuTrackResponse;
		if (remoteInetSocket == null) {
			return;
		}
		DatagramPacket packet = new DatagramPacket(this.ohuTrackResponse.getBuffer().retain(), remoteInetSocket, localInetSocket);
		sendMessage(packet);
	}

	public void sendMessage(OHUSenderMetaTextResponse ohuMetaTextResponse) throws Exception {
		this.ohuMetaTextResponse = ohuMetaTextResponse;
		if (remoteInetSocket == null) {
			return;
		}
		DatagramPacket packet = new DatagramPacket(ohuMetaTextResponse.getBuffer().retain(), remoteInetSocket, localInetSocket);
		sendMessage(packet);
	}

	public String getURI() {
		if (Utils.isEmpty(myURI)) {
			try {
				myURI = run();
			} catch (Exception e) {
				log.error("Could Not Start OHUSenderConnection");
			}
		}
		return myURI;
	}

	public void setEnabled(boolean b) {
		this.enabled = b;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUSenderConnection [remoteInetSocket=");
		builder.append(getRemoteHostString());
		builder.append(", myURI=");
		builder.append(myURI);
		builder.append(", enabled=");
		builder.append(enabled);
		builder.append("]");
		return builder.toString();
	}

	public void sendTrackDetails() {
		if (ohuTrackResponse != null) {
			try {
				sendMessage(ohuTrackResponse);
			} catch (Exception e) {
				log.error("Error Sending OHUTrackResponse", e);
			}
		}
		if (ohuMetaTextResponse != null) {
			try {
				sendMessage(ohuMetaTextResponse);
			} catch (Exception e) {
				log.error("Error Sending OHUMetaTextResponse", e);
			}
		}

	}

}
