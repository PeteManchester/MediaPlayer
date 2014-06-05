package org.rpi.web.longpolling;

public class ArtistInfo {
	
	private String biography = "";
	private String imageURL = "";
	private String news = "";
	/**
	 * @return the biography
	 */
	public String getBiography() {
		return biography;
	}
	/**
	 * @param biography the biography to set
	 */
	public void setBiography(String biography) {
		this.biography = biography;
	}
	/**
	 * @return the imageURL
	 */
	public String getImageURL() {
		return imageURL;
	}
	/**
	 * @param imageURL the imageURL to set
	 */
	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}
	
	public void setNews(String news)
	{
		this.news = news;
	}
	
	public String getNews() {
		return news;
	}

}
