package org.rpi.channel;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.rpi.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ChannelBase {
	private Logger log = Logger.getLogger(this.getClass());
	
    protected static final String ENTRY_START = "<Entry>";
    protected static final String ENTRY_END = "</Entry>";
    protected static final String ID_START = "<Id>";
    protected static final String ID_END = "</Id>";
    protected static final String URI_START = "<Uri>";
    protected static final String URI_END = "</Uri>";
    protected static final String META_START = "<Metadata>";
    protected static final String META_END = "</Metadata>";

    private static final String personSeparator = ",";

    private String uri = "";

    private String title = "";
    private String album = "";
    private String artist = "";
    private String albumArtist = "";
    private String performer = "";
    private String composer = "";
    private String conductor = "";
    private String disc_number = "";
    private String trackNumber = "";
    private String full_text = "";
    private String date = "";
    private boolean icy_reverse = false;
    private boolean keep_url = false;
    private String albumArtUri = "";
    private String genre = "";

    private String metadata;

    private int id;
    private String metatext = "";
    private long time = -99;
    private Long duration = new Long(0);
    private String full_details;

    public ChannelBase(String uri, String metadata, int id) {
        // long startTime = System.nanoTime();
    	setUri(uri);
        setMetadata(metadata);
        setId(id);
        full_text = getXmlString();
        
        if (!metadata.equalsIgnoreCase("")) {
            getTrackDetails();
            setFullDetails();
        }
        // long endTime = System.nanoTime();
        // long duration = endTime - startTime;
        // LOG.warn("Time to Add CustomTrack: " + duration);
    }

    public String getUniqueId() {
        return "PL:" + id;
    }

    private String getXmlString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ENTRY_START);
        sb.append(ID_START);
        sb.append(getId());
        sb.append(ID_END);
        sb.append(URI_START);
        sb.append(protectSpecialCharacters(getUri()));
        sb.append(URI_END);
        sb.append(META_START);
        sb.append(protectSpecialCharacters(getMetadata()));
        sb.append(META_END);
        sb.append(ENTRY_END);
        return sb.toString();
    }

    public String getFullText() {
        return full_text;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMetadata() {
    	
        return metadata;
    }

    public String getMetaClean() {
        return protectSpecialCharacters(getMetadata());
    }

    private void setMetadata(String metadata) {
    	String original = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">";
    	String replace = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\">";
    	
    	//String test = "<DIDL-Lite xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dlna=\"urn:schemas-dlna-org:metadata-1-0/\"><item id=\"d86410-co23709\" parentID=\"co23709\" restricted=\"0\"><dc:title>  6   Heart and Soul</dc:title><dc:creator>Joy Division</dc:creator><dc:date>1980-01-01T00:00:00Z</dc:date><upnp:artist role=\"AlbumArtist\">Joy Division</upnp:artist><upnp:artist role=\"Composer\">Bernard Albrecht</upnp:artist><upnp:artist role=\"Composer\">Ian Curtis</upnp:artist><upnp:artist role=\"Composer\">Peter Hook</upnp:artist><upnp:artist role=\"Composer\">Stephen Morris</upnp:artist><upnp:artist role=\"Performer\">Joy Division</upnp:artist><upnp:album>Closer</upnp:album><upnp:genre>Pop/Rock</upnp:genre><upnp:albumArtURI dlna:profileID=\"JPEG_TN\">http://192.168.1.205:26125/aa/138547/1659915372/cover.jpg?size=0</upnp:albumArtURI><upnp:originalTrackNumber>6</upnp:originalTrackNumber><res duration=\"0:05:51.000\" size=\"37025157\" bitrate=\"176400\" bitsPerSample=\"16\" sampleFrequency=\"44100\" nrAudioChannels=\"2\" protocolInfo=\"http-get:*:audio/x-flac:DLNA.ORG_PN=;DLNA.ORG_OP=01\">http://192.168.1.205:26125/content/c2/b16/f44100/d86410-co23709.flac</res><res duration=\"0:05:51.000\" size=\"62010464\" bitrate=\"176400\" bitsPerSample=\"16\" sampleFrequency=\"44100\" nrAudioChannels=\"2\" protocolInfo=\"http-get:*:audio/wav:DLNA.ORG_PN=WAV;DLNA.ORG_OP=01\">http://192.168.1.205:26125/content/c2/b16/f44100/d86410-co23709.forced.wav</res><res duration=\"0:05:51.000\" size=\"62010420\" bitrate=\"176400\" bitsPerSample=\"16\" sampleFrequency=\"44100\" nrAudioChannels=\"2\" protocolInfo=\"http-get:*:audio/L16:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01;DLNA.ORG_CI=1\">http://192.168.1.205:26125/content/c2/b16/f44100/d86410-co23709.forced.l16</res><res duration=\"0:05:51.000\" size=\"37025157\" bitrate=\"176400\" bitsPerSample=\"16\" sampleFrequency=\"44100\" nrAudioChannels=\"2\" protocolInfo=\"http-get:*:audio/x-flac:DLNA.ORG_PN=;DLNA.ORG_OP=01\">http://192.168.1.205:26125/content/c2/b16/f44100/d86410-co23709.flac</res><upnp:class>object.item.audioItem.musicTrack</upnp:class></item></DIDL-Lite>";
        //metadata = metadata.replace(original, replace);
    	String rItem = "<item id=\"d5112915403826242371-co533\" parentID=\"co533\" restricted=\"0\">";
    	String oItem = "<item>";
    	metadata = metadata.replace(oItem, rItem);
    	this.metadata = metadata;
    }

    public int getId() {
        return id;
    }

    private void setId(int id) {
        this.id = id;
    }

    public String tidyMetaData() {

        try {
            Vector<Node> removeNodes = new Vector<Node>();
            StringBuilder temp = new StringBuilder();
            Document doc = getDocument();
            Node node = doc.getFirstChild();
            Node item = node.getFirstChild();
            NodeList childs = item.getChildNodes();
            temp.append("<item>");
            for (int i = 0; i < childs.getLength(); i++) {
                Node n = childs.item(i);
                boolean remove = true;
                if (n.getNodeName() == "dc:title") {
                    remove = false;
                }
                else if (n.getNodeName() == "upnp:album") {
                    remove = false;
                }
                else if (n.getNodeName() == "upnp:artist") {
                    remove = false;
                }
                else if (n.getNodeName() == "upnp:class") {
                    remove = false;
                }
                else if (n.getNodeName() == "upnp:albumArtURI") {
                    remove = false;
                }
                else if ("upnp:genre".equals(n.getNodeName())) {
                    remove = false;
                }
                else if ("upnp:originalTrackNumber".equals(n.getNodeName())) {
                    remove = false;
                }
                else if ("upnp:originalDiscNumber".equals(n.getNodeName())) {
                    remove = false;
                }
                else if ("res".equals(n.getNodeName())) {
                    remove = false;
                }

                if (remove) {
                    removeNodes.add(n);
                }
            }
            for (Node n : removeNodes) {
                item.removeChild(n);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (Exception e) {
            log.error("Erorr TidyMetaData", e);
        }

        return "";
    }

    public String updateTrack(String title, String artist) {
        try {
            String full_title = title + " - " + artist;
            full_title = tidyUpString(full_title);
            Document doc = getDocument();
            Node node = doc.getFirstChild();
            NodeList childs = null;
            for(int i = 0; i < node.getChildNodes().getLength(); i++ )
            {
            	Node test = node.getChildNodes().item(i);
            	if(test.getNodeName().equalsIgnoreCase("ITEM") )
            	{
            		childs = test.getChildNodes();
            		break;
            	}
            }
            for (int i = 0; i < childs.getLength(); i++) {
                Node n = childs.item(i);
                if (n.getNodeName() == "dc:title") {
                    n.setTextContent(full_title);
                    log.info("ICY INFO Replacing dc:title: " + full_title);
                }else if(n.getNodeName() == "upnp:artist")
                {
                   n.setTextContent(artist);
                   log.info("ICY INFO Replacing dc:artist: " + artist);
                }
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            metatext = result.getWriter().toString();
            return metatext;
        } catch (Exception e) {
            log.error("Error Creating XML Doc", e);
        }
        return null;
    }

    private String tidyUpString(String s) throws Exception {
        String string = "";
        if (s.equals(s.toUpperCase())) {
            // s = s.toLowerCase();
            String[] splits = s.split(" ");
            for (String word : splits) {
                try {
                    word = word.replace(word.substring(1), word.substring(1).toLowerCase());
                } catch (Exception e) {
                    log.debug("Error with Word: " + word);
                }
                string += word + " ";
            }

        }
        if (string.equals("")) {
            return s;
        }
        return string.trim();
    }

    private String protectSpecialCharacters(String originalUnprotectedString) {
        // String test = StringEscapeUtils.escapeXml(originalUnprotectedString);
        if (originalUnprotectedString == null) {
            return null;
        }
        boolean anyCharactersProtected = false;

        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < originalUnprotectedString.length(); i++) {
            char ch = originalUnprotectedString.charAt(i);

            boolean controlCharacter = ch < 32;
            boolean unicodeButNotAscii = ch > 126;
            boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

            if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
                stringBuffer.append("&#" + (int) ch + ";");
                anyCharactersProtected = true;
            } else {
                stringBuffer.append(ch);
            }
        }
        if (anyCharactersProtected == false) {
            return originalUnprotectedString;
        }
        return stringBuffer.toString();
    }

    @Override
    public String toString() {
        String res = "Track Id: " + id + " + URI: " + uri + "  MetaData:\r\n " + getMetadata();
        return res;
    }

    private Document getDocument() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource insrc = new InputSource(new StringReader(metadata.trim()));
            return builder.parse(insrc);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Using XPath Queries, which are slower
     */
    public void getTrackDetailsNew() {
        try {
            Document doc = getDocument();
            String ex_title = "DIDL-Lite/item/title";
            XPath xPath = XPathFactory.newInstance().newXPath();
            String title = xPath.compile(ex_title).evaluate(doc);
            setTitle(title);
            String exAlbum = "DIDL-Lite/item/album";
            String album = xPath.compile(exAlbum).evaluate(doc);
            setAlbum(album);
            String ex_Performer = "DIDL-Lite/item/artist[@role='Performer']";
            String performer = xPath.compile(ex_Performer).evaluate(doc);
            setPerformer(performer);
            String ex_artist = "DIDL-Lite/item/artist[@role='AlbumArtist']";
            String artist = xPath.compile(ex_artist).evaluate(doc);
            setArtist(artist);
        } catch (Exception e) {
            log.error("Erorr TidyMetaData", e);
        }
    }

    public void getTrackDetails() {
        try {
        	log.debug(metadata);
            Document doc = getDocument();//
            Node node = doc.getFirstChild();
            NodeList childs = null;
            for(int i = 0; i < node.getChildNodes().getLength(); i++ )
            {
            	Node test = node.getChildNodes().item(i);
            	if(test.getNodeName().equalsIgnoreCase("ITEM") )
            	{
            		childs = test.getChildNodes();
            		break;
            	}
            }
            
           
            for (int i = 0; i < childs.getLength(); i++) {
                Node n = childs.item(i);
                if ("dc:title".equals(n.getNodeName())) {
                    setTitle(n.getTextContent());
                }
                else if ("upnp:album".equals(n.getNodeName())) {
                    setAlbum(n.getTextContent());
                }
                else if ("upnp:artist".equals(n.getNodeName())) {
                    NamedNodeMap map = n.getAttributes();
                    Node role = map.getNamedItem("role");
                    if (role != null) {
                        String role_type = role.getTextContent();
                        if (role_type.equalsIgnoreCase("AlbumArtist")) {
                            setAlbumArtist(n.getTextContent());
                        }
                        else if (role_type.equalsIgnoreCase("Performer")) {
                            setPerformer(n.getTextContent());
                        }
                        else if(role_type.equalsIgnoreCase("Composer")) {
                            setComposer(n.getTextContent());
                        }
                        else if(role_type.equalsIgnoreCase("Conductor")) {
                            setConductor(n.getTextContent());
                        }
                    }
                    else {
                    	if(this.artist.length()==0)
                    	{
                    		setArtist(n.getTextContent());
                    	}
                    }
                }
                else if ("dc:date".equals(n.getNodeName())) {
                    setDate(n.getTextContent());
                }
                else if ("upnp:originalDiscNumber".equals(n.getNodeName())) {
                    this.setDiscNumber(n.getTextContent());
                }
                else if ("upnp:originalTrackNumber".equals(n.getNodeName())) {
                    this.setTrackNumber(n.getTextContent());
                }
                else if ("upnp:albumArtURI".equals(n.getNodeName())) {
                    setAlbumArtUri(n.getTextContent());
                }
                else if ("upnp:genre".equals(n.getNodeName())) {
                    setGenre(n.getTextContent());
                }
                else if ("res".equals(n.getNodeName())) {
                	Node node_duration = n.getAttributes().getNamedItem("duration");
                	if(node_duration !=null)
                	{
	                    String duration = node_duration.getTextContent();
	                    if(duration!=null)
	                    {
		                    Long period = Utils.parseDurationString(duration);
		                    setDuration(period);
	                    }
                	}
                }
            }
        } catch (Exception e) {
            log.error("Error GetTrackDetails", e);
        }
    }
    
    private void getTrackDetailsKazoo()
    {
    	
    }

    private void setDate(String date) {
        this.date = date;
    }

    public String getDate()
    {
    	if(date==null)
    	{
    		return "";
    	}
        return date;
    }

    public void setMetaText(String metatext) {
        this.metatext = metatext;

    }

    public String getMetaText() {
        return metatext;
    }

    public void setTime(long duration) {
        this.time = duration;
    }

    public long getTime() {
        return time;
    }

    public String getTitle() {
        String mTitle = title.trim();
        if(disc_number!=null)
        {
            if(!disc_number.equalsIgnoreCase(""))
            {
                if(mTitle.startsWith(disc_number))
                {
                    mTitle = mTitle.substring(disc_number.length());
                    if(mTitle.trim().startsWith("."))
                    {
                        mTitle = mTitle.substring(1);
                    }
                }
            }
        }
        return mTitle.trim();
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album.trim();
    }

    public String getArtist() {
    	if(!artist.equalsIgnoreCase(""))
    	{
    		return this.cleanArtistString(this.artist);
    	}
    	return getAlbumArtist();    		
    }

    public void setArtist(String artist) {
        this.artist = this.addString(this.artist, artist);
    }

    public String getComposer(){
        return this.cleanArtistString(this.composer);
    }

    private void setComposer(String composer) {
        this.composer = this.addString(this.composer, composer);
    }

    public String getPerformer() {
    	String value = this.performer;
    	if(value==null || value.equalsIgnoreCase(""))
    	{
    		value = this.artist;
    	}
        return this.cleanArtistString(value);
    }

    public void setPerformer(String performer) {
        this.performer = this.addString(this.performer, performer);
    }

    public String getConductor() {
        return this.cleanArtistString(this.conductor);
    }

    private void setConductor(String conductor) {
        this.conductor = this.addString(this.conductor, conductor);
    }

    public String getAlbumArtist() {
        return this.cleanArtistString(this.albumArtist);
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = this.addString(this.albumArtist, albumArtist);
    }

    private String cleanArtistString(String person) {
        String text = person;
        if(text.endsWith(personSeparator))
            text = text.substring(0, text.length() - personSeparator.length());
        return text;
    }

    private String addString(String original, String addString) {
    	if(cleanArtistString(original).equalsIgnoreCase(addString))
    	{
    		return original;
    	}
        return original + addString.trim() + personSeparator;
    }

    public String getDiscNumber() {
        return disc_number;
    }

    public void setDiscNumber(String disc_number) {
        this.disc_number = disc_number;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    private void setFullDetails() {
        StringBuffer sb = new StringBuffer();
        sb.append(getTitle());
        sb.append(" - ");
        if (!getPerformer().equalsIgnoreCase("")) {
            sb.append(getPerformer());
            sb.append(" - ");
        }
        if (!getPerformer().equalsIgnoreCase(getArtist())) {
            sb.append(getArtist());
            sb.append(" - ");
        }
        sb.append(getAlbum());
        full_details = sb.toString();
        if (full_details.endsWith(" - ")) {
            full_details = full_details.substring(0, full_details.length() - 3);
            full_details.trim();
        }
    }

    public String getFullDetails() {
        return full_details;
    }

    public boolean isICYReverse() {
        return icy_reverse;
    }

    public void setICYReverse(boolean icy_reverse) {
        this.icy_reverse = icy_reverse;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

	/**
	 * @return the keep_url
	 */
	public boolean isKeepURL() {
		return keep_url;
	}

	/**
	 * @param keep_url the keep_url to set
	 */
	public void setKeepURL(boolean keep_url) {
		this.keep_url = keep_url;
	}

}
