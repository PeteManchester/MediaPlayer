package org.rpi.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;


import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.mplayer.CloseMe;
import org.rpi.utils.NetworkUtils;
import org.rpi.utils.Utils;

import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

public class StartMe {

	private static boolean stop = false;
	private static Logger log = Logger.getLogger(StartMe.class);

	// private static PluginManager pm = null;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//ResourceLeakDetector.setLevel(Level.PARANOID);
		Config.getInstance();
		boolean bInput = false;
		for (String s : args) {
			if (s.equalsIgnoreCase("-input")) {
				bInput = true;
			}
		}

		log.info("Starting......");

		if (log.isInfoEnabled()) {
			// to improve startup performance, if loglevel info is not enabled,
			// this is not needed, right?
			NetworkUtils.printNetworkInterfaceDetails();
		}

		// Do we need to attempt to set the AudioCard
		if (Config.getInstance().isMediaplayerEnableReceiver() || Config.getInstance().isAirPlayEnabled()) {
			log.info("Available Audio Devices:");
			try {
				Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
				List<String> endsWith = Config.getInstance().getJavaSoundcardSuffix();
				boolean bFoundSoundCard = false;
				for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
					String mixer = mixerInfo[cnt].getName().trim();
					log.info("'" + mixer + "'");
					if (!bFoundSoundCard) {
						for (String endWith : endsWith) {
							if (mixer.trim().toUpperCase().endsWith(endWith.trim().toUpperCase())) {
								Config.getInstance().setJavaSoundcardName(mixer.trim());
								bFoundSoundCard = true;
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Error getting Audio Devices");
			}
			log.info("End Of Audio Devices");
		}
		setAudioDevice();
		log.info("JVM Version: " + System.getProperty("java.version"));
		printSystemProperties();
		try {
			SimpleDevice sd = new SimpleDevice();
			sd.attachShutDownHook();
			if (bInput) {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(System.in));
					String line = "";
					try {
						while (line.equalsIgnoreCase("quit") == false) {
							line = in.readLine();
						}
						in.close();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				} catch (Exception e) {

				} finally {
					if (in != null) {
						CloseMe.close(in);
						in = null;
					}
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
		} catch (Exception e) {
			log.error("PETE!!!!!", e);
		}

		// loadPlugins();

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
		log.fatal("#####Start of System Properties#########");
		Properties pr = System.getProperties();
		TreeSet propKeys = new TreeSet(pr.keySet());
		for (Iterator it = propKeys.iterator(); it.hasNext();) {
			String key = (String) it.next();
			log.fatal("" + key + "=" + pr.get(key));
		}
		log.fatal("#####End of System Properties#########");
		log.fatal("");
		Map<String, String> variables = System.getenv();
		log.fatal("#####Start of System Variables#########");
		for (Map.Entry<String, String> entry : variables.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			log.fatal(name + "=" + value);
		}
		log.fatal("#####End of System Variables#########");
		log.fatal("");
	}

	/**
	 * Used to set the Songcast Audio Device
	 */
	private static void setAudioDevice() {
		Properties props = System.getProperties();
		String name = Config.getInstance().getJavaSoundcardName();
		if (!Utils.isEmpty(name)) {
			props.setProperty("javax.sound.sampled.SourceDataLine", name);
			log.fatal("###Setting Sound Card Name: " + name);
		}
	}

}
