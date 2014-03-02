package org.rpi.songcast;

import java.math.BigInteger;
import java.util.Vector;

import org.apache.log4j.Logger;

public class OHMMessageQueue implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());

	private Vector mWorkQueue = new Vector();
	private boolean run = true;

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
		case 3:
			OHMResponseJoin audio = new OHMResponseJoin();
			audio.data = data;
			audio.checkMessageType();
			break;
		default:
			log.debug("OHM Mesage: " + iType);
			break;
		}
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

}
