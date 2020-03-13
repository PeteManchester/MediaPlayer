package org.rpi.songcast.ohz.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.songcast.ohu.receiver.OHUReceiverController;
import org.rpi.songcast.ohu.sender.OHUSenderController;
import org.rpi.songcast.ohz.sender.OHZZoneQueryMessage;
import org.rpi.songcast.ohz.sender.OHZZoneUriResponse;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

public class OHZLogicHandler extends ChannelDuplexHandler {

	private Logger log = Logger.getLogger(this.getClass());
	private String latestZone = "";
	private InetAddress localInetAddr = null;
	private InetSocketAddress remoteInetSocketAddr = null;
	private InetSocketAddress localInetSocketAddr = null;
	private long lastZoneQueryRequest = 0;
	//private OHUSenderConnection ohuSender = null;
	private String myZoneId = Config.getInstance().getMediaplayerFriendlyName();

	public OHZLogicHandler(InetAddress localInetAddr, InetSocketAddress remoteInetSocketAddr, InetSocketAddress localInetSocketAddress) {
		this.localInetAddr = localInetAddr;
		this.remoteInetSocketAddr = remoteInetSocketAddr;
		this.localInetSocketAddr = localInetSocketAddress;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		log.debug("write..");
		if (msg instanceof OHZZoneQueryRequest) {
			OHZZoneQueryRequest ohzZR = (OHZZoneQueryRequest) msg;
			DatagramPacket packet = new DatagramPacket(ohzZR.getBuffer(), remoteInetSocketAddr, localInetSocketAddr);
			lastZoneQueryRequest = System.currentTimeMillis();
			ctx.write(packet, promise);
		} else {
			ctx.write(msg, promise);
		}

	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.debug("Read: " + ctx.channel().remoteAddress());

		if (msg instanceof OHZZoneUriMessage) {
			long timeNow = System.currentTimeMillis();
			if (timeNow - lastZoneQueryRequest <= 1000) {
				OHZZoneUriMessage mess = (OHZZoneUriMessage) msg;
				log.debug("OHZZoneUriMessage: " + mess.toString());
				if (mess.getUri().endsWith("0.0.0.0:0")) {
					log.debug("OHZZoneUriMessage URI ends with 0.0.0.0:0");
					if (latestZone.equalsIgnoreCase(mess.getZone())) {
						log.debug("OHZZoneUriMessage Stop OHUConnector");
						OHUReceiverController.getInstance().stop();
						latestZone = "";
					}
				}
				log.debug("OHZZoneUriMessage OHU is NULL so create it");
				latestZone = mess.getZone();
				OHUReceiverController.getInstance().startReceiver(mess.getUri(), latestZone, localInetAddr);

			} else {
				log.info("Zone URI Message was received with no corresponding Zone QueryRequest: " + lastZoneQueryRequest);
			}

		} else if (msg instanceof OHZZoneQueryMessage) {
			OHZZoneQueryMessage ohz = (OHZZoneQueryMessage) msg;
			if (ohz.getZoneId().equals(myZoneId)) {
				log.debug("#####################  This is a request for my OHU URL");
				// TODO for now just create a new Sender..
				//if(ohuSender !=null) {
				//	ohuSender.stop();
				//	ohuSender = null;
				//}
				//if(ohuSender ==null) {
				//	ohuSender = new OHUSenderConnection(localInetAddr);
				//}				
				//String myURI = ohuSender.getURI();
				String myURI = OHUSenderController.getInstance().startSenderConnection(localInetAddr, ohz.getRemoteAddress());
				OHZZoneUriResponse res = new OHZZoneUriResponse(myURI, myZoneId);
				DatagramPacket packet = new DatagramPacket(res.getBuffer(), remoteInetSocketAddr, localInetSocketAddr);
				log.debug("Sending ZoneQuery Request : " + packet.toString());
				ctx.writeAndFlush(packet).sync();
			}
		}
	}

}
