package org.rpi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.json.JSONObject;
import org.rpi.log.CustomPatternLayout;
import org.rpi.log.MemoryAppender;
import org.rpi.utils.Utils;

enum Props {
	MEDIAPLAYER_FRIENDLY_NAME("mediaplayer_friendly_name"), MEDIAPLAYER_PLAYER("mediaplayer_player"), MEDIAPLAYER_PLAYLIST_MAX("mediaplayer_playlist_max"), MEDIAPLAYER_ENABLE_AVTRANSPORT("mediaplayer_enable_avTransport"), MEDIAPLAYER_ENABLE_RECEIVER("mediaplayer_enable_receiver"), MEDIAPLAYER_STARTUP_VOLUME("mediaplayer_startup_volume"), MEDIAPLAYER_MAX_VOLUME("mediaplayer_max_volume"), MEDIAPLAYER_SAVE_LOCAL_PLAYLIST("mediaplayer_save_local_playlist"), MPLAYER_PLAY_DEFINITIONS("mplayer_play_definitions"), MPLAYER_PATH("mplayer_path"), MPLAYER_CACHE_SIZE("mplayer_cache_size"), MPLAYER_CACHE_MIN("mplayer_cache_min"), MPD_HOST("mpd_host"), MPD_PORT("mpd_port"), MPD_LISTEN_PORT("mpd_listen_port"), MPD_PRELOAD_TIMER("mpd_preload_timer"), LOG_FILE_NAME("log_file_name"),LOG_FILE_SIZE("log_file_size"), LOG_FILE_MAX_BACKUPS("log_file_max_backups"), LOG_FILE_LEVEL("log_file_level"), LOG_CONSOLE_LEVEL("log_console_level"), OPENHOME_PORT("openhome_port"), OPENHOME_LOG_LEVEL("openhome_log_level"), JAVA_SOUNDCARD_SUFFIX("java_soundcard_suffix"), JAVA_SOUND_SOFTWARE_MIXER_ENABLED("java_sound_software_mixer_enabled"), SONGCAST_LATENCY("songcast_latency"), RADIO_TUNEIN_USERNAME("radio_tunein_username"), RADIO_TUNEIN_PARTNERID("radio_tunein_partnerid"), WEB_SERVER_PORT("web_server_port"), WEB_SERVER_ENABLED("web_server_enabled"), AIRPLAY_ENABLED("airplay_enabled"), AIRPLAY_LATENCY("airplay_latency"), AIRPLAY_PORT("airplay_port"), AIRPLAY_AUDIO_START_DELAY("airplay_audio_start_delay"), AIRPLAY_MASTER_VOLUME_ENABLED("airplay_master_volume_enabled"),PINS_DEVICE_MAX("pins_device_max"),PINS_SERVICE_URL("pins_service_url"),REPORT_BITRATE_IN_BYTES_SEC("report_bitrate_in_bytes_sec"), CLEAR_PLAYLIST_ONSTART_ENABLED("clear_playlist_onstart_enabled");

	private final String stringValue;

	private Props(final String s) {
		stringValue = s;
	}

	public String toString() {
		return stringValue;
	}
}

public class Config {

	private String version = "0.0.1.8";

	private Logger log = Logger.getLogger(this.getClass());

	private String songcast_nic_name = "";

	private String resourceURIPrefix = "";

	private String java_soundcard_name = "";

	private static Properties pr = null;

	private static Calendar cal = null;

	private MemoryAppender memory_appender = new MemoryAppender();

	private static Config instance = null;

	public static Config getInstance() {
		if (instance == null) {
			instance = new Config();
			instance.ConfigureLogging();
			instance.printAppProperties();
		}
		return instance;
	}

	private void printAppProperties() {
		if (log != null) {
			log.fatal("###Start of app.properties###");
			for (String key : pr.stringPropertyNames()) {
				String value = pr.getProperty(key);
				log.fatal("'" + key + "' : '" + value + "'");
			}
			log.fatal("###End of app.properties####");
		}
	}

	private Config() {
		cal = Calendar.getInstance();
		getConfig();
	}

