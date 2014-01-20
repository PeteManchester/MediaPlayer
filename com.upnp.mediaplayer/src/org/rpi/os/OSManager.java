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

public class OSManager {

	private Logger log = Logger.getLogger(this.getClass());
	private boolean bRaspi = false;
	private boolean bSoftFloat = false;
	private PluginManager pm = null;
	private boolean bUsedPi4J = false;
	private static OSManager instance = null;

    private static final String OHNET_LIB_DIR = "/mediaplayer_lib/ohNet";

	public static OSManager getInstance() {
		if (instance == null) {
			instance = new OSManager();
		}
		return instance;
	}

	protected OSManager() {
		setJavaPath();
		if (isRaspi()) {
			log.debug("This is a Raspi so Attempt to initialize Pi4J");
			// initPi4J();
		}
	}

	// private void initPi4J()
	// {
	// try
	// {
	// setGpio(GpioFactory.getInstance());
	// }
	// catch(Exception e)
	// {
	// log.error("Error Initializing Pi4J",e);
	// }
	// }

	/**
	 * Not clever enough to work out how to override ClassLoader functionality,
	 * so using this nice trick instead..
	 * 
	 * @param pathToAdd
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public void addLibraryPath(String pathToAdd) throws Exception {
		log.debug("Adding Path: " + pathToAdd);
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
	private void setJavaPath() {
		try	{
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = getFilePath(this.getClass(), true);
			String full_path = path + OHNET_LIB_DIR + "/default";
			log.debug("Path of this File is: " + path);
			String os = System.getProperty("os.name").toUpperCase();
			log.debug("OS Name: " + os);
			if (os.startsWith("WINDOWS")) {
				log.debug("Windows OS");
				String osPathName = "windows";
                String osArch = System.getProperty("os.arch");

                String architecture = "x86";
                if (osArch.endsWith("64")) {
                    architecture = "x64";
                }

                full_path = path + OHNET_LIB_DIR + "/" + osPathName + "/" + architecture;
			}
            else if (os.startsWith("LINUX")) {
                String osPathName = "linux";

				String arch = System.getProperty("os.arch").toUpperCase();
				if (arch.startsWith("ARM")) {
                    String osArch = "arm";

					log.debug("Its an ARM device, now check, which revision");
                    String elfCommand = "readelf -A " + System.getProperty("java.home") + "/lib/arm/libjava.so";
                    try {
                        Process pa = Runtime.getRuntime().exec(elfCommand);
                        pa.waitFor();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));
                        String line;
                        String armVersion = "";
                        Boolean isHardFloat = Boolean.FALSE;
                        while ((line = reader.readLine()) != null) {
                            log.debug("Result of " + elfCommand + " : " + line);
                            if (line.startsWith("Tag_CPU_arch: ")) {
                                armVersion = line.substring(line.indexOf(" "));
                            }
                            else if (line.startsWith("Tag_ABI_VFP_args:")) {
                                isHardFloat = Boolean.TRUE;
                            }
                        }

                        if (armVersion.equals("v5")) {
                            osArch = osArch + "v5sf";
                        }
                        else if (armVersion.equals("v6")) {
                            // we believe that a v6 arm is always a raspi (could be a pogoplug...)
                            setRaspi(true);
                            if (isHardFloat) {
                                osArch = osArch + "v6hf";
                            }
                            else {
                                osArch = osArch + "v6sf";
                            }
                        }
                        else {
                            osArch = osArch + "v7";
                        }

                    }
                    catch (Exception e) {
                            log.debug("Error Determining Raspi OS Type: ", e);
                    }
				}
                else if(arch.startsWith("I386")) {
					String version = System.getProperty("os.version");
					log.debug("OS is Linux, and arch is  " + arch + ". Version is: " + version);
    				full_path = path + OHNET_LIB_DIR + "/" + osPathName + "/x86";
				}
                else if(arch.startsWith("AMD64")) {
					String version = System.getProperty("os.version");
					log.debug("OS is Linux, and arch is " + arch + ". Version is: " + version);
					full_path = path + OHNET_LIB_DIR + "/" + osPathName + "/amd64";
				}
			}

            log.debug("using full_path " + full_path);
			addLibraryPath(full_path);
		} catch (Exception e) {
			log.error(e);
		}

	}

	/***
	 * Get the Path of this ClassFile Must be easier ways to do this!!!!
	 * 
	 * @param mClass
     * @param bUseFullNamePath
	 * @return
	 */
	public synchronized String getFilePath(Class mClass, boolean bUseFullNamePath) {
		String className = mClass.getName();
		if (!className.startsWith("/")) {
			className = "/" + className;
		}
		className = className.replace('.', '/');
		className = className + ".class";
		log.debug("Find Class, Full ClassName: " + className);
		String[] splits = className.split("/");
		String properName = splits[splits.length - 1];
		log.debug("Find Class, Proper ClassName: " + properName);
		URL classUrl = mClass.getResource(className);
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
					if (bUseFullNamePath) {
						temp = temp.substring(0, (temp.length() - className.length()));
					} else {
						temp = temp.substring(0, (temp.length() - properName.length()));
					}
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
			if (files == null)
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
	 * 
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
	 * 
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
			log.error("Error closing PluginManager", e);
		}
		try {
			if (bUsedPi4J)
				Pi4JManager.getInstance().dispose();
		} catch (Exception e) {
			log.error("Error closing pi4j", e);
		}
	}

	public GpioController getGpio() {
		bUsedPi4J = true;
		return Pi4JManager.getInstance().getGpio();
	}
}
