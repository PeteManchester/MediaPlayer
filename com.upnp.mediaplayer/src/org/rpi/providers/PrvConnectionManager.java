package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgConnectionManager1;
import org.rpi.config.Config;
import org.rpi.utils.Utils;

public class PrvConnectionManager extends DvProviderUpnpOrgConnectionManager1 {

	private Logger log = Logger.getLogger(PrvConnectionManager.class);

	private String currentConnectionIDs = "0";
	// private String sinkProtocolInfo =
	// "http-get:*:audio/mpegurl:*,http-get:*:audio/mp3:*,http-get:*:audio/mpeg:*,http-get:*:audio/x-ms-wma:*,http-get:*:audio/wma:*,http-get:*:audio/mpeg3:*,http-get:*:video/x-ms-wmv:*,http-get:*:video/x-ms-asf:*,http-get:*:video/x-ms-avi:*,http-get:*:video/mpeg:*";
	private String sinkProtocolInfo = Config.getProtocolInfo();
	private String sourceProtocolInfo = "";// "http-get:*:*:*";//
											// "";//Config.getProtocolInfo();

	private int iConnectionID = 0;

	private int iAVTransportID = 0;

	private int iRcsID = 0;

	public PrvConnectionManager(DvDevice device) {
		super(device);

		enablePropertySourceProtocolInfo();
		setPropertySourceProtocolInfo(sourceProtocolInfo);
		enablePropertySinkProtocolInfo();
		setPropertySinkProtocolInfo(sinkProtocolInfo);
		enablePropertyCurrentConnectionIDs();
		setPropertyCurrentConnectionIDs(currentConnectionIDs);

		// enableActionPrepareForConnection();
		// enableActionConnectionComplete();
		enableActionGetCurrentConnectionIDs();
		enableActionGetCurrentConnectionInfo();
		enableActionGetProtocolInfo();
	}

	@Override
	protected PrepareForConnection prepareForConnection(IDvInvocation paramIDvInvocation, String sinkProtocolInfo, String peerConnectionManager, int remoteConnId, String paramString3) {
		// setPropertyCurrentConnectionIDs("");
		// setPropertyCurrentConnectionIDs("0");
		setPropertySourceProtocolInfo(sinkProtocolInfo);
		// boolean res = setPropertyCurrentConnectionIDs("" + remoteConnId);
		// if (res) {
		// log.warn("#################### This happened");
		// }
		log.debug("PrepareForConnection: ProtocolInfo: " + sinkProtocolInfo + " PeerConnectionManager: " + peerConnectionManager + " RemoteConnId: " + remoteConnId + Utils.getLogText(paramIDvInvocation));
		PrepareForConnection prep = new PrepareForConnection(0, 0, 0);
		return prep;
	}

	@Override
	protected void connectionComplete(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("connectionComplete: " + paramInt + Utils.getLogText(paramIDvInvocation));
		setPropertyCurrentConnectionIDs("");
	}

	@Override
	protected DvProviderUpnpOrgConnectionManager1.GetProtocolInfo getProtocolInfo(IDvInvocation paramIDvInvocation) {
		log.debug("GetProtocolInfo: " + Utils.getLogText(paramIDvInvocation));

		log.debug("GetProtolInfo Source=" + sourceProtocolInfo + " Sink=" + sinkProtocolInfo);
		return new DvProviderUpnpOrgConnectionManager1.GetProtocolInfo(getPropertySourceProtocolInfo(), getPropertySinkProtocolInfo());
	}

	@Override
	protected String getCurrentConnectionIDs(IDvInvocation paramIDvInvocation) {
		log.debug("getCurrentConnectionIDs " + Utils.getLogText(paramIDvInvocation));
		log.debug("ConnectionManager getCurrentConnectionIDs ConnectionIDs=" + getPropertyCurrentConnectionIDs());
		return getPropertyCurrentConnectionIDs();
	}

	@Override
	protected DvProviderUpnpOrgConnectionManager1.GetCurrentConnectionInfo getCurrentConnectionInfo(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("GetCurrentConnectionInfo: " + paramInt + Utils.getLogText(paramIDvInvocation));
		// int iRcsID = 0;
		// int iAVTransportID = 0;
		setPropertyCurrentConnectionIDs("" + paramInt);
		String iProtocolInfo = sinkProtocolInfo;
		String iPeerConnectionManager = "/";
		int iPeerConnectionID = -1;
		String iDirection = "Input";
		String iStatus = "Unknown";

		log.debug("ConnectionManager response: GetCurrentConnectionInfo: RcsID=" + iRcsID + " AVTransportID=" + iAVTransportID + " ProtocolInfo=" + iProtocolInfo + " PeerConnectionManager=" + iPeerConnectionManager + " PeerConnectionID=" + iPeerConnectionID + " Direction=" + iDirection + " Status=" + iStatus);
		return new DvProviderUpnpOrgConnectionManager1.GetCurrentConnectionInfo(iRcsID, iAVTransportID, iProtocolInfo, iPeerConnectionManager, iPeerConnectionID, iDirection, iStatus);
	}



	// protected String getProtocolInfo() {
	// log.debug("GetProtocol");
	// return sinkProtocolInfo;
	// }

}
