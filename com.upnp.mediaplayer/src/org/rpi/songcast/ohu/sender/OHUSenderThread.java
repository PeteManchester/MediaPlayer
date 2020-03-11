package org.rpi.songcast.ohu.sender;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

import io.netty.channel.socket.DatagramPacket;

public class OHUSenderThread implements Runnable {
	
	private boolean isRun = true;
	private OHUSenderConnection ohu = null;
	private int frameCount = 0;
	
	private Logger log = Logger.getLogger(this.getClass());
	
	
	public OHUSenderThread(OHUSenderConnection ohu ) {
		this.ohu = ohu;
	}
	
	

	@Override
	public void run() {		
		while(isRun) {
			try
			{
				TestAudioByte tab = MPDStreamerController.getInstance().getNext();
				if(tab !=null) {
					if(ohu !=null ) {
						byte[] buffer = tab.getAudio();
						OHUSenderAudioResponse r = new OHUSenderAudioResponse(frameCount, buffer);	
						try {
							ohu.sendMessage(r);
							frameCount++;
						}
						catch(Exception e) {
							log.error("Error Send AudioBytes", e);
						}					
					}
				}
			}
			catch(Exception e) {
				log.error("Error Get AudioBytes", e);
			}
			
		}		
	}

}
