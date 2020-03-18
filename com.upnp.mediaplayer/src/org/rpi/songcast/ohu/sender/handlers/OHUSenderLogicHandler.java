package org.rpi.songcast.ohu.sender.handlers;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.OHUSenderController;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.messages.OHUMessageListen;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUSenderLogicHandler extends SimpleChannelInboundHandler<Object> {

	private Logger log = Logger.getLogger(this.getClass());


	public OHUSenderLogicHandler() {

	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof OHUMessageJoin) {
			OHUMessageJoin mJoin = (OHUMessageJoin) msg;
			OHUSenderController.getInstance().AddClient(mJoin);

		} else if (msg instanceof OHUMessageLeave) {
			OHUMessageLeave ohuLeave = (OHUMessageLeave) msg;
			log.debug("Leave Request. Removing Songcast Receiver: " + ohuLeave.toString());
			OHUSenderController.getInstance().removeClient(ohuLeave);
		} else if (msg instanceof OHUMessageListen) {
			OHUMessageListen listen = (OHUMessageListen) msg;
			OHUSenderController.getInstance().addListen(listen);
			//log.debug("Listen: " + listen.getAddress());
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
