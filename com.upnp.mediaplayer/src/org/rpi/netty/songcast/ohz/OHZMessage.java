package org.rpi.netty.songcast.ohz;

public class OHZMessage {
	private String zone = "";
	private String uri = "";
	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}
	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("OHZMessage");
		sb.append("\r\n");
		sb.append("URI: " + uri);
		sb.append("\r\n");
		sb.append("Zone: " + zone);
		return sb.toString();
	}
}
