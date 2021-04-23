package org.rpi.songcast.ohz.common;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import io.netty.util.ReferenceCountUtil;

public class OHZLogicHandler extends ChannelDuplexHandler {

	private Logger log = Logger.getLogger(this.getClass());
	private String latestZone = "";
	private InetAddress localInetAddr = null;
	private InetSocketAddress remoteInetSocketAddr = null;
	private InetSocketAddress localInetSocketAddr = null;
	private long lastZoneQueryRequest = 0;
	// private OHUSenderConnection ohuSender = null;
	private String myZoneId = Config.getInstance().getMediaplayerFriendlyName();

	private ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
	private String statusZoneQueryRequest = "NONE";
	//private ChannelHandlerContext ctx = null;
	private DatagramPacket zoneQueryRequestPacket = null;

	public OHZLogicHandler(InetAddress localInetAddr, InetSocketAddress remoteInetSocketAddr, InetSocketAddress localInetSocketAddress) {
		this.localInetAddr = localInetAddr;
		this.remoteInetSocketAddr = remoteInetSocketAddr;
		this.localInetSocketAddr = localInetSocketAddress;
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if (msg instanceof OHZZoneQueryRequest) {
			OHZZoneQueryRequest ohzZR = (OHZZoneQueryRequest) msg;
			log.debug("write OHZZoneQueryRequest: " + ohzZR);
			DatagramPacket packet = new DatagramPacket(ohzZR.getBuffer(), remoteInetSocketAddr, localInetSocketAddr);
			this.zoneQueryRequestPacket = packet;
			lastZoneQueryRequest = System.currentTimeMillis();
			startTimer("FIRSTTIME", ctx);
			ctx.write(packet, promise);
		} else {
			ctx.write(msg, promise);
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.debug("Read: " + msg);

		if (msg instanceof OHZZoneUriMessage) {
			log.debug("Received OHZZoneUriMessage: " + msg);
			long timeNow = System.currentTimeMillis();
			//if(timeNow - lastZoneQueryRequest < 1000) {
			if (statusZoneQueryRequest.equalsIgnoreCase("FIRSTTIME") || statusZoneQueryRequest.equalsIgnoreCase("SECONDTIME")) {
				stopTimer("NONE");
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
				latestZone = mess.getZone();
				OHUReceiverController.getInstance().startReceiver(mess.getUri(), latestZone, localInetAddr);

			} else {
				log.info("Zone URI Message was received with no corresponding Zone QueryRequest ");
				statusZoneQueryRequest = "NONE";
				ReferenceCountUtil.release(msg);
			}
		} else if (msg instanceof OHZZoneQueryMessage) {
			OHZZoneQueryMessage ohz = (OHZZoneQueryMessage) msg;
			if (ohz.getZoneId().equals(myZoneId)) {
				log.debug("#####################  This is a request for my OHU URL");
				String myURI = OHUSenderController.getInstance().startSenderConnection(localInetAddr, ohz.getRemoteAddress());
				OHZZoneUriResponse res = new OHZZoneUriResponse(myURI, myZoneId);
				DatagramPacket packet = new DatagramPacket(res.getBuffer(), remoteInetSocketAddr, localInetSocketAddr);
				log.debug("Sending ZoneQuery Request : " + packet.toString());
				// ctx.writeAndFlush(packet).sync();
				ctx.writeAndFlush(packet);
			}
		}
	}

	
	private void startTimer(String status, ChannelHandlerContext ctx) {
		statusZoneQueryRequest = status;
		//this.ctx = ctx;
		ses.schedule(new Runnable() {
			@Override
			public void run() {
				log.debug("TimerFired");
				if (statusZoneQueryRequest.equalsIgnoreCase("FIRSTTIME")) {
					log.debug("Timer Fired for the first time, resend the ZoneQueryRequest");
					if (ctx != null && zoneQueryRequestPacket != null) {
						log.debug("Resending the ZoneQueryRequest: " + zoneQueryRequestPacket);
						ctx.writeAndFlush(zoneQueryRequestPacket);
						startTimer("SECONDTIME", ctx);
					}
				} else if (statusZoneQueryRequest.equalsIgnoreCase("SECONDTIME")) {
					log.debug("Timer Fired Second ZoneQuery Request times out, do nothing else");
					stopTimer("NONE");

				} else {
					log.debug("Timer Fired, no status: " + statusZoneQueryRequest);
				}
			}
		}, 3000, TimeUnit.MILLISECONDS);
	}
	

	private void stopTimer(String status) {
		//ses.shutdownNow();
		log.debug("StopTimer. Status: " + status);
		statusZoneQueryRequest = status;

	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Error. OHZLogicHandler: ",cause);
		ctx.close();
	}
}
