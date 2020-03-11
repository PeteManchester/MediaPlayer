package org.rpi.songcast.ohu.sender;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.Logger;

public class MPDStreamerController {

	private static MPDStreamerController instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private Queue<TestAudioByte> queue = new LinkedList<TestAudioByte>();
	Thread threadPlayer = null;
	
	
	/***
	 * 
	 * @return
	 */
	public static MPDStreamerController getInstance() {
		if (instance == null) {
			instance = new MPDStreamerController();
			
		}
		return instance;
	}

	/***
	 * 
	 */
	private MPDStreamerController() {
		MPDStreamerConnector player = new MPDStreamerConnector();
		threadPlayer = new Thread(player, "MPDStreamerConnector");
		threadPlayer.start();
	}

	/***
	 * 
	 * @param b
	 */
	public void addSoundByte(byte[] b, int frameId) {
		TestAudioByte t = new TestAudioByte(b,frameId);
		queue.add(t);
	}
	
	


	/**
	 * @return the queue
	 */
	public Queue<TestAudioByte> getQueue() {
		return queue;
	}	
	
	/***
	 * 
	 * @return
	 */
	public TestAudioByte getNext() {
		//log.debug("Queue Size: " + queue.size());
		return queue.poll();
	}


}
