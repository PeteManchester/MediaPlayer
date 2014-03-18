package org.rpi.main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.rpi.config.Config;
import org.rpi.log.CustomPatternLayout;
import org.rpi.utils.Utils;


public class StartMe {

	private static boolean stop = false;
	private static Logger log = Logger.getLogger(StartMe.class);

	// private static PluginManager pm = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// NativeLibraryLoader.load("mssql", "mssqlserver.jar");
		// NativeLibraryLoader.load("pi4j", "libpi4j.so");
		Config.setStartTime();
		boolean bInput = false;
		for (String s : args) {
			if (s.equalsIgnoreCase("-input")) {
				bInput = true;
			}
		}
		getConfig();
		ConfigureLogging();
		log.info("Starting......");
		if (!Utils.isEmpty(Config.songcastSoundCardName)) {
			setAudioDevice();
		}
		try {
			log.info("Getting Network Interfaces");
			Enumeration e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration ee = n.getInetAddresses();
				log.info("Network Interface Display Name: '" + n.getDisplayName() + "'");
				log.info("NIC Name: '" + n.getName() + "'");
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					log.info("IPAddress for Network Interface: " + n.getDisplayName() + " : " + i.getHostAddress());
				}
			}
		} catch (Exception e) {
			log.error("Error Getting IPAddress", e);
		}

		log.info("End Of Network Interfaces");
		log.info("Available Audio Devices:");
		try {
			Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();

			for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
				log.info("'" + mixerInfo[cnt].getName() + "'");
			}
		} catch (Exception e) {
			log.error("Error getting Audio Devices");
		}
		log.info("End Of Audio Devices");
		log.info("JVM Version: " + System.getProperty("java.version"));
		printSystemProperties();
		SimpleDevice sd = new SimpleDevice();
		
		// loadPlugins();
		sd.attachShutDownHook();
		if (bInput) {

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line = "";

			try {
				while (line.equalsIgnoreCase("quit") == false) {
					line = in.readLine();
				}
				in.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

		else {
			while (!stop) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.error("Error", e);
				}
			}
		}
		System.exit(0);
	}

	/***
	 * List all the files in this directory and sub directories.
	 * 
	 * @param directoryName
	 * @return
	 */
	// public static List<File> listFiles(String directoryName) {
	// File directory = new File(directoryName);
	// List<File> resultList = new ArrayList<File>();
	// File[] fList = directory.listFiles();
	// resultList.addAll(Arrays.asList(fList));
	// for (File file : fList) {
	// if (file.isFile()) {
	// } else if (file.isDirectory()) {
	// resultList.addAll(listFiles(file.getAbsolutePath()));
	// }
	// }
	// return resultList;
	// }

	/***
	 * Print out the System Properties.
	 */
	private static void printSystemProperties() {
		log.warn("#####Start of System Properties#########");
		Properties pr = System.getProperties();
		TreeSet propKeys = new TreeSet(pr.keySet());
		for (Iterator it = propKeys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			log.warn("" + key + "=" + pr.get(key));
		}
		log.warn("#####End of System Properties#########");
		log.warn("");
		Map<String, String> variables = System.getenv();
		log.warn("#####Start of System Variables#########");
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			log.warn(name + "=" + value);
		}
		log.warn("#####End of System Variables#########");
		log.warn("");
	}

	/***
	 * Read the app.properties file
	 */
	private static void getConfig() {
		Properties pr = new Properties();
		try {
			pr.load(new FileInputStream("app.properties"));
			Config.friendly_name = pr.getProperty("friendly.name");

			try {
				String playlists = pr.getProperty("mplayer.playlist");
				String[] splits = playlists.split(",");
				Config.playlists = Arrays.asList(splits);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Config.debug = pr.getProperty("openhome.debug.level");
			Config.logfile = pr.getProperty("log.file");
			Config.loglevel = pr.getProperty("log.file.level");
			Config.logconsole = pr.getProperty("log.console.level");
			Config.mplayer_path = pr.getProperty("mplayer.path");
			Config.setSaveLocalPlayList(pr.getProperty("save.local.playlist"));
			Config.port = Config.converStringToInt(pr.getProperty("openhome.port"));
			Config.mplayer_cache = Config.converStringToInt(pr.getProperty("mplayer.cache"));
			Config.mplayer_cache_min = Config.converStringToInt(pr.getProperty("mplayer.cache_min"));
			Config.playlist_max = Config.converStringToInt(pr.getProperty("playlist.max"), 1000);
			Config.mpd_host = pr.getProperty("mpd.host");
			Config.mpd_port = Config.converStringToInt(pr.getProperty("mpd.port"), 6600);
			Config.mpd_preload_timer = Config.converStringToInt(pr.getProperty("mpd.preload.timer"), 10);
			Config.player = pr.getProperty("player");
			Config.enableAVTransport = Config.convertStringToBoolean(pr.getProperty("enableAVTransport"), true);
			Config.enableReceiver = Config.convertStringToBoolean(pr.getProperty("enableReceiver"), true);
			//Config.songcastNICName = NetworkUtils.getNICName(pr.getProperty("songcast.nic.name"));
			Config.songcastSoundCardName = pr.getProperty("songcast.soundcard.name");
			Config.songcastLatencyEnabled = Config.convertStringToBoolean(pr.getProperty("songcast.latency.enabled"),true);
			Config.webHttpPort=pr.getProperty("web.http.port");
			Config.radio_tunein_username = pr.getProperty("radio.tunein.username");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Used to set the Songcast Audio Device
	 */
	private static void setAudioDevice() {
		Properties props = System.getProperties();
		String name = "#" + Config.songcastSoundCardName;
		props.setProperty("javax.sound.sampled.SourceDataLine", name);
		log.warn("###Setting Sound Card Name: " + name);
	}

	/***
	 * Set up our logging
	 */
	private static void ConfigureLogging() {

		try {
			CustomPatternLayout pl = new CustomPatternLayout();
			pl.setConversionPattern("%d [%t] %-5p [%-10c] %m%n");
			pl.activateOptions();
			// CustomRollingFileAppender fileAppender = new
			// CustomRollingFileAppender(pl,Config.logfile,".log",true);
			RollingFileAppender fileAppender = new RollingFileAppender();
			fileAppender.setAppend(true);
			fileAppender.setMaxFileSize("5mb");
			fileAppender.setMaxBackupIndex(5);
			fileAppender.setFile(Config.logfile);
			fileAppender.setThreshold(Config.getLogFileLevel());
			fileAppender.setLayout(pl);
			fileAppender.activateOptions();
			Logger.getRootLogger().addAppender(fileAppender);
			ConsoleAppender consoleAppender = new ConsoleAppender();
			consoleAppender.setLayout(pl);
			consoleAppender.activateOptions();
			consoleAppender.setThreshold(Config.getLogConsoleLevel());
			Logger.getRootLogger().addAppender(consoleAppender);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
