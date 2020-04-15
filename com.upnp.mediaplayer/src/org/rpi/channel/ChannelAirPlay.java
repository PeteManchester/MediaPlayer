package org.rpi.channel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

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

public class ChannelAirPlay extends ChannelBase {
	
	private Logger log = Logger.getLogger(this.getClass());
	private String metadata = "";

	public ChannelAirPlay(String uri, String metadata, int id,String name) {
		super(uri, metadata, id);
		metadata = this.createMetaData(name, uri, "");
		super.setMetaText(metadata);
		setName(name);
	}
	
	public ChannelAirPlay(String uri, String metadata, int id,String name,String image) {
		super(uri, metadata, id);
		this.metadata = metadata;
		this.metadata = this.createMetaData(name, uri, image);
		super.setMetaText(this.metadata);
		setName(name);
	}
	
	@Override
    public String getMetadata() {
        return metadata;
    }
	
	
	
	private String name = "";
	
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
		String temp_meta = super.updateTrack(name, "AirPlay");
		super.setMetaText(temp_meta);
		super.getTrackDetails();
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
		StreamResult result = null;
		Writer w = null;
		try {

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource insrc = new InputSource(new StringReader(super.getMetadata()));
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
			result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			w = result.getWriter();
			res = (w.toString());
			metadata = res;
		} catch (Exception e) {
			log.error("Error Creating XML Doc", e);
		}
		finally {
			if(w !=null ) {
				try {
					w.close();
				} catch (IOException e) {
					log.error("Error closing Writer");
				};
			}
		}
		return res;
	}

}
