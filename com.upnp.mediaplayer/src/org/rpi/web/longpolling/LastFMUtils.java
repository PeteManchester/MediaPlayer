package org.rpi.web.longpolling;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;

public class LastFMUtils {

	private String lastfrm_api_key = "003dc9a812e87f5058f53439dd26038e";
	private Logger log = Logger.getLogger(LastFMUtils.class);

	public ArtistInfo searchArtistByName(String artist) {
		ArtistInfo info = new ArtistInfo();

		try {
			// artist = java.net.URLEncoder.encode(artist, "UTF-8");
			String USER_AGENT = "Mozilla/5.0";
			String url = "http://ws.audioscrobbler.com/2.0/?";
			// String parameters =
			// "method=artist.getinfo&artist=%1$s&api_key=%2$s&format=json";
			String parameters = "method=artist.getinfo&artist=%1$s&api_key=%2$s&format=json";
			// String parameters =
			// "apikey=8e8ff105f5380c9f4d75e4d1518cf50750167cf5&application=SmartHome&event=%1$s&description=%2$s&priority=%3$s";
			parameters = String.format(parameters, URLEncoder.encode(artist, "UTF-8"), lastfrm_api_key);

			URL obj = new URL(url + parameters);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			// String urlParameters =
			// "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			// wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			log.debug("Sending 'POST' request to URL : " + url);
			// log.debug("Post parameters : " + urlParameters);
			log.debug("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			log.debug(response.toString());
			// info.setBiography(response.toString());
			info = getBioInfo(response.toString());
		} catch (Exception e) {
			log.error(" ", e);
		}

		return info;
	}

	private ArtistInfo getBioInfo(String info) {
		ArtistInfo artistInfo = new ArtistInfo();
		//JsonReader reader = Json.createReader(new StringReader(info));
		//JsonObject jsonObject = reader.readObject();
		JSONObject jsonObject = new JSONObject(info);
		//reader.close();
		if (jsonObject.has("artist")) {
			JSONObject artist = jsonObject.getJSONObject("artist");
			if (artist.has("bio")) {
				JSONObject bio = artist.getJSONObject("bio");
				//log.debug("Bio: " + bio);

				if (bio.has("content")) {
					String content = bio.getString("content");

					if (!content.equals("")) {
						artistInfo.setBiography(content);
					} else {
						if (bio.has("summary")) {
							String summary = bio.getString("summary");
							artistInfo.setBiography(summary);
						}
					}
				}
			}
			if (artist.has("image")) {
				JSONArray images = artist.getJSONArray("image");
				if (images != null) {
					for (Object o : images) {
						if (o instanceof JSONObject) {
							JSONObject image = (JSONObject) o;
							if (image.has("size")) {
								String size = image.getString("size");
								String url = image.getString("#text");
								if (!url.equals("")) {
									artistInfo.setImageURL(url);
									if (size.equalsIgnoreCase("LARGE")) {
										break;
									}
								}
							}
						}
					}
					//log.debug("Images");
				}

			}
			/*
			 * for (JsonValue jsonValue : body) { if (jsonValue.getValueType()
			 * == ValueType.OBJECT) { JsonObject object = (JsonObject)
			 * jsonValue; if (object.containsKey("artist")) {
			 * log.debug("ContainsArtist"); } if (object.containsKey("name")) {
			 * //name = object.getString("name"); } if
			 * (object.containsKey("type")) { //type = object.getString("type");
			 * } if (object.containsKey("time")) { //time =
			 * object.getString("time"); } if (object.containsKey("volume")) {
			 * //volume = object.getInt("volume"); } if
			 * (object.containsKey("channel")) { //channel =
			 * object.getString("channel"); } if (object.containsKey("shuffle"))
			 * { //shuffle = object.getBoolean("shuffle"); }
			 * 
			 * } }
			 */

		}
		return artistInfo;
	}

}
