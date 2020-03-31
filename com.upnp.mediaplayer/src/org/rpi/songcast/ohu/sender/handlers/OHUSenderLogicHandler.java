package org.rpi.songcast.ohu.sender.handlers;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.OHUSenderController;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.messages.OHUMessageListen;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/***
 * Handle the OHU messages
 * Join - Client wants to register for Songcast
 * Listen - Client want to listen to Songcast
 * Leave - Client wants to leave session
 * @author phoyle
 *
 */
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
		}
	}

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Error. OHUSenderLogicHandler: ",cause);
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.debug("Channel Active::");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive::");
	}




}
