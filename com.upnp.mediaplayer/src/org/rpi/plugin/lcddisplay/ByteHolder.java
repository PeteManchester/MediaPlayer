package org.rpi.plugin.lcddisplay;

import java.nio.ByteBuffer;

public class ByteHolder {
	
	private int width = 0;
	private int height= 0;
	private int positionX = 0;
	private int positionY = 0;
	private int BytesPerFrame = 0;
	private ByteBuffer buffer = null;
	public ByteHolder(ByteBuffer buff, int rwt, int rht, int x, int y, int bytesPerFrame) {
		this.buffer = buff;
		this.width= rwt;
		this.height = rht;
		this.positionX = x;
		this.positionY = y;
		this.BytesPerFrame = rht/8 * 128;
	}
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}
	/**
	 * @return the positionX
	 */
	public int getPositionX() {
		return positionX;
	}
	/**
	 * @param positionX the positionX to set
	 */
	public void setPositionX(int positionX) {
		this.positionX = positionX;
	}
	/**
	 * @return the positionY
	 */
	public int getPositionY() {
		return positionY;
	}
	/**
	 * @param positionY the positionY to set
	 */
	public void setPositionY(int positionY) {
		this.positionY = positionY;
	}
	/**
	 * @return the bytesPerFrame
	 */
	public int getBytesPerFrame() {
		return BytesPerFrame;
	}
	/**
	 * @param bytesPerFrame the bytesPerFrame to set
	 */
	public void setBytesPerFrame(int bytesPerFrame) {
		BytesPerFrame = bytesPerFrame;
	}
	/**
	 * @return the buffer
	 */
	public ByteBuffer getBuffer() {
		return buffer;
	}
	/**
	 * @param buffer the buffer to set
	 */
	public void setBuffer(ByteBuffer buffer) {
		this.buffer = buffer;
	}

}
