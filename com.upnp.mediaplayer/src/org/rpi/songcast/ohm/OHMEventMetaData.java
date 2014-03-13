package org.rpi.songcast.ohm;

import java.math.BigInteger;
import org.apache.log4j.Logger;
import org.rpi.songcast.core.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       Sequence (n)
//4         4                       Metatext Bytes (n)
//8         n                       n bytes of metatext

public class OHMEventMetaData extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	private int sequence = -99;
	private String metaText = "";

	public void checkMessageType() {
//		StringBuilder sb = new StringBuilder();
//		for (byte b : data) {
//			sb.append(String.format("%02X ", b));
//		}
//		String s = sb.toString().trim();
//		log.debug(s);

		sequence = new BigInteger(getBytes(8, 11)).intValue();
		int meta_length = new BigInteger(getBytes(12, 15)).intValue();
		if (meta_length > 0) {
			byte[] bMetaLength = getBytes(16, meta_length - 1);
			metaText = byteToString(bMetaLength);
			log.debug("MetaData: " + metaText);
		}
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
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	/**
	 * @return the metaText
	 */
	public String getMetaText() {
		return metaText;
	}

	/**
	 * @param metaText
	 *            the metaText to set
	 */
	public void setMetaText(String metaText) {
		this.metaText = metaText;
	}

}
