package org.rpi.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.rpi.config.Config;


public class StartMe {

	private static boolean stop = false;
	private static Logger log = Logger.getLogger(StartMe.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// loadClassPath();
		boolean bInput = false;
		for (String s : args) {
			if (s.equalsIgnoreCase("-input")) {
				bInput = true;
			}
		}
		getConfig();
		ConfigureLogging();
		log.info("Starting......");
		log.info("JVM Version: " + System.getProperty("java.version"));
		printSystemProperties();
		SimpleDevice sd = new SimpleDevice();
		loadPlugins();
		sd.attachShutDownHook();
		if (bInput) {

			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
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

	
//	private static void loadClassPath() {
//		String path = "/home/pi/mediaplayer/mediaplayer_lib";
//		String files;
//		File folder = new File(path);
//		File[] listOfFiles = folder.listFiles();
//		System.out.println("Number of Files: " + listOfFiles.length);
//		for (int i = 0; i < listOfFiles.length; i++) {
//
//			if (listOfFiles[i].isFile()) {
//				files = listOfFiles[i].getName();
//				if (files.toUpperCase().endsWith(".JAR")) {
//					try {
//						System.out.println("File: " + files);
//						ClassPathHack.addFile(path + "\\" + files);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
//	}

	/***
	 * Load the Plugins
	 */
	private static void loadPlugins() {
		log.info("Start of LoadPlugnis");
		PluginManager pm = PluginManagerFactory.createPluginManager();
		List<File> files = listFiles("plugins");
		for (File file : files) {
			try {
				if (file.getName().toUpperCase().endsWith(".JAR")) {
					pm.addPluginsFrom(file.toURI());
				}
			} catch (Exception e) {
				log.error("Unable to load Plugins", e);
			}
		}
		log.info("End of LoadPlugnis");
	}

	/***
	 * List all the files in this directory and sub directories.
	 * @param directoryName
	 * @return
	 */
	public static List<File> listFiles(String directoryName) {
		File directory = new File(directoryName);
		List<File> resultList = new ArrayList<File>();
		File[] fList = directory.listFiles();
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isFile()) {
			} else if (file.isDirectory()) {
				resultList.addAll(listFiles(file.getAbsolutePath()));
			}
		}
		return resultList;
	}

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

			String playlists = pr.getProperty("playlist");
			String[] splits = playlists.split(",");
			Config.playlists = Arrays.asList(splits);
			Config.debug = pr.getProperty("openhome.debug.level");
			Config.logfile = pr.getProperty("log.file");
			Config.loglevel = pr.getProperty("log.file.level");
			Config.logconsole = pr.getProperty("log.console.level");
			Config.mplayer_path = pr.getProperty("mplayer.path");
			Config.setSaveLocalPlayList(pr.getProperty("save.local.playlist"));
			Config.port = Config.converStringToInt(pr
					.getProperty("openhome.port"));
			Config.mplayer_cache = Config.converStringToInt(pr
					.getProperty("mplayer.cache"));
			Config.mplayer_cache_min = Config.converStringToInt(pr
					.getProperty("mplayer.cache_min"));
			Config.playlist_max = Config.converStringToInt(pr
					.getProperty("playlist.max"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/***
	 * Set up our logging
	 */
	private static void ConfigureLogging() {
		RollingFileAppender fileAppender = new RollingFileAppender();
		fileAppender.setAppend(true);
		fileAppender.setMaxFileSize("5mb");
		fileAppender.setMaxBackupIndex(5);
		fileAppender.setFile(Config.logfile);
		fileAppender.setThreshold(Config.getLogFileLevel());
		PatternLayout pl = new PatternLayout();
		pl.setConversionPattern("%d [%t] %-5p [%-10c] %m%n");
		pl.activateOptions();
		fileAppender.setLayout(pl);
		fileAppender.activateOptions();
		Logger.getRootLogger().addAppender(fileAppender);
		ConsoleAppender consoleAppender = new ConsoleAppender();
		consoleAppender.setLayout(pl);
		consoleAppender.activateOptions();
		consoleAppender.setThreshold(Config.getLogConsoleLevel());
		Logger.getRootLogger().addAppender(consoleAppender);
	}

}
