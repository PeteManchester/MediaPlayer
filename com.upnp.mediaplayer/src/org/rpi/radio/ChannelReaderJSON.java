package org.rpi.radio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.JsonValue;
//import javax.json.JsonValue.ValueType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rpi.channel.ChannelPlayList;
import org.rpi.channel.ChannelRadio;
import org.rpi.config.Config;
import org.rpi.mplayer.CloseMe;
import org.rpi.providers.PrvRadio;
import org.rpi.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChannelReaderJSON implements Runnable {

	private Logger log = Logger.getLogger(this.getClass());

	// private String metaData =
	// "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item
	// id=''><dc:title
	// xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist
	// role='Performer'
	// xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:class
	// xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res
	// bitrate='' nrAudioChannels=''
	// protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI
	// xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";
	private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:artist><upnp:album xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'/><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";
	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();

	private PrvRadio prvRadio = null;

	public ChannelReaderJSON(PrvRadio prvRadio) {
		this.prvRadio = prvRadio;
	}

	// public List<ChannelRadio> getChannels() {
	//
	// getAllChannels();
	// //return channels;
	// }

	private void getAllChannels() {
		try {
			getJSONFromFile();
			String partnerId = Config.getInstance().getRadioTuneInPartnerId();
			if (Utils.isEmpty(partnerId)) {
				log.debug("TuneIn PartnerId not configured, do not attempt to load TuneIn stations");

			} else {
				// String url =
				// "http://opml.radiotime.com/Browse.ashx?c=presets&partnerid="
				// + partnerId + "&username=" +
				// Config.getInstance().getRadioTuneinUsername() +
				// "&render=json";
				String url = "http://opml.radiotime.com/Browse.ashx?&c=presets&options=recurse:tuneShows&partnerid=" + partnerId + "&username=" + Config.getInstance().getRadioTuneinUsername() + "&formats=mp3,wma,wmvideo,ogg,hls" + "&render=json";
				// String url =
				// "http://opml.radiotime.com/Browse.ashx?&c=presets&options=recurse:tuneShows&partnerid="
				// + partnerId + "&username=" +
				// Config.getInstance().getRadioTuneinUsername() +
				// "&render=json" + "&formats=mp3,wma,aac,wmvideo,ogg,hls";
				getJsonFromURL(url);
				// getBody(object);
			}
			prvRadio.addChannels(channels);
		} catch (Exception e) {
			log.error("Error Getting Channels", e);
		}
	}

	/**
	 * Get a JSON Object from a File
	 */
	private void getJSONFromFile() {
		/*
		 * Reader reader = null; try { reader = new
		 * FileReader("RadioList.json"); } catch (FileNotFoundException e) {
		 * log.error("Cannot find RadioList.json", e); // Bail out here is we
		 * can't find the file return; }
		 * 
		 * this.getJsonFromReader(reader, "RadionList.json");
		 */

		String content;
		try {
			content = new String(Files.readAllBytes(Paths.get("RadioList.json")));
			JSONObject array = new JSONObject(content);
			getBody(array);
		} catch (IOException e) {
			log.error("getJSONFromFile", e);
		}

	}

	private String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	/*
	 * Get a JSON Object from a URL
	 */
	private void getJsonFromURL(String url) {
		JSONObject res = new JSONObject();
		InputStream is = null;
		BufferedReader rd = null;
		try {
			is = new URL(url).openStream();
			rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			res = new JSONObject(jsonText);
			getBody(res);
		} catch (Exception e) {
			log.error("Error GetJSONFromURL", e);
		} finally {
			if (is != null) {
				CloseMe.close(is);
				is = null;
			}
			if (rd != null) {
				CloseMe.close(rd);
				rd = null;
			}
		}

		/*
		 * URL mUrl = null; Reader reader = null;
		 * 
		 * try { mUrl = new URL(url); reader = new
		 * InputStreamReader(mUrl.openStream()); } catch (MalformedURLException
		 * e) { log.error("Invalid URL given", e); return; } catch (IOException
		 * e) { log.error("Cannot open stream", e); return; }
		 * 
		 * this.getJsonFromReader(reader, url);
		 */
	}

	/*
	 * private void getJsonFromReader(Reader reader, String url) { String test =
	 * ""; try { test = getJSONFromReader(reader);
	 * log.debug("####TuneIn. Attempting to Reader for URL: " + url + " \r\n" +
	 * test); // JsonReader jsonReader = Json.createReader(reader); JsonReader
	 * jsonReader = Json.createReader(new StringReader(test)); JsonObject array
	 * = jsonReader.readObject(); jsonReader.close(); getBody(array); } catch
	 * (Exception e) {
	 * log.error("####TuneIn. Error Reading RadioList.json from given reader. "
	 * + url + "\r\n #####" + test, e); // printReader(reader); } }
	 */

	private String getJSONFromReader(Reader reader) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			log.error("Error Printing Reader", e);
		} finally {
			if (br != null) {
				CloseMe.close(br);
			}
		}
		return sb.toString();
	}

	private void printReader(Reader reader) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(reader);
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			log.error("Error Printing Reader", e);
		} finally {
			if (br != null) {
				CloseMe.close(br);
			}
		}

		log.info("####BAD RADIO START");
		log.info(sb.toString());
		log.info("####BAD RADIO END");
	}

	private void getBody(JSONObject array) {
		if (array == null)
			return;
		if (array.has("body")) {
			JSONArray body = array.getJSONArray("body");
			getStations(body);
		}
	}

	private void getStations(JSONArray array) {
		Iterator<Object> l = array.iterator();
		while (l.hasNext()) {
			JSONObject object = (JSONObject) l.next();
			boolean bType = object.has("type");// Standard Station
			boolean bKey = object.has("key");// If key=topics it's an//
												// ondemand
			String type = "";
			String key = "";
			if (bType) {
				type = object.getString("type");
			}
			if (bKey) {
				key = object.getString("key");
			}
			if (bType || bKey) {
				if (type.equalsIgnoreCase("container")) {
					if (type.equalsIgnoreCase("container")) {
						JSONArray children = object.getJSONArray("children");
						{
							if (children != null) {
								log.debug("Get Container Children");
								getStations(children);
							}
						}
					}
				}
				if (key.equalsIgnoreCase("topics")) {
					// OnDemand
					JSONArray children = object.getJSONArray("children");
					{
						if (children != null) {
							log.debug("Get Topics Children");
							getStations(children);
						}
					}
				}
				if (key.equalsIgnoreCase("presetURLs")) {
					JSONArray children = object.getJSONArray("children");
					if (children != null) {
						log.debug("Get PresetURLs Children");
						getStations(children);
					}
				}
				if (key.equalsIgnoreCase("stations")) {
					JSONArray children = object.getJSONArray("children");
					if (children != null) {
						log.debug("Get Stations Children");
						getStations(children);
					}
				}
				boolean bItem = object.has("item");
				if (bType && bItem) {// Probably a ListenLive
					if (object.getString("type").toLowerCase().equalsIgnoreCase("link") && object.getString("item").equalsIgnoreCase("show")) {
						String url = object.getString("URL");
						log.debug("Get Shows: " + url);
						getJsonFromURL(url + "&render=json" + "&c=pbrowse");
					} else if (object.getString("type").equalsIgnoreCase("audio") || object.getString("item").equalsIgnoreCase("url") || object.getString("item").equalsIgnoreCase("topic")) {
						String text = getString(object, "text");
						String url = getString(object, "URL");
						url = tidyURL(url);
						String image = getString(object, "image");
						String preset_id = getString(object, "guide_id");
						preset_id = preset_id.replaceAll("[^0-9]+", "");
						String item = getString(object, "item");
						boolean icy_reverse = getBoolean(object, "icy_reverse", false);
						boolean keep_url = getBoolean(object, "keep_url", false);
						addChannel(text, url, image, icy_reverse, preset_id, item, keep_url);
					}
				}
			}
		}
	}

	/*
	 * private void getStationsOLD(JSONArray array) { // JsonArray arr =
	 * array.getJsonArray(1);
	 * 
	 * for (JsonValue jsonValue : array) { if (jsonValue.getValueType() ==
	 * ValueType.OBJECT) { JsonObject object = (JsonObject) jsonValue; if
	 * (object.containsKey("children")) { if (object.containsKey("key")) { if
	 * (object.getString("key").equalsIgnoreCase("stations")) { JsonArray
	 * children = object.getJsonArray("children"); getStations(children); } else
	 * if (object.getString("key").equalsIgnoreCase("shows")) { JsonArray
	 * children = object.getJsonArray("children");
	 * 
	 * // String s = object.getString("URL"); //
	 * getJsonFromURL(object.getString("URL")); getStations(children); } else if
	 * (object.getString("key").equalsIgnoreCase("topics")) { JsonArray children
	 * = object.getJsonArray("children"); getStations(children); } else if
	 * (object.getString("key").equalsIgnoreCase("presetUrls")) { JsonArray
	 * children = object.getJsonArray("children"); getStations(children); } } }
	 * else { boolean bType = object.containsKey("type"); boolean bItem =
	 * object.containsKey("item"); if (bType && bItem) {// Probably a ListenLive
	 * if (object.getString("type").toLowerCase().equalsIgnoreCase("link") &&
	 * object.getString("item").equalsIgnoreCase("show")) { String url =
	 * object.getString("URL"); log.debug("Get Shows: " + url); // int temp =
	 * getIntFromString(object, // "preset_number"); getJsonFromURL(url +
	 * "&render=json" + "&c=pbrowse"); } else if
	 * (object.getString("type").equalsIgnoreCase("audio") ||
	 * object.getString("item").equalsIgnoreCase("url") ||
	 * object.getString("item").equalsIgnoreCase("topic")) { String text =
	 * getString(object, "text"); String url = getString(object, "URL"); url =
	 * tidyURL(url); String image = getString(object, "image"); String preset_id
	 * = getString(object, "guide_id"); preset_id =
	 * preset_id.replaceAll("[^0-9]+", ""); String item = getString(object,
	 * "item"); boolean icy_reverse = getBoolean(object, "icy_reverse", false);
	 * boolean keep_url = getBoolean(object, "keep_url", false); // pres_number
	 * = getIntFromString(object, // "preset_number", pres_number); //
	 * if(pres_number <=0) // { // preset_number ++; // } // else // { //
	 * preset_number = pres_number; // } addChannel(text, url + "&c=ebrowse",
	 * image, icy_reverse, preset_id, item, keep_url); } } } } } //
	 * Collections.sort(channels); }
	 * 
	 */

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
				// if (!(key.toLowerCase().startsWith("partnerid=") ||
				// key.toLowerCase().startsWith("username="))) {
				if (!key.toLowerCase().startsWith("username=")) {
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

	private String getString(JSONObject value, String key) {
		String res = "";
		try {
			if (value.has(key)) {
				return value.getString(key);
			}
		} catch (Exception e) {

		}
		return res;
	}

	private boolean getBoolean(JSONObject value, String key, boolean default_value) {
		boolean res = default_value;
		try {
			if (value.has(key)) {
				return value.getBoolean(key);
			}
		} catch (Exception e) {

		}
		return res;
	}

	private int getIntFromString(JSONObject value, String key, int default_value) {
		int res = default_value;
		try {
			if (value.has(key)) {
				String temp = value.getString(key);
				return Integer.parseInt(temp);
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
	private void addChannel(String name, String url, String image, boolean icy_reverse, String preset_id, String item, boolean keep_url) {

		String m = createMetaData(name, url, image);
		int id = channels.size() + 1;
		try {
			id = Integer.parseInt(preset_id);
		} catch (Exception e) {
			log.debug("Could Not  Parse ChannelID: " + preset_id);
		}

		ChannelRadio channel = null;
		ChannelRadio oldChannel = null;
		for (ChannelRadio ch : channels) {
			if (name.equalsIgnoreCase(ch.getName()) && item.equalsIgnoreCase("station")) {
				if (ch.isKeepURL()) {
					m = createMetaData(name, ch.getUri(), image);
					url = ch.getUri();
				}
				channel = new ChannelRadio(url, m, id, name);
				channel.setICYReverse(ch.isICYReverse());
				if (ch.isKeepURL()) {
					log.debug("Channel " + ch.getName() + " to keep URL from Config File: " + ch.getUri());
					channel.setUri(ch.getUri());
					channel.setKeepURL(ch.isKeepURL());
				}
				oldChannel = ch;
				log.debug("Updated Channel: " + channel.getId() + " - " + channel.getUri() + " " + channel.getFullDetails());
				break;
			}
		}

		if (oldChannel != null) {
			channels.remove(oldChannel);
		}

		if (channel == null) {
			channel = new ChannelRadio(url, m, id, name);
			channel.setICYReverse(icy_reverse);
			channel.setKeepURL(keep_url);
		}
		log.info("Channel Name (For AlarmClock Config: '" + name + "'");
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
					// } else if (n.getNodeName() == "upnp:artist") {
					// n.setTextContent(name);
				} else if (n.getNodeName() == "upnp:album") {
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

	/***
	 * Used for the Pins service An TuneIn station id is given and this builds
	 * up the metadata from that.
	 * 
	 * @param presetId
	 * @param url
	 * @param image
	 * @return
	 */
	public String getMetaDataForTuneInId(String presetId, String url, String image) {
		String mUrl = "http://opml.radiotime.com/Browse.ashx?render=json&id=" + presetId;
		String title = getMetaData(mUrl);
		String m = createMetaData(title, url, image);
		return m;
	}

	private String getMetaData(String url) {
		String title = "";
		try {
			JSONObject array = getJSONObject(url);
			if(array.has("head")) {
				JSONObject o = array.getJSONObject("head");
				if(o.has("title")) {
					title = o.getString("title");
				}
				else {
					log.error("Error getMetaData does not contain 'title'. URL: " + url + " JSON: " + o.toString());
				}
			}
			else {
				log.error("Error getMetaData does not contain 'head'. URL: " + url + " JSON: " + array.toString());
			}
		} catch (Exception e) {
			log.error("Invalid URL given", e);
		}
		return title;
	}

	/***
	 * Used by the Pin Service Get a List of TuneIn Podcasts
	 * 
	 * @param url
	 * @param image
	 * @return
	 */
	public List<ChannelPlayList> getPodcasts(String url, String image) {
		List<ChannelPlayList> res = new ArrayList<ChannelPlayList>();
		JSONObject array = getJSONObject(url + "&render=json");
		log.debug(array);
		if (array == null)
			return res;
		if (array.has("body")) {
			JSONArray body = array.getJSONArray("body");

			getPodCasts(body, res);
		}

		return res;
	}

	/***
	 * Iterate all levels to get the pod casts
	 * 
	 * @param array
	 * @param list
	 * @return
	 */
	private List<ChannelPlayList> getPodCasts(JSONArray array, List<ChannelPlayList> list) {
		Iterator<Object> l = array.iterator();
		while (l.hasNext()) {
			JSONObject object = (JSONObject) l.next();
			boolean bType = object.has("type");// Standard Station
			boolean bKey = object.has("key");// If key=topics it's an//
												// ondemand
			String type = "";
			String key = "";
			if (bType) {
				type = object.getString("type");
			}
			if (bKey) {
				key = object.getString("key");
			}
			if (bType || bKey) {
				if (type.equalsIgnoreCase("container")) {
					if (type.equalsIgnoreCase("container")) {
						JSONArray children = object.getJSONArray("children");
						{
							if (children != null) {
								log.debug("Get Container Children");
								// getStations(children);
							}
						}
					}
				}
				if (key.equalsIgnoreCase("topics")) {
					// OnDemand
					JSONArray children = object.getJSONArray("children");
					{
						if (children != null) {
							log.debug("Get Topics Children");
							getPodCasts(children, list);
						}
					}
				}
				if (key.equalsIgnoreCase("presetURLs")) {
					JSONArray children = object.getJSONArray("children");
					if (children != null) {
						log.debug("Get PresetURLs Children");
						// getStations(children);
					}
				}
				if (key.equalsIgnoreCase("stations")) {
					JSONArray children = object.getJSONArray("children");
					if (children != null) {
						log.debug("Get Stations Children");
						// getStations(children);
					}
				}
				boolean bItem = object.has("item");
				if (bType && bItem) {// Probably a ListenLive
					if (object.getString("type").toLowerCase().equalsIgnoreCase("link") && object.getString("item").equalsIgnoreCase("show")) {
						String url = object.getString("URL");
						log.debug("Get Shows: " + url);
						// getJsonFromURL(url + "&render=json" + "&c=pbrowse");
					} else if (object.getString("type").equalsIgnoreCase("audio") || object.getString("item").equalsIgnoreCase("url") || object.getString("item").equalsIgnoreCase("topic")) {
						String text = getString(object, "text");
						String url = getString(object, "URL");
						url = tidyURL(url);
						String image = getString(object, "image");
						String preset_id = getString(object, "guide_id");
						preset_id = preset_id.replaceAll("[^0-9]+", "");
						String m = createMetaData(text, url, image);
						ChannelPlayList c = new ChannelPlayList(url, m, list.size());
						log.debug(c);
						list.add(c);
					}
				}
			}
		}
		return list;
	}

	/***
	 * Open the StreamReader for the URL, then get the JSONObject
	 * 
	 * @param url
	 * @return
	 */
	private JSONObject getJSONObject(String url) {
		JSONObject array = new JSONObject();
		InputStream is = null;
		BufferedReader rd = null;
		try {
			is = new URL(url).openStream();
			rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			// rd.close();
			String jsonText = readAll(rd);
			array = new JSONObject(jsonText);
		} catch (Exception e) {
			log.error("Error GetJSONFromURL", e);
		} finally {
			if (is != null) {
				CloseMe.close(is);
				is = null;
			}
			if (rd != null) {
				CloseMe.close(rd);
				rd = null;
			}
		}

		return array;

		/*
		 * Reader reader = null; try { URL mUrl = new URL(url); reader = new
		 * InputStreamReader(mUrl.openStream()); String test =
		 * getJSONFromReader(reader);
		 * log.debug("####TuneIn. Attempting to Reader for URL: " + url +
		 * " \r\n" + test); JsonReader jsonReader = Json.createReader(new
		 * StringReader(test)); JsonObject array = jsonReader.readObject();
		 * return array; } catch (Exception e) { log.error("Invalid URL given",
		 * e); } return null;
		 */
	}

	@Override
	public void run() {
		try {
			getAllChannels();
		} catch (Exception e) {
			log.error(e);
		}
	}

}
