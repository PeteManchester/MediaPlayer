package org.rpi.songcast.ohu.sender.mpd.copy;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;

public class MPDStreamerController {

	private static MPDStreamerController instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private Queue<OHUSenderAudioResponse> queue = new ConcurrentLinkedQueue<OHUSenderAudioResponse>();
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
	}

	/***
	 * 
	 * @param b
	 * 
	 *            public void addSoundByte(byte[] b, int frameId) {
	 *            TestAudioByte t = new TestAudioByte(b,frameId); queue.add(t);
	 *            }
	 */

	/**
	 * @return the queue
	 */
	public Queue<OHUSenderAudioResponse> getQueue() {
		return queue;
	}

	/***
	 * 
	 * @return
	 */
	public OHUSenderAudioResponse getNext() {
		// log.debug("Queue Size: " + queue.size());
		return queue.poll();
	}

	public void addSoundByte(OHUSenderAudioResponse a, int frameCount) {
		queue.add(a);
	}

}
