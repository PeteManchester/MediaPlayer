package org.rpi.songcast.ohu.sender.mpd;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohu.sender.response.OHUSenderAudioResponse;
import org.scratchpad.songcast.test.http.streaming.TestHttpURLConnection;

public class MPDStreamerController {

	private static MPDStreamerController instance = null;
	private Logger log = Logger.getLogger(this.getClass());

	// private Queue<OHUSenderAudioResponse> queue = new
	// ConcurrentLinkedQueue<OHUSenderAudioResponse>();
	private Queue<OHUSenderAudioResponse> queue = new ArrayDeque<OHUSenderAudioResponse>();
	private Thread mpdThread = null;
	private MPDStreamerConnector mpdClient = null;
	//private TestHttpURLConnection mpdClient = null;
	private int frameCount = 0;// Integer.MAX_VALUE - 1000;

	// private boolean bFinished = false;

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
		OHUSenderAudioResponse a = queue.poll();
		if (a != null) {
			frameCount++;
			if (frameCount < 0) {
				log.debug("Integer RollOver: " + frameCount);
				frameCount = 0;
			}
			a.setFrameId(frameCount);
			if (frameCount % 1000 == 0) {
				log.debug("Frame: " + frameCount);
			}
		}
		return a;
	}

	public void addSoundByte(OHUSenderAudioResponse a) {
		queue.add(a);
	}

	/***
	 * 
	 */
	private void startMPDConnection() {
		log.debug("Start MPDConnection");
		if (mpdThread != null) {
			stopMPDConnection();
		}
		mpdClient = new MPDStreamerConnector();
		//mpdClient = new TestHttpURLConnection();
		mpdThread = new Thread(mpdClient, "MPDStreamerConnector");
		mpdThread.start();
	}

	/***
	 * 
	 */
	private void stopMPDConnection() {
		try {
			if (mpdClient != null) {
				log.debug("Stopping MPD Connection");
				mpdClient.stop();
			}
			mpdClient = null;
			mpdThread = null;
		} catch (Exception e) {
			log.error("Error Stopping MPDConnection", e);
		}
	}

	/**
	 * @return the bFinished
	 * 
	 *         public boolean isFinished() { return bFinished; }
	 */

	/**
	 * @param bFinished
	 *            the bFinished to set
	 * 
	 *            public void setFinished(boolean bFinished) { this.bFinished =
	 *            bFinished; if(bFinished) { stopMPDConnection(); } }
	 */

	public void start() {
		if (mpdClient != null) {
			return;
		}
		stopMPDConnection();
		startMPDConnection();

	}

	public void stop() {
		stopMPDConnection();
	}

}
