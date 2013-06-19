package org.rpi.main;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openhome.net.core.DebugLevel;
import org.openhome.net.core.DeviceStack;
import org.openhome.net.core.IMessageListener;
import org.openhome.net.core.InitParams;
import org.openhome.net.core.Library;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.DvDeviceFactory;
import org.openhome.net.device.IDvDeviceListener;
import org.openhome.net.device.IResourceManager;
import org.openhome.net.device.IResourceWriter;
import org.rpi.config.Config;
import org.rpi.playlist.PlayManager;
import org.rpi.providers.PrvAVTransport;
import org.rpi.providers.PrvConnectionManager;
import org.rpi.providers.PrvInfo;
import org.rpi.providers.PrvPlayList;
import org.rpi.providers.PrvProduct;
import org.rpi.providers.PrvRadio;
import org.rpi.providers.PrvReceiver;
import org.rpi.providers.PrvRenderingControl;
import org.rpi.providers.PrvTime;
import org.rpi.providers.PrvVolume;
import org.rpi.radio.ChannelReader;

public class SimpleDevice implements IResourceManager, IDvDeviceListener, IMessageListener {

	private Logger log = Logger.getLogger(SimpleDevice.class);
	private DvDevice iDevice = null;
	private Library lib = null;

	private PrvConnectionManager iConnectionManager = null;
	private PrvVolume iVolume = null;
	private PrvPlayList iPlayList = null;
	private PrvProduct iProduct = null;
	private PrvInfo iInfo = null;
	private PrvTime iTime = null;
	private PrvRadio iRadio;
	private PrvReceiver iReceiver = null;
	private PrvAVTransport iAVTransport = null;
	private PrvRenderingControl iRenderingControl = null;

	private PlayManager iPlayer = PlayManager.getInstance();

	/***
	 * Constructor for our Simple Device
	 */
	public SimpleDevice() {
		log.debug("Creating Simple Device version: " + Config.version);
		InitParams initParams = new InitParams();
		initParams.setLogOutput(new OpenHomeLogger());
		if (Config.port > 0) {
			initParams.setDvServerPort(Config.port);
		}
		//initParams.setDvEnableBonjour();
		initParams.setFatalErrorHandler(this);
		lib = Library.create(initParams);
		lib.setDebugLevel(getDebugLevel(Config.debug));

		DeviceStack ds = lib.startDv();
		String iDeviceName = "device-" + Config.friendly_name + "-" + GetHostName() + "-MediaRenderer";
		iDevice = new DvDeviceFactory(ds).createDeviceStandard(iDeviceName, this);
		log.debug("Created StandardDevice: " + iDevice.getUdn());
		iDevice.setAttribute("Upnp.Domain", "openhome.org");
		iDevice.setAttribute("Upnp.Type", "Music Renderer");
		iDevice.setAttribute("Upnp.Version", "1");
		iDevice.setAttribute("Upnp.FriendlyName", Config.friendly_name);
		iDevice.setAttribute("Upnp.Manufacturer", "Pete");
		iDevice.setAttribute("Upnp.ModelName", "Open Home Java Render v" + Config.version);
		iDevice.setAttribute("Upnp.ModelDescription", "Simple Java Renderer based on OpenHome API and MPlayer");
		//iDevice.setAttribute("Upnp.ModelUri", "www.google.co.uk");
		//iDevice.setAttribute("Upnp.ModelImageUri","http://upload.wikimedia.org/wikipedia/en/thumb/0/04/Joy_Division.JPG/220px-Joy_Division.JPG");

		iConnectionManager = new PrvConnectionManager(iDevice);
		iProduct = new PrvProduct(iDevice);
		iVolume = new PrvVolume(iDevice);
		iPlayList = new PrvPlayList(iDevice);
		iInfo = new PrvInfo(iDevice);
		iTime = new PrvTime(iDevice);
		iRadio = new PrvRadio(iDevice);
		iReceiver = new PrvReceiver(iDevice);
		//iAVTransport = new PrvAVTransport(iDevice);
		//iRenderingControl = new PrvRenderingControl(iDevice);

		try {
			ChannelReader cr = new ChannelReader();
			iRadio.addChannels(cr.getChannels());
		} catch (Exception e) {
			log.error("Error Reading Radio Channels");
		}

		iDevice.setEnabled();
		log.debug("Device Enabled UDN: " + iDevice.getUdn());
	}

