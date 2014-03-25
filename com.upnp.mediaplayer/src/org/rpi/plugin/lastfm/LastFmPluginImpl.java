package org.rpi.plugin.lastfm;

/***
 * LastFM Scrobbler Plugin
 * Written by Markus M May
 * Contribution by Pete Hoyle
 * Feb 2014
 */

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.Utils;
import org.rpi.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;

/**
 *
 */
@PluginImplementation
public class LastFmPluginImpl implements LastFmPluginInterface, Observer {

    private static Logger log = Logger.getLogger(LastFmPluginImpl.class);

    private static final String lastfm_api_key = "003dc9a812e87f5058f53439dd26038e";
    private static final String lastfm_secret = "5a9a78a8442187172d136a84568309f8";
    private static final String userAgent = "MediaPlayer";

    private String lastfm_username = null;
    private String lastfm_password = null;
    private String key = "Ofewtraincrvheg!";

    private Boolean lastfm_debugmode = false;

    private Proxy.Type lastfm_proxymode = Proxy.Type.DIRECT;
    private String lastfm_proxy_ip = null;
    private Integer lastfm_proxy_port = null;
    
    private String title = "";
    private String artist = "";
    private String last_scrobbled_title = "";
    private String last_scrobbled_artist = "";
    private String last_scrobbled_album = "";
    
    private List<BlackList> blackList = new ArrayList<BlackList>();

    private static Session session =null;

    /**
     *
     */
    public LastFmPluginImpl() {
        log.info("Init LastFmPluginImpl");
        getConfig();
        init();
        PlayManager.getInstance().observeInfoEvents(this);
        PlayManager.getInstance().observeProductEvents(this);
        log.info("Finished LastFmPluginImpl");
    }

    @Override
    public void update(Observable o, Object e) {
        EventBase base = (EventBase) e;
        switch (base.getType()) {
            case EVENTTRACKCHANGED:
                EventTrackChanged etc = (EventTrackChanged) e;
                ChannelBase track = etc.getTrack();
                if (track != null) {
                    log.debug("scrobble track with performer: " + track.getPerformer());
                    scrobble(track.getTitle(), track.getPerformer(), track.getAlbum());
                } else {
                    log.debug("Track was NULL");
                }

                break;
            case EVENTUPDATETRACKMETATEXT:
                EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;

                if (et != null) {
                    log.debug("scrobble metatext with artist: " + et.getArtist());
                    scrobble(et.getTitle(), et.getArtist());
                }
                break;
        }
    }

    /**
     *
     * @param title
     * @param artist
     */
    private void scrobble(String title, String artist) {
        scrobble(title, artist, "");
    }

    /**
     *
     * @param title
     * @param artist
     * @param album
     */
    private void scrobble(String title, String artist, String album) {
        if(session ==null)
            return;
        if (!changedTrack(title, artist))
            return;
        if (title.equalsIgnoreCase("") || artist.equalsIgnoreCase("")) {
            log.debug("One is a blank Title: " + title + " Artist: " + artist);
            return;
        }  
        
        if(last_scrobbled_title.equalsIgnoreCase(title) && last_scrobbled_artist.equalsIgnoreCase(artist))
        {
        	if(album.equalsIgnoreCase(album))
        	{
        		log.debug("Repeat of Last Scrobble, do not Scrobble. Title: " + title + " Artist: " + artist + " Album: " + album );
        		return;
        	}
        }
        

        for (BlackList bl : blackList) {
            if (bl.matches(artist, title)) {
                log.debug("BlackList Found Title: " + title + " : Artist: " + artist + "Rule: " + bl.toString());
                return;
            }
        }

        log.debug("TrackChanged: " + title + " : " + artist + " Album: " + album);
        int now = (int) (System.currentTimeMillis() / 1000);
        ScrobbleData data = new ScrobbleData();
        data.setTimestamp(now);
        data.setArtist(artist);
        data.setTrack(title);
        if (!Utils.isEmpty(album)) {
            data.setAlbum(album);
        }
        ScrobbleResult sres = Track.scrobble(data, session);
        if (!sres.isSuccessful()|| sres.isIgnored())
        {
            log.debug(sres.toString());
        }
        else
        {
        	last_scrobbled_title = title;
        	last_scrobbled_artist = artist;
        	last_scrobbled_album = album;
        }
    }

    /**
     *
     */
    private void init() {
        try {
            log.debug("INIT");
            Caller.getInstance().setUserAgent(userAgent);

            if (lastfm_proxymode != Proxy.Type.DIRECT) {
                SocketAddress sa = new InetSocketAddress(lastfm_proxy_ip, lastfm_proxy_port);
                Proxy proxy = new Proxy(lastfm_proxymode, sa);
                Caller.getInstance().setProxy(proxy);
            }

            if (lastfm_username.equalsIgnoreCase("") || lastfm_password == null || lastfm_password.equalsIgnoreCase("")) {
                log.error("LastFM User Credentials not supplied");
            } else {
                session = Authenticator.getMobileSession(lastfm_username, lastfm_password, lastfm_api_key, lastfm_secret);
                log.debug("SessionKey: " + session.getKey());
            }
        } catch (Exception e) {
          	log.error("An error occured during initialization of the lastfm connection", e);
        }
        log.debug("End of INIT");
    }

