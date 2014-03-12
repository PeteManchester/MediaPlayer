package org.rpi.songcast.ohm;

import java.math.BigInteger;
import java.util.List;
import java.util.Observable;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.songcast.core.ISongcastPlayer;
import org.rpi.songcast.core.SlaveEndpoint;
import org.rpi.songcast.core.SongcastPlayerJSLatency;
import org.rpi.songcast.core.SongcastPlayerJavaSound;
import org.rpi.songcast.events.EventOHMAudioStarted;
import org.rpi.songcast.events.EventSongCastBase;

public class OHMMessageQueue extends Observable implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());
	// private SongcastTimer timer = null;
	// private Thread timerThread = null;
	private Vector mWorkQueue = new Vector();
	private boolean run = true;
	private boolean started = false;

	private ConcurrentHashMap<String, SlaveEndpoint> endpoints = new ConcurrentHashMap<String, SlaveEndpoint>();
	private boolean bSentInfo  = false;	
	
	private ISongcastPlayer player = null;
	private Thread threadPlayer = null;

	public OHMMessageQueue() {
		log.debug("Opening OHM Message Queue");
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
		if(Config.songcastLatencyEnabled)
		{
			//With Latency
			player = new SongcastPlayerJSLatency();
			threadPlayer = new Thread(player,"SongcastPlayerJavaSoundLatency");
			
		}
		else
		{
			player = new SongcastPlayerJavaSound();
			threadPlayer = new Thread(player,"SongcastPlayerJavaSound");
		}		
		
		threadPlayer.start();
		while (run) {
			if (!isEmpty()) {
				try {
					byte[] command = (byte[]) get();
					processEvent(command);
				} catch (Exception e) {

				}
			} else {
				sleep(1);
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
			if(!bSentInfo)
			{
				player.createSoundLine(audio.getTrackInfo());
				bSentInfo = true;
			}
			forwardToSlaves(data);
			
			audio.checkMessageType();
			player.put(audio);
			break;
		case 4:// TRACK INFO
			startListen();
			log.debug("OHM TrackInfo");
			OHMEventTrack evt = new OHMEventTrack();
			evt.data = data;
			evt.checkMessageType();
			forwardToSlaves(data);
			// Do Something..
			break;
		case 5:// MetaText INFO
			startListen();
			log.debug("OHM MetaInfo");
			OHMEventMetaData evm = new OHMEventMetaData();
			evm.data = data;
			evm.checkMessageType();
			break;
		case 6:
			log.debug("OHU Slave Info");
			OHMEventSlave evs = new OHMEventSlave();
			evs.data = data;
			evs.checkMessageType(this);
			break;
		default:
			log.debug("OHM Message: " + iType);
			break;
		}
	}

	private void forwardToSlaves(byte[] bytes) {
		for (SlaveEndpoint endpoint : endpoints.values()) {
			endpoint.sendData(bytes);
		}
	}

	private void startListen() {
		if (!started) {
			EventOHMAudioStarted ev = new EventOHMAudioStarted();
			fireEvent(ev);
			started = true;
		}
	}

	public void stop() {
		run = false;
		clear();
		player.stop();
		if(threadPlayer !=null)
		{
			threadPlayer =null;
		}
		removeAllEndpoints();
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

	/**
	 * @param endpoints
	 *            the endpoints to set
	 */
	public synchronized void setEndpoints(ConcurrentHashMap<String, SlaveEndpoint> endpoints) {
		this.endpoints = endpoints;
	}
	
	public synchronized ConcurrentHashMap<String, SlaveEndpoint> getEndpoints()
	{
		return endpoints;
	}
	
	/**
	 * Add an Endpoint
	 * @param slave
	 */
	public synchronized void addEndpoint(SlaveEndpoint slave)
	{
		if(!getEndpoints().containsKey(slave.getName()))
		{
			slave.createSocket();
			getEndpoints().put(slave.getName(), slave);
		}

	}
	
	/**
	 * Dispose and Remove the Endpoint
	 * @param list
	 */
	public synchronized void removeEndpoint(List<String> list)
	{
		for(String s : list)
		{
			if(getEndpoints().containsKey(s))
			{
				getEndpoints().get(s).dispose();
				getEndpoints().remove(s);
			}
		}
	}
	
	private synchronized void removeAllEndpoints()
	{
		for(SlaveEndpoint slave : getEndpoints().values())
		{
			slave.dispose();
		}
		getEndpoints().clear();
	}

}
