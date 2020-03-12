package org.rpi.songcast.ohu.sender.handlers;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.OHUSenderConnection;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.mpd.MPDSreamerConnector;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUSenderLogicHandler extends SimpleChannelInboundHandler<Object> {

	private Logger log = Logger.getLogger(this.getClass());
	
	private boolean isConnected = false;
	private Thread threadPlayer = null;
	MPDSreamerConnector client = null;
	OHUSenderConnection ohuSenderConnection = null;
	
	public OHUSenderLogicHandler(OHUSenderConnection ohuSenderConnection) {
		this.ohuSenderConnection = ohuSenderConnection;
	}


	/*
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.debug("Read");
	}
	*/

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof OHUMessageJoin) {
			OHUMessageJoin mJoin = (OHUMessageJoin) msg;
			//long timeNow = System.currentTimeMillis();
			ohuSenderConnection.setRemoteAddress(mJoin.getAddress());
			if(!isConnected) {
				startMPDConnection();
			}
			
			isConnected = true;
			
		} else if (msg instanceof OHUMessageLeave) {
			OHUMessageLeave ohuLeave = (OHUMessageLeave) msg;
			isConnected = false;
			ohuSenderConnection.setRemoteAddress(null);
			stopMPDConnection();
		}
	}
	
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();
        log.debug("Channel Active::");
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	log.debug("Channel Inactive::");
    }
    
    private void startMPDConnection() {
    	if(threadPlayer !=null) {
    		stopMPDConnection();
    	}
    	client = new MPDSreamerConnector();
		threadPlayer = new Thread(client, "MPDStreamerConnector");
		threadPlayer.start();
    }
    
    private void stopMPDConnection() {
    	
    	try {
    		if(client !=null) {
    			client.stop();
    		}    		
        	client = null;
        	threadPlayer =null;
    	}
    	catch(Exception e) {
    		log.error("Error Stopping MPDConnection",e);
    	}   	
    	
    }
}
