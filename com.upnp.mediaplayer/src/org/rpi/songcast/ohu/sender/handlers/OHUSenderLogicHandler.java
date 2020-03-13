package org.rpi.songcast.ohu.sender.handlers;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageSlave;
import org.rpi.songcast.ohu.sender.OHUSenderConnection;
import org.rpi.songcast.ohu.sender.OHUSenderController;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.mpd.MPDStreamerConnector;
import org.rpi.songcast.ohu.sender.response.OHUSenderSlave;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class OHUSenderLogicHandler extends SimpleChannelInboundHandler<Object> {

	private Logger log = Logger.getLogger(this.getClass());

	//private Thread threadPlayer = null;
	//MPDSreamerConnector client = null;
	//OHUSenderConnection ohuSenderConnection = null;

	//Map<String, InetSocketAddress> receivers = new ConcurrentSkipListMap<String, InetSocketAddress>();
	//InetSocketAddress primaryReceiver = null;

	public OHUSenderLogicHandler() {
		//this.ohuSenderConnection = ohuSenderConnection;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof OHUMessageJoin) {
			OHUMessageJoin mJoin = (OHUMessageJoin) msg;
			OHUSenderController.getInstance().AddClient(mJoin);
			/*
			if (!receivers.containsKey(mJoin.getHostString())) {
				log.debug("Join Request. Adding Songcast Receiver: " + mJoin.toString());
				receivers.put(mJoin.getHostString(), mJoin.getAddress());
				if (primaryReceiver == null) {
					log.debug("Join Request. No other Songcast Receiver so making this one Primary: " + mJoin.toString());
					setRemoteAddress(mJoin.getAddress());
					startMPDConnection();
				}
				buildSlaveRequest();
			}
			*/
			// long timeNow = System.currentTimeMillis();
			// ohuSenderConnection.setRemoteAddress(mJoin.getAddress());

		} else if (msg instanceof OHUMessageLeave) {
			OHUMessageLeave ohuLeave = (OHUMessageLeave) msg;
			log.debug("Leave Request. Removing Songcast Receiver: " + ohuLeave.toString());
			OHUSenderController.getInstance().removeClient(ohuLeave);
			/*
			if (receivers.containsKey(ohuLeave.getHostString())) {
				receivers.remove(ohuLeave.getHostString());
				if (receivers.size() == 0) {
					log.debug("Leave Request. Removed Songcast Receiver, no other Songcast Receivers to stop sending");
					setRemoteAddress(null);
				} else {
					String key = receivers.keySet().stream().findFirst().get();
					if (!receivers.containsKey(primaryReceiver.getHostString())) {
						log.debug("Leave Request. Removed Songcast Receiver: " + ohuLeave.toString() + " Promoting this Receiver to be Primary: " + key);
						setRemoteAddress(receivers.get(key));
					}

				}
				buildSlaveRequest();
			}
			*/
		}
	}

	

	/***
	 * Set the RemoteAddress of the primary Songcast Receiver.
	 * 
	 * @param remoteAddress
	 
	private void setRemoteAddress(InetSocketAddress remoteAddress) {
		primaryReceiver = remoteAddress;
		ohuSenderConnection.setRemoteAddress(remoteAddress);
		if (remoteAddress == null) {
			log.debug("All Songcast Receivers have been removed, stop the MPD Connection");
			stopMPDConnection();
		}
	}
	*/

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		// final Channel inboundChannel = ctx.channel();
		log.debug("Channel Active::");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive::");
	}




}
