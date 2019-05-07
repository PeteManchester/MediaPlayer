package org.rpi.controlpoint;

import java.io.StringReader;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.rpi.utils.Utils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Class that holds the info about a Kazoo MediaServer that has been discovered by the ControlPoint service.
 * @author phoyle
 *
 */

public class DeviceInfo {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private String host = "";
	private int port = -1;
	private String udn = "";

	public DeviceInfo(String udn, String xml) {
		setUdn(udn);
		parseText(xml);
	}
	
	private void parseText(String xml) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource insrc = new InputSource(new StringReader(xml.trim()));
            Document doc = builder.parse(insrc);
			
            String ex_title = "root/device/presentationURL";
            XPath xPath = XPathFactory.newInstance().newXPath();
            String title = xPath.compile(ex_title).evaluate(doc);
            log.debug("PresentationURL: " + title);
            URL url = new URL(title);
            setHost(url.getHost());
            setPort(url.getPort());
            log.debug("Host and Port");
		} catch (Exception e) {
			log.error("Error parsing xml");
		}
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUdn() {
		return udn;
	}

	public void setUdn(String udn) {
		this.udn = udn;
	}

	public boolean isValid() {
		if(Utils.isEmpty(host)) {
			return false;
		}
		if(port <= 0)
		{
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DeviceInfo [host=");
		builder.append(host);
		builder.append(", port=");
		builder.append(port);
		builder.append(", udn=");
		builder.append(udn);
		builder.append("]");
		return builder.toString();
	}

}