	public static int convertStringToInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {

		}
		return -99;
	}

	public static int convertStringToInt(String s, int iDefault) {
		try {
			return Integer.parseInt(s);
		} catch (Exception e) {

		}
		return iDefault;
	}

	public static boolean convertStringToBoolean(String s, boolean bDefault) {
		if (s == null || s.equalsIgnoreCase(""))
			return bDefault;
		if (s.equalsIgnoreCase("TRUE"))
			return true;
		if (s.equalsIgnoreCase("YES"))
			return true;
		if (s.equalsIgnoreCase("1"))
			return true;
		return false;
	}

	public void setStartTime() {
		try {
			Date date = new Date();
			cal.setTime(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Date getStartTime() {
		return cal.getTime();
	}

	private void getConfig() {
		
		try (InputStream in = new FileInputStream(new File("app.properties"));
			    InputStreamReader writer = new InputStreamReader(in,Charset.forName("UTF-8"))) {
				pr = new Properties();
			    pr.load(writer);
			}
		catch(Exception e) {
			log.error("Error Loading Config", e);
		}
		
		/*
		FileInputStream fs =null;
		try {
			pr = new Properties();
			fs = new FileInputStream("app.properties");
			pr.load(fs);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(fs !=null) {
				try {
					fs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		*/
	}

	/**
	 * Get the ProtocolInfo
	 * 
	 * @return
	 */
	public String getProtocolInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("http-get:*:audio/x-flac:*,");
		sb.append("http-get:*:audio/wav:*,");
		sb.append("http-get:*:audio/wave:*,");
		sb.append("http-get:*:audio/x-wav:*,");
		sb.append("http-get:*:audio/mpeg:*,");
		sb.append("http-get:*:audio/x-mpeg:*,");
		sb.append("http-get:*:audio/mp1:*,");
		sb.append("http-get:*:audio/aiff:*,");
		sb.append("http-get:*:audio/x-aiff:*,");
		sb.append("http-get:*:audio/x-m4a:*,");
		sb.append("http-get:*:audio/x-ms-wma:*,");
		sb.append("rtsp-rtp-udp:*:audio/x-ms-wma:*,");
		sb.append("http-get:*:audio/x-scpls:*,");
		sb.append("http-get:*:audio/x-mpegurl:*,");
		sb.append("http-get:*:audio/x-ms-asf:*,");
		sb.append("http-get:*:audio/x-ms-wax:*,");
		sb.append("http-get:*:audio/x-ms-wvx:*,");
		sb.append("http-get:*:text/xml:*,");
		sb.append("http-get:*:audio/aac:*,");
		sb.append("http-get:*:audio/aacp:*,");
		sb.append("http-get:*:audio/mp4:*,");
		sb.append("http-get:*:audio/ogg:*,");
		sb.append("http-get:*:audio/x-ogg:*,");
		sb.append("http-get:*:application/ogg:*,");
		sb.append("http-get:*:video/mpeg:*,");
		sb.append("http-get:*:video/mp4:*,");
		sb.append("http-get:*:video/quicktime:*,");
		sb.append("http-get:*:video/webm:*,");
		sb.append("http-get:*:video/x-ms-wmv:*,");
		sb.append("http-get:*:video/x-ms-asf:*,");
		sb.append("http-get:*:video/x-msvideo:*,");
		sb.append("http-get:*:video/x-ms-wax:*,");
		sb.append("http-get:*:video/x-ms-wvx:*,");
		sb.append("http-get:*:video/x-m4v:*,");
		sb.append("http-get:*:video/x-matroska:*,");
		sb.append("http-get:*:application/octet-stream:*");
		return sb.toString();
	}

	/**
	 * Get a String value from the properties map
	 * 
	 * @param key
	 * @param default_value
	 * @return
	 */
	private String getValue(Props key, String default_value) {
		if (pr == null)
			return default_value;
		try {

			return pr.getProperty(key.toString(), default_value);
		} catch (Exception e) {

		}
		return default_value;
	}

	/**
	 * Get an int value from the properties map
	 * 
	 * @param key
	 * @param default_value
	 * @return
	 */
	private int getValueInt(Props key, int default_value) {
		try {
			return convertStringToInt(getValue(key, "" + default_value));
		} catch (Exception e) {

		}
		return default_value;
	}

	/**
	 * Get a boolean value from the properties map
	 * 
	 * @param key
	 * @param default_value
	 * @return
	 */
	private boolean getValueBool(Props key, Boolean default_value) {
		try {
			return convertStringToBoolean(getValue(key, default_value.toString()), default_value);
		} catch (Exception e) {

		}
		return default_value;
	}

	/**
	 * @return the mediaplayer_friendly_name
	 */
	public String getMediaplayerFriendlyName() {
		return getValue(Props.MEDIAPLAYER_FRIENDLY_NAME, "Default");
	}

	/**
	 * @param mediaplayer_friendly_name
	 *            the mediaplayer_friendly_name to set
	 */
	public void setMediaplayerFriendlyName(String mediaplayer_friendly_name) {
		pr.setProperty(Props.MEDIAPLAYER_FRIENDLY_NAME.toString(), mediaplayer_friendly_name);
	}

	/**
	 * @return the mediaplayer_player
	 */
	public String getMediaplayerPlayerType() {
		return getValue(Props.MEDIAPLAYER_PLAYER, "mpd");
	}

	/**
	 * @param mediaplayer_player
	 *            the mediaplayer_player to set
	 */
	public void setMediaplayerPlayerType(String mediaplayer_player) {
		pr.setProperty(Props.MEDIAPLAYER_PLAYER.toString(), mediaplayer_player);
	}

	/**
	 * @return the mediaplayer_playlist_max
	 */
	public int getMediaplayerPlaylistMax() {
		return getValueInt(Props.MEDIAPLAYER_PLAYLIST_MAX, 1000);
	}

	/**
	 * @param mediaplayer_playlist_max
	 *            the mediaplayer_playlist_max to set
	 */
	public void setMediaplayerPlaylistMax(int mediaplayer_playlist_max) {
		// Config.mediaplayer_playlist_max = mediaplayer_playlist_max;
	}

	/**
	 * @return the mediaplayer_enable_avTransport
	 */
	public boolean isMediaplayerEnableAVTransport() {
		return getValueBool(Props.MEDIAPLAYER_ENABLE_AVTRANSPORT, true);
	}

	/**
	 * @param mediaplayer_enable_avTransport
	 *            the mediaplayer_enable_avTransport to set
	 */
	public void setgetMediaplayerPlaylistMax(boolean mediaplayer_enable_avTransport) {
		// Config.mediaplayer_enable_avTransport =
		// mediaplayer_enable_avTransport;
	}

	/**
	 * @return the mediaplayer_enable_receiver
	 */
	public boolean isMediaplayerEnableReceiver() {
		return getValueBool(Props.MEDIAPLAYER_ENABLE_RECEIVER, true);
	}

	/**
	 * @param mediaplayer_enable_receiver
	 *            the mediaplayer_enable_receiver to set
	 */
	public void setMediaplayerEnableReceiver(boolean mediaplayer_enable_receiver) {
		// Config.mediaplayer_enable_receiver = mediaplayer_enable_receiver;
	}

	/**
	 * @return the mediaplayer_startup_volume
	 */
	public long getMediaplayerStartupVolume() {
		return getValueInt(Props.MEDIAPLAYER_STARTUP_VOLUME, -1);
	}

	public long getMaxVolume() {
		return getValueInt(Props.MEDIAPLAYER_MAX_VOLUME, 100);
	}
	
	public int getPinsDeviceMax() {
		return getValueInt(Props.PINS_DEVICE_MAX,20);
	}
	
	public String getPinsServiceURL() {
		return getValue(Props.PINS_SERVICE_URL,"");
	}


	/**
	 * @param mediaplayer_startup_volume
	 *            the mediaplayer_startup_volume to set
	 */
	public void setMediaplayerStartupVolume(long mediaplayer_startup_volume) {
		// Config.mediaplayer_startup_volume = mediaplayer_startup_volume;
	}

	/**
	 * @return the mediaplayer_save_local_playlist
	 */
	public boolean isMediaplayerSaveLocalPlaylist() {
		return getValueBool(Props.MEDIAPLAYER_SAVE_LOCAL_PLAYLIST, true);
	}

	/**
	 * @param mediaplayer_save_local_playlist
	 *            the mediaplayer_save_local_playlist to set
	 */
	public void setMediaplayerSaveLocalPlaylist(boolean mediaplayer_save_local_playlist) {
		// Config.mediaplayer_save_local_playlist =
		// mediaplayer_save_local_playlist;
	}

	/**
	 * @return the mplayer_play_definitions
	 */
	public List<String> getMplayerPlayListDefinitions() {
		List<String> res = new ArrayList<String>();
		try {
			String lists = getValue(Props.MEDIAPLAYER_PLAYER, "");
			String[] splits = lists.split(",");
			res = Arrays.asList(splits);
		} catch (Exception e) {

		}
		return res;
	}

	/**
	 * @param mplayer_play_definitions
	 *            the mplayer_play_definitions to set
	 */
	public void setMplayerPlayListDefinitions(List<String> mplayer_play_definitions) {
		// Config.mplayer_playlist_definitions = mplayer_play_definitions;
	}

	/**
	 * @return the mplayer_path
	 */
	public String getMPlayerPath() {
		return getValue(Props.MPLAYER_PATH, "/usr/bin/mplayer");
	}

	/**
	 * @param mplayer_path
	 *            the mplayer_path to set
	 */
	public void setMPlayerPath(String mplayer_path) {
		// Config.mplayer_path = mplayer_path;
	}

	/**
	 * @return the mplayer_cache_size
	 */
	public int getMplayerCacheSize() {
		return getValueInt(Props.MPLAYER_CACHE_SIZE, 520);
	}

	/**
	 * @param mplayer_cache_size
	 *            the mplayer_cache_size to set
	 */
	public void setMPlayerCacheSize(int mplayer_cache_size) {
		// Config.mplayer_cache_size = mplayer_cache_size;
	}

	/**
	 * @return the mplayer_cache_min
	 */
	public int getMPlayerCacheMin() {
		return getValueInt(Props.MPLAYER_CACHE_MIN, 80);
	}

	/**
	 * @param mplayer_cache_min
	 *            the mplayer_cache_min to set
	 */
	public void setMPlayerCacheMin(int mplayer_cache_min) {
		// Config.mplayer_cache_min = mplayer_cache_min;
	}

	/**
	 * @return the mpd_host
	 */
	public String getMpdHost() {
		return getValue(Props.MPD_HOST, "localhost");
	}

	/**
	 * @param mpd_host
	 *            the mpd_host to set
	 */
	public void setMpdHost(String mpd_host) {
		// Config.mpd_host = mpd_host;
	}

	/**
	 * @return the mpd_port
	 */
	public int getMpdPort() {
		return getValueInt(Props.MPD_PORT, 6600);
	}
	
	public int getMpdListenPort() {
		return getValueInt(Props.MPD_LISTEN_PORT, 8000);
	}

	/**
	 * @param mpd_port
	 *            the mpd_port to set
	 */
	public void setMpdPort(int mpd_port) {
		// Config.mpd_port = mpd_port;
	}

	/**
	 * @return the mpd_preload_timer
	 */
	public int getMpdPreloadTimer() {
		return getValueInt(Props.MPD_PRELOAD_TIMER, 2);
	}

	/**
	 * @param mpd_preload_timer
	 *            the mpd_preload_timer to set
	 */
	public void setMpdPreloadTimer(int mpd_preload_timer) {
		// Config.mpd_preload_timer = mpd_preload_timer;
	}

	/**
	 * @return the log_file_name
	 */
	public String getLogFileName() {
		return getValue(Props.LOG_FILE_NAME, "mediaplayer.log");
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLogFileSize() {
		return getValue(Props.LOG_FILE_SIZE, "5");
	}
	
	/**
	 * 
	 * @return
	 */
	public int getLogFileMaxBackups() {
		return getValueInt(Props.LOG_FILE_MAX_BACKUPS, 5);
	}

	/**
	 * @param log_file_name
	 *            the log_file_name to set
	 */
	public void setLogFileName(String log_file_name) {
		// Config.log_file_name = log_file_name;
	}

	/**
	 * @return the log_file_level
	 */
	public String getLogFileLevel() {
		return getValue(Props.LOG_FILE_LEVEL, "info");
	}

	/**
	 * @param log_file_level
	 *            the log_file_level to set
	 */
	public void setLogFileLevel(String log_file_level) {
		// Config.log_file_level = log_file_level;
	}

	/**
	 * @return the log_console_level
	 */
	public String getLogConsoleLevel() {
		return getValue(Props.LOG_CONSOLE_LEVEL, "off");
	}

	/**
	 * @param log_console_level
	 *            the log_console_level to set
	 */
	public void setLogConsoleLevel(String log_console_level) {

	}

	/**
	 * @return the openhome_port
	 */
	public int getOpenhomePort() {
		return getValueInt(Props.OPENHOME_PORT, 52821);
	}

	/**
	 * @param openhome_port
	 *            the openhome_port to set
	 */
	public void setOpenhomePort(int openhome_port) {
		// Config.openhome_port = openhome_port;
	}

	/**
	 * @return the openhome_log_level
	 */
	public String getOpenhomeLogLevel() {
		return getValue(Props.OPENHOME_LOG_LEVEL, "Off");
	}

	/**
	 * @param openhome_log_level
	 *            the openhome_log_level to set
	 */
	public void setOpenhomeLogLevel(String openhome_log_level) {
		// Config.openhome_log_level = openhome_log_level;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getJavaSoundcardSuffix() {
		List<String> res = new ArrayList<String>();
		try {
			String names = getValue(Props.JAVA_SOUNDCARD_SUFFIX, "");
			String[] list = names.split("--");
			res = Arrays.asList(list);
		} catch (Exception e) {
			log.error("Error parsing SoundCard Suffix", e);
		}

		return res;
	}

	/**
	 * 
	 * @return
	 */
	public String getJavaSoundcardName() {
		return java_soundcard_name;
	}

	/**
	 * @param java_soundcard_name
	 *            the songcast_soundcard_name to set
	 */
	public void setJavaSoundcardName(String java_soundcard_name) {
		this.java_soundcard_name = "";
		if (!Utils.isEmpty(java_soundcard_name)) {
			this.java_soundcard_name = "#" + java_soundcard_name;
		}
	}

	/**
	 * @return the radio_tunein_username
	 */
	public String getRadioTuneinUsername() {
		return getValue(Props.RADIO_TUNEIN_USERNAME, "");
	}

	/**
	 * @param radio_tunein_username
	 *            the radio_tunein_username to set
	 */
	public void setRadioTuneinUsername(String radio_tunein_username) {
		// this.radio_tunein_username = radio_tunein_username;
	}

	/**
	 * 
	 * @return
	 */
	public String getRadioTuneInPartnerId() {
		return getValue(Props.RADIO_TUNEIN_PARTNERID, "");
	}

	/**
	 * @return the web_server_enabled
	 */
	public boolean isWebWerverEnabled() {
		return getValueBool(Props.WEB_SERVER_ENABLED, true);
	}

	/**
	 * @param web_server_enabled
	 *            the web_server_enabled to set
	 */
	public void setWebServerEnabled(boolean web_server_enabled) {
		// Config.web_server_enabled = web_server_enabled;
	}

	/**
	 * @return the songcast_latency
	 */
	public int getSongcastLatency() {
		return getValueInt(Props.SONGCAST_LATENCY, 0);
	}
	
	/***
	 * 
	 * @return report_bitrate_in_bytes_sec
	 */
	public boolean getReportBitRateInBytesSec() {
		return getValueBool(Props.REPORT_BITRATE_IN_BYTES_SEC, false);
	}



	/**
	 * @return the web_server_port
	 */
	public String getWebServerPort() {
		return getValue(Props.WEB_SERVER_PORT, "8088");
	}

	/**
	 * @param web_server_port
	 *            the web_server_port to set
	 */
	public void setWebServerPort(String web_server_port) {
		// Config.web_server_port = web_server_port;
	}

	public boolean isAirPlayEnabled() {

		return getValueBool(Props.AIRPLAY_ENABLED, true);
	}

	public int getAirPlayLatency() {
		return getValueInt(Props.AIRPLAY_LATENCY, 0);
	}

	public boolean isAirPlayStartAudioDelayEnabled() {
		return getValueBool(Props.AIRPLAY_AUDIO_START_DELAY, false);
	}
	
	public boolean isAirPlayMasterVolumeEnabled(){
		return getValueBool(Props.AIRPLAY_MASTER_VOLUME_ENABLED,true);
	}

	/**
	 * 
	 * @return
	 */
	public int getAirPlayPort() {
		return getValueInt(Props.AIRPLAY_PORT, 5000);
	}
	
	public boolean isClearPlaylistOnStartupEnbled() {
		return getValueBool(Props.CLEAR_PLAYLIST_ONSTART_ENABLED, false);
	}

	/***
	 * Set up our logging
	 */
	private void ConfigureLogging() {

		try {
			CustomPatternLayout pl = new CustomPatternLayout();
			pl.setConversionPattern("%d [%t] %-5p [%-10c] %m%n");
			pl.activateOptions();
			// CustomRollingFileAppender fileAppender = new
			// CustomRollingFileAppender(pl,Config.logfile,".log",true);
			RollingFileAppender fileAppender = new RollingFileAppender();
			fileAppender.setName("fileAppender");
			fileAppender.setAppend(true);
			fileAppender.setMaxFileSize(getLogFileSize() +"mb");
			fileAppender.setMaxBackupIndex(getLogFileMaxBackups());
			fileAppender.setFile(getLogFileName());
			fileAppender.setThreshold(getLogLevel(getLogFileLevel()));
			fileAppender.setLayout(pl);
			fileAppender.activateOptions();
			Logger.getRootLogger().addAppender(fileAppender);
			ConsoleAppender consoleAppender = new ConsoleAppender();
			consoleAppender.setName("consoleLayout");
			;
			consoleAppender.setLayout(pl);
			consoleAppender.activateOptions();
			consoleAppender.setThreshold(getLogLevel(getLogConsoleLevel()));
			Logger.getRootLogger().addAppender(consoleAppender);
			memory_appender.setLayout(pl);
			memory_appender.activateOptions();
			memory_appender.setThreshold(Level.DEBUG);
			Logger.getRootLogger().addAppender(memory_appender);
			log.info("Logging Configured");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Level getLogLevel(String s) {
		return Level.toLevel(s, Level.DEBUG);
	}

	public String getVersion() {
		return version;
	}

	public String getSongCastNICName() {
		return songcast_nic_name;
	}

	public void setSongCastNICName(String nic) {
		songcast_nic_name = nic;
	}

	/**
	 * Change the Console log level
	 * 
	 * @param level
	 */
	private void changeConsoleLogLevel(String level) {
		try {
			ConsoleAppender append = (ConsoleAppender) LogManager.getRootLogger().getAppender("consoleLayout");
			append.setThreshold(getLogLevel(level));
			log.warn("Console Log Level Changed to: " + level);
		} catch (Exception e) {
			log.error("Error setting Console Log Level", e);
		}
	}

	/**
	 * Change the LogFile log level
	 * 
	 * @param level
	 */
	private void changeFileLogLevel(String level) {
		try {
			RollingFileAppender append = (RollingFileAppender) LogManager.getRootLogger().getAppender("fileAppender");
			append.setThreshold(getLogLevel(level));
			log.warn("File Log Level Changed to: " + level);
		} catch (Exception e) {
			log.error("Error setting File Log Level", e);
		}
	}

	/**
	 * Update the app.properties file
	 * 
	 * @param configObject
	 */
	public void updateConfig(JSONObject configObject) {
		try {
			for (String key : configObject.keySet()) {
				String value = configObject.getString(key);
				log.debug("Key: " + key + " = " + value);
				if (key.equalsIgnoreCase(Props.LOG_CONSOLE_LEVEL.toString())) {
					if (!value.toString().equalsIgnoreCase(pr.getProperty(Props.LOG_CONSOLE_LEVEL.toString()))) {
						changeConsoleLogLevel(value);
					}
				} else if (key.equalsIgnoreCase(Props.LOG_FILE_LEVEL.toString())) {
					if (!value.toString().equalsIgnoreCase(pr.getProperty(Props.LOG_FILE_LEVEL.toString()))) {
						changeFileLogLevel(value);
					}
				}				
				String s = StringEscapeUtils.unescapeJson(value);
				pr.put(key, s );
			}
		} catch (Exception e) {

		}
		OutputStream out = null;
		FileWriter fw = null;
		try {

			File f = new File("app.properties");
			out = new FileOutputStream(f);
			Properties tmp = new Properties() {
				@Override
				public synchronized Enumeration<Object> keys() {
					return Collections.enumeration(new TreeSet<Object>(super.keySet()));
				}
			};
			tmp.putAll(pr);
			fw = new FileWriter(f);
			tmp.store(fw, "Modified Using the Web Page");

		} catch (Exception e) {
			log.error("Error Updating Config", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {

				}
			}
			if(fw !=null) {
				try {
					fw.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public void setResourceURIPrefix(String resourceURIPrefix) {
		this.resourceURIPrefix = resourceURIPrefix;
	}

	public String getResourceURIPrefix() {
		return resourceURIPrefix;
	}

	public String getLoggingEvents() {
		String text = "";
		try {
			text = URLEncoder.encode(memory_appender.getEventString(), "UTF-8");
		} catch (Exception e) {

		}
		return text;
	}

	/**
	 * @return the java_sound_software_mixer_enabled
	 */
	public boolean isSoftwareMixerEnabled() {
		return getValueBool(Props.JAVA_SOUND_SOFTWARE_MIXER_ENABLED, false);
	}

}
