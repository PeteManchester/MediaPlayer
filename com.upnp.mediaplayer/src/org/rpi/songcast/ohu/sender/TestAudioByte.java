package org.rpi.songcast.ohu.sender;

public class TestAudioByte {
	
	private byte[] audio;
	private int frameId = 0;
	public TestAudioByte(byte[] b, int frameId) {
		audio = b;
		this.frameId = frameId;
	}
	/**
	 * @return the audio
	 */
	public byte[] getAudio() {
		return audio;
	}
	/**
	 * @param audio the audio to set
	 */
	public void setAudio(byte[] audio) {
		this.audio = audio;
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

}
