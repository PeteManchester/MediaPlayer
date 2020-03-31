package org.rpi.airplay.audio;

import io.netty.buffer.ByteBuf;

@Deprecated
public class AirPlayAudioHolder {
	
	private int frameId = 0;
	private ByteBuf buf = null;
	
	public AirPlayAudioHolder(int sequence, ByteBuf audio) {
		this.frameId = sequence;
		this.buf = audio;
	}

	/**
	 * @return the frameId
	 */
	public int getFrameId() {
		return frameId;
	}

	/**
	 * @param frameId the frameId to set
	 */
	public void setFrameId(int frameId) {
		this.frameId = frameId;
	}

	/**
	 * @return the buf
	 */
	public ByteBuf getBuf() {
		return buf;
	}

	/**
	 * @param buf the buf to set
	 */
	public void setBuf(ByteBuf buf) {
		this.buf = buf;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AirPlayAudioHolder [frameId=");
		builder.append(frameId);
		builder.append("]");
		return builder.toString();
	}
	

}
