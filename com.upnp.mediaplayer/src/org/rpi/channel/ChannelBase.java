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
	
    private static final String ENTRY_START = "<Entry>";
    private static final String ENTRY_END = "</Entry>";
    private static final String ID_START = "<Id>";
    private static final String ID_END = "</Id>";
    private static final String URI_START = "<Uri>";
    private static final String URI_END = "</Uri>";
    private static final String META_START = "<Metadata>";
    private static final String META_END = "</Metadata>";

    private String uri = "";

    private String title = "";
    private String album = "";
    private StringBuffer artist = new StringBuffer();
    private StringBuffer performer = new StringBuffer();
    private StringBuffer composer = new StringBuffer();
    private StringBuffer conductor = new StringBuffer();
    private String disc_number = "";
    private String full_text = "";
    private String date = "";
    private boolean icy_reverse = false;
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

    private void setUri(String uri) {
        this.uri = uri;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getMetaClean() {
        return protectSpecialCharacters(getMetadata());
    }

    private void setMetadata(String metadata) {
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
            // DocumentBuilderFactory factory =
            // DocumentBuilderFactory.newInstance();
            // DocumentBuilder builder = factory.newDocumentBuilder();
            // InputSource insrc = new InputSource(new StringReader(Metadata));
            // Document doc = builder.parse(insrc);
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
                    NamedNodeMap map = n.getAttributes();
                    Node role = map.getNamedItem("role");
                    String role_type = role.getTextContent();
                    if (role.getTextContent().equalsIgnoreCase("AlbumArtist")) {
                        remove = false;
                    }
                    if (role.getTextContent().equalsIgnoreCase("Performer")) {
                        // remove =false;
                    }

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
            Node item = node.getFirstChild();
            NodeList childs = item.getChildNodes();
            for (int i = 0; i < childs.getLength(); i++) {
                Node n = childs.item(i);
                if (n.getNodeName() == "dc:title") {
                    n.setTextContent(full_title);
                    log.info("ICY INFO Replacing dc:title: " + full_title);
                    break;
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
            InputSource insrc = new InputSource(new StringReader(metadata));
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
            Document doc = getDocument();
            Node node = doc.getFirstChild();
            Node item = node.getFirstChild();
            NodeList childs = item.getChildNodes();
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
                            setArtist(n.getTextContent());
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
                        setArtist(n.getTextContent());
                    }
                }
                else if ("dc:date".equals(n.getNodeName())) {
                    setDate(n.getTextContent());
                }
                else if ("upnp:originalDiscNumber".equals(n.getNodeName())) {
                    disc_number = n.getTextContent();
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

    private void setDate(String date) {
        this.date = date;
    }

    public String getDate()
    {
        return date;
    }

    private void setComposer(String composer) {
        this.composer.append(composer);
        this.composer.append(",");
    }

    public String getComposer(){
        String text = composer.toString();
        if(text.endsWith(","))
            text = text.substring(0,text.length()-1);
        return text;
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
        return getPerformer();
    }

    public void setArtist(String artist) {
        this.artist.append(artist.trim());
        this.artist.append(",");
    }

    public String getPerformer() {
        String text = performer.toString();
        if(text.equalsIgnoreCase(""))
        {
            text = artist.toString();
        }
        if(text.endsWith(","))
            text = text.substring(0,text.length()-1);
        return text;
    }

    public void setPerformer(String performer) {
        this.performer.append(performer.trim());
        this.performer.append(",");
    }

    public String getConductor() {
        String text = conductor.toString();
        if(text.endsWith(","))
            text = text.substring(0,text.length()-1);
        return text;
    }

    private void setConductor(String conductor) {
        this.conductor.append(conductor);
        this.conductor.append(",");
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
}
