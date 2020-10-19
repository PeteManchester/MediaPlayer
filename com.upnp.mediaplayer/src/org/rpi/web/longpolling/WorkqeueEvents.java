package org.rpi.web.longpolling;

/**
 * Pete Hoyle
 * This code is very sloppy and needs improving, but it's the last bit I do before having a break for the summer, so it can wait for a while :-)
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import net.htmlparser.jericho.Element;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.Utils;

import com.echonest.api.v4.EchoNestException;
import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;

public class WorkqeueEvents implements Runnable {
	private Logger log = Logger.getLogger(this.getClass());
	private boolean run = true;
	private Vector<EventBase> mWorkQueue = new Vector<EventBase>();

	private String title = "";
	private String artist = "";
	private String year = "";
	private String last_title = "";
	private String last_artist = "";
	private String lyrics = "";
	private String image_url = "";
	private String json = "{}";
	private long time_played = 0;
	private String artist_biography = "";
	private ChannelBase track = null;
	private String contextPath = "";
	// private ConcurrentHashMap<String, ArtistInfo> mArtistInfo = new
	// ConcurrentHashMap<String,ArtistInfo>();
	private final int MAX_ENTRIES = 5;
	private LinkedHashMap<String, ArtistInfo> mArtistInfo = new LinkedHashMap<String, ArtistInfo>(MAX_ENTRIES + 1, .75F, false) {
		protected boolean removeEldestEntry(Map.Entry eldest) {
			return size() > MAX_ENTRIES;
		}
	};

	public WorkqeueEvents(String contextPath) {
		this.contextPath = contextPath;
		ChannelBase track = PlayManager.getInstance().getCurrentTrack();
		if (track != null) {
			this.track = track;
			title = track.getTitle();
			String artist = track.getArtist();
			if (Utils.isEmpty(artist)||artist.equalsIgnoreCase("VARIOUS ARTISTS")) {
				artist = track.getPerformer();
			}
			this.artist = artist;
			year = track.getDate();
			image_url = track.getAlbumArtUri();
		} else {

		}
		try {
			TrackInfo info = new TrackInfo(track, artist, title);
			setJson(info.getJSON());
		} catch (Exception e) {
			log.error(e);
		}

	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized EventBase get() {
		EventBase object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public EventBase peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public void put(EventBase event) {
		try {
			mWorkQueue.addElement(event);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void stop() {
		run = false;
		clear();
	}

	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					EventBase base = get();
					switch (base.getType()) {
					case EVENTTRACKCHANGED:
						EventTrackChanged etc = (EventTrackChanged) base;
						ChannelBase track = etc.getTrack();
						this.track = track;
						if (track != null) {
							try {
								setTitle(track.getTitle());
								String artist = track.getArtist();
								if (Utils.isEmpty(artist)||artist.equalsIgnoreCase("VARIOUS ARTISTS")) {
									artist = track.getPerformer();
								}
								setArtist(artist);
								year = track.getDate();
								log.debug("TrackChanged: '" + artist + "' '" + title + "'");
								image_url = track.getAlbumArtUri();
								checkUpdate();
							} catch (Exception ex) {
								log.error("Error TrackChanged", ex);
							}
						} else {

						}

						break;
					case EVENTUPDATETRACKMETATEXT:
						EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) base;
						log.debug("EVENTUPDATETRACKMETATEXT: " + et.toString());
						try {
							setTitle(et.getTitle());
							setArtist(et.getArtist());
							if (this.track != null) {
								this.track.updateTrack(title, artist);
							}
							log.debug("MetaTextChanged: " + artist + " " + title);
							checkUpdate();
						} catch (Exception ex) {
							log.error("Error MetaTextChanged", ex);
						}
						break;
					case EVENTTIMEUPDATED:
						EventTimeUpdate ed = (EventTimeUpdate) base;
						time_played = ed.getTime();
						checkUpdate();
						break;
					}

				} catch (Exception e) {
					log.error("Error in Run Method", e);
				}
			} else {
				sleep(10);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WorkqeueEvents [getTitle()=");
		builder.append(getTitle());
		builder.append(", getArtist()=");
		builder.append(getArtist());
		builder.append(", getJson()=");
		builder.append(getJson());
		builder.append("]");
		return builder.toString();
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
		checkUpdate();
	}

	/**
	 * Determine if we should check for new lyrics
	 */
	private void checkUpdate() {
		CometContext<HttpServletResponse> context = CometEngine.getEngine().getCometContext(contextPath);
		if (context.getCometHandlers().size() == 0) {
			return;
		}
		TrackInfo info = new TrackInfo(track, artist, title);
		info = getLyrics(info, true);
		if (info != null) {
			info.setTimePlayed(time_played);
			setJson(info.getJSON());
		}

		try {
			// log.debug("Send Notify");
			context.addAttribute("Test", getJson());
			context.notify(null);
		} catch (Exception e) {
			log.error("Error Contact Notify", e);
		}

		last_artist = artist;
		last_title = title;
	}

	private TrackInfo getLyrics(TrackInfo info, boolean bGetArtistInfo) {
		
		if(title ==null || artist == null) {
			info.setLyrics("Not Found");
			return info;
		}

		if (title.equalsIgnoreCase(last_title) && artist.equalsIgnoreCase(last_artist)) {
			if (!lyrics.equalsIgnoreCase("")) {
				last_artist = artist;
				last_title = title;
				info.setLyrics(lyrics);
				info.setArtist_biography(artist_biography);
				return info;
			}
		} else {
			lyrics = "";
		}

		if (artist.equalsIgnoreCase("") || title.equalsIgnoreCase("")) {
			log.debug("Artist and/or Title was empty: " + artist + " " + title);
			last_artist = artist;
			last_title = title;
			lyrics = "Cannot Find Lyrics";
			info.setLyrics("Cannot Find Lyrics");
			return info;
		}
		
		LyricsClient client = new LyricsClient();

		info.setLyrics("Unable to find Lyrics");
		try {
			String mArtist = artist;
			String mSong = title;
			//String first_part = "http://lyrics.wikia.com/";
			//String first_part = "https://lyrics.fandom.com/";
			//String sURL = first_part + mArtist + ":" + mSong;
			if (mArtist.equalsIgnoreCase("") || mSong.equalsIgnoreCase("")) {

				return info;
			}
			//sURL = sURL.replace(" ", "_");
			//log.debug(sURL);
			// Try Artist, Song first
			//String res = makeHTTPQuery(sURL);
			String res = "";
			Lyrics lyricsTest = client.getLyrics(title + " " + artist).get();
			log.debug("Lyrics: " + lyricsTest);
			if(lyricsTest != null) {
				lyrics = lyricsTest.getContent();
				res = lyrics;
			}
			

			if (res == null || lyricsTest == null) {
				mArtist = artist;
				mSong = title;
				if (title.contains(".")) {
					String[] splits = title.split("\\.");
					if (splits.length == 2) {
						try {
							Integer.parseInt(splits[0]);
							//sURL = first_part + artist.trim() + ":" + splits[1].trim();
							//sURL = sURL.replace(" ", "_");
							lyricsTest = client.getLyrics(splits[1].trim() + " " + artist ).get();
							log.debug("Lyrics: " + lyricsTest);
							lyrics = lyricsTest.getContent();
							res = lyrics;
							//res = makeHTTPQuery(sURL);
						} catch (Exception ep) {
							log.debug("Could Not Find Lyrics: " + ep);
						}
					}
				}
				//try {
					
					
				//}catch(Exception e) {
				//	log.error("Error Getting Lyrics",e);
				//}
			}
			if (res == null) {
				res = "";
			}
			lyrics = res;
			if (lyrics.equalsIgnoreCase("")) {
				lyrics = "Not Found";
			}
			else {
				if(lyricsTest !=null) {
					lyrics += System.lineSeparator();
					lyrics += System.lineSeparator();
					lyrics += System.lineSeparator() + " Obtained From: " + lyricsTest.getSource();
					lyrics += System.lineSeparator() + " By: " + lyricsTest.getAuthor();
				}
				
			}
			info.setLyrics(res);
			if (bGetArtistInfo) {

				ArtistInfo artist_info = GetArtistInfo(mArtist);
				try {
					artist_biography = URLEncoder.encode(artist_info.getBiography(), "UTF-8");
					info.setArtist_biography(artist_biography);
				} catch (Exception e) {
					log.error(e);
				}
			}
			info.setArtist_biography(artist_biography);
			return info;
		} catch (Exception e) {
			log.error(e);
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
									// Strip out all adverts and links to
									// ringtone downloads..
									lyrics = lyrics.replace(element3.getRenderer().toString().replaceAll("> ", ">").trim(), "");
								}
							}
						}
						log.debug("### Found Lyrics. URL: " + url);
						return lyrics;
					}
				}
			} catch (Exception e) {
				log.error("Error Get Lytrics: " ,e );

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

	private ArtistInfo GetArtistInfo(String artist) {

		if (mArtistInfo.containsKey(artist.toUpperCase())) {
			ArtistInfo info = mArtistInfo.get(artist.toUpperCase());
			if (!info.getBiography().equalsIgnoreCase("")) {
				return info;
			} else {
				mArtistInfo.remove(artist.toUpperCase());
			}
		}

		ArtistInfo info = new ArtistInfo();
		if (artist.equalsIgnoreCase("SOON"))
			return info;
		try {
			//JenUttils util = new JenUttils();
			LastFMUtils util = new LastFMUtils();
			info = util.searchArtistByName(artist);
		} catch (Exception e1) {
			log.error("Error Getting ArtistInfo form EchoNest",e1);
		}
		mArtistInfo.put(artist.toUpperCase(), info);

		return info;
	}

	/**
	 * @return the json
	 */
	public String getJson() {
		return json;
	}

	/**
	 * @param json
	 *            the json to set
	 */
	public void setJson(String json) {
		this.json = json;
	}
}
