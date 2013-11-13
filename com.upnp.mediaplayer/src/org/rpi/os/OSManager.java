package org.rpi.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;

import org.apache.log4j.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;

public class OSManager {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean bRaspi = false;
	private boolean bSoftFloat = false;
	private PluginManager pm = null;
	private static OSManager instance = null;
	private GpioController gpio = null;

	public static OSManager getInstance() {
		if (instance == null) {
			instance = new OSManager();
		}
		return instance;
	}

	protected OSManager() {
		SetJavaPath();
		if(isRaspi())
		{
			log.debug("This is a Raspi so Attempt to initialize Pi4J");
			initPi4J();
		}
	}
	
	private void initPi4J()
	{
		try
		{
		setGpio(GpioFactory.getInstance());
		}
		catch(Exception e)
		{
			log.error("Error Initializing Pi4J",e);
		}
	}

	/**
	 * Not clever enough to work out how to override ClassLoader functionality,
	 * so using this nice trick instead..
	 * 
	 * @param path
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void addLibraryPath(String pathToAdd) throws Exception {
		Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);

		String[] paths = (String[]) usrPathsField.get(null);

		for (String path : paths)
			if (path.equals(pathToAdd))
				return;

		String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}

	/**
	 * Set the Path to the ohNetxx.so files
	 */
	private void SetJavaPath() {
		try

		{
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = getFilePath(class_name);
			log.debug("Path of this File is: " + path);
			String os = System.getProperty("os.name").toUpperCase();
			log.debug("OS Name: " + os);
			if (os.startsWith("WINDOWS")) {
				log.debug("Windows OS");
				// System.setProperty("java.library.path", path +
				// "/mediaplayer_lib/ohNet/win32");
				addLibraryPath(path + "/mediaplayer_lib/ohNet/win32");
			} else if (os.startsWith("LINUX")) {
				String arch = System.getProperty("os.arch").toUpperCase();
				if (arch.startsWith("ARM")) {
					log.debug("Its a Raspi, check for HardFloat or SoftFloat");
					setRaspi(true);
					// readelf -a /usr/bin/readelf | grep armhf
					boolean hard_float = true;
					String command = "dpkg -l | grep 'armhf\\|armel'";
					String full_path = path + "/mediaplayer_lib/ohNet/raspi/hard_float";
					try {
						Process pa = Runtime.getRuntime().exec(command);
						pa.waitFor();
						BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));
						String line;
						while ((line = reader.readLine()) != null) {
							log.debug("Result of " + command + " : " + line);
							if (line.toUpperCase().contains("ARMHF")) {
								log.debug("HardFloat Raspi Set java.library.path to be: " + path);

								hard_float = true;
								break;
							} else if (line.toUpperCase().contains("ARMEL")) {
								full_path = path + "/mediaplayer_lib/ohNet/raspi/soft_float";
								log.debug("SoftFloat Raspi Set java.library.path to be: " + path);
								hard_float = false;
								setSoftFloat(true);
								break;
							}

						}
					} catch (Exception e) {
						log.debug("Error Determining Raspi OS Type: ", e);
					}
					addLibraryPath(full_path);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}

	}

	/***
	 * Get the Path of this ClassFile Must be easier ways to do this!!!!
	 * 
	 * @param className
	 * @return
	 */
	private String getFilePath(String className) {
		if (!className.startsWith("/")) {
			className = "/" + className;
		}
		className = className.replace('.', '/');
		className = className + ".class";
		log.debug("Find Class, Full ClassName: " + className);
		String[] splits = className.split("/");
		String properName = splits[splits.length - 1];
		log.debug("Find Class, ClassName: " + properName);
		URL classUrl = this.getClass().getResource(className);
		if (classUrl != null) {
			String temp = classUrl.getFile();
			log.debug("Find Class, ClassURL: " + temp);
			if (temp.startsWith("file:")) {
				temp = temp.substring(5);
			}

			if (temp.toUpperCase().contains(".JAR!")) {
				log.debug("Find Class, This is a JarFile: " + temp);
				String[] parts = temp.split("/");
				String jar_path = "";
				for (String part : parts) {
					if (!part.toUpperCase().endsWith(".JAR!")) {
						jar_path += part + "/";
					} else {
						log.debug("Find File: Returning JarPath: " + jar_path);
						return jar_path;
					}
				}
			} else {
				log.debug("Find Class, This is NOT a Jar File: " + temp);
				if (temp.endsWith(className)) {
					temp = temp.substring(0, (temp.length() - className.length()));
				}
			}
			log.debug("Find File: Returning FilePath: " + temp);
			return temp;
		} else {
			log.debug("Find Class, URL Not Found");
			return "\nClass '" + className + "' not found in \n'" + System.getProperty("java.class.path") + "'";
		}
	}

	/***
	 * Load the Plugins
	 */
	public void loadPlugins() {
		try {
			log.info("Start of LoadPlugnis");
			pm = PluginManagerFactory.createPluginManager();
			List<File> files = listFiles("plugins");
			if(files==null)
				return;
			for (File file : files) {
				try {
					if (file.getName().toUpperCase().endsWith(".JAR")) {
						log.debug("Attempt to Load Plugin: " + file.getName());
						pm.addPluginsFrom(file.toURI());
					}
				} catch (Exception e) {
					log.error("Unable to load Plugins", e);
				}
			}
			log.info("End of LoadPlugnis");
		} catch (Exception e) {
			log.error("Error Loading Plugins");
		}
	}

	/***
	 * List all the files in this directory and sub directories.
	 * 
	 * @param directoryName
	 * @return
	 */
	private List<File> listFiles(String directoryName) {
		File directory = new File(directoryName);
		List<File> resultList = new ArrayList<File>();
		File[] fList = directory.listFiles();
		if (fList == null)
			return resultList;
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isFile()) {
			} else if (file.isDirectory()) {
				resultList.addAll(listFiles(file.getAbsolutePath()));
			}
		}
		return resultList;
	}

	/**
	 * Is this a Raspberry Pi
	 * @return
	 */
	public boolean isRaspi() {
		return bRaspi;
	}

	private void setRaspi(boolean bRaspi) {
		this.bRaspi = bRaspi;
	}

	/**
	 * Is this a SoftFloat Raspberry Pi
	 * @return
	 */
	public boolean isSoftFloat() {
		return bSoftFloat;
	}

	private void setSoftFloat(boolean bSoftFloat) {
		this.bSoftFloat = bSoftFloat;
	}

	/**
	 * Tidy up..
	 */
	public void dispose() {
		try {
			if (pm != null) {
				pm.shutdown();
			}
		} catch (Exception e) {
			log.error("Error closing PluginManager",e);
		}
		try
		{
			gpio.shutdown();
		}
		catch(Exception e)
		{
			log.error("Error closing pi4j",e);
		}
	}

	public GpioController getGpio() {
		return gpio;
	}

	private void setGpio(GpioController gpio) {
		this.gpio = gpio;
	}

}
