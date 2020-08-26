package org.rpi.plugingateway;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.rpi.alarm.Alarm;
import org.rpi.config.Config;
import org.rpi.main.SimpleDevice;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventDisposing;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.plug.interfaces.AlarmClockInterface;
import org.rpi.sources.Source;

public class PluginGateWay extends Observable implements Observer {

	private static PluginGateWay instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String, Source>();
	private String source_name = "";

	private SimpleDevice simpleDevice = null;
	private String default_pin = "";
	private String standbyPin = "";

	public static PluginGateWay getInstance() {
		if (instance == null) {
			instance = new PluginGateWay();
		}
		return instance;
	}

	protected PluginGateWay() {

	}

	public void setSimpleDevice(SimpleDevice simpleDevice) {
		this.simpleDevice = simpleDevice;
		addObserver(simpleDevice);
	}

	/**
	 * Add a Source to the Product Provider
	 * 
	 * @param name
	 * @param type
	 * @param visible
	 */
	public synchronized void addSource(String type, String name, boolean visible) {
		simpleDevice.getProduct().addSource(Config.getInstance().getMediaplayerFriendlyName(), name, type, true);
	}

	/**
	 * The Product Source has Changed
	 * 
	 * @param name
	 */
	public synchronized void setSourceId(String name, String type) {
		setSourceName(name);
		EventSourceChanged ev = new EventSourceChanged();
		ev.setName(name);
		ev.setSourceType(type);
		fireEvent(ev);
		//simpleDevice.getProduct().setSourceByname(name);
	}
	
	

	private synchronized void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}

	/**
	 * Get the Input Sources
	 * 
	 * @return
	 */
	public ConcurrentHashMap<String, Source> getSources() {
		return sources;
	}

	/**
	 * Set the Input Sources
	 * 
	 * @param sources
	 */
	public void setSources(ConcurrentHashMap<String, Source> sources) {
		this.sources = sources;
	}

	public String getSourceName() {
		return source_name;
	}

	private void setSourceName(String source_name) {
		this.source_name = source_name;
	}

	/**
	 * Used to set the Source Name
	 * 
	 * @param source_name
	 */
	public String setSourceByname(String source_name) {
		return simpleDevice.getProduct().setSourceByname(source_name);
	}

	public void setDefaultSourcePin(String default_pin) {
		this.default_pin = default_pin;
	}

	public String getDefaultSourcePin() {
		return default_pin;
	}

	public void setStandbyPin(String standbyPin) {
		this.standbyPin = standbyPin;

	}

	public String getStandbyPin() {
		return standbyPin;
	}

	/**
	 * SetSleepTimer
	 * 
	 * @param value
	 * @return
	 */
	public String setSleepTimer(String value) {
		try {
			// AlarmClockInterface alarm = OSManager.getInstance().getPlugin();
			// if(alarm !=null)
			// {
			// return alarm.createSleepTimer(value);
			// }
			return Alarm.getInstance().createSleepTimer(value);
		} catch (Exception e) {
			log.error("Error Setting SleepTimer: ", e);
			return "Error: " + e.getMessage();
		}
		// return "";
	}

	/**
	 * CancelSleepTimer
	 * 
	 * @return
	 */
	public String cancelSleepTimer() {
		try {
			// AlarmClockInterface alarm = OSManager.getInstance().getPlugin();
			// if(alarm !=null)
			// {
			// return alarm.cancelSleepTimer();
			// }
			return Alarm.getInstance().cancelSleepTimer();
		} catch (Exception e) {
			log.error("Error Cancelling SleepTimer: ", e);
			return "Error: " + e.getMessage();
		}
		// return "";
	}

	/**
	 * GetSleepTimer
	 * 
	 * @return
	 */
	public String getSleepTimer() {
		try {
			// AlarmClockInterface alarm = OSManager.getInstance().getPlugin();
			// if(alarm !=null)
			// {
			// return alarm.getSleepTimer();
			// }
			return Alarm.getInstance().getSleepTimer();
		} catch (Exception e) {
			log.error("Error Getting SleepTimer: ", e);
			return "Error: " + e.getMessage();
		}
		// return "";
	}

	@Override
	public void update(Observable o, Object event) {

		EventBase base = (EventBase) event;
		switch (base.getType()) {
		case EVENTSOURCECHANGED:
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			String source_type = es.getSourceType();
			setSourceName(name);
			log.debug("Source Changed: " + name);
			break;

		}
	}

	/***
	 * 
	 */
	public void dispose() {
		EventDisposing ev = new EventDisposing();
		fireEvent(ev);		
	}
}
