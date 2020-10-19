package org.rpi.plugin.lastfm;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.Utils;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Session;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import de.umass.lastfm.scrobble.ScrobbleResult;
import net.xeoh.plugins.base.annotations.PluginImplementation;

/**
 *
 */
@PluginImplementation
public class LastFmPluginImpl implements LastFmPluginInterface, Observer {

	private final Logger log = Logger.getLogger(this.getClass());

	private static final String lastfm_api_key = "003dc9a812e87f5058f53439dd26038e";
	private static final String lastfm_secret = "5a9a78a8442187172d136a84568309f8";
	// private final String lastfm_api_key = "c9d2dfd3e2de79fa26949c947649d8ef";
	// private final String lastfm_secret = "d663e2e02b4cf92fbd1b7e2bdac3107b";
	private static final String userAgent = "MediaPlayer";

	private final String key = "Ofewtraincrvheg!";

	private String clearTextPassword;

	private String last_scrobbled_title = "";
	private String last_scrobbled_artist = "";
	private String last_scrobbled_album = "";

	private String artist = "";
	private String title = "";

	// private org.rpi.plugin.lastfm.configmodel.LastFMConfigModel configModel =
	// null;

	private static Session session = null;

	private LastFMConfigJSON config = null;

	/**
	 *
	 */
	public LastFmPluginImpl() {
		log.info("LastFM Init LastFmPluginImpl");
		getConfig();
		log.info("LastFM Got Config");
		init();
		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeProductEvents(this);
		log.info("Finished LastFmPluginImpl");
	}

	@Override
	public void update(Observable o, Object e) {
		EventBase base = (EventBase) e;
		log.debug("LastFM Event: " + e);
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
		if (session == null)
			return;
		if (!changedTrack(title, artist))
			return;
		if (title.equalsIgnoreCase("") || artist.equalsIgnoreCase("")) {
			log.debug("One is a blank Title: " + title + " Artist: " + artist);
			return;
		}

		if (last_scrobbled_title.equalsIgnoreCase(title) && last_scrobbled_artist.equalsIgnoreCase(artist)) {
			if (album.equalsIgnoreCase(album)) {
				log.debug("Repeat of Last Scrobble, do not Scrobble. Title: " + title + " Artist: " + artist + " Album: " + album);
				return;
			}
		}

		for (BlackList bl : config.getBlackList()) {
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
		if (!sres.isSuccessful() || sres.isIgnored()) {
			log.debug(sres.toString());
		} else {
			last_scrobbled_title = title;
			last_scrobbled_artist = artist;
			last_scrobbled_album = album;
		}
	}

	/**
	 * initialises this object according the config options read in the
	 * getConfig method.
	 */
	private void init() {
		log.debug("LastFM INIT");
		try {
			Caller.getInstance().setUserAgent(userAgent);

			if (config.getProxyType() != Proxy.Type.DIRECT) {
				SocketAddress sa = new InetSocketAddress(config.getProxyIP(), config.getProxyPort());
				Proxy proxy = new Proxy(config.getProxyType(), sa);
				Caller.getInstance().setProxy(proxy);
			}

			if (Utils.isEmpty(config.getUserName()) || Utils.isEmpty(this.clearTextPassword)) {
				log.error("LastFM User Credentials not supplied");
			} else {
				session = Authenticator.getMobileSession(config.getUserName(), this.clearTextPassword, lastfm_api_key, lastfm_secret);
				log.debug("SessionKey: " + session.getKey());
			}
		} catch (Exception e) {
			log.error("An error occured during initialization of the lastfm connection", e);
		}
		log.debug("End of LastFM INIT");
	}

	/**
	 * Reads the config from the LastFM.xml file
	 */
	private void getConfig() {
		String class_name = this.getClass().getName();
		log.debug("Find Class, ClassName: " + class_name);
		String path = OSManager.getInstance().getFilePath(this.getClass(), false);
		log.debug("Getting LastFM.xml from Directory: " + path);
		config = new LastFMConfigJSON();
		this.clearTextPassword = config.getPassword();
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

}
