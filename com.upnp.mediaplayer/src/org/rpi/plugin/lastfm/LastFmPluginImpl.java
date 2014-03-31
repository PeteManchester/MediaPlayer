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
import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.plugin.lastfm.configmodel.LastFMConfigModel;
import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

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

    private static final Logger LOGGER = Logger.getLogger(LastFmPluginImpl.class);

    private static final String lastfm_api_key = "003dc9a812e87f5058f53439dd26038e";
    private static final String lastfm_secret = "5a9a78a8442187172d136a84568309f8";
    private static final String userAgent = "MediaPlayer";

    private final String key = "Ofewtraincrvheg!";

    private String clearTextPassword;

    private String last_scrobbled_title = "";
    private String last_scrobbled_artist = "";
    private String last_scrobbled_album = "";

    private String artist = "";
    private String title = "";

    private org.rpi.plugin.lastfm.configmodel.LastFMConfigModel configModel = null;

    private static Session session =null;

    /**
     *
     */
    public LastFmPluginImpl() {
        LOGGER.info("Init LastFmPluginImpl");
        getConfig();
        init();
        PlayManager.getInstance().observeInfoEvents(this);
        PlayManager.getInstance().observeProductEvents(this);
        LOGGER.info("Finished LastFmPluginImpl");
    }

    @Override
    public void update(Observable o, Object e) {
        EventBase base = (EventBase) e;
        switch (base.getType()) {
            case EVENTTRACKCHANGED:
                EventTrackChanged etc = (EventTrackChanged) e;
                ChannelBase track = etc.getTrack();
                if (track != null) {
                    LOGGER.debug("scrobble track with performer: " + track.getPerformer());
                    scrobble(track.getTitle(), track.getPerformer(), track.getAlbum());
                } else {
                    LOGGER.debug("Track was NULL");
                }

                break;
            case EVENTUPDATETRACKMETATEXT:
                EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;

                if (et != null) {
                    LOGGER.debug("scrobble metatext with artist: " + et.getArtist());
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
        if(session == null)
            return;
        if (!changedTrack(title, artist))
            return;
        if (title.equalsIgnoreCase("") || artist.equalsIgnoreCase("")) {
            LOGGER.debug("One is a blank Title: " + title + " Artist: " + artist);
            return;
        }  
        
        if(last_scrobbled_title.equalsIgnoreCase(title) && last_scrobbled_artist.equalsIgnoreCase(artist))
        {
        	if(album.equalsIgnoreCase(album))
        	{
        		LOGGER.debug("Repeat of Last Scrobble, do not Scrobble. Title: " + title + " Artist: " + artist + " Album: " + album );
        		return;
        	}
        }
        

        for (org.rpi.plugin.lastfm.configmodel.BlackList bl : this.configModel.getBlackList()) {
            if (bl.matches(artist, title)) {
                LOGGER.debug("BlackList Found Title: " + title + " : Artist: " + artist + "Rule: " + bl.toString());
                return;
            }
        }

        LOGGER.debug("TrackChanged: " + title + " : " + artist + " Album: " + album);
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
            LOGGER.debug(sres.toString());
        }
        else
        {
        	last_scrobbled_title = title;
        	last_scrobbled_artist = artist;
        	last_scrobbled_album = album;
        }
    }

    /**
     * initializes this object according the config options read in the getConfig method.
     */
    private void init() {
        LOGGER.debug("INIT");
        try {
            Caller.getInstance().setUserAgent(userAgent);

            org.rpi.plugin.lastfm.configmodel.LastFMConfig config = configModel.getConfig();

            if (config.getProxyType() != Proxy.Type.DIRECT) {
                SocketAddress sa = new InetSocketAddress(config.getProxyIP(), config.getProxyPort());
                Proxy proxy = new Proxy(config.getProxyType(), sa);
                Caller.getInstance().setProxy(proxy);
            }

            if (Utils.isEmpty(config.getUserName()) || Utils.isEmpty(config.getPassword())) {
                LOGGER.error("LastFM User Credentials not supplied");
            } else {
                session = Authenticator.getMobileSession(config.getUserName(), config.getPassword(), lastfm_api_key, lastfm_secret);
                LOGGER.debug("SessionKey: " + session.getKey());
            }
        } catch (Exception e) {
          	LOGGER.error("An error occured during initialization of the lastfm connection", e);
        }
        LOGGER.debug("End of INIT");
    }

    /**
     * Reads the config from the LastFM.xml file
     */
    private void getConfig() {
        String class_name = this.getClass().getName();
        LOGGER.debug("Find Class, ClassName: " + class_name);
        String path = OSManager.getInstance().getFilePath(this.getClass(), false);
        LOGGER.debug("Getting LastFM.xml from Directory: " + path);
        File file = new File(path + "LastFM.xml");

        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(LastFMConfigModel.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            configModel = (LastFMConfigModel) unmarshaller.unmarshal(file);
        }
        catch (JAXBException e) {
            LOGGER.error("Cannot unmarshall (read) config file");
        }

        boolean passwordEncrypted = false;
        if (!Utils.isEmpty(configModel.getConfig().getPassword())) {
            if (!configModel.getConfig().getPassword().startsWith("ENC:")) {
                this.clearTextPassword = configModel.getConfig().getPassword();

                String encryptedPassword = SecUtils.encrypt(key, clearTextPassword);

                configModel.getConfig().setPassword("ENC:" + encryptedPassword);
                passwordEncrypted = true;
            }
            else {
                String encryptedPassword = configModel.getConfig().getPassword().substring(4);
                this.clearTextPassword = SecUtils.decrypt(key, encryptedPassword);
            }
        }

        if (passwordEncrypted && context != null) {
            LOGGER.debug("update LastFM config with encrypted password");
            try {
                Marshaller marshaller = context.createMarshaller();
                marshaller.marshal(configModel, file);
            } catch (JAXBException e) {
                LOGGER.error("Cannot marshall (write) config file");
            }
        }
    }

    /**
     *
     * @param title
     * @param artist
     * @return
     */
    private boolean changedTrack(String title, String artist) {
        if (this.title.equalsIgnoreCase(title) && this.artist.equalsIgnoreCase(artist)) {
            LOGGER.debug("Track didn't Change: " + title + " : " + artist);
            return false;
        }

        this.title = title;
        this.artist = artist;
        return true;
    }

}
