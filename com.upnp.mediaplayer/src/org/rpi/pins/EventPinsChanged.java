package org.rpi.pins;

import org.rpi.player.events.EnumPlayerEvents;
import org.rpi.player.events.EventBase;

public class EventPinsChanged implements EventBase {
	
	private String pinInfo = "";

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTPINSCHANGED;
	}

	/**
	 * @return the pinInfo
	 */
	public String getPinInfo() {
		return pinInfo;
	}

	/**
	 * @param pinInfo the pinInfo to set
	 */
	public void setPinInfo(String pinInfo) {
		this.pinInfo = pinInfo;
	}
}
