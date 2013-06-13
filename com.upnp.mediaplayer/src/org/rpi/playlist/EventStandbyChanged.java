package org.rpi.playlist;

import org.rpi.player.events.EnumPlayerEvents;
import org.rpi.player.events.EventBase;

public class EventStandbyChanged implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTSTANDBYCHANGED;
	}
	
	public boolean isStandby() {
		return standby;
	}

	public void setStandby(boolean standby) {
		this.standby = standby;
	}

	private boolean standby = true;

}
