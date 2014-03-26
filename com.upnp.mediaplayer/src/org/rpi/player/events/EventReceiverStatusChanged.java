package org.rpi.player.events;

public class EventReceiverStatusChanged implements EventBase {

	@Override
	public EnumPlayerEvents getType() {
		return EnumPlayerEvents.EVENTRECEIVERSTATUSCHANGED;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private String status = "";

}
