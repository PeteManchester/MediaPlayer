package org.rpi.kazo.server;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class DidlLiteImpl {

	public DidlLiteImpl() {
		createNodeTemplate();
	}

	Map<String, DidlNode> nodeTemplate = new HashMap<String, DidlNode>();
	
	private String xmlStart = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
	private String didlStart = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">";
	private String itemStart = "<item ";
	private String itemEnd = "</item>";
	private String didlEnd = "</DIDL-Lite>";
	private String xmlEnd = "</>";

	private String kNsDc = "dc=\"http://purl.org/dc/elements/1.1/\"";
	private String kNsUpnp = "upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"";
	private String kNsOh = "oh=\"http://www.openhome.org\"";
	
	
	private String dq = "\"";
	private String s = " ";

	private void createNodeTemplate() {
		addTemplateNode(new DidlNode("artist", "upnp:artist", kNsUpnp));
		addTemplateNode(new DidlNode("albumArtist", "upnp:albumArtist", kNsUpnp));
		addTemplateNode(new DidlNode("composer", "upnp:composer", kNsUpnp));
		addTemplateNode(new DidlNode("conductor", "upnp:conductor", kNsUpnp));
		addTemplateNode(new DidlNode("narrator", "upnp:narrator", kNsUpnp));
		addTemplateNode(new DidlNode("performer", "upnp:performer", kNsUpnp));
		addTemplateNode(new DidlNode("genre", "upnp:genre", kNsUpnp));
		addTemplateNode(new DidlNode("albumGenre", "upnp:genre", kNsUpnp));
		
		addTemplateNode(new DidlNode("author", "dc:author", kNsDc));
		addTemplateNode(new DidlNode("title", "dc:title", kNsDc));
		addTemplateNode(new DidlNode("year", "dc:date", kNsDc));
		
		addTemplateNode(new DidlNode( "albumTitle", "upnp:album", kNsUpnp ));
		addTemplateNode(new DidlNode( "albumArtwork", "upnp:albumArtURI", kNsUpnp ));
		
		addTemplateNode(new DidlNode( "provider", "oh:provider", kNsOh ));
		addTemplateNode(new DidlNode( "artwork", "oh:artwork", kNsOh ));
		
		addTemplateNode(new DidlNode( "track", "upnp:originalTrackNumber", kNsUpnp ));
		
		addTemplateNode(new DidlNode("tracks", "oh:originalTrackCount", kNsOh));
		addTemplateNode(new DidlNode("disc", "oh:originalDiscNumber", kNsOh));
		addTemplateNode(new DidlNode("discs", "oh:originalDiscCount", kNsOh));
		addTemplateNode(new DidlNode("work", "oh:work", kNsOh));
		addTemplateNode(new DidlNode("movement", "oh:movement", kNsOh));
		addTemplateNode(new DidlNode("show", "oh:show", kNsOh));
		addTemplateNode(new DidlNode("episode", "oh:episodeNumber", kNsOh));
		addTemplateNode(new DidlNode("episodes", "oh:episodeCount", kNsOh));
		addTemplateNode(new DidlNode("published", "oh:published", kNsOh));
		addTemplateNode(new DidlNode("website", "oh:website", kNsOh));
		addTemplateNode(new DidlNode("location", "oh:location", kNsOh));
		addTemplateNode(new DidlNode("details", "oh:details", kNsOh));
		addTemplateNode(new DidlNode("extensions", "oh:extensions", kNsOh));
		
		addTemplateNode(new DidlNode("publisher", "dc:publisher", kNsDc));
		addTemplateNode(new DidlNode("description", "dc:description", kNsDc));
		
		addTemplateNode(new DidlNode("rating", "upnp:rating", kNsUpnp));

	}

	private void addTemplateNode(DidlNode n) {
		nodeTemplate.put(n.getElementName(), n);
	}

	public String createMetaData(Map<String, String> data) {
		StringBuilder sb = new StringBuilder();
		sb.append(xmlStart);
		sb.append(didlStart);
		sb.append(itemStart);
		//Build the item tag
		sb.append("id=");
		sb.append(dq );
		sb.append(data.get("id"));
		sb.append(dq );
		sb.append(" parentID=");
		sb.append(dq );
		sb.append(data.get("parentID"));
		sb.append(dq );
		sb.append(" restricted=\"0\"");
		sb.append(">");		
		sb.append("<upnp:class xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">object.item.audioItem.musicTrack</upnp:class>");
		//Iterate meta data
		for (String key : data.keySet()) {
			String value = data.get(key);
			String node = getMetaDataNode(key, value);
			if (node != null) {
				sb.append(node);
			}
		}
		
		String res = createResTag(data);
		sb.append(res);
		//create the res node

		

		sb.append(itemEnd);
		sb.append(didlEnd);
		return sb.toString();
	}
	
	
	private String createResTag(Map<String, String> data)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<res ");
		sb.append("duration=");
		String duration = data.get("duration");
		sb.append(dq );
		sb.append(getDuration(duration));
		sb.append(dq );
		sb.append(s);
		sb.append("sampleFrequency=");
		sb.append(dq );
		sb.append(getValue(data,"sampleRate"));
		sb.append(dq );
		sb.append(s);		
		sb.append("nrAudioChannels=");
		sb.append(dq);
		sb.append(getValue(data,"channels"));
		sb.append(dq);
		/*
		sb.append("bitrate=");
		sb.append(dq );
		sb.append(getValue(data,"bitRate"));
		sb.append(dq );
		sb.append(s);
		sb.append(s);
		sb.append("bitsPerSample=");
		sb.append(dq);
		sb.append(getValue(data,"bitDepth"));
		sb.append(dq);
		*/		
		sb.append(">");
		sb.append(getValue(data,"uri"));
		sb.append("</res>");
		return sb.toString();
	}
	
	
	private String getValue(Map<String, String> data, String key) {
		String res = "";
		if(data.containsKey(key))
		{
			res = data.get(key);
		}
		return res;
	}
	
	/***
	 * Convert number of seconds to hh:mm:ss
	 * @param d
	 * @return
	 */
	private String getDuration(String d) {
		String res = "00:00:00";
		if(d == null) {
			return res;
		}
		try
		{
			int sec = Integer.parseInt(d);		
			res = LocalTime.MIN.plusSeconds(sec).toString();
		}
		catch(Exception e) {			
		}		
		return res;
	}

	/***
	 * Create a meta data node from the key and value
	 * @param key
	 * @param value
	 * @return
	 */
	private String getMetaDataNode(String key, String value) {
		DidlNode n = nodeTemplate.get(key);
		if (n != null && value !=null) {
			n.setValue(value);
			return n.getXMLNode();
		}
		return null;
	}
	

}
