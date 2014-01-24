package org.rpi.plugin.lirc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventRequestVolumeDec;
import org.rpi.player.events.EventRequestVolumeInc;
import org.rpi.plugin.input.InputSourcesInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@PluginImplementation
public class LircIntegrationImpl implements InputSourcesInterface, Observer {

	Logger log = Logger.getLogger(LircIntegrationImpl.class);
	ConcurrentHashMap<String, String> commands = new ConcurrentHashMap<String, String>();

	public LircIntegrationImpl() {
		getConfig();
		PlayManager.getInstance().observVolumeEvents(this);
	}

	@Override
	public void update(Observable o, Object event) {
		if (event instanceof EventRequestVolumeDec) {
			String command = commands.get("VolumeDec");
			processEvent(command);
		} else if (event instanceof EventRequestVolumeInc) {
			String command = commands.get("VolumeInc");
			processEvent(command);
		}
	}

	private void processEvent(String command) {
		if(command ==null)
			return;
		log.debug("Sending Command: " + command);
		try
		{
		Process pa = Runtime.getRuntime().exec(command);
		pa.waitFor();
		BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			log.debug("Result of " + command + " : " + line);
		}
		reader.close();
		pa.getInputStream().close();
		}
		catch(Exception e)
		{
			log.error("Error Sending Command: " + command , e);
		}
	}

	/***
	 *Read the Config file and get the mappings between the Events and commands.
	 */
	private void getConfig() {
		try {
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = OSManager.getInstance().getFilePath(this.getClass(), false);
			log.debug("Getting LIRCConfig.xml from Directory: " + path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(path + "LIRCConfig.xml"));
			NodeList mappings = doc.getElementsByTagName("Mapping");
			int i = 1;
			for (int s = 0; s < mappings.getLength(); s++) {
				String event = null;
				String command = null;

				Node mapping = mappings.item(s);
				if (mapping.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) mapping;
					event = getElementTest(element, "Event");
					command = getElementTest(element, "Command");
					addToCommands(event, command);
				}
			}
		} catch (Exception e) {
			log.error("Error Reading LIRCConfig.xml");
		}
	}

	private void addToCommands(String event, String command) {
		if (!commands.containsKey(event)) {
			commands.put(event, command);
		}
	}

	/***
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	private String getElementTest(Element element, String name) {
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

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
	}

}
