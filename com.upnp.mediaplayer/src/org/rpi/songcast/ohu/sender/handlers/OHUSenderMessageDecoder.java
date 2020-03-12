package org.rpi.songcast.ohu.sender.handlers;

import java.nio.charset.Charset;
import java.util.List;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;
import org.rpi.songcast.ohu.sender.messages.OHUMessageJoin;
import org.rpi.songcast.ohu.sender.messages.OHUMessageLeave;
import org.rpi.songcast.ohu.sender.messages.OHUMessageListen;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;



public class OHUSenderMessageDecoder extends MessageToMessageDecoder<DatagramPacket> {
	
	private Logger log = Logger.getLogger(this.getClass());

	
	public OHUSenderMessageDecoder()
	{
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		if (msg instanceof DatagramPacket) {
			
			ByteBuf buf = msg.content();
			int type = buf.getByte(5) & ~0x80;
			SongcastMessage message = null;
			switch (type) {
			case 0://Join
				log.debug("Join: " + msg.sender() + " " + buf.toString(Charset.forName("utf-8")));
				message = new OHUMessageJoin(buf,msg.sender());
				
				out.add(message);
				break;
			case 1://Listen
				//log.debug("Listen " + msg.sender() + " " + buf.toString(Charset.forName("utf-8")));
				message = new OHUMessageListen(buf,msg.sender());
				out.add(message);
				break;
			case 2://Leave
				log.debug("Leave  "+ msg.sender() + " " + buf.toString(Charset.forName("utf-8")));
				message = new OHUMessageLeave(buf, msg.sender());
				out.add(message);
				
				break;
			default:
				log.info("Unknown Message: " + buf.toString(Charset.forName("utf-8")));
				break;
			}
			
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		//ctx.close();
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Unregistered: " + ctx.name());
		super.channelUnregistered(ctx);
	}
}

