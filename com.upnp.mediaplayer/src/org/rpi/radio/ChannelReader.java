package org.rpi.radio;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelRadio;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChannelReader {
	
	private static Logger log = Logger.getLogger(ChannelReader.class);
	
	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();
	
	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";
	
	/***
	 * Read the RadioList.xml file and build a List of Radio Channels
	 * @return
	 */
	public List<ChannelRadio> getChannels() {
		
		try {
			channels.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File("RadioList.xml"));
			NodeList listOfChannels = doc.getElementsByTagName("Channel");
			int i = 1;
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				String id = null;
				String url = null;
				String image = "";
				String s_icy_reverse ="";
				boolean icy_reverse = false;

				Node channel = listOfChannels.item(s);
				if (channel.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) channel;
					id = getElement(element, "id");
					url = getElement(element, "url");
					image = getElement(element, "image");
					s_icy_reverse = getElement(element,"icy_reverse");
					if(s_icy_reverse.equalsIgnoreCase("TRUE"))
					{
						icy_reverse = true;
					}

				}

				if (id != null && url != null) {
					addChannel(id, url, image, i,icy_reverse);
					i++;
				}
			}
		} catch (Exception e) {
			log.error("Error Getting Radio List", e);
		}
		return channels;
	}

	/***
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	private String getElement(Element element, String name) {
		String res = "";
		NodeList nid = element.getElementsByTagName(name);
		if (nid != null) {
			Element fid = (Element) nid.item(0);
			if (fid != null) {
				res = fid.getTextContent();
				//log.debug("ElementName: " + name + " Value: " + res);
				return res;

			}
		}
		return res;
	}

	/***
	 * Create a Channel and add it to the List
	 * @param name
	 * @param url
	 * @param image
	 * @param id
	 */
	private void addChannel(String name, String url, String image, int id,boolean icy_reverse) {
		String m = createMetaData(name, url, image);
		ChannelRadio channel = new ChannelRadio(url, m, id,name);
		channel.setICYReverse(icy_reverse);
		channels.add(channel);
		log.debug("Added Channel: " + channel.getUri() + " " + channel.getFullDetails());
	}
	
	/***
	 * Build a simple MetaData String for the Channel
	 * @param name
	 * @param url
	 * @param image
	 * @return
	 */
	private String createMetaData(String name, String url, String image) {
		String res = "";

		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(metaData));
			Document doc = builder.parse(insrc);
			Node node = doc.getFirstChild();
			Node item = node.getFirstChild();
			//int count = item.getAttributes().getLength();
			NamedNodeMap attts = item.getAttributes();
			Node nid = attts.getNamedItem("id");
			nid.setTextContent(name);
			log.debug("Item Child Nodes " + item.getChildNodes().getLength());
			NodeList childs = item.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node n = childs.item(i);
				//log.debug("Name: " + n.getNodeName() + " Value " + n.getTextContent());
				if (n.getNodeName() == "dc:title") {
					n.setTextContent(name);
				} else if (n.getNodeName() == "res") {
					n.setTextContent(url);
				} else if (n.getNodeName() == "upnp:albumArtURI") {
					n.setTextContent(image);
				} else if (n.getNodeName()=="upnp:artist")
				{
					n.setTextContent(name);
				}
			}
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			res = (result.getWriter().toString());
		} catch (Exception e) {
			log.error("Error Creating XML Doc", e);
		}
		return res;
	}

}
