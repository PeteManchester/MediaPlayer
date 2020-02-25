package org.rpi.controlpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/***
 * Class to hold the active devices (Kazoo MediaServers) Used by the Pins
 * Provider to get the host and port of the Kazoo Server
 * 
 * @author phoyle
 *
 */

public class DeviceManager {

	private static DeviceManager instance = null;
	private Map<String, DeviceInfo> devices = new ConcurrentHashMap<String, DeviceInfo>();

	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * SingleInstance of the PlayManager
	 * 
	 * @return
	 */
	public static DeviceManager getInstance() {
		if (instance == null) {
			instance = new DeviceManager();
		}
		return instance;
	}

	public void addDevice(String udn, DeviceInfo info) {
		log.debug("Device## Added Device: " + udn);
		devices.put(udn, info);
	}

	public void deleteDevice(String udn) {
		try
		{
			log.debug("Device## Delete Device: " + udn);
			devices.remove(udn);
		}
		catch(Exception e)
		{
			log.error("Device## Error Removing Device", e);
		}
		
	}

	public DeviceInfo getDevice(String udn) {
		if (udn == null) {
			return null;
		}
		if (devices.containsKey(udn)) {
			log.debug("Device## Get Device: " + udn);
			return devices.get(udn);
		}
		return null;
	}

	/**
	 * @return the devices
	 */
	public Map<String, DeviceInfo> getDevices() {
		return devices;
	}

	/**
	 * @param devices
	 *            the devices to set
	 */
	public void setDevices(Map<String, DeviceInfo> devices) {
		this.devices = devices;
	}

}
