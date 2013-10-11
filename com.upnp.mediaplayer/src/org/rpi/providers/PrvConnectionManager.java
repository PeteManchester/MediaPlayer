package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgConnectionManager1;
import org.rpi.config.Config;

public class PrvConnectionManager extends DvProviderUpnpOrgConnectionManager1 {

	private Logger log = Logger.getLogger(PrvConnectionManager.class);

	private String currentConnectionIDs = "0";
	//private String sinkProtocolInfo = "http-get:*:audio/mpegurl:*,http-get:*:audio/mp3:*,http-get:*:audio/mpeg:*,http-get:*:audio/x-ms-wma:*,http-get:*:audio/wma:*,http-get:*:audio/mpeg3:*,http-get:*:video/x-ms-wmv:*,http-get:*:video/x-ms-asf:*,http-get:*:video/x-ms-avi:*,http-get:*:video/mpeg:*";
	private String sinkProtocolInfo = Config.getProtocolInfo();
	private String sourceProtocolInfo = "";//Config.getProtocolInfo();

	public PrvConnectionManager(DvDevice device) {
		super(device);
		enablePropertyCurrentConnectionIDs();
		enablePropertySinkProtocolInfo();
		enablePropertySourceProtocolInfo();

		setPropertyCurrentConnectionIDs(currentConnectionIDs);
		setPropertySinkProtocolInfo(sinkProtocolInfo);
		setPropertySourceProtocolInfo(sourceProtocolInfo);

		//enableActionConnectionComplete();
		enableActionGetCurrentConnectionIDs();
		enableActionGetCurrentConnectionInfo();
		//enableActionPrepareForConnection();
		enableActionGetProtocolInfo();
	}

	protected void connectionComplete(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("ConnectionManager  ConnectionComplete: " + paramInt);
	}

	protected DvProviderUpnpOrgConnectionManager1.GetProtocolInfo getProtocolInfo(IDvInvocation paramIDvInvocation) {
		log.debug("GetProtolInfo Source=" + sourceProtocolInfo + " Sink=" + sinkProtocolInfo);
		return new DvProviderUpnpOrgConnectionManager1.GetProtocolInfo(sourceProtocolInfo, sinkProtocolInfo);
	}

	protected String getCurrentConnectionIDs(IDvInvocation paramIDvInvocation) {
		log.debug("ConnectionManager getCurrentConnectionIDs ConnectionIDs=" + currentConnectionIDs);
		return currentConnectionIDs;
	}

	protected DvProviderUpnpOrgConnectionManager1.GetCurrentConnectionInfo getCurrentConnectionInfo(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("ConnectionManager action: GetCurrentConnectionInfo");
		log.debug(" ConnectionID=" + paramInt);

		int iRcsID = 0;
		int iAVTransportID = 0;
		String iProtocolInfo = ":::";
		String iPeerConnectionManager = "/";
		int iPeerConnectionID = -1;
		String iDirection = "Input";
		String iStatus = "Unknown";

		log.debug("ConnectionManager response: GetCurrentConnectionInfo");
		log.debug(" RcsID=" + iRcsID + " AVTransportID=" + iAVTransportID + " ProtocolInfo=" + iProtocolInfo + " PeerConnectionManager=" + iPeerConnectionManager + " PeerConnectionID=" + iPeerConnectionID + " Direction=" + iDirection + " Status=" + iStatus);
		return new DvProviderUpnpOrgConnectionManager1.GetCurrentConnectionInfo(iRcsID, iAVTransportID, iProtocolInfo, iPeerConnectionManager, iPeerConnectionID, iDirection, iStatus);
	}
	
	protected  String getProtocolInfo() {
		log.debug("GetProtocol");
		return sinkProtocolInfo;
	}

}
