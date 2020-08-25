package org.rpi.plugin.oled;
public class TextHeight {
	
	private int ascent = 0;
	private int descent = 0;
	public TextHeight(int ascent, int descent) {
		setAscent(ascent);
		setDescent(descent);
	}
	/**
	 * @return the ascent
	 */
	public int getAscent() {
		return ascent;
	}
	/**
	 * @param ascent the ascent to set
	 */
	public void setAscent(int ascent) {
		this.ascent = ascent;
	}
	/**
	 * @return the descent
	 */
	public int getDescent() {
		return descent;
	}
	/**
	 * @param descent the descent to set
	 */
	public void setDescent(int descent) {
		this.descent = descent;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TextHeight [ascent=");
		builder.append(ascent);
		builder.append(", descent=");
		builder.append(descent);
		builder.append("]");
		return builder.toString();
	}

}
