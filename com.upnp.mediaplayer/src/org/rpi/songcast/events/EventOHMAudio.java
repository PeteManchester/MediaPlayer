package org.rpi.songcast.events;

public class EventOHMAudio implements EventSongCastBase {

	@Override
	public EnumSongCastEvents getType() {
		return EnumSongCastEvents.EVENT_OHM_AUDIO;
	}
	
	/**
	 * @return the data
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(byte[] data) {
		this.data = data;
	}

	private byte[] data ;

}
