package org.rpi.sources;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SourceReader {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap<String, Source> sources = new ConcurrentHashMap<String, Source>();
	private String default_pin = "";

	public SourceReader() {
		readSources();
	}

	/***
	 * Read the InputSources.xml file and build a List of Radio Channels
	 * 
	 * @return
	 */
	private void readSources() {

		try {
			sources.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File("InputSources.xml"));
			NodeList listOfChannels = doc.getElementsByTagName("Source");
			String ex_columns = "/Sources/@default_pin";
			XPath xPath = XPathFactory.newInstance().newXPath();
			setDefaultPin(xPath.compile(ex_columns).evaluate(doc));
			int i = 1;
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				boolean addToSource = true;
				String name = null;
				String type = null;
				String GPIO_PIN = "";

				Node channel = listOfChannels.item(s);
				if (channel.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) channel;
					name = getElement(element, "name");
					type = getElement(element, "type");
					GPIO_PIN = getElement(element, "GPIO_PIN");
					Source source = new Source(name, type, GPIO_PIN);
					if (type.equalsIgnoreCase("RECEIVER")) {
						if (!Config.enableReceiver) {
							addToSource = false;
						}
					}else if (type.equalsIgnoreCase("UPNP"))
					{
						if(!Config.enableAVTransport)
						{
							addToSource = false;
						}
					}
					if (addToSource) {
						addSource(source);
					}
				}
			}
		} catch (Exception e) {
			if (e instanceof FileNotFoundException) {
				log.warn("ListSources.xml Not Found");
			} else {
				log.error("Error Getting Source List", e);
			}
		}
	}

	private void addSource(Source source) {
		if (!getSources().containsKey(source.getName())) {
			getSources().put(source.getName(), source);
		}
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

	public ConcurrentHashMap<String, Source> getSources() {
		return sources;
	}

	private void setSources(ConcurrentHashMap<String, Source> sources) {
		this.sources = sources;
	}

	public String getDefaultPin() {
		return default_pin;
	}

	private void setDefaultPin(String default_pin) {
		this.default_pin = default_pin;
	}

}