	private int getDebugLevel(String sLevel) {

		if (sLevel.equalsIgnoreCase("NONE")) {
			return DebugLevel.None.intValue();
		} else if (sLevel.equalsIgnoreCase("TRACE")) {
			return DebugLevel.Trace.intValue();
		} else if (sLevel.equalsIgnoreCase("NETWORK")) {
			return DebugLevel.Network.intValue();
		} else if (sLevel.equalsIgnoreCase("TIMER")) {
			return DebugLevel.Timer.intValue();
		} else if (sLevel.equalsIgnoreCase("SsdpMulticast")) {
			return DebugLevel.SsdpMulticast.intValue();
		} else if (sLevel.equalsIgnoreCase("SsdpUnicast")) {
			return DebugLevel.SsdpUnicast.intValue();
		} else if (sLevel.equalsIgnoreCase("Http")) {
			return DebugLevel.Http.intValue();
		} else if (sLevel.equalsIgnoreCase("Device")) {
			return DebugLevel.Device.intValue();
		} else if (sLevel.equalsIgnoreCase("XmlFetch")) {
			return DebugLevel.XmlFetch.intValue();
		} else if (sLevel.equalsIgnoreCase("Service")) {
			return DebugLevel.Service.intValue();
		} else if (sLevel.equalsIgnoreCase("Event")) {
			return DebugLevel.Event.intValue();
		} else if (sLevel.equalsIgnoreCase("Topology")) {
			return DebugLevel.Topology.intValue();
		} else if (sLevel.equalsIgnoreCase("DvInvocation")) {
			return DebugLevel.DvInvocation.intValue();
		} else if (sLevel.equalsIgnoreCase("DvInvocation")) {
			return DebugLevel.DvInvocation.intValue();
		} else if (sLevel.equalsIgnoreCase("DvEvent")) {
			return DebugLevel.DvEvent.intValue();
		} else if (sLevel.equalsIgnoreCase("DvWebSocket")) {
			return DebugLevel.DvWebSocket.intValue();
		} else if (sLevel.equalsIgnoreCase("Bonjour")) {
			return DebugLevel.Bonjour.intValue();
		} else if (sLevel.equalsIgnoreCase("DvDevice")) {
			return DebugLevel.DvDevice.intValue();
		} else if (sLevel.equalsIgnoreCase("Error")) {
			return DebugLevel.Error.intValue();
		} else if (sLevel.equalsIgnoreCase("All")) {
			return DebugLevel.All.intValue();
		} else if (sLevel.equalsIgnoreCase("Verbose")) {
			return DebugLevel.Verbose.intValue();
		}
		return DebugLevel.None.intValue();
	}

