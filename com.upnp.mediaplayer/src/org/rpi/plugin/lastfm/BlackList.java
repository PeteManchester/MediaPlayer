package org.rpi.plugin.lastfm;

public class BlackList {
	
	private String artist = "";
	private String title = "";
	
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public boolean matches(String artist, String title)
	{
		if(this.artist.equalsIgnoreCase(""))
		{
			if(this.title.equalsIgnoreCase(title))
				return true;
		}
		else if(this.title.equalsIgnoreCase(""))
		{
			if(this.artist.equalsIgnoreCase(artist))
				return true;
		}
		else
		{
			if(this.artist.equalsIgnoreCase(artist) && this.title.equalsIgnoreCase(title))
				return true;
		}
		return false;
	}
	
	public String toString()
	{
		return "Artist: " + artist + " Title: " + title;
	}
	
	

}
