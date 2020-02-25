package org.rpi.plugin.lirc;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.rpi.mplayer.CloseMe;


public class LIRCWorkQueue extends Thread {
	private Logger log = Logger.getLogger(LIRCWorkQueue.class);
	
	private Vector mWorkQueue = new Vector();
	private boolean run = true;
	
	public LIRCWorkQueue() {
		this.setName("LIRCWorkQueue");
	}
	
	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}
	
	public synchronized void put(Object object) {
		log.debug("Put Object in WorkQueue " + object.toString());
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
	
	private synchronized void stopRunning() {
		log.info("Stopping WorkerQueue");
		run = false;
		log.info("Stopped WorkerQueue");
		clear();
	}
	
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					String command = (String) get();
					log.info("Pulled Command Subscription " + command);
					processEvent(command);
					log.info("Number of Commands in Queue: " + mWorkQueue.size());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				sleep(100);
			}
		}
	}
	
	private void processEvent(String command) {
		if(command ==null)
			return;
		log.debug("Sending Command: " + command);
		Process pa =  null;
		try
		{
		pa = Runtime.getRuntime().exec(command);
		pa.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			log.debug("Result of " + command + " : " + line);
		}
		reader.close();
		//pa.getInputStream().close();
		}
		catch(Exception e)
		{
			log.error("Error Sending Command: " + command , e);
		}finally {
			if(pa !=null && pa.getInputStream() != null) {
				CloseMe.close(pa.getInputStream());
				pa = null;
			}
		}
	}

}
