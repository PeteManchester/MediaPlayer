package org.rpi.plugingateway;

import java.util.Observable;

import org.rpi.config.Config;
import org.rpi.main.SimpleDevice;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.player.events.EventVolumeChanged;

public class PluginGateWay extends Observable {

	private static PluginGateWay instance = null;

	private SimpleDevice simpleDevice = null;

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
		simpleDevice.getProduct().addSource(Config.friendly_name, type, name, true);
	}

	/**
	 * The Product Source has Changed
	 * 
	 * @param name
	 */
	public synchronized void setSourceId(String name) {
		EventSourceChanged ev = new EventSourceChanged();
		ev.setName(name);
		fireEvent(ev);
	}
	
	private synchronized void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}
	
	
}


