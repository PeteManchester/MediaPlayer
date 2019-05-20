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


	private String sNsDc = "dc=\"http://purl.org/dc/elements/1.1/\"";
	private String sNsUpnp = "upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\"";
	private String sNsOh = "oh=\"http://www.openhome.org\"";
	
	//Double quotes
	private String dq = "\"";
	//Space
	private String s = " ";

	private void createNodeTemplate() {
		addTemplateNode(new DidlNode("artist", "upnp:artist", sNsUpnp));
		addTemplateNode(new DidlNode("albumArtist", "upnp:albumArtist", sNsUpnp));
		addTemplateNode(new DidlNode("composer", "upnp:composer", sNsUpnp));
		addTemplateNode(new DidlNode("conductor", "upnp:conductor", sNsUpnp));
		addTemplateNode(new DidlNode("narrator", "upnp:narrator", sNsUpnp));
		addTemplateNode(new DidlNode("performer", "upnp:performer", sNsUpnp));
		addTemplateNode(new DidlNode("genre", "upnp:genre", sNsUpnp));
		addTemplateNode(new DidlNode("albumGenre", "upnp:genre", sNsUpnp));
		
		addTemplateNode(new DidlNode("author", "dc:author", sNsDc));
		addTemplateNode(new DidlNode("title", "dc:title", sNsDc));
		addTemplateNode(new DidlNode("year", "dc:date", sNsDc));
		
		addTemplateNode(new DidlNode( "albumTitle", "upnp:album", sNsUpnp ));
		addTemplateNode(new DidlNode( "albumArtwork", "upnp:albumArtURI", sNsUpnp ));
		
		addTemplateNode(new DidlNode( "provider", "oh:provider", sNsOh ));
		addTemplateNode(new DidlNode( "artwork", "oh:artwork", sNsOh ));
		
		addTemplateNode(new DidlNode( "track", "upnp:originalTrackNumber", sNsUpnp ));
		
		addTemplateNode(new DidlNode("tracks", "oh:originalTrackCount", sNsOh));
		addTemplateNode(new DidlNode("disc", "oh:originalDiscNumber", sNsOh));
		addTemplateNode(new DidlNode("discs", "oh:originalDiscCount", sNsOh));
		addTemplateNode(new DidlNode("work", "oh:work", sNsOh));
		addTemplateNode(new DidlNode("movement", "oh:movement", sNsOh));
		addTemplateNode(new DidlNode("show", "oh:show", sNsOh));
		addTemplateNode(new DidlNode("episode", "oh:episodeNumber", sNsOh));
		addTemplateNode(new DidlNode("episodes", "oh:episodeCount", sNsOh));
		addTemplateNode(new DidlNode("published", "oh:published", sNsOh));
		addTemplateNode(new DidlNode("website", "oh:website", sNsOh));
		addTemplateNode(new DidlNode("location", "oh:location", sNsOh));
		addTemplateNode(new DidlNode("details", "oh:details", sNsOh));
		addTemplateNode(new DidlNode("extensions", "oh:extensions", sNsOh));
		
		addTemplateNode(new DidlNode("publisher", "dc:publisher", sNsDc));
		addTemplateNode(new DidlNode("description", "dc:description", sNsDc));
		
		addTemplateNode(new DidlNode("rating", "upnp:rating", sNsUpnp));

	}

	private void addTemplateNode(DidlNode n) {
		nodeTemplate.put(n.getElementName(), n);
	}

	public String createMetaData(Map<String, String> data) {
		StringBuilder sb = new StringBuilder();
		sb.append(createStartOfDidl(data));
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
	
		//Finish it
		sb.append(itemEnd);
		sb.append(didlEnd);
		return sb.toString();
	}
	
	private String createStartOfDidl(Map<String, String> data)
	{
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
