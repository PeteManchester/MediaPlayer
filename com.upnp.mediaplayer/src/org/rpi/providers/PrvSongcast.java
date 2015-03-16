package org.rpi.providers;

/**
 * Very basic start to implementing a songcast sender..
 * http://www.openhome.org/wiki/Av:Developer:SenderService
 */

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgSender1;
import org.rpi.config.Config;


public class PrvSongcast extends DvProviderAvOpenhomeOrgSender1 implements  IDisposableDevice {
	
	private Logger log = Logger.getLogger(this.getClass());
	private String meta_data = "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"> <item id=\"0\" restricted=\"True\"> <dc:title>" + Config.getInstance().getMediaplayerFriendlyName() +"</dc:title>\" <res protocolInfo=\"ohz:*:*:u\">ohz://239.255.255.250:51972/"+ Config.getInstance().getMediaplayerFriendlyName() +"</res>  <upnp:albumArtURI>http://192.168.1.73:52821/device-Default-D067E5492684-MediaRenderer/Upnp/resource/org/rpi/image/mediaplayer240.png</upnp:albumArtURI>  <upnp:class>object.item.audioItem</upnp:class>  </item></DIDL-Lite>";

	public PrvSongcast(DvDevice arg0) {
		super(arg0);
		enablePropertyAttributes();
		enablePropertyAudio();
		enablePropertyMetadata();
		enablePropertyPresentationUrl();
		enablePropertyStatus();
		
		setPropertyAttributes("");
		setPropertyAudio(true);
		setPropertyMetadata(meta_data);
		setPropertyPresentationUrl("");
		setPropertyStatus("Enabled");
		
		enableActionAttributes();
		enableActionAudio();
		enableActionMetadata();
		enableActionPresentationUrl();
		enableActionStatus();
	}
	
	@Override
	protected String presentationUrl(IDvInvocation paramIDvInvocation) {
		log.debug("presentationUrl");
		return getPropertyPresentationUrl();
	}
	@Override
	protected String metadata(IDvInvocation paramIDvInvocation) {
		log.debug("metadata");
		return meta_data;// getPropertyMetadata();
	}
	@Override
	protected boolean audio(IDvInvocation paramIDvInvocation) {
		log.debug("audio");
		return getPropertyAudio();
	}
	@Override
	protected String status(IDvInvocation paramIDvInvocation) {
		log.debug("status");
		return getPropertyStatus();
	}
	@Override
	protected String attributes(IDvInvocation paramIDvInvocation) {
		log.debug("attributes");
		return getPropertyAttributes();
	}

	@Override
	public String getName() {
		return "Songcast";
	}

}
