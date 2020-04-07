package org.rpi.songcast.ohu.receiver.handlers;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageAudio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.DatagramPacket;

public class OHUThreadTester implements Runnable {

	private boolean isRun = true;
	private Logger log = Logger.getLogger(this.getClass());
	private Queue<ByteBuf> queue = new ArrayDeque<ByteBuf>();
	private int lastId = 0;

	@Override
	public void run() {
		while (isRun) {
			ByteBuf packet = queue.poll();
			if (packet != null) {
				
				//int type = packet.getByte(5) & ~0x80;
				
				short type = packet.getByte(5);
				
				if(type == 3) {				
					OHUMessageAudio message = new OHUMessageAudio(packet);
					if(message.getFrameNumber() - lastId != 1) {
						log.debug("Last Frame was missing: This FrameId: " + message.getFrameNumber() + " Last FrameId: " + lastId);
					}
					lastId = message.getFrameNumber();
				}
				
				
			} else {
				//log.debug("No packets");
				try {
					TimeUnit.MILLISECONDS.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	}

	public void addToQueue(ByteBuf packet) {
		queue.add(packet.retain());
	}

}
