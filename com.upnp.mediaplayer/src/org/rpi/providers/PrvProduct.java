package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgProduct1;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.Utils;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CopyOnWriteArrayList;

public class PrvProduct extends DvProviderAvOpenhomeOrgProduct1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvProduct.class);
	private String friendly_name = Config.friendly_name;
	// private String iSourceXml =
	// "<SourceList><Source><Name>Playlist</Name><Type>Playlist</Type><Visible>1</Visible></Source><Source><Name>Receiver</Name><Type>Receiver</Type><Visible>1</Visible></Source><Source><Name>Radio</Name><Type>Radio</Type><Visible>1</Visible></Source></SourceList>";
	private String iSourceXml = "";
	// private boolean standby = true;
	private String attributes = "Info Time Volume";
	// private String attributes = "";
	private String man_name = "Java Inc";
	private String man_info = "Developed in Java using OpenHome and MPlayer";
	private String man_url = "";
	private String man_image = "";
	private String model_name = "Test Model Name";
	private String model_info = "Test Model Info";
	private String model_url = "";
	private String model_image = "";
	private String prod_room = friendly_name;
	private String prod_name = "Java MediaPlayer";
	private String prod_info = "Developed by Pete";
	private String prod_url = "";
	private String prod_image = "";

	private PlayManager iPlayer = null;

	private long iSourceXMLChangeCount = 0;

	public PrvProduct(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomProduct");
		iPlayer = PlayManager.getInstance();
		enablePropertyStandby();
		enablePropertyAttributes();
		enablePropertyManufacturerName();
		enablePropertyManufacturerInfo();
		enablePropertyManufacturerUrl();
		enablePropertyManufacturerImageUri();
		enablePropertyModelName();
		enablePropertyModelInfo();
		enablePropertyModelUrl();
		enablePropertyModelImageUri();
		enablePropertyProductRoom();
		enablePropertyProductName();
		enablePropertyProductInfo();
		enablePropertyProductUrl();
		enablePropertyProductImageUri();
		enablePropertySourceIndex();
		enablePropertySourceCount();
		enablePropertySourceXml();

		setPropertyStandby(PlayManager.getInstance().isStandby());
		setPropertyAttributes(attributes);

		setPropertyManufacturerName(man_name);
		setPropertyManufacturerInfo(man_info);
		setPropertyManufacturerUrl(man_url);
		setPropertyManufacturerImageUri(man_image);

		setPropertyModelName(model_name);
		setPropertyModelInfo(model_info);
		setPropertyModelUrl(model_url);
		setPropertyModelImageUri(model_image);

		setPropertyProductRoom(prod_room);
		setPropertyProductName(prod_name);
		setPropertyProductInfo(prod_info);
		setPropertyProductUrl(prod_url);
		setPropertyProductImageUri(prod_image);

		setPropertySourceIndex(4);
		setPropertySourceCount(sources.size());
		setPropertySourceXml(iSourceXml);

		enableActionManufacturer();
		enableActionModel();
		enableActionProduct();
		enableActionStandby();
		enableActionSetStandby();
		enableActionSourceCount();
		enableActionSourceXml();
		enableActionSourceIndex();
		enableActionSetSourceIndex();
		enableActionSetSourceIndexByName();
		enableActionSource();
		enableActionAttributes();
		enableActionSourceXmlChangeCount();
		PlayManager.getInstance().observeProductEvents(this);
		// initSources();
		setPropertySourceIndex(0);

	}

	private CopyOnWriteArrayList<Source> sources = new CopyOnWriteArrayList<Source>();

	private void initSources() {
		addSource(Config.friendly_name, "Radio", "Radio", true);
		addSource(Config.friendly_name, "Playlist", "Playlist", true);
		if (Config.enableReceiver) {
			addSource(Config.friendly_name, "Receiver", "Receiver", true);
		}
		if (Config.enableAVTransport) {
			addSource(Config.friendly_name, "UpnpAV", "UpnpAv", false);
		}
	}

	public void addSource(String system_name, String name, String type, boolean visible) {
		Source source = new Source(system_name, type, name, visible);
		sources.add(source);
		iSourceXMLChangeCount++;
		updateSourceXML();
	}

	private void updateSourceXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<SourceList>");
		for (Source s : sources) {
			sb.append("<Source>");
			sb.append("<Name>");
			sb.append(s.getName());
			sb.append("</Name>");
			sb.append("<Type>");
			sb.append(s.getType());
			sb.append("</Type>");
			sb.append("<Visible>");
			sb.append(s.getVisible());
			sb.append("</Visible>");
			sb.append("</Source>");
		}
		sb.append("</SourceList>");
		iSourceXml = sb.toString();
		log.debug("SourceXML \r\n " + iSourceXml);
		propertiesLock();
		setPropertySourceCount(sources.size());
		setPropertySourceXml(iSourceXml.trim());
		propertiesUnlock();
	}

	@Override
	protected void setStandby(IDvInvocation paramIDvInvocation, boolean paramBoolean) {
		log.debug("SetStandby: " + paramBoolean + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().setStandby(paramBoolean);
		if (paramBoolean == false) {
			updateCurrentSource();
		}
	}

	@Override
	protected boolean standby(IDvInvocation paramIDvInvocation) {
		boolean standby = PlayManager.getInstance().isStandby();
		log.debug("GetStandby: " + standby + Utils.getLogText(paramIDvInvocation));
		return standby;
	}

	@Override
	protected String attributes(IDvInvocation paramIDvInvocation) {
		log.debug("Attributes: " + attributes + Utils.getLogText(paramIDvInvocation));
		return attributes;
	}

	@Override
	protected Manufacturer manufacturer(IDvInvocation paramIDvInvocation) {
		log.debug("Manufacturer" + Utils.getLogText(paramIDvInvocation));
		Manufacturer man = new Manufacturer(man_name, man_info, man_url, man_image);
		return man;
	}

	@Override
	protected Model model(IDvInvocation paramIDvInvocation) {
		log.debug("Model" + Utils.getLogText(paramIDvInvocation));
		Model model = new Model(model_name, model_info, model_url, model_image);
		return model;
	}

	@Override
	protected Product product(IDvInvocation paramIDvInvocation) {
		log.debug("Product" + Utils.getLogText(paramIDvInvocation));
		Product product = new Product(Config.friendly_name, prod_name, prod_info, prod_url, prod_image);
		return product;
	}

	@Override
	protected Source source(IDvInvocation paramIDvInvocation, long iD) {
		log.debug("Source: " + iD + Utils.getLogText(paramIDvInvocation));
		if (sources.size() >= iD) {
			try {
				Source s = sources.get((int) iD);
				return s;
			} catch (Exception e) {
				log.error("Error GetSource: " + e);
			}
		}
		return null;
	}

	@Override
	protected long sourceCount(IDvInvocation paramIDvInvocation) {
		long source_count = getPropertySourceCount();
		log.debug("SourceCount: " + source_count + Utils.getLogText(paramIDvInvocation));
		return source_count;
	}

	@Override
	protected void setSourceIndex(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("SetSourceIndex: " + paramLong + Utils.getLogText(paramIDvInvocation));
		Source source = sources.get((int) paramLong);
		String name = source.getName();
		log.debug("Source Selected: " + name);
		setPropertySourceIndex(paramLong);
		PluginGateWay.getInstance().setSourceId(name,source.getType());

	}

	@Override
	protected long sourceIndex(IDvInvocation paramIDvInvocation) {
		long source_index = getPropertySourceIndex();
		log.debug("SourceIndex: " + source_index + Utils.getLogText(paramIDvInvocation));
		return source_index;
	}

	@Override
	protected String sourceXml(IDvInvocation paramIDvInvocation) {
		log.debug("SourceXML: " + iSourceXml + Utils.getLogText(paramIDvInvocation));
		return iSourceXml;
	}

	@Override
	protected long sourceXmlChangeCount(IDvInvocation paramIDvInvocation) {
		log.debug("SourceXmlChangeCount: " + iSourceXMLChangeCount + Utils.getLogText(paramIDvInvocation));
		return iSourceXMLChangeCount;
	}

	@Override
	protected void setSourceIndexByName(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("SetSourceIndexByName: " + paramString + Utils.getLogText(paramIDvInvocation));
		setSourceByname(paramString);
	}

	public void updateStandby(boolean standby) {
		propertiesLock();
		setPropertyStandby(standby);
		propertiesUnlock();
	}

	/**
	 * Attempt to try and set the PlayList as Default Source
	 * 
	 * @param paramLong
	 */
	public synchronized void setSourceId(long paramLong) {
		setPropertySourceIndex(paramLong);
	}

	public synchronized void setSourceByname(String name) {
		long count = 0;
		for (Source source : sources) {
			if (source.getName().equalsIgnoreCase(name)) {
				setPropertySourceIndex(count);
			}
			count++;
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		EventBase e = (EventBase) arg;
		switch (e.getType()) {
		case EVENTSTANDBYCHANGED:
			EventStandbyChanged ev = (EventStandbyChanged) e;
			updateStandby(ev.isStandby());
			break;
		}

	}

	/**
	 * Test to see of the initial Source can be obtained.
	 */
	public void updateCurrentSource() {
		try {
			long source_index = getPropertySourceIndex();
			Source source = sources.get((int) source_index);
			String name = source.getName();
			log.debug("Source Selected: " + name);
			PluginGateWay.getInstance().setSourceId(name,source.getType());
		} catch (Exception e) {
			log.error(e);
		}

	}

	@Override
	public String getName() {
		return "Product";
	}
}
