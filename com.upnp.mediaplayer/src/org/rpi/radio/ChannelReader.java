package org.rpi.radio;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelRadio;
import org.rpi.config.Config;
import org.rpi.radio.parsers.ASHXParser;
import org.rpi.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChannelReader {

	private static Logger log = Logger.getLogger(ChannelReader.class);

	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();

	// private int iCount = 0;

	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	/***
	 * Read the RadioList.xml file and build a List of Radio Channels
	 * 
	 * @return
	 */
	public List<ChannelRadio> getChannels() {

		try {
			// this.iCount = iCount;
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
				String s_icy_reverse = "";
				String enabled = "";
				boolean icy_reverse = false;

				Node channel = listOfChannels.item(s);
				if (channel.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) channel;
					id = getElement(element, "id");
					url = getElement(element, "url");
					image = getElement(element, "image");
					s_icy_reverse = getElement(element, "icy_reverse");
					if (s_icy_reverse.equalsIgnoreCase("TRUE")) {
						icy_reverse = true;
					}
					enabled = getElement(element, "enabled");
					if (enabled == null) {
						enabled = "";
					}
				}

				if (id != null && url != null && !enabled.equalsIgnoreCase("FALSE")) {
					addChannel(id, url, image, icy_reverse);
					i++;
				}
			}
		} catch (Exception e) {
			log.error("Error Getting Radio List", e);
		}
		getTuneInChannels();
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
				// log.debug("ElementName: " + name + " Value: " + res);
				return res;

			}
		}
		return res;
	}

	/***
	 * Create a Channel and add it to the List
	 * 
	 * @param name
	 * @param url
	 * @param image
	 * @param id
	 */
	private void addChannel(String name, String url, String image, boolean icy_reverse) {
		String m = createMetaData(name, url, image);
		ChannelRadio channel = new ChannelRadio(url, m, channels.size() + 1, name);
		channel.setICYReverse(icy_reverse);
		channels.add(channel);
		// iCount++ ;
		// log.debug("iCount: " + iCount);
		log.debug("Added Channel: " + channel.getId() + " - " + channel.getUri() + " " + channel.getFullDetails());
	}

	/***
	 * Build a simple MetaData String for the Channel
	 * 
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
			// int count = item.getAttributes().getLength();
			NamedNodeMap attts = item.getAttributes();
			Node nid = attts.getNamedItem("id");
			nid.setTextContent(name);
			// log.debug("Item Child Nodes " +
			// item.getChildNodes().getLength());
			NodeList childs = item.getChildNodes();
			for (int i = 0; i < childs.getLength(); i++) {
				Node n = childs.item(i);
				// log.debug("Name: " + n.getNodeName() + " Value " +
				// n.getTextContent());
				if (n.getNodeName() == "dc:title") {
					n.setTextContent(name);
				} else if (n.getNodeName() == "res") {
					n.setTextContent(url);
				} else if (n.getNodeName() == "upnp:albumArtURI") {
					n.setTextContent(image);
				} else if (n.getNodeName() == "upnp:artist") {
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

	/**
	 * Used for TuneIn
	 * 
	 * @param url
	 * @return
	 */
	private URLConnection getConnection(String url) {
		URLConnection mUrl;
		try {
			mUrl = new URL(url).openConnection();
			return mUrl;
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

	/**
	 * Used for TuneIn
	 * 
	 * @param text
	 * @return
	 */
	private Document createDocument(String text) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(text));
			return builder.parse(insrc);
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Get the Favourites from the TuneIn Radio Account
	 */
	private void getTuneInChannels() {
		if (Utils.isEmpty(Config.radio_tunein_username)) {
			log.debug("TuneIn UserName not configured, do not attempt to load TuneIn stations");
			return;
		}
		String partnerId="";
		if(Utils.isEmpty(partnerId))
		{
			log.debug("TuneIn PartnerId not configured, do not attempt to load TuneIn stations");
			return;
		}

		String url = "http://opml.radiotime.com/Browse.ashx?c=presets&partnerid="+ partnerId +"&username=" + Config.radio_tunein_username;
		log.debug("Getting TuneIn Radio Favourites");
		try {

			Document doc = getDocument(url);
			if (doc != null) {
				NodeList nodeList = doc.getElementsByTagName("outline");
				parseDocument(nodeList);
			}
		} catch (Exception e) {
			log.error("Error getting TuneIn favourites", e);
		}
	}

	/**
	 * Parse the XML Doc to get the Audio feeds
	 * @param nodeList
	 */
	private void parseDocument(NodeList nodeList) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NamedNodeMap map = node.getAttributes();
				if (map != null) {
					Node type = map.getNamedItem("type");
					if (type != null) {
						if (type.getTextContent().equalsIgnoreCase("link")) {
							Node urls = map.getNamedItem("URL");
							if (urls != null) {
								Document docLink = getDocument(urls.getTextContent());
								if (docLink != null) {
									NodeList linkNodeList = docLink.getElementsByTagName("outline");
									//parseDocument(linkNodeList); Disabled for now because it can swamp the Radio List if there are too many episodes in teh lisk
								}
							}
						}
						if (type.getTextContent().equalsIgnoreCase("audio")) {
							addNodeChannel(map);
						}
					}
				}
			}
		}
	}

	/**
	 * Get the XML from a URL
	 * @param url
	 * @return
	 */
	private Document getDocument(String url) {
		Document doc = null;
		try {
			URLConnection conn = getConnection(url);
			if (conn != null) {
				log.debug("URL: " + url + " Headers: " + conn.getHeaderFields());
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				while (true) {
					try {
						String line = br.readLine();

						if (line == null) {
							break;
						}
						System.out.println(line);
						sb.append(line);
					} catch (IOException e) {
						log.error(e);
					}
				}
				doc = createDocument(sb.toString());
			}

		} catch (Exception e) {
		}
		return doc;
	}

	/**
	 * Add a NodeMap as a Channel
	 * @param map
	 */
	private void addNodeChannel(NamedNodeMap map) {
		if (map != null) {
			Node text = map.getNamedItem("text");
			Node urls = map.getNamedItem("URL");
			Node image = map.getNamedItem("image");
			String sText = "";
			String sURL = "";
			String sImage = "";
			if (text != null) {
				sText = text.getTextContent();
			}
			if (urls != null) {
				sURL = urls.getTextContent();
			}
			if (image != null) {
				sImage = image.getTextContent();
			}
			ASHXParser parser = new ASHXParser();
			LinkedList<String> ashxURLs = parser.getStreamingUrl(sURL);
			if (ashxURLs.size() > 0) {
				addChannel(sText, ashxURLs.getFirst(), sImage, false);
			}
		}
	}

}