package org.rpi.plugin.lastfm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rpi.os.OSManager;
import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

public class LastFMConfigJSON {

	private final String key = "Ofewtraincrvheg!";

	private final Logger log = Logger.getLogger(this.getClass());

	private JSONObject jsonConfig = new JSONObject();
	String fileName = "";

	private List<BlackList> blackList = new ArrayList<BlackList>();

	private String password = "";
	private String userName = "";

	public LastFMConfigJSON() {
		init();
	}

	private void init() {
		String class_name = this.getClass().getName();
		log.debug("Find Class, ClassName: " + class_name);
		String path = OSManager.getInstance().getFilePath(this.getClass(), false);
		log.debug("Getting LastFM.json from Directory: " + path);
		jsonConfig = new JSONObject();

		try {
			Path filePath = Paths.get(path, "LastFM.json");
			fileName = filePath.toString();
			log.debug("Getting LastFM.json from: " + filePath.toString());
			String content = new String(Files.readAllBytes(filePath));
			jsonConfig = new JSONObject(content);

			password = getPasswordInternal();
			userName = getUserNameInternal();

			log.debug("LastFM. Got UserName");
			buildBlackList(jsonConfig);

		} catch (IOException e) {
			log.error("getJSONFromFile", e);
		}
		log.debug("LastFM End of Init");
	}

	private void buildBlackList(JSONObject jsonConfig) {
		try {
			log.debug("LastFM. Start of BuildBlackList");
			if (jsonConfig.has("BlackList")) {
				JSONArray bls = jsonConfig.getJSONArray("BlackList");
				for(Object o : bls) {
					if(o instanceof JSONObject) {
					JSONObject obj = (JSONObject) o;
					log.debug("LastFM. BlackList: " + obj.toString());
					String artist = "";
					if (obj.has("artist")) {
						artist = obj.getString("artist");
					}

					String title = "";
					if (obj.has("title")) {
						title = obj.getString("title");
					}
					if (!Utils.isEmpty(artist) || !Utils.isEmpty(title)) {
						log.debug("Creating BlackList: " + artist + " : " + title);
						BlackList bl = new BlackList(artist, title);
						log.debug("Created BlackList: " + artist + " : " + title);
						blackList.add(bl);
						log.debug("Added BlackList: " + artist + " : " + title);
					}
					}
				}
				log.debug("LastFM End of BlackList");
			}
		} catch (Exception e) {
			log.error("Error BuildBlackList", e);
		}
		log.debug("LastFM. End of BuildBlackList");
	}

	/**
	 * @return the userName
	 */
	private String getUserNameInternal() {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			if (conf.has("UserName")) {
				return conf.getString("UserName");
			}
		}
		return "";
	}

	/**
	 * @return the password
	 */
	private String getPasswordInternal() {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			if (conf.has("Password")) {
				String password = conf.getString("Password");
				if (password.startsWith("ENC:")) {
					return SecUtils.getInstance().decrypt(key, password.substring("ENC:".length()));
				} else {

					String encryptedPassword = SecUtils.getInstance().encrypt(key, password);
					setPassword("ENC:" + encryptedPassword);
					saveFile();
					return password;
				}
			}

		}
		return "";
	}

	/***
	 * Save the JSON Config
	 */
	private void saveFile() {
		log.debug("Saving LastFM.json: " + fileName);
		try (FileWriter fw = new FileWriter(fileName)) {
			String json = jsonConfig.toString(2);
			fw.write(json);
		} catch (Exception e) {
			log.error("Error Saving to: " + fileName, e);
		}
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			conf.put("Password", password);
		}
	}

	/**
	 * @return the proxyType
	 */
	public Proxy.Type getProxyType() {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			if (conf.has("ProxyType")) {
				return Proxy.Type.valueOf(conf.getString("ProxyType"));
			}
		}
		return Proxy.Type.DIRECT;
	}

	/**
	 * @return the proxyIP
	 */
	public String getProxyIP() {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			if (conf.has("ProxyIP")) {
				return conf.getString("ProxyIP");
			}
		}
		return "";
	}

	/**
	 * @return the proxyPort
	 */
	public int getProxyPort() {
		if (jsonConfig.has("config")) {
			JSONObject conf = jsonConfig.getJSONObject("config");
			if (conf.has("ProxyPort")) {
				return conf.getInt("ProxyPort");
			}
		}
		return 0;
	}

	/***
	 * 
	 * @return
	 */
	public List<BlackList> getBlackList() {
		return blackList;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

}
