package org.rpi.songcast.ohu.receiver.handlers;



import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;


public class OHUMessageTester extends SimpleChannelInboundHandler<DatagramPacket> {
	
	private Thread testerThread = null;
	private OHUThreadTester tester = new OHUThreadTester();
	
	public OHUMessageTester() {
		testerThread = new Thread(tester);
		testerThread.start();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf content = msg.content();
		tester.addToQueue(content);		
	}



}
