package org.rpi.plugingateway;

import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.main.SimpleDevice;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.sources.Source;

public class PluginGateWay extends Observable {

	private static PluginGateWay instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String,Source>();
	private String source_name = "";

	private SimpleDevice simpleDevice = null;
	private String default_pin = "";

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
	}

	/**
	 * Add a Source to the Product Provider
	 * 
	 * @param name
	 * @param type
	 * @param visible
	 */
	public synchronized void AddSource(String type, String name, boolean visible) {
		simpleDevice.getProduct().addSource(Config.friendly_name, name, type, true);
	}

	/**
	 * The Product Source has Changed
	 * 
	 * @param name
	 */
	public synchronized void setSourceId(String name) {
		setSourceName(name);
		EventSourceChanged ev = new EventSourceChanged();
		ev.setName(name);
		fireEvent(ev);
	}
	
	private synchronized void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}

	/**
	 * Get the Input Sources
	 * @return
	 */
	public ConcurrentHashMap<String, Source> getSources() {
		return sources;
	}

	/**
	 * Set the Input Sources
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

	public void setDefaultSourcePin(String default_pin) {
		this.default_pin = default_pin;
	}

	public String getDefaultSourcePin() {
		return default_pin;
	}
}


