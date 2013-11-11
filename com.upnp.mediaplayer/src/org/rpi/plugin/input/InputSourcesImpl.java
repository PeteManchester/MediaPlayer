package org.rpi.plugin.input;

import java.io.File;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.rpi.player.events.EventSourceChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@PluginImplementation
public class InputSourcesImpl implements InputSourcesInterface, Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private ConcurrentHashMap< String, String> sources = new ConcurrentHashMap<String,String>();
	private String default_pin = "";

	public InputSourcesImpl() {
		getConfig();
		PluginGateWay.getInstance().addObserver(this);
	}

	private void getConfig() {
		try {
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = getFilePath(class_name);
			log.debug("Getting AlarmClock.xml from Directory: " + path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(path + "InputSources.xml"));
			NodeList listOfChannels = doc.getElementsByTagName("Source");
			int i = 1;
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				String name = null;
				String type = null;
				String pin = null;


				Node node = listOfChannels.item(s);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					name = getElement(element, "name");
					type = getElement(element, "type");
					pin = getElement(element, "GPIO_PIN");
				}
				sources.put(name, pin);
				log.debug("Adding Source: " + name);
				PluginGateWay.getInstance().AddSource(name, type, true);
			}
			
			NodeList defaultNodeList = doc.getElementsByTagName("Default");
			Node node =defaultNodeList.item(0);
			if(node!=null)
			{
				default_pin  = getElement((Element)node, "GPIO_PIN");
			}
			
		} catch (Exception e) {
			log.error("Error Reading InputSources.xml");
		}
	}

	/***
	 * Get the Path of this ClassFile Must be easier ways to do this!!!!
	 * 
	 * @param className
	 * @return
	 */
	private String getFilePath(String className) {
		if (!className.startsWith("/")) {
			className = "/" + className;
		}
		className = className.replace('.', '/');
		className = className + ".class";
		log.debug("Find Class, Full ClassName: " + className);
		String[] splits = className.split("/");
		String properName = splits[splits.length - 1];
		log.debug("Find Class, ClassName: " + properName);
		URL classUrl = this.getClass().getResource(className);
		if (classUrl != null) {
			String temp = classUrl.getFile();
			log.debug("Find Class, ClassURL: " + temp);
			if (temp.startsWith("file:")) {
				temp = temp.substring(5);
			}

			if (temp.toUpperCase().contains(".JAR!")) {
				log.debug("Find Class, This is a JarFile: " + temp);
				String[] parts = temp.split("/");
				String jar_path = "";
				for (String part : parts) {
					if (!part.toUpperCase().endsWith(".JAR!")) {
						jar_path += part + "/";
					} else {
						log.debug("Find File: Returning JarPath: " + jar_path);
						return jar_path;
					}
				}
			} else {
				log.debug("Find Class, This is NOT a Jar File: " + temp);
				if (temp.endsWith(properName)) {
					temp = temp.substring(0, (temp.length() - properName.length()));
				}
			}
			log.debug("Find File: Returning FilePath: " + temp);
			return temp;
		} else {
			log.debug("Find Class, URL Not Found");
			return "\nClass '" + className + "' not found in \n'" + System.getProperty("java.class.path") + "'";
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
				return res;
			}
		}
		return res;
	}

	@Override
	public void update(Observable o, Object event) {
		if (event instanceof EventSourceChanged) {
			EventSourceChanged es = (EventSourceChanged) event;
			String name = es.getName();
			log.debug("Source Changed: " + name);
			if(sources.containsKey(name))
			{
				String pin = sources.get(name);
				log.debug("Change Input to PIN: " + pin);
			}
			else
			{
				log.debug("Not A CustomSource Change Input to PIN: " + default_pin);
			}
		}

	}

}
