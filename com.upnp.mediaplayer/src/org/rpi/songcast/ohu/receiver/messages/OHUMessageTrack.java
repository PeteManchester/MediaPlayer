package org.rpi.songcast.ohu.receiver.messages;

import io.netty.buffer.ByteBuf;
import org.apache.log4j.Logger;
import org.rpi.songcast.common.SongcastMessage;

//Offset    Bytes                   Desc
//0         4                       Sequence
//4         4                       Uri Bytes (n)
//8         4                       Metadata Bytes (m)
//12        n                       n bytes of uri
//12 + n    m                       m bytes of didl lite metadata

public class OHUMessageTrack extends SongcastMessage {

	private Logger log = Logger.getLogger(this.getClass());

	private int sequence = -99;
	private String uri = "";
	private String metaData = "";

	public OHUMessageTrack(ByteBuf buf) {
		super.setData(buf.retain());
		try {
			sequence = buf.getInt(8);
			int url_length = buf.getInt(12);
			int meta_length = buf.getInt(16);
			byte[] bURI = new byte[url_length];
			buf.getBytes(20, bURI, 0, url_length);
			setUri(new String(bURI));
			byte[] bmeta_data = new byte[meta_length];
			buf.getBytes(20 + url_length, bmeta_data, 0, meta_length);
			setMetaData(new String(bmeta_data));
			log.debug(this.toString());
		} catch (Exception e) {
			log.error("Error OHUMessageTrack", e);
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
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("OHUMessageTrack");
		sb.append("\r\n");
		sb.append("URI: " + uri);
		sb.append("\r\n");
		sb.append("MetaData: " + metaData);
		return sb.toString();
	}

}
