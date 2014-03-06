package org.rpi.songcast.ohm;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.rpi.songcast.events.EventOHMAudioStarted;
import org.rpi.songcast.events.EventSongCastBase;

public class OHMMessageQueue extends Observable implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	//private SongcastTimer timer = null;
	//private Thread timerThread = null;
	private Vector mWorkQueue = new Vector();
	private boolean run = true;
	private boolean started = false;

	public OHMMessageQueue() {
		log.warn("Opening OHM Message Queue");
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public synchronized void put(Object object) {
		// log.debug("Put Object in WorkQueue " + object.toString());
		try {
			mWorkQueue.addElement(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized Object get() {
		Object object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					byte[] command = (byte[]) get();
					processEvent(command);
				} catch (Exception e) {

				}
			} else {
				sleep(100);
			}
		}
	}

	private void processEvent(byte[] data) {
		byte[] type = getBytes(5, 5, data);

		int iType = new BigInteger(type).intValue();
		switch (iType) {
		case 3:// AUDIO
			startListen();
			OHMEventAudio audio = new OHMEventAudio();
			audio.data = data;
			audio.checkMessageType();
			break;
		case 4:// TRACK INFO
			startListen();
			log.debug("TrackInfo");
			OHMEventTrack evt = new OHMEventTrack();
			evt.data = data;
			evt.checkMessageType();
			// Do Something..
			break;
		case 5:// MetaText INFO
			startListen();
			log.debug("MetaInfo");
			OHMEventMetaData evm = new OHMEventMetaData();
			evm.data = data;
			evm.checkMessageType();
			break;
		default:
			log.debug("OHM Message: " + iType);
			break;
		}
	}
	
	private void startListen()
	{
		if (!started) {
		EventOHMAudioStarted ev = new EventOHMAudioStarted();
		fireEvent(ev);
		started = true;
		}
	}

	public void stop() {
		run = false;
//		if (timer != null) {
//			timer.setRun(false);
//			timer = null;
//		}
		//if (timerThread != null) {
		//	timerThread = null;
		//}
	}

	/*
	 * DUPLICATE refactor later.. Get a portion of the bytes in the array.
	 */
	public byte[] getBytes(int start, int end, byte[] data) {
		int size = (end - start) + 1;
		int count = 0;
		byte[] res = new byte[size];
		for (int i = start; i <= end; i++) {
			res[count] = data[i];
			count++;
		}
		return res;
	}
	
	/**
	 * Fire the Events
	 * 
	 * @param ev
	 */
	public void fireEvent(EventSongCastBase ev) {
		setChanged();
		notifyObservers(ev);
	}

}
