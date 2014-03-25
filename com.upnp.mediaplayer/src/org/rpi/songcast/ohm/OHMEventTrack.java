package org.rpi.songcast.ohm;

import java.math.BigInteger;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.songcast.core.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       Sequence
//4         4                       Uri Bytes (n)
//8         4                       Metadata Bytes (m)
//12        n                       n bytes of uri
//12 + n    m                       m bytes of didl lite metadata

public class OHMEventTrack extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	private int sequence = -99;
	private String uri = "";
	private String metaData = "";

	public void checkMessageType() {

		// StringBuilder sb = new StringBuilder();
		// for (byte b : data) {
		// sb.append(String.format("%02X ", b));
		// }
		// String s = sb.toString().trim();
		//
		// log.debug(s);

		sequence = new BigInteger(getBytes(8, 11)).intValue();
		int uri_length = new BigInteger(getBytes(12, 15)).intValue();
		int meta_length = new BigInteger(getBytes(16, 19)).intValue();
		if (uri_length > 0) {
			byte[] bURI = getBytes(20, uri_length - 1);
			uri = byteToString(bURI);
			log.debug("URI: " + uri);
		}
		if (meta_length > 0) {
			byte[] bMetaLength = getBytes(20 + uri_length, data.length - 1);
			metaData = byteToString(bMetaLength);
			log.debug("MetaData: " + metaData);
			EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
			ev.setMetaText(metaData);
			if (ev != null) {
				PlayManager.getInstance().updateTrackInfo(ev);
			}
		}
	}

	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param uri
	 *            the uri to set
	 */
	private void setUri(String uri) {
		this.uri = uri;
	}

	/**
	 * @return the metaData
	 */
	public String getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData
	 *            the metaData to set
	 */
	private void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	/**
	 * @return the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @param sequence
	 *            the sequence to set
	 */
	private void setSequence(int sequence) {
		this.sequence = sequence;
	}

}
