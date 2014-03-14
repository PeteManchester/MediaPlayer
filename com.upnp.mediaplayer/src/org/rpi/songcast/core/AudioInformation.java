package org.rpi.songcast.core;

public class AudioInformation {
	
	public AudioInformation(long sampleRate, long bitRate, long bitDepth, int channels, String codec, int length, int sampleCount )
	{
		this.sampleRate = sampleRate;
		this.bitRate = (int)bitRate;
		this.bitDepth = (int)bitDepth;
		this.channels = channels;
		this.codec = codec;
		this.soundByteSize = length;
		this.sampleCount = sampleCount;
	}
	
	private float sampleRate = 0;
	private int bitRate = 0;
	private int bitDepth = 0;
	private String codec = "";
	private int channels = 0;
	private int soundByteSize = 0;
	private int sampleCount = 0;
	/**
	 * @return the sampleRate
	 */
	public float getSampleRate() {
		return sampleRate;
	}
	/**
	 * @param sampleRate the sampleRate to set
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
	 * @param butRate the butRate to set
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
	 * @param bitDepth the bitDepth to set
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
	 * @param codec the codec to set
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
	 * @param channels the channels to set
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}
	public boolean isSigned() {
		return true;
	}
	public boolean isBigEndian() {
		return true;
	}
	
	public boolean compare(AudioInformation ai)
	{
		if(ai==null)
			return false;
		if(this.sampleRate!= ai.getSampleRate() ||this.bitRate != ai.getBitRate() || this.bitDepth != ai.getBitDepth())
		{
		return false;
		}
		return true;
		
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("SampleRate:" + sampleRate);
		sb.append(" ");
		sb.append("BitRate:" + bitRate);
		sb.append(" ");
		sb.append("BitDepth:" + bitDepth);
		sb.append(" ");
		sb.append("Channels:" + channels);
		sb.append(" ");
		sb.append("Codec:" + codec);
		sb.append(" ");
		sb.append("Signed:" + isSigned());
		sb.append(" ");
		sb.append("SoundByteSize:" + soundByteSize);
		sb.append(" ");
		sb.append("SamplesCount:" + sampleCount);
		
		return sb.toString();
	}
	
	

}
