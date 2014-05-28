package org.rpi.web.longpolling;

import java.net.URLEncoder;

public class TrackInfo {

	private String lyrics = "";

	private String title = "";
	private String artist = "";

	public TrackInfo(String artist, String title) {
		this.artist = artist;
		this.title = title;
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
		if (lyrics == null) {
			this.lyrics = "Unable to find Lyrics";
		} else {
			this.lyrics = lyrics;
		}
	}

	public String getJSON() {
		String encodedLyrics = lyrics;
		try {
			encodedLyrics = URLEncoder.encode(lyrics, "UTF-8");
		} catch (Exception e) {

		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		String q = "\"";
		String colon = ":";
		String space = " ";
		String comma = ",";
		sb.append(q + "details" + q + colon + space + q + artist + " " + title + q);
		sb.append(comma + q + "lyrics" + q + colon + space + q + encodedLyrics + q);
		sb.append("}");
		return sb.toString();
	}
	// /**
	// * @return the artistInfo
	// */
	// public ArtistInfo getArtistInfo() {
	// return artistInfo;
	// }
	// /**
	// * @param artistInfo the artistInfo to set
	// */
	// public void setArtistInfo(ArtistInfo artistInfo) {
	// this.artistInfo = artistInfo;
	// }
}
