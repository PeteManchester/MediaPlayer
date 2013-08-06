package org.rpi.playlist;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CustomTrack {

	private Logger log = Logger.getLogger(CustomTrack.class);
	private String Uri = "";
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

	public CustomTrack(String uri, String metadata, int id) {
		setUri(uri);
		setMetadata(metadata);
		setId(id);
		full_text = GetFullString();
	}

	public String getUniqueId() {
		return "PL:" + Id;
	}

	private String Metadata;

	private int Id;
	private String metatext= "";
	private long time = -99;

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

	public String getFullString() {
		return full_text;
	}

	public String getUri() {
		return Uri;
	}

	private void setUri(String uri) {
		Uri = uri;
	}

	public String getMetadata() {
		return Metadata;
	}

	public String getMetaClean() {
		return protectSpecialCharacters(getMetadata());
	}

	private void setMetadata(String metadata) {
		Metadata = metadata;
	}

	public int getId() {
		return Id;
	}

	private void setId(int id) {
		Id = id;
	}

	public String tidyMetaData() {

		try {
			Vector<Node> removeNodes = new Vector<Node>();
			StringBuilder temp = new StringBuilder();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(Metadata));
			Document doc = builder.parse(insrc);
			Node node = doc.getFirstChild();
			Node item = node.getFirstChild();
			NodeList childs = item.getChildNodes();
			temp.append("<item>");
			for (int i = 0; i < childs.getLength(); i++) {
				Node n = childs.item(i);
				boolean remove = true;
				if (n.getNodeName() == "dc:title") {
					remove = false;
				}
				else if (n.getNodeName() == "upnp:album") {
					remove = false;
				}
				
				else if (n.getNodeName() == "upnp:artist") {
					NamedNodeMap map = n.getAttributes();
					Node role = map.getNamedItem("role");
					String role_type = role.getTextContent();
					if(role.getTextContent().equalsIgnoreCase("AlbumArtist"))
					{
						remove = false;
					}	
					if(role.getTextContent().equalsIgnoreCase("Performer"))
					{
						//remove =false;
					}
				}
				
				else if(n.getNodeName() == "upnp:class")
				{
					remove =false;
				}
				
				else if(n.getNodeName() == "upnp:albumArtURI")
				{
					remove = false;
				}
				
				
				if (remove)
				{
					removeNodes.add(n);
				}
			}
			for(Node n : removeNodes)
			{
				item.removeChild(n);
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Exception e) {
			log.error("Erorr TidyMetaData",e);
		}
		
		return "";
	}

	public String updateTrack(String title, String artist) {
		try {
			String full_title = title + " - " + artist;
			full_title = tidyUpString(full_title);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(Metadata));
			Document doc = builder.parse(insrc);
			Node node = doc.getFirstChild();
			Node item = node.getFirstChild();
			NodeList childs = item.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node n = childs.item(i);
				if (n.getNodeName() == "dc:title") {
					n.setTextContent(full_title);
					log.info("ICY INFO Replacing dc:title: " + full_title);
					break;
				}
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			metatext = result.getWriter().toString();
			return metatext;
		} catch (Exception e) {
			log.error("Error Creating XML Doc", e);
		}
		return null;
	}

	private String tidyUpString(String s) throws Exception {
		String string = "";
		if (s.equals(s.toUpperCase())) {
			// s = s.toLowerCase();
			String[] splits = s.split(" ");
			for (String word : splits) {
				try {
					word = word.replace(word.substring(1), word.substring(1).toLowerCase());
				} catch (Exception e) {
					log.debug("Error with Word: " + word);
				}
				string += word + " ";
			}

		}
		if (string.equals("")) {
			return s;
		}
		return string.trim();
	}

	private String protectSpecialCharacters(String originalUnprotectedString) {
		// String test = StringEscapeUtils.escapeXml(originalUnprotectedString);
		if (originalUnprotectedString == null) {
			return null;
		}
		boolean anyCharactersProtected = false;

		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < originalUnprotectedString.length(); i++) {
			char ch = originalUnprotectedString.charAt(i);

			boolean controlCharacter = ch < 32;
			boolean unicodeButNotAscii = ch > 126;
			boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

			if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
				stringBuffer.append("&#" + (int) ch + ";");
				anyCharactersProtected = true;
			} else {
				stringBuffer.append(ch);
			}
		}
		if (anyCharactersProtected == false) {
			return originalUnprotectedString;
		}
		// if(test.equals(stringBuffer.toString()))
		// {
		// log.debug("My Routine was the Same");
		// }
		// else
		// {
		// log.debug("Mine: " + test);
		// log.debug("Theirs: " + stringBuffer.toString());
		// }
		return stringBuffer.toString();
		// return test;
	}

	@Override
	public String toString() {
		String res = "Track Id: " + Id + " + URI: " + Uri + "  MetaData:\r\n " + getMetadata();
		// log.debug(res);
		return res;
		// return
		// string.Format("Track Id: {0} \r\n URI: {1} \r\n MetaData: {2}",
		// getId(), getUri(), getMetadata());
	}

	public String getArtist() {
		try {
			Vector<Node> removeNodes = new Vector<Node>();
			StringBuilder temp = new StringBuilder();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(Metadata));
			Document doc = builder.parse(insrc);
			Node node = doc.getFirstChild();
			Node item = node.getFirstChild();
			NodeList childs = item.getChildNodes();
			temp.append("<item>");
			for (int i = 0; i < childs.getLength(); i++) {
				Node n = childs.item(i);
				boolean remove = true;
				if (n.getNodeName() == "dc:title") {
					remove = true;
				}
				else if (n.getNodeName() == "upnp:album") {
					remove = false;
				}
				
				else if (n.getNodeName() == "upnp:artist") {
					NamedNodeMap map = n.getAttributes();
					Node role = map.getNamedItem("role");
					String role_type = role.getTextContent();
					if(role.getTextContent().equalsIgnoreCase("AlbumArtist"))
					{
						remove = true;
					}	
					if(role.getTextContent().equalsIgnoreCase("Performer"))
					{
						remove = false;
					}
				}
				
				else if(n.getNodeName() == "upnp:class")
				{
					remove =false;
				}
				
				else if(n.getNodeName() == "upnp:albumArtURI")
				{
					remove = false;
				}
				
				
				if (remove)
				{
					removeNodes.add(n);
				}
			}
			for(Node n : removeNodes)
			{
				item.removeChild(n);
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (Exception e) {
			log.error("Erorr TidyMetaData",e);
		}
		
		return Metadata;
	}

	public void setMetaText(String metatext) {
		this.metatext = metatext;
		
	}
	
	public String getMetaText()
	{
		return metatext;
	}

	public void setTime(long duration) {
		// TODO Auto-generated method stub
		this.time = duration;
	}
	
	public long getTime()
	{
		return time;
	}

	// public void updateTitle(String title) {
	// // title = WordUtils.capitalizeFully(title);
	// String meta_data = Metadata;
	// String sStart =
	// "<dc:title xmlns:dc=\"http://purl.org/dc/elements/1.1/\">";
	// String sEnd = "</dc:title>";
	// int start = meta_data.indexOf(sStart);
	// int end = meta_data.indexOf(sEnd);
	// if (start > 0) {
	// if (end > 0) {
	// String s = meta_data.substring(start, end);
	// log.debug("Found Title: " + s);
	// log.debug("Replace With: " + sStart + title);
	//
	// meta_data = meta_data.replace(s, sStart + title);
	// log.debug(meta_data);
	// Metadata = meta_data;
	// }
	// }
	// }

	// public void updateArtist(String artist) {
	// // Probably need to replace "<item id="...
	// // artist = WordUtils.capitalizeFully(artist);
	// String meta_data = Metadata;
	// String sStart =
	// "<upnp:artist role=\"Performer\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">";
	// String sEnd = "</upnp:artist>";
	// int start = meta_data.indexOf(sStart);
	// int end = meta_data.indexOf(sEnd);
	// if (start > 0) {
	// if (end > 0) {
	// String s = meta_data.substring(start, end);
	// log.debug("Found Artist: " + s);
	// log.debug("Replace With: " + sStart + title);
	//
	// meta_data = meta_data.replace(s, sStart + title);
	// log.debug(meta_data);
	// Metadata = meta_data;
	// }
	// } else {
	// String first_part = meta_data.substring(0,
	// meta_data.indexOf("</item></DIDL-Lite>"));
	// meta_data = first_part + sStart + artist + sEnd + "</item></DIDL-Lite>";
	// Metadata = meta_data;
	// }
	// }
}
