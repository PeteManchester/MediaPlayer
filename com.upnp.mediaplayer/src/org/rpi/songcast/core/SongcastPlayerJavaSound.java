package org.rpi.songcast.core;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.rpi.songcast.ohm.OHMEventAudio;

public class SongcastPlayerJavaSound implements ISongcastPlayer, Runnable {

	private Vector mWorkQueue = new Vector();
	private boolean run = true;

	private Logger log = Logger.getLogger(this.getClass());

	private AudioFormat audioFormat = new AudioFormat(44100, 16, 2, true, true);
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	private SourceDataLine soundLine = null;

	private boolean bWrite = false;

//	public static SongcastPlayerJavaSound getInstance() {
//		if (instance == null) {
//			instance = new SongcastPlayerJavaSound();
//		}
//		return instance;
//	}

	public SongcastPlayerJavaSound() {
		createSoundLine();
	}

	public void createSoundLine() {
		try {
			if (soundLine == null) {
				soundLine = (SourceDataLine) AudioSystem.getLine(info);
				soundLine.open(audioFormat);
				soundLine.start();
				bWrite = true;
			}
		} catch (Exception e) {
			log.error("Error opening Sound:", e);
		}
	}

	@Override
	public void play() {

	}


	public void close() {
		try {
			soundLine.close();
			soundLine = null;
			bWrite = false;
		} catch (Exception e) {
			log.error("Error Closing Stream", e);
		}

	}

	public void addData(byte[] data) {
		if (!bWrite)
			return;
		try {
			if (soundLine != null) {
				// log.debug(data.length);
				soundLine.write(data, 0, data.length);
			}
		} catch (Exception e) {
			log.error("Error Writing Data", e);
		}
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
	
	@Override
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					OHMEventAudio audio  = (OHMEventAudio) get();
					while(audio.getTimeToPlay() > System.currentTimeMillis())
					{
						if(audio.expired())
						{
							break;
						}
						sleep(2);
						audio.incAttempts();
					}
					addData(audio.getSound());					
				} catch (Exception e) {
					log.error("Error in Run Method");
				}
			} else {
				sleep(10);
			}
		}
	}
	
	@Override
	public void stop() {
		run = false;
		clear();
		close();
	}
	

}
