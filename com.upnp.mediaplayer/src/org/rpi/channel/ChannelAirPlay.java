package org.rpi.channel;

public class ChannelAirPlay extends ChannelBase {

	public ChannelAirPlay(String uri, String metadata, int id,String name) {
		super(uri, metadata, id);
		setName(name);
	}
	
	private String name = "";
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	private void setName(String name) {
		this.name = name;
	}

}