    /**
     *
     */
    private void getConfig() {
        try {
            String class_name = this.getClass().getName();
            log.debug("Find Class, ClassName: " + class_name);
            String path = OSManager.getInstance().getFilePath(this.getClass(), false);
            log.debug("Getting LastFM.xml from Directory: " + path);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            File file = new File(path + "LastFM.xml");
            Document doc = builder.parse(file);
            NodeList listOfConfig = doc.getElementsByTagName("Config");
            int i = 1;
            String encrypted_password = "";
            for (int s = 0; s < listOfConfig.getLength(); s++) {
                Node config = listOfConfig.item(s);
                if (config.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) config;
                    lastfm_username = XMLUtils.getStringFromElement(element, "UserName");
                    String password = "";
                    password = XMLUtils.getStringFromElement(element, "Password");
                    if (!password.equalsIgnoreCase("")) {
                        encrypted_password = encrypt(key, password);
                        lastfm_password = password;
                    } else {
                        String enc_password = XMLUtils.getStringFromElement(element, "Password_ENC");
                        if (!enc_password.equalsIgnoreCase("")) {
                            lastfm_password = decrypt(key, enc_password);
                        }
                    }
                    String proxymode = XMLUtils.getStringFromElement(element, "ProxyType", "DIRECT");
                    lastfm_proxymode = Proxy.Type.valueOf(proxymode);
                    lastfm_proxy_ip = XMLUtils.getStringFromElement(element, "Proxy_IP");
                    String proxy_port = XMLUtils.getStringFromElement(element, "Proxy_Port", "-1");
                    lastfm_proxy_port = Integer.parseInt(proxy_port);
                }
            }

            NodeList listOfBlackList = doc.getElementsByTagName("BlackListItem");
            for (int s = 0; s < listOfBlackList.getLength(); s++) {
                Node bl = listOfBlackList.item(s);
                if (bl.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) bl;
                    String artist = XMLUtils.getStringFromElement(element, "artist", "");
                    String title = XMLUtils.getStringFromElement(element, "title", "");
                    BlackList bli = new BlackList();
                    bli.setArtist(artist);
                    bli.setTitle(title);
                    log.debug("Adding BlackList: " + bli.toString());
                    blackList.add(bli);
                }
            }

            if (!encrypted_password.equalsIgnoreCase("")) {
                updateMyXML(doc, "LastFM/Config/Password", "");
                updateMyXML(doc, "LastFM/Config/Password_ENC", encrypted_password);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.transform(new DOMSource(doc), new StreamResult(file));

            }

        } catch (Exception e) {
            log.error("Error Reading LastFM.xml", e);
        }
    }

    /**
     *
     * @param doc
     * @param path
     * @param def
     */
    public void updateMyXML(Document doc, String path, String def) {
        String p[] = path.split("/");
        // search nodes or create them if they do not exist
        Node n = doc;
        for (int i = 0; i < p.length; i++) {
            NodeList kids = n.getChildNodes();
            Node nfound = null;
            for (int j = 0; j < kids.getLength(); j++)
                if (kids.item(j).getNodeName().equals(p[i])) {
                    nfound = kids.item(j);
                    break;
                }
            if (nfound == null) {
                nfound = doc.createElement(p[i]);
                n.appendChild(nfound);
                n.appendChild(doc.createTextNode("\n"));
            }
            n = nfound;
        }
        NodeList kids = n.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++)
            if (kids.item(i).getNodeType() == Node.TEXT_NODE) {
                // text node exists
                kids.item(i).setNodeValue(def); // override
                return;
            }
        n.appendChild(doc.createTextNode(def));
    }

    /**
     *
     * @param title
     * @param artist
     * @return
     */
    private boolean changedTrack(String title, String artist) {
        if (this.title.equalsIgnoreCase(title) && this.artist.equalsIgnoreCase(artist)) {
            log.debug("Track didn't Change: " + title + " : " + artist);
            return false;
        }

        this.title = title;
        this.artist = artist;
        return true;
    }

    // Simple attempt to encode the password...
    private  String encrypt(String key, String value) {
        try {
            byte[] raw = key.getBytes(Charset.forName("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encode(encrypted);
        } catch (Exception ex) {
            log.error("Error encrypt: " ,ex);
        }
        return null;
    }

    private  String decrypt(String key, String encrypted) {
        try {
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            byte[] original = cipher.doFinal(Base64.decode(encrypted));

            return new String(original);
        } catch (Exception ex) {
            log.error("Error decrypt: " ,ex);
        }
        return null;
    }
}
