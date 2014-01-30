package org.rpi.plugin.lirc;

import java.io.File;
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
import org.rpi.player.events.EventSourceChanged;
import org.rpi.plugin.input.InputSourcesInterface;
import org.rpi.plugingateway.PluginGateWay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@PluginImplementation
public class LircIntegrationImpl implements InputSourcesInterface, Observer {

	Logger log = Logger.getLogger(LircIntegrationImpl.class);
	ConcurrentHashMap<String, LIRCCommand> commands = new ConcurrentHashMap<String, LIRCCommand>();
	LIRCWorkQueue wq = null;

	public LircIntegrationImpl() {
		wq = new LIRCWorkQueue();
		wq.start();
		getConfig();
		//Register for Volume Events
		PlayManager.getInstance().observVolumeEvents(this);
		//Register for Source Events
		PluginGateWay.getInstance().addObserver(this);
	}

	@Override
	public void update(Observable o, Object event) {
		if (event instanceof EventRequestVolumeDec) {
			LIRCCommand command = commands.get("VolumeDec");
			wq.put(command.getCommand());
		} else if (event instanceof EventRequestVolumeInc) {
			LIRCCommand command = commands.get("VolumeInc");
			wq.put(command.getCommand());
		}
		else if (event instanceof EventSourceChanged) {
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			log.debug("Source Changed: " + name);
			LIRCCommand command = commands.get("SourceChanged@"+name);
			if(command !=null)
			{
				wq.put(command.getCommand());
			}
			else
			{
				log.debug("Could Not Find Command for SourceChanged@" + name);
			}		
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
				String name = null;
				Node mapping = mappings.item(s);
				if (mapping.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) mapping;
					event = getElementTest(element, "Event");
					command = getElementTest(element, "Command");
					name = getElementTest(element, "Name");
					String key = event;
					if(name !=null && !name.equalsIgnoreCase(""))
						key += "@" + name;
					addToCommands(key, command,name);
				}
			}
		} catch (Exception e) {
			log.error("Error Reading LIRCConfig.xml");
		}
	}

	private void addToCommands(String event, String command,String name) {
		if (!commands.containsKey(event)) {
			LIRCCommand cmd = new LIRCCommand(command,name);
			commands.put(event, cmd);
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
		if(wq!=null)
		{ 
			wq.clear();
			wq = null;
		}
	}

}
