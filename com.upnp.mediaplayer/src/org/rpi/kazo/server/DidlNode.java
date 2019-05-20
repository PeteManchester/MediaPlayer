package org.rpi.kazo.server;

public class DidlNode {
	
	/**
	 * @param nsName
	 * @param nodeName
	 * @param elementName
	 */
	public DidlNode(String elementName , String nodeName, String nsName) {
		this.nsName = nsName;
		this.nodeName = nodeName;
		this.elementName = elementName;
		
	}
	private String nsName = "";
	private String nodeName = "";
	private String elementName = "";
	private String value = "";
	/**
	 * @return the NameSpaceName
	 */
	public String getNSName() {
		return nsName;
	}
	/**
	 * @param nsName the nsName to set
	 */
	public void setNSName(String nsName) {
		this.nsName = nsName;
	}
	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}
	/**
	 * @param nodeName the nodeName to set
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	/**
	 * @return the elementName
	 */
	public String getElementName() {
		return elementName;
	}
	/**
	 * @param elementName the elementName to set
	 */
	public void setElementName(String elementName) {
		this.elementName = elementName;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return protectSpecialCharacters(value);
		//return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
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
	
	public String  getXMLNode() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(getNodeName());
		sb.append(" xmlns:");
		sb.append(getNSName());
		sb.append(">");
		sb.append(getValue());
		sb.append("</");
		sb.append(getNodeName());
		sb.append(">");
		sb.append(System.lineSeparator());
		return sb.toString();
	}
	

}


