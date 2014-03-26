package org.rpi.radio;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelRadio;
import org.rpi.config.Config;
import org.rpi.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChannelReaderJSON {

	private Logger log = Logger.getLogger(this.getClass());

	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();

	public List<ChannelRadio> getChannels() {

		getAllChannels();
		return channels;
	}

	private void getAllChannels() {
		try {
			getJSONFromFile();
			String partnerId = "";
			if (Utils.isEmpty(partnerId)) {
				log.debug("TuneIn PartnerId not configured, do not attempt to load TuneIn stations");

			} else {
				String url = "http://opml.radiotime.com/Browse.ashx?c=presets&partnerid=" + partnerId + "&username=" + Config.radio_tunein_username + "&render=json";
				getJsonFromURL(url);
			}

		} catch (Exception e) {
			log.error("Error Getting Channels", e);
		}
	}

	/**
	 * Get a JSON Object from a File
	 */
	private void getJSONFromFile() {
        Reader reader = null;
        try {
            reader = new FileReader("RadioList.json");
        } catch (FileNotFoundException e) {
            log.error("Cannot find RadioList.json", e);
            //Bail out here is we can't find the file
            return;
        }

        this.getJsonFromReader(reader);
	}

	/*
	 * Get a JSON Object from a URL
	 */
	private void getJsonFromURL(String url) {
        URL mUrl = null;
        Reader reader = null;

        try {
            mUrl = new URL(url);
            reader = new InputStreamReader(mUrl.openStream());
        } catch (MalformedURLException e) {
            log.error("Invalid URL given", e);
            return;
        } catch (IOException e) {
            log.error("Cannot open stream", e);
            return;
        }

        this.getJsonFromReader(reader);
	}

    private void getJsonFromReader(Reader reader) {
        try {
            JsonReader jsonReader = Json.createReader(reader);
            JsonObject array = jsonReader.readObject();
            jsonReader.close();
            getBody(array);
        } catch (Exception e) {
            log.error("Error Reading RadioList.json from given reader", e);
        }
    }

	private void getBody(JsonObject array) {
		if (array == null)
			return;
		if (array.containsKey("body")) {
			JsonArray body = array.getJsonArray("body");
			getStations(body);

		}
	}

	// private void parseJSON(JsonObject array) {
	// if (array == null)
	// return;
	// if (array.containsKey("body")) {
	// JsonArray body = array.getJsonArray("body");
	// for (JsonValue jsonValue : body) {
	// if (jsonValue.getValueType() == ValueType.OBJECT) {
	// JsonObject object = (JsonObject) jsonValue;
	// if (object.containsKey("children") &&
	// object.getString("key").equalsIgnoreCase("stations")) {
	// JsonArray stations = object.getJsonArray("children");
	// getStations(stations);
	// }
	//
	// }
	// }
	// }
	// }

	private void getStations(JsonArray array) {
		for (JsonValue jsonValue : array) {
			if (jsonValue.getValueType() == ValueType.OBJECT) {
				JsonObject object = (JsonObject) jsonValue;
				if (object.containsKey("children")) {
					if (object.containsKey("key")) {
						if (object.getString("key").equalsIgnoreCase("stations")) {
							JsonArray children = object.getJsonArray("children");
							getStations(children);
						} else if (object.getString("key").equalsIgnoreCase("shows")) {
							JsonArray children = object.getJsonArray("children");
							// String s = object.getString("URL");
							// getJsonFromURL(object.getString("URL"));
							getStations(children);
						} else if (object.getString("key").equalsIgnoreCase("topics")) {
							JsonArray children = object.getJsonArray("children");
							getStations(children);
						}
					}
				} else {
					if (object.getString("type").toLowerCase().equalsIgnoreCase("link")) {
						String url = object.getString("URL");

						getJsonFromURL(url + "&render=json");
					} else if (object.getString("type").equalsIgnoreCase("audio")) {

						String text = getString(object, "text");
						String url = getString(object, "URL");
						url = tidyURL(url);
						String image = getString(object, "image");
						String preset_id = getString(object, "preset_id");
						preset_id = preset_id.replaceAll("[^0-9]+", "");
						String item = getString(object, "item");
						boolean icy_reverse = getBoolean(object, "icy_reverse", false);
						addChannel(text, url, image, icy_reverse, preset_id, item);
					}
				}
			}
		}
	}

	/**
	 * Remove the partnerId and username details from the url..
	 * 
	 * @param url
	 * @return
	 */
	private String tidyURL(String url) {
		String[] splits = url.split("&");
		StringBuilder sb = new StringBuilder();
		String sep = "";
		if (splits.length > 0) {
			for (String key : splits) {
				if (!(key.toLowerCase().startsWith("partnerid=") || key.toLowerCase().startsWith("username="))) {
					sb.append(sep);
					sb.append(key);
					sep = "&";
				}
			}
		}
		String temp = sb.toString();
		if (temp.endsWith("?")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		if (temp.endsWith("&")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		return temp;
	}

	private String getString(JsonObject value, String key) {
		String res = "";
		try {
			if (value.containsKey(key)) {
				return value.getString(key);
			}
		} catch (Exception e) {

		}
		return res;
	}

	private boolean getBoolean(JsonObject value, String key, boolean default_value) {
		boolean res = default_value;
		try {
			if (value.containsKey(key)) {
				return value.getBoolean(key);
			}
		} catch (Exception e) {

		}
		return res;
	}

    /**
     * Create a Channel and add it to the List
     *
     * @param name
     * @param url
     * @param image
     * @param icy_reverse
     * @param preset_id
     * @param item
     */
	private void addChannel(String name, String url, String image, boolean icy_reverse, String preset_id, String item) {

		String m = createMetaData(name, url, image);
		int id = channels.size() + 1;
		try {
			id = Integer.parseInt(preset_id);
		} catch (Exception e) {
			log.debug("Could Not Parse ChannelID: " + preset_id);
		}

		ChannelRadio channel = null;
        ChannelRadio oldChannel = null;
		for (ChannelRadio ch : channels) {
			if (name.equalsIgnoreCase(ch.getName()) && item.equalsIgnoreCase("station")) {
				channel = new ChannelRadio(url, m, id, name);
				channel.setICYReverse(ch.isICYReverse());
				oldChannel = ch;
				log.debug("Updated Channel: " + channel.getId() + " - " + channel.getUri() + " " + channel.getFullDetails());
				break;
			}
		}

		if(oldChannel !=null)
		{
			channels.remove(oldChannel);
		}
		
		if(channel ==null)
		{
			channel = new ChannelRadio(url, m, id, name);
			channel.setICYReverse(icy_reverse);
		}		
		
		channels.add(channel);
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

}
