package org.rpi.os;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.rpi.utils.Utils;
import com.pi4j.io.gpio.GpioController;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.PluginManager;

public class OSManager {

	private static Logger log = Logger.getLogger(OSManager.class);
	private boolean bRaspi = false;
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
		log.debug("Initializing OSManager");
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
		try {
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
			} else if (os.startsWith("LINUX")) {
				String osPathName = "linux";

				String arch = System.getProperty("os.arch").toUpperCase();
				if (arch.startsWith("ARM")) {
					String osArch = "arm";

					log.debug("Its an ARM device, now check, which revision");
					try {
                        String armVersion = getReadElfTag("Tag_CPU_arch");

						if (armVersion == null) {
                            log.error("Cannot determine ARM version...");
                            osArch = "UNKNOWN";
                        } else if (armVersion.equals("v5")) {
							osArch = osArch + "v5sf";
						} else if (armVersion.equals("v6")) {
							// we believe that a v6 arm is always a raspi (could
							// be a pogoplug...)
							log.debug("We think this is a Raspi");
							setRaspi(true);
							if (isHardFloat()) {
								osArch = osArch + "v6hf";
							} else {
								osArch = osArch + "v6sf";
							}
						} else if (armVersion.equals("v7")) {
							osArch = osArch + "v7";
						} else {
                            log.error("Unknown ARM version...(" + armVersion + ")");
                            osArch = "UNKNOWN";
                        }

                        if (!osArch.equals("UNKNOWN"))  {
                            full_path = path + OHNET_LIB_DIR + "/" + osPathName + "/" + osArch;
                        }
					} catch (Exception e) {
						log.debug("Error Determining ARM OS Type: ", e);
					}
				} else if (arch.startsWith("I386")) {
					String version = System.getProperty("os.version");
					log.debug("OS is Linux, and arch is  " + arch + ". Version is: " + version);
					full_path = path + OHNET_LIB_DIR + "/" + osPathName + "/x86";
				} else if (arch.startsWith("AMD64")) {
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
	 * Get the Path of this ClassFile and/or the path of the current JAR, which should be basically the same! No?
	 * Returned to the old method because the new method did not work when a plugin requested the path.
	 * Getting LCD.xml from Directory: /C:/Keep/git/repository/MediaPlayer/com.upnp.mediaplayer/bin
	 * Instead of
	 * /C:/Keep/git/repository/MediaPlayer/com.upnp.mediaplayer/bin/org/rpi/plugin/lcddisplay/
	 * @return
	 */
//	public synchronized String getFilePath(Class mClass, boolean bUseFullNamePath) {
//        String path = mClass.getProtectionDomain().getCodeSource().getLocation().getPath();
//        String retValue = path.substring(0, path.lastIndexOf("/"));
//        log.debug("Returning Path of Class: " + mClass.getName() + " " + retValue);
//        return retValue;
//	}
	
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
			log.info("Start of LoadPlugins");
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
		return !isHardFloat();
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

// the following is taken fully from pi4j (https://github.com/Pi4J/pi4j/blob/master/pi4j-core/src/main/java/com/pi4j/system/SystemInfo.java)
// we should get rid of this dependency, but right now it does work nicely

    /*
     * this method was partially derived from :: (project) jogamp / (developer) sgothel
     * https://github.com/sgothel/gluegen/blob/master/src/java/jogamp/common/os/PlatformPropsImpl.java#L160
     * https://github.com/sgothel/gluegen/blob/master/LICENSE.txt
     *
     */
    public  boolean isHardFloat() {

        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            ArrayList<String> gnueabihf = new ArrayList<String>();
           public Boolean run() {
           	gnueabihf.add("gnueabihf");
           	gnueabihf.add("armhf");
               if (Utils.containsString(System.getProperty("sun.boot.library.path"), gnueabihf) ||
               		Utils.containsString(System.getProperty("java.library.path"), gnueabihf) ||
               		Utils.containsString(System.getProperty("java.home"), gnueabihf) ||
                       getBashVersionInfo().contains("gnueabihf") ||
                       hasReadElfTag("Tag_ABI_HardFP_use")) {
            	   log.debug("This is a HardFloat");
                   return true; //
               }
               log.debug("This is a HardFloat");
               return false;
           }
       });
    }
    


    /*
     * taken from https://github.com/Pi4J/pi4j/blob/master/pi4j-core/src/main/java/com/pi4j/system/SystemInfo.java
     *
     * this method will to obtain the version info string from the 'bash' program
     * (this method is used to help determine the HARD-FLOAT / SOFT-FLOAT ABI of the system)
     */
    private static String getBashVersionInfo() {
        String versionInfo = "";
        try {
            String result[] = Utils.execute("bash --version");
            for(String line : result) {
                if(!line.isEmpty()) {
                    versionInfo = line; // return only first output line of version info
                    break;
                }
            }
        }
        catch(Exception e)
        {
        	log.error("Error Executing bash --version", e);
        }
        //catch (IOException ioe) { ioe.printStackTrace(); }
        //catch (InterruptedException ie) { ie.printStackTrace(); }
        return versionInfo;
    }

    /*
     * this method will determine if a specified tag exists from the elf info in the '/proc/self/exe' program
     * (this method is used to help determine the HARD-FLOAT / SOFT-FLOAT ABI of the system)
     */
    private static boolean hasReadElfTag(String tag) {
        String tagValue = getReadElfTag(tag);
        if(tagValue != null && !tagValue.isEmpty())
            return true;
        return false;
    }

    /*
     * this method will obtain a specified tag value from the elf info in the '/proc/self/exe' program
     * (this method is used to help determine the HARD-FLOAT / SOFT-FLOAT ABI of the system)
     */
    private static String getReadElfTag(String tag) {
        String tagValue = null;
        try {
            String result[] = Utils.execute("/usr/bin/readelf -A /proc/self/exe");
            if(result != null){
                for(String line : result) {
                    line = line.trim();
                    if (line.startsWith(tag) && line.contains(":")) {
                        String lineParts[] = line.split(":", 2);
                        if(lineParts.length > 1)
                            tagValue = lineParts[1].trim();
                        break;
                    }
                }
            }
        }
        catch(Exception e)
        {
        	log.error("IOException during readelf operation", e);
        }
        //catch (IOException ioe) {
        //    log.error("IOException during readelf operation", ioe);
        //}
        //catch (InterruptedException ie) {
        //    log.error("InterruptedEx during readelf operation", ie);

        //}
        return tagValue;
    }


}
