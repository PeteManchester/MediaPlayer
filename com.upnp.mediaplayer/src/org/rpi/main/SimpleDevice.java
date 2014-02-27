package org.rpi.main;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

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
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.plugingateway.PluginGateWay;
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
import org.rpi.sources.Source;
import org.rpi.sources.SourceReader;
import org.rpi.utils.NetworkUtils;

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
	private PrvRadio iRadio =  null;
	private PrvReceiver iReceiver = null;
	private PrvAVTransport iAVTransport = null;
	private PrvRenderingControl iRenderingControl = null;

	private PlayManager iPlayer = PlayManager.getInstance();

	//private PluginManager pm = null;


	/***
	 * Constructor for our Simple Device
	 */
	public SimpleDevice() {
		PluginGateWay.getInstance().setSimpleDevice(this);
		log.debug("Creating Simple Device version: " + Config.version);
		// System.loadLibrary("ohNetJni");
		//Call the OSManager to set our path to the libohNet libraries
		OSManager.getInstance();

		InitParams initParams = new InitParams();
		initParams.setLogOutput(new OpenHomeLogger());
		if (Config.port > 0) {
			initParams.setDvServerPort(Config.port);
		}
		// initParams.setDvEnableBonjour();
		initParams.setFatalErrorHandler(this);

		lib = Library.create(initParams);
		lib.setDebugLevel(getDebugLevel(Config.debug));

		DeviceStack ds = lib.startDv();
		String friendly_name = Config.friendly_name.replace(":", " ");
		String iDeviceName = "device-" + friendly_name + "-" + NetworkUtils.getHostName() + "-MediaRenderer";
		iDevice = new DvDeviceFactory(ds).createDeviceStandard(iDeviceName, this);
		log.debug("Created StandardDevice: " + iDevice.getUdn());
		iDevice.setAttribute("Upnp.IconList", this.constructIconList(iDeviceName));

		// iDevice.setAttribute("Upnp.Domain", "openhome-org");
		iDevice.setAttribute("Upnp.Domain", "schemas-upnp-org");
		iDevice.setAttribute("Upnp.Type", "MediaRenderer");
		iDevice.setAttribute("Upnp.Version", "1");
		iDevice.setAttribute("Upnp.FriendlyName", Config.friendly_name);
		iDevice.setAttribute("Upnp.Manufacturer", "Made in Manchester");
		iDevice.setAttribute("Upnp.ModelName", "Open Home Java Renderer: v" + Config.version);
		iDevice.setAttribute("Upnp.ModelDescription", "'We Made History Not Money' - Tony Wilson..");
		// iDevice.setAttribute("Upnp.IconList" , sb.toString());
		// iDevice.setAttribute("Upnp.ModelUri", "www.google.co.uk");
		// iDevice.setAttribute("Upnp.ModelImageUri","http://upload.wikimedia.org/wikipedia/en/thumb/0/04/Joy_Division.JPG/220px-Joy_Division.JPG");

		iConnectionManager = new PrvConnectionManager(iDevice);
		iProduct = new PrvProduct(iDevice);
		iVolume = new PrvVolume(iDevice);
		iPlayList = new PrvPlayList(iDevice);
		iInfo = new PrvInfo(iDevice);
		iTime = new PrvTime(iDevice);
		iRadio = new PrvRadio(iDevice);
		//iInput = new PrvRadio(iDevice);
		if (Config.enableReceiver) {
			iReceiver = new PrvReceiver(iDevice);
		}
		if (Config.enableAVTransport) {
			iAVTransport = new PrvAVTransport(iDevice);
			iRenderingControl = new PrvRenderingControl(iDevice);
		}

		try {
			ChannelReader cr = new ChannelReader();
			iRadio.addChannels(cr.getChannels());
		} catch (Exception e) {
			log.error("Error Reading Radio Channels");
		}
		
		try{
			SourceReader sr = new SourceReader();
			ConcurrentHashMap<String, Source> sources = sr.getSources();
			if(sources.size()==0)
			{
				Source playlist = new Source("PlayList","Playlist","-99");
				sources.put(playlist.getName(),playlist);
				Source radio = new Source("Radio","Radio","-99");
				sources.put(radio.getName(),radio);
				if(Config.enableReceiver)
				{
					Source reciever = new Source("Receiver", "Receiver", "-99");
					sources.put(reciever.getName(), reciever);
				}
			}
			PluginGateWay.getInstance().setSources(sources);
			PluginGateWay.getInstance().setDefaultSourcePin(sr.getDefaultPin());
			for(String key : sources.keySet())
			{
				Source s = sources.get(key);
				log.debug("Adding Source: " +s.toString());
				iProduct.addSource(Config.friendly_name, s.getName(), s.getType(), true);
			}
			iProduct.updateCurrentSource();
			
		}
		catch(Exception e)
		{
			log.error("Error Reading Input Sources");
		}

		iDevice.setEnabled();
		log.debug("Device Enabled UDN: " + iDevice.getUdn());
		iProduct.setSourceByname("PlayList");
		OSManager.getInstance().loadPlugins();
	}

    protected SimpleDevice(boolean test) {
        // this constructor is just for test purposes...
        // do not remove it
    }

    protected String constructIconList(String deviceName) {
        StringBuffer sb = new StringBuffer();
        sb.append(this.constructIconEntry(deviceName, "image/png", ".png", "240"));
        sb.append(this.constructIconEntry(deviceName, "image/jpeg", ".jpg", "240"));
        sb.append(this.constructIconEntry(deviceName, "image/png", ".png", "120"));
        sb.append(this.constructIconEntry(deviceName, "image/jpeg", ".jpg", "120"));
        sb.append(this.constructIconEntry(deviceName, "image/png", ".png", "50"));
        sb.append(this.constructIconEntry(deviceName, "image/jpeg", ".jpg", "50"));

        return sb.toString();
    }

    protected String constructIconEntry(String deviceName, String mimeType, String fileSuffix, String size) {
        StringBuffer sb = new StringBuffer();

        sb.append("<icon>");
        sb.append("<mimetype>");
        sb.append(mimeType);
        sb.append("</mimetype>");
        sb.append("<width>");
        sb.append(size);
        sb.append("</width>");
        sb.append("<height>");
        sb.append(size);
        sb.append("</height>");
        sb.append("<depth>24</depth>");
        sb.append("<url>/" + deviceName + "/Upnp/resource/org/rpi/image/mediaplayer");
        sb.append(size);
        sb.append(fileSuffix);
        sb.append("</url>");
        sb.append("</icon>");

        return sb.toString();
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
				log.debug("Shutdown Hook, Start of Shutdown");
				dispose();
			}
		});
		log.debug("Shut Down Hook Attached.");
	}

	public void dispose() {

		try {
			OSManager.getInstance().dispose();
		} catch (Exception e) {

		}

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
	public void writeResource(String resource_name, int arg1, List<String> arg2, IResourceWriter writer) {
		log.info("writeResource Called: " + resource_name);
		try {
			resource_name = "/" + resource_name;
			URL url = this.getClass().getResource(resource_name);
			BufferedImage image = ImageIO.read(url);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String fileType = "image/png";
			String format = "png";
			if (resource_name.toUpperCase().endsWith("JPG")) {
				format = "jpg";
				fileType = "image/jpeg";
			}
			ImageIO.write(image, format, baos);
			int length = baos.toByteArray().length;
			writer.writeResourceBegin(length, fileType);
			writer.writeResource(baos.toByteArray(), length);
			writer.writeResourceEnd();
		} catch (IOException e) {
			log.error("Error Writing Resource: " + resource_name, e);
		}

	}

	private void writeFile(String s) {
		try {
			File newTextFile = new File("C:/temp/thetextfile.txt");

			FileWriter fw = new FileWriter(newTextFile);
			fw.write(s);
			fw.close();

		} catch (IOException iox) {
			// do stuff with exception
			iox.printStackTrace();
		}
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
	
	/**
	 * Get the Product Provider
	 * @return
	 */
	public PrvProduct getProduct()
	{
		return iProduct;
	}
}
