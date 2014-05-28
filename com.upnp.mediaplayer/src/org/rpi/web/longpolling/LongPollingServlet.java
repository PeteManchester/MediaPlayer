
package org.rpi.web.longpolling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.htmlparser.jericho.Element;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;

public class LongPollingServlet extends HttpServlet implements Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private static final long serialVersionUID = 1L;

	private String contextPath = null;

	private String title = "";
	private String artist = "";
	private String last_title = "";
	private String last_artist = "";
	private String lyrics = "";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ServletContext context = config.getServletContext();
		contextPath = context.getContextPath() + "/long_polling";

		CometEngine engine = CometEngine.getEngine();
		CometContext cometContext = engine.register(contextPath);
		cometContext.setExpirationDelay(5 * 30 * 1000);
		PlayManager.getInstance().observeInfoEvents(this);
		ChannelBase track = PlayManager.getInstance().getCurrentTrack();
		if(track !=null)
		{
			title = track.getTitle();
			artist = track.getPerformer();
			checkLyrics();
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		CometEngine engine = CometEngine.getEngine();
		CometContext<HttpServletResponse> context = engine.getCometContext(contextPath);
		final int hash = context.addCometHandler(new LyricHandler(res));
		checkLyrics();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		CometContext<HttpServletResponse> context = CometEngine.getEngine().getCometContext(contextPath);
		context.notify(null);
		PrintWriter writer = res.getWriter();
		checkLyrics();
		writer.write(lyrics);
		writer.flush();
	}

	@Override
	public void update(Observable o, Object e) {
		CometEngine engine = CometEngine.getEngine();
		CometContext<HttpServletResponse> contexts = engine.getCometContext(contextPath);
		int count = contexts.getCometHandlers().size();
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged etc = (EventTrackChanged) e;
			ChannelBase track = etc.getTrack();
			if (track != null) {
				try {
					setTitle(track.getTitle());
					setArtist(track.getPerformer());
					checkLyrics();
				} catch (Exception ex) {

				}
			} else {

			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			try {
				setTitle(et.getTitle());
				setArtist(et.getArtist());
				checkLyrics();
			} catch (Exception ex) {

			}
			break;
		}

	}

	/**
	 * @return the title
	 */
	private String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	private void setTitle(String title) {
		this.title = title;

	}

	/**
	 * @return the artist
	 */
	private String getArtist() {
		return artist;
	}

	/**
	 * @param artist
	 *            the artist to set
	 */
	private void setArtist(String artist) {
		this.artist = artist;
		checkLyrics();
	}

	/**
	 * Determine if we should check for new lyrics
	 */
	private void checkLyrics() {
		CometContext<HttpServletResponse> context = CometEngine.getEngine().getCometContext(contextPath);

		if (artist.equalsIgnoreCase("") || title.equalsIgnoreCase("")) {
			last_artist = artist;
			last_title = title;
			return;
		}
		if (title.equalsIgnoreCase(last_title) && artist.equalsIgnoreCase(last_artist) && !lyrics.equalsIgnoreCase("")) {
			last_artist = artist;
			last_title = title;
			return;
		}
		
		if(context.getCometHandlers().size()==0)
		{
			last_artist = artist;
			last_title = title;
			return;
		}

		TrackInfo info = getLyrics(artist, title, false);
		if (info != null) {
			lyrics = info.getJSON();
		} 

		try {
			context.addAttribute("Test", lyrics);
			context.notify(null);
		} catch (Exception e) {
			log.error("Error Contact Notify", e);
		}

		last_artist = artist;
		last_title = title;
	}

	private TrackInfo getLyrics(String artist, String song, boolean bGetArtistInfo) {
		TrackInfo info = new TrackInfo(artist, song);
		info.setLyrics("Unable to find Lyrics");
		try {
			String mArtist = artist;
			String mSong = song;
			String first_part = "http://lyrics.wikia.com/";
			String sURL = first_part + mArtist + ":" + mSong;
			if (mArtist.equalsIgnoreCase("") || mSong.equalsIgnoreCase("")) {

				return info;
			}
			sURL = sURL.replace(" ", "_");
			log.debug(sURL);
			// Try Artist, Song first
			String res = makeHTTPQuery(sURL);

			if (res == null) {
				mArtist = artist;
				mSong = song;
				if (song.contains(".")) {
					String[] splits = song.split("\\.");
					if (splits.length == 2) {
						try {
							Integer.parseInt(splits[0]);
							sURL = first_part + artist.trim() + ":" + splits[1].trim();
							sURL = sURL.replace(" ", "_");
							res = makeHTTPQuery(sURL);
						} catch (Exception ep) {
							log.debug("Could Not Find Lyrics: " + sURL);
						}
					}
				}
			}
			info.setLyrics(res);
			// log.debug(res);
			if (bGetArtistInfo) {
				// ArtistInfo artist_info = GetArtistInfo(mArtist);
				// info.setArtistInfo(artist_info);
			}
			return info;
		} catch (Exception e) {
			log.error(e);
		} finally {
			// in.close();
		}
		return info;
	}

	/**
	 * Make an http request to the the lyrics
	 * 
	 * @param sURL
	 * @return
	 */
	private String makeHTTPQuery(String sURL) {
		try {
			URL url = new URL(sURL);
			URLConnection connection = url.openConnection();
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String lyrics = "";
				net.htmlparser.jericho.Source source = new net.htmlparser.jericho.Source(in);
				List<Element> elements = source.getAllElements("div");
				for (Element element : elements) {
					if ("lyricbox".equals(element.getAttributeValue("class"))) {
						// Get all content
						lyrics = element.getRenderer().toString();
						List<Element> subElements = element.getChildElements();
						for (Element element2 : subElements) {
							if ("rtMatcher".equals(element2.getAttributeValue("class"))) {
								List<Element> subSubElements = element2.getChildElements();
								for (Element element3 : subSubElements) {
									// log.debug(element3.getRenderer().toString());
									// Strip out all adverts and links to
									// ringtone downloads..
									lyrics = lyrics.replace(element3.getRenderer().toString().replaceAll("> ", ">").trim(), "");
									log.debug("### Found Lyrics. URL: " + url);
									// return lyrics;
									// }
								}
								return lyrics;
							}
						}
					}
				}
			} catch (Exception e) {

			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception ex) {

					}
				}
			}
		} catch (Exception e) {
			log.debug("Could Not Find Lyrics: " + e.getMessage());
		}
		return null;
	}
}