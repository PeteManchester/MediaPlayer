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
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.sources.Source;
import org.rpi.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@PluginImplementation
public class LircIntegrationImpl implements LircIntegrationInterface, Observer {

	Logger log = Logger.getLogger(LircIntegrationImpl.class);
	ConcurrentHashMap<String, LIRCCommand> commands = new ConcurrentHashMap<String, LIRCCommand>();
	LIRCWorkQueue wq = null;

	public LircIntegrationImpl() {
		log.debug("Starting LircIntegrationImpl");
		try {
			wq = new LIRCWorkQueue();
			wq.start();
		} catch (Exception e) {
			log.error("Error: Starting WorkQueue");
		}
		getConfig();

		// Register for Volume Events
		PlayManager.getInstance().observeVolumeEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
		// Register for Source Events
		PluginGateWay.getInstance().addObserver(this);
	}

	@Override
	public void update(Observable o, Object event) {
		LIRCCommand command = null;
		EventBase base = (EventBase) event;
		switch (base.getType()) {
		case EVENTREQUESTVOLUMEINC:
			command = commands.get("VolumeInc");
			wq.put(command.getCommand());
			break;
		case EVENTREQUESTVOLUMEDEC:
			command = commands.get("VolumeDec");
			wq.put(command.getCommand());
			break;
		case EVENTSOURCECHANGED:
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			log.debug("Source Changed: " + name);
			 command =  commands.get("SourceChanged@" + name);
			if (command != null) {
				wq.put(command.getCommand());
			} else {
				log.debug("Could Not Find Command for SourceChanged@" + name);
			}
			sendStopCommand(name);
			sendStartCommand(name);
			break;
		case EVENTSTANDBYCHANGED:
			EventStandbyChanged esb = (EventStandbyChanged) event;
			if(esb !=null)
			{
				if(esb.isStandby())
				{
					command = commands.get("StandbyChanged@true");
				}
				else
				{
					command = commands.get("StandbyChanged@false");
				}
				if (command != null) 
					wq.put(command.getCommand());
			}
			break;
		}		
	}

	/***
	 * Read the Config file and get the mappings between the Events and
	 * commands.
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
					event = XMLUtils.getStringFromElement(element, "Event");
					command = XMLUtils.getStringFromElement(element, "Command");
					name = XMLUtils.getStringFromElement(element, "Name");
					String key = event;
					if (name != null && !name.equalsIgnoreCase(""))
						key += "@" + name;
					addToCommands(key, command, name);
				}
			}
		} catch (Exception e) {
			log.error("Error Reading LIRCConfig.xml");
		}
	}

	private void addToCommands(String event, String command, String name) {
		if (!commands.containsKey(event)) {
			LIRCCommand cmd = new LIRCCommand(command, name);
			commands.put(event, cmd);
		}
	}

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		if (wq != null) {
			wq.clear();
			wq = null;
		}
	}
	
	private void sendStartCommand(String name)
	{
		ConcurrentHashMap<String, Source> sources =  PluginGateWay.getInstance().getSources();
		for(String s : sources.keySet())
		{
			if(s.equalsIgnoreCase(name))
			{
				Source source = sources.get(s);
				if(source !=null)
				{
					if(source.getStartScript() !=null)
					{
						log.debug("Source Changed, run StartScript: " + source.getStartScript());
						wq.put(source.getStartScript());
					}
				}
			}
		}
	}
	
	private void sendStopCommand(String name)
	{
		ConcurrentHashMap<String, Source> sources =  PluginGateWay.getInstance().getSources();
		for(String s : sources.keySet())
		{
			if(!s.equalsIgnoreCase(name))
			{
				Source source = sources.get(s);
				if(source !=null)
				{
					if(source.getStopScript() !=null)
					{
						log.debug("Source Changed, run StopScript: " + source.getStopScript());
						wq.put(source.getStopScript());
					}
				}
			}
		}
	}

}
