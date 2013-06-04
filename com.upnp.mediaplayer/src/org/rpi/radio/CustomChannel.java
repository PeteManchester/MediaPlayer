package org.rpi.radio;

import org.apache.log4j.Logger;
import org.rpi.playlist.CustomTrack;

public class CustomChannel extends CustomTrack {

	private static Logger log = Logger.getLogger(CustomChannel.class);

	public CustomChannel(String uri, String metadata, int id,String name) {
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
		//String test = StringEscapeUtils.escapeXml(originalUnprotectedString);
		//return test;
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
//		 if(test.equals(stringBuffer.toString()))
//		 {
//		 log.debug("My Routine was the Same");
//		 }
//		 else
//		 {
//		 log.debug("Mine: " + test);
//		 log.debug("Theirs: " + stringBuffer.toString());
//		 }
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
//		if (temp_meta != null) {
//			if (!temp_meta.equalsIgnoreCase("")) {
//				return temp_meta;
//			}
//		}
		return super.getMetadata();
	}

	// public String getMetadata() {
	// return getTempMeta();
	// }

	// public String updateTrack(String title, String artist) {
	// try {
	// title = WordUtils.capitalizeFully(title);
	// artist = WordUtils.capitalizeFully(artist);
	// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	// DocumentBuilder builder = factory.newDocumentBuilder();
	// InputSource insrc = new InputSource(new
	// StringReader(super.getMetadata()));
	// Document doc = builder.parse(insrc);
	// Node node = doc.getFirstChild();
	// Node item = node.getFirstChild();
	// log.debug("Item Child Nodes " + item.getChildNodes().getLength());
	// NodeList childs = item.getChildNodes();
	// for (int i = 0; i < childs.getLength(); i++) {
	// Node n = childs.item(i);
	// log.debug("Name: " + n.getNodeName() + " Value " + n.getTextContent());
	// if (n.getNodeName() == "dc:title") {
	// n.setTextContent(title + " - " + artist);
	// }
	// }
	// Transformer transformer =
	// TransformerFactory.newInstance().newTransformer();
	// StreamResult result = new StreamResult(new StringWriter());
	// DOMSource source = new DOMSource(doc);
	// transformer.transform(source, result);
	// temp_meta = result.getWriter().toString();
	// return temp_meta;
	// } catch (Exception e) {
	// log.error("Error Creating XML Doc", e);
	// }
	// return null;
	// }

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
