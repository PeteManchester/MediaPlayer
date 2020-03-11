package org.rpi.java.sound;

public class AudioInformation {

	public AudioInformation() {

	}

	/**
	 * Create the AudioInformation class
	 * 
	 * @param sampleRate
	 * @param bitRate
	 * @param bitDepth
	 * @param channels
	 * @param codec
	 * @param length
	 * @param sampleCount
	 */
	public AudioInformation(long sampleRate, long bitRate, long bitDepth, int channels, String codec, int length, int sampleCount) {
		this.sampleRate = sampleRate;
		this.bitRate = (int) bitRate;
		this.bitDepth = (int) bitDepth;
		this.channels = channels;
		this.codec = codec;
		this.soundByteSize = length;
		this.sampleCount = sampleCount;
		compareString = sampleRate + bitRate + bitDepth;
	}

	private float sampleRate = -99;
	private int bitRate = -99;
	private int bitDepth = -99;
	private String codec = "Not Defined";
	private int channels = -99;
	private int soundByteSize = -99;
	private int sampleCount = -99;
	private long compareString = 0;
	private boolean isBigEndian = true;
	private boolean isSigned = true;

	/**
	 * @return the sampleRate
	 */
	public float getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sampleRate
	 *            the sampleRate to set
	 */
	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	/**
	 * @return the butRate
	 */
	public int getBitRate() {
		return bitRate;
	}

	/**
	 * @param butRate
	 *            the butRate to set
	 */
	public void setBitRate(int butRate) {
		this.bitRate = butRate;
	}

	/**
	 * @return the bitDepth
	 */
	public int getBitDepth() {
		return bitDepth;
	}

	/**
	 * @param bitDepth
	 *            the bitDepth to set
	 */
	public void setBitDepth(int bitDepth) {
		this.bitDepth = bitDepth;
	}

	/**
	 * @return the codec
	 */
	public String getCodec() {
		return codec;
	}

	/**
	 * @param codec
	 *            the codec to set
	 */
	public void setCodec(String codec) {
		this.codec = codec;
	}

	/**
	 * @return the channels
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @param channels
	 *            the channels to set
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}

	public boolean isSigned() {
		return this.isSigned;
	}

	public boolean isBigEndian() {
		return this.isBigEndian;
	}

	public long CompareString() {
		return compareString;
	}

	/**
	 * Compare the SampleRate, BitDepth, Channels and BitRate
	 * 
	 * @param ai
	 * @return
	 */
	// public boolean compare(AudioInformation ai) {
	// if (ai == null)
	// return false;
	// if (this.sampleRate != ai.getSampleRate() || this.bitDepth != ai.bitDepth
	// || this.channels != ai.getChannels() || this.bitRate != ai.getBitRate())
	// {
	// return false;
	// }
	// return true;
	// }

	public boolean compare(AudioInformation ai) {
		// if (ai == null)
		// return false;
		if (compareString == ai.CompareString()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AudioInformation [sampleRate=");
		builder.append(sampleRate);
		builder.append(", bitRate=");
		builder.append(bitRate);
		builder.append(", bitDepth=");
		builder.append(bitDepth);
		builder.append(", codec=");
		builder.append(codec);
		builder.append(", channels=");
		builder.append(channels);
		builder.append(", soundByteSize=");
		builder.append(soundByteSize);
		builder.append(", sampleCount=");
		builder.append(sampleCount);
		builder.append(", isBigEndian=");
		builder.append(isBigEndian());
		builder.append(", isSigned=");
		builder.append(isSigned());
		builder.append("]");
		return builder.toString();
	}

	

}
