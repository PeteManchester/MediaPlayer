package org.rpi.channel;

import org.apache.log4j.Logger;

public class ChannelRadio extends ChannelBase {

	private static Logger log = Logger.getLogger(ChannelRadio.class);

	public ChannelRadio(String uri, String metadata, int id,String name) {
		super(uri, metadata, id);
		setName(name);
		setFull_text(GetFullString());
	}

	private String entryStart = "<Entry>";
	private String entryEnd = "</Entry>";
	private String idStart = "<Id>";
	private String idEnd = "</Id>";
	private String uriStart = "<Uri>";
	private String uriEnd = "</Uri>";
	private String metaStart = "<Metadata>";
	private String metaEnd = "</Metadata>";
	private String title = "";
	private String full_text = "";
	private String name = "";

	public String getUniqueId()
	{
		return "Radio:" + super.getId();
	}

	private String GetFullString() {
		StringBuilder sb = new StringBuilder();
		sb.append(entryStart);
		sb.append(idStart);
		sb.append(getId());
		sb.append(idEnd);
		sb.append(uriStart);
		sb.append(protectSpecialCharacters(getUri()));
		sb.append(uriEnd);
		sb.append(metaStart);
		sb.append(protectSpecialCharacters(getMetadata()));
		sb.append(metaEnd);
		sb.append(entryEnd);
		return sb.toString();
	}

	private String protectSpecialCharacters(String originalUnprotectedString) {
		 if (originalUnprotectedString == null) {
		 return null;
		 }
		 boolean anyCharactersProtected = false;
		
		 StringBuffer stringBuffer = new StringBuffer();
		 for (int i = 0; i < originalUnprotectedString.length(); i++) {
		 char ch = originalUnprotectedString.charAt(i);
		
		 boolean controlCharacter = ch < 32;
		 boolean unicodeButNotAscii = ch > 126;
		 boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' ||
		 ch == '>';
		
		 if (characterWithSpecialMeaningInXML || unicodeButNotAscii ||
		 controlCharacter) {
		 stringBuffer.append("&#" + (int) ch + ";");
		 anyCharactersProtected = true;
		 } else {
		 stringBuffer.append(ch);
		 }
		 }
		 if (anyCharactersProtected == false) {
		 return originalUnprotectedString;
		 }
		 return stringBuffer.toString();
	}

	/**
	 * @return the full_text
	 */
	public String getFull_text() {
		return full_text;
	}

	/**
	 * @param full_text
	 *            the full_text to set
	 */
	private void setFull_text(String full_text) {
		this.full_text = full_text;
	}

	private String temp_meta = "";

	public String updateTrack(String title, String artist) {
		temp_meta = super.updateTrack(title, artist);
		return temp_meta;
	}

	public String getMetadata() {
		return super.getMetadata();
	}


	/**
	 * @return the temp_meta
	 */
	public String getTempMeta() {
		return temp_meta;
	}

	/**
	 * @param temp_meta
	 *            the temp_meta to set
	 */
	public void setTempMeta(String temp_meta) {
		this.temp_meta = temp_meta;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	private void setName(String name) {
		this.name = name;
	}

}
