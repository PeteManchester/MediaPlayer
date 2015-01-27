package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgCredentials1;
import org.rpi.utils.Utils;

public class PrvCredentials extends DvProviderAvOpenhomeOrgCredentials1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvCredentials.class);
	private String iPropertyIds = "tidalhifi.com";
	private String iPropertyPublicKey = "MyPublicKey";
	private int iPropertySequenceNumber = 0;

	public PrvCredentials(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating AvTransport");
		enablePropertyIds();
		enablePropertyPublicKey();
		enablePropertySequenceNumber();
		
		enableActionGetIds();
		enableActionGetPublicKey();
		enableActionGetSequenceNumber();
		enableActionLogin();
		enableActionReLogin();
	}
	

	

	


	@Override
	protected String login(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("login" + Utils.getLogText(paramIDvInvocation));
		return "OK";
	}

	@Override
	protected String reLogin(IDvInvocation paramIDvInvocation, String paramString1, String paramString2) {
		log.debug("reLogin" + Utils.getLogText(paramIDvInvocation));
		return "OK";
	}

	@Override
	protected String getIds(IDvInvocation paramIDvInvocation) {
		log.debug("getIds" + Utils.getLogText(paramIDvInvocation));
		return iPropertyIds;
	}

	@Override
	protected String getPublicKey(IDvInvocation paramIDvInvocation) {
		log.debug("getPublicKey" + Utils.getLogText(paramIDvInvocation));
		return iPropertyPublicKey;
	}

	@Override
	protected long getSequenceNumber(IDvInvocation paramIDvInvocation) {
		log.debug("getSequenceNumber" + Utils.getLogText(paramIDvInvocation));
		return iPropertySequenceNumber++;
	}	
	
	@Override
	public String getName() {
		return "PrvCredentials";
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}

}
