package org.rpi.web.longpolling;

import java.net.URLEncoder;

import org.rpi.channel.ChannelBase;
import org.rpi.utils.Utils;

public class TrackInfo {

	private String lyrics = "";
	private ChannelBase track = null;

	private String title = "";
	private String artist = "";
	private String year = "";
	private String image_url = "";
	private String album_title = "";
	private String performer = "";
	private long track_duration = 0;
	private long time_played = 0;
	private String genre = "";
	private String artist_biography = "";

	public TrackInfo(ChannelBase track, String artist, String title) {
		this.track = track;
		if (track != null) {
			this.artist = artist;
			performer = track.getPerformer();
			album_title = track.getAlbum();
			this.title = title;
			track_duration = track.getDuration();
			if (track_duration > 0) {
				track_duration = track_duration / 1000;
			}
			year = track.getDate();
			image_url = track.getAlbumArtUri();
		}
	}

	/**
	 * @return the lyrics
	 */
	public String getLyrics() {
		if (lyrics == null)
			return "";
		return lyrics;
	}

	/**
	 * @param lyrics
	 *            the lyrics to set
	 */
	public void setLyrics(String lyrics) {
		if (Utils.isEmpty(lyrics)) {
			this.lyrics = "Unable to find Lyrics";
		} else {
			this.lyrics = lyrics;
		}
		try {
			this.lyrics = URLEncoder.encode(this.lyrics, "UTF-8");
		} catch (Exception e) {

		}
	}

	public String getJSON() {
		String q = "\"";
		String colon = ":";
		String space = " ";
		String comma = ",";
		String nl = "\r\n";
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(nl);
		sb.append(q + "details" + q + colon + space + q + cleanString(artist) + " " + cleanString(title) + " " + year + q);
		sb.append(nl);
		sb.append(comma + q + "album_title" + q + colon + space + q + cleanString(album_title) + q);
		sb.append(nl);
		sb.append(comma + q + "artist" + q + colon + space + q + cleanString(artist) + q);
		sb.append(nl);
		sb.append(comma + q + "album_artist" + q + colon + space + q + cleanString(performer) + q);
		sb.append(nl);
		sb.append(comma + q + "title" + q + colon + space + q + cleanString(title) + q);
		sb.append(nl);
		sb.append(comma + q + "image_uri" + q + colon + space + q + image_url + q);
		sb.append(nl);
		sb.append(comma + q + "track_duration" + q + colon + space + q + track_duration + q);
		sb.append(nl);
		sb.append(comma + q + "genre" + q + colon + space + q + cleanString(genre) + q);
		sb.append(nl);
		sb.append(comma + q + "time_played" + q + colon + space + q + time_played + q);
		sb.append(nl);
		sb.append(comma + q + "lyrics" + q + colon + space + q + lyrics + q);
		sb.append(nl);
		sb.append(comma + q + "artist_biography" + q + colon + space + q + artist_biography + q);
		sb.append(nl);
		sb.append("}");
		return sb.toString();
	}

	private String cleanString(String value) {
		if(value ==null) {
			return "";
		}
		value =  value.replace("\"", "'");
		try
		{
			return URLEncoder.encode(value,"UTF-8");
		}
		catch(Exception e)
		{
			
		}
		return value;
	}

	public void setTimePlayed(long time_played) {
		this.time_played = time_played;

	}

	/**
	 * @param artist_biography
	 *            the artist_biography to set
	 */
	public void setArtist_biography(String artist_biography) {
		try {
			this.artist_biography = artist_biography;
			//this.artist_biography = URLEncoder.encode(artist_biography, "UTF-8");
			
		} catch (Exception e) {

		}
	}

}
