package org.rpi.mplayer;

import java.util.Observable;

import org.apache.log4j.Logger;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventUpdateTrackInfo;

public class TrackInfo extends Observable {
	
	private Logger log = Logger.getLogger(this.getClass());

	private boolean bSentUpdate = false;

	public TrackInfo() {

	}

	private String codec = null;
	private long bitRate = -99;
	private long sampleRate = -99;
	private long duration = -99;
	private long depth = -99;

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
	 * @return the bitrate
	 */
	public long getBitrate() {
		return bitRate;
	}

	/**
	 * @param bitrate
	 *            the bitrate to set
	 */
	public void setBitrate(long bitrate) {
		this.bitRate = bitrate;
	}

	/**
	 * @return the sample_rate
	 */
	public long getSampleRate() {
		return sampleRate;
	}

	/**
	 * @param sample_rate
	 *            the sample_rate to set
	 */
	public void setSampleRate(long sample_rate) {
		this.sampleRate = sample_rate;
	}

	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	public boolean isSet() {
		if (bitRate != -99 && sampleRate != -99 && codec != null && duration != -99) {
			if (!bSentUpdate) {
				EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
				ev.setTrackInfo(this);
				fireEvent(ev);
				bSentUpdate = true;
			}
			return true;
		}
		return false;
	}

	public void setBitDepth(long depth) {
		this.depth = depth;
	}

	public long getBitDepth() {
		return depth;
	}

	public boolean isUpdated() {
		return bSentUpdate;
	}

	public void setUpdated(boolean bUpdated) {
		this.bSentUpdate = bUpdated;
		if (!bUpdated) {
			codec = null;
			bitRate = -99;
			sampleRate = -99;
			duration = -99;
			depth = -99;
		}
	}

	private void fireEvent(EventBase ev) {
		try
		{
		setChanged();
		notifyObservers(ev);
		}
		catch(Exception e)
		{
			log.error("Error notifyObservers",e);
		}
	}

}
