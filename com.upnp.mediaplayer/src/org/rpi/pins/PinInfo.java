package org.rpi.pins;

import org.json.JSONObject;

public class PinInfo {

	/**
	 * @param id
	 * @param mode
	 * @param type
	 * @param uri
	 * @param title
	 * @param description
	 * @param artworkUri
	 * @param shuffle
	 */
	public PinInfo(int id, String mode, String type, String uri, String title, String description, String artworkUri, boolean shuffle) {
		setId(id);
		this.mode = mode;
		this.type = type;
		setUri(uri);
		this.title = title;
		this.description = description;
		setArtworkUri(artworkUri);
		this.shuffle = shuffle;
	}

	public PinInfo(JSONObject jpi) {
		setId((long) jpi.getLong("id"));
		setMode(jpi.getString("mode"));
		setType(jpi.getString("type"));
		setUri(jpi.getString("uri"));
		setTitle(jpi.getString("title"));
		setDescription(jpi.getString("description"));
		setArtworkUri(jpi.getString("artworkUri"));
		setShuffle(jpi.getBoolean("shuffle"));
	}

	private long id = -1;
	// private int index = -1;
	private String mode = "";
	private String type = "";
	private String uri = "";
	private String title = "";
	private String description = "";
	private String artworkUri = "";
	private boolean shuffle = false;

	public long getId() {
		return id;
	}

	public Integer getIdAsInt() {
		return (int) id;
	}

	public void setId(long id) {
		this.id = id;
		// this.index = (int) id;
	}

	/*
	 * public Integer getIndex() { return this.index ; }
	 */

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		try {
			this.uri = uri;
			// this.uri = URLEncoder.encode(uri);
		} catch (Exception e) {
			this.uri = uri;
		}
		;
	}

	public String getTitle() {
		//return title;
		String myType = type;
		if(type.length() > 0) {
			myType = myType.substring(0, 1).toUpperCase() + myType.substring(1).toLowerCase();
		}
		return myType + " : " + description ;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		setTitle(description);
	}

	public String getArtworkUri() {
		return artworkUri;
	}

	public void setArtworkUri(String artworkUri) {
		// this.artworkUri = artworkUri;
		try {
			// this.artworkUri = URLEncoder.encode(artworkUri);
			this.artworkUri = artworkUri;
		} catch (Exception e) {
			this.artworkUri = artworkUri;
		}
		;
	}

	public boolean isShuffle() {
		return shuffle;
	}

	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}

	public JSONObject getJSONObject() {
		JSONObject o = new JSONObject();
		o.put("id", getId());
		o.put("mode", getMode());
		o.put("type", getType());
		o.put("uri", getUri());
		o.put("title", getTitle());
		o.put("description", getDescription());
		o.put("artworkUri", getArtworkUri());
		o.put("shuffle", isShuffle());
		return o;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PinInfo [id=");
		builder.append(id);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", type=");
		builder.append(type);
		builder.append(", uri=");
		builder.append(uri);
		builder.append(", title=");
		builder.append(title);
		builder.append(", description=");
		builder.append(description);
		builder.append(", artworkUri=");
		builder.append(artworkUri);
		builder.append(", shuffle=");
		builder.append(shuffle);
		builder.append("]");
		return builder.toString();
	}

}
