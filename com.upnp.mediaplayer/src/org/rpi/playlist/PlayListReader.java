package org.rpi.playlist;

import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.rpi.channel.ChannelPlayList;
import org.apache.log4j.Logger;
import org.rpi.providers.PrvPlayList;
import org.rpi.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PlayListReader {

	private CopyOnWriteArrayList<ChannelPlayList> tracks = new CopyOnWriteArrayList<ChannelPlayList>();

	private Logger log = Logger.getLogger(PlayListReader.class);
	private PrvPlayList iPlayList = null;
	
	private int max_id = 0;

	public PlayListReader(PrvPlayList iPlayList) {
		this.iPlayList = iPlayList;
	}

	public String getXML() {
		tracks.clear();
		try {
			long startTime = System.nanoTime();
			File file = new File("PlayList.xml");
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document doc = documentBuilder.parse(file);
			NodeList listOfChannels = doc.getElementsByTagName("Entry");
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				Node channel = listOfChannels.item(s);
				Element element = (Element) channel;
				String id = XMLUtils.getStringFromElement(element, "Id");
				int iId = Integer.parseInt(id);
				if(iId > max_id)
				{
					max_id = iId;
				}
				String url = XMLUtils.getStringFromElement(element, "Uri");
				String metadata = XMLUtils.getStringFromElement(element, "Metadata");
				ChannelPlayList t = new ChannelPlayList(url, metadata, Integer.parseInt(id));
				tracks.add(t);
				log.debug("Adding Track Id: " + id + " URL: " + url +  " " + t.getFullDetails());
			}
			iPlayList.setNextId(max_id);
			iPlayList.setTracks(tracks);
			long endTime = System.nanoTime();
			long duration = endTime - startTime;
			//log.warn("Time to Add CustomTracks: " + duration);
			//log.debug("HoldHere");
		} catch (Exception e) {
			log.error("Error: Reading XML", e);
		}
		return "";
	}

}
