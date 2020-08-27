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
			// if (!bSentUpdate) {
			// EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
			// ev.setTrackInfo(this);
			// fireEvent(ev);
			// bSentUpdate = true;
			// }
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
		try {
			setChanged();
			notifyObservers(ev);
		} catch (Exception e) {
			log.error("Error notifyObservers", e);
		}
	}

	@Override
	public boolean equals(Object old) {
		if (!(old instanceof TrackInfo)) {
			return false;
		}
		TrackInfo previous = (TrackInfo) old;
		if (previous.getBitrate() != this.bitRate) {
			return false;
		}
		if (!previous.getCodec().equals(this.codec)) {
			return false;
		}
		if (previous.getBitDepth() != this.depth) {
			return false;
		}
		if (previous.getDuration() != this.duration) {
			return false;
		}
		if (previous.getSampleRate() != this.sampleRate) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TrackInfo [bSentUpdate=");
		builder.append(bSentUpdate);
		builder.append(", codec=");
		builder.append(codec);
		builder.append(", bitRate=");
		builder.append(bitRate);
		builder.append(", sampleRate=");
		builder.append(sampleRate);
		builder.append(", duration=");
		builder.append(duration);
		builder.append(", depth=");
		builder.append(depth);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * 
	 * @return cloned attribute values
	 */
	public TrackInfo cloneToValueObject() {
		TrackInfo cloned = new TrackInfo();
		cloned.bitRate = bitRate;
		cloned.codec = codec;
		cloned.depth = depth;
		cloned.duration = duration;
		cloned.sampleRate = sampleRate;
		return cloned;
	}

	/***
	 * Send an event to notify Observers
	 */
	public void sendEvent() {
		log.debug("Send EventUpdateTrackInfo: " + this.toString());
		EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
		ev.setTrackInfo(this);
		fireEvent(ev);
	}
}