	public void attachShutDownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.debug("Inside Add Shutdown Hook");
				dispose();
			}
		});
		log.debug("Shut Down Hook Attached.");
	}
	

	public void dispose() {
		
		if (iPlayer != null) {
			log.info("Destroying IPlayer");
			try {
				iPlayer.destroy();
				log.info("Destroyed IPlayer");
			} catch (Exception e) {
				log.error("Error Destroying IPlayer", e);
			}
		}
		
		if (iDevice != null) {
			log.info("Destroying device");

			try {
				iDevice.destroy();
				log.info("Destroyed device");
			} catch (Exception e) {
				log.error("Error Destroying Device", e);
			}
		}

		if (iConnectionManager != null) {
			log.info("Destroying ConnectionManager");
			try {
				iConnectionManager.dispose();
				log.info("Destroyed ConnectionManager");
			} catch (Exception e) {
				log.error("Error Destroying ConnectionManager", e);
			}
		}

		if (iPlayList != null) {
			log.info("Destroying PlayList");
			try {
				iPlayList.dispose();
				log.info("Destroyed PlayList");
			} catch (Exception e) {
				log.error("Error Destroying PlayList", e);
			}
		}

		if (iVolume != null) {
			log.info("Destroying Volume");
			try {
				iVolume.dispose();
				log.info("Destroyed Volume");
			} catch (Exception e) {
				log.error("Error Destroying Volume", e);
			}
		}

		if (iProduct != null) {
			log.info("Destroying Product");
			try {
				iProduct.dispose();
				log.info("Destroyed Product");
			} catch (Exception e) {
				log.error("Error Destroying Product", e);
			}
		}

		if (iInfo != null) {
			log.info("Destroying Info");
			try {
				iInfo.dispose();
				log.info("Destroyed Info");
			} catch (Exception e) {
				log.error("Error Destroying Info", e);
			}
		}

		if (iTime != null) {
			log.info("Destroying Time");
			try {
				iTime.dispose();
				log.info("Destroyed Time");
			} catch (Exception e) {
				log.error("Error Destroying Time", e);
			}

		}

		if (iRadio != null) {
			log.info("Destroying Radio");
			try {
				iRadio.dispose();
				log.info("Destroyed Radio");
			} catch (Exception e) {
				log.error("Error Destroying Radio", e);
			}

		}

		if (iReceiver != null) {
			log.info("Destroying Receiver");
			try {
				iReceiver.dispose();
				log.info("Destroyed Receiver");
			} catch (Exception e) {
				log.error("Error Destroying Receiver", e);
			}

		}

		if (iAVTransport != null) {
			log.info("Destroying AVTransport");
			try {
				iAVTransport.dispose();
				log.info("Destroyed AVTransport");
			} catch (Exception e) {
				log.error("Error Destroying AVTransport", e);
			}

		}

		if (iRenderingControl != null) {
			log.info("Destroying RenderingControl");
			try {
				iRenderingControl.dispose();
				log.info("Destroyed RenderingControl");
			} catch (Exception e) {
				log.error("Error Destroying RenderingControl", e);
			}

		}


		
		if (lib != null) {
			try {
				log.info("Attempting to Close DeviceStack");
				lib.close();
				log.info("Closed DeviceStack");
			} catch (Exception e) {
				log.error("Error Closing DeviceStack", e);
			}
		}


	}

	@Override
	public void writeResource(String arg0, int arg1, List<String> arg2, IResourceWriter arg3) {
		log.debug("writeResource Called");
	}

	/***
	 * Get the MAC Address..
	 * @return
	 */
	private String getMacAddress() {
		String mac = "";
		String command = "/sbin/ifconfig";

		String sOsName = System.getProperty("os.name");
		if (sOsName.startsWith("Windows")) {
			command = "ipconfig /all";
		} else {

			if ((sOsName.startsWith("Linux")) || (sOsName.startsWith("Mac")) || (sOsName.startsWith("HP-UX"))) {
				command = "/sbin/ifconfig";
			} else {
				log.info("The current operating system '" + sOsName + "' is not supported.");
			}
		}

		Pattern p = Pattern.compile("([a-fA-F0-9]{1,2}(-|:)){5}[a-fA-F0-9]{1,2}");
		try {
			Process pa = Runtime.getRuntime().exec(command);
			pa.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));

			String line;
			Matcher m;
			while ((line = reader.readLine()) != null) {
				log.debug(line);
				m = p.matcher(line);

				if (!m.find())
					continue;
				line = m.group();
				mac = line.replace(":", "");
				mac = mac.replace("-", "");
				break;

			}
			//System.out.println(line);
		} catch (Exception e) {
			log.debug("Error MAC Address: ", e);
		}
		return mac;
	}
	
	/***
	 * Get the HostName, if any problem attempt to get the MAC Address
	 * @return
	 */
	private String GetHostName()
	{
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String hostName = iAddress.getHostName();
		    //String canonicalHostName = iAddress.getCanonicalHostName();
		    return hostName;
		} catch (Exception e) {
			log.error("Error Getting HostName: ",e);
		}
	    return getMacAddress();
	}


	public PrvVolume getCustomVolume() {
		return iVolume;
	}

	@Override
	public void deviceDisabled() {
		log.info("Device has been Disabled");
		dispose();
	}

	@Override
	public void message(String paramString) {
		log.fatal("Fatal Error: " + paramString);	
	}
}
