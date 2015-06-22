package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgCredentials1;
import org.rpi.utils.Utils;

public class PrvCredentials extends DvProviderAvOpenhomeOrgCredentials1 implements  IDisposableDevice {

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
		
		setPropertyPublicKey("Test");
		setPropertySequenceNumber(0);
		setPropertyIds("tidalhifi.com");
		
		enableActionGetIds();
		enableActionGetPublicKey();
		enableActionGetSequenceNumber();
		enableActionLogin();
		enableActionReLogin();
		enableActionClear();
		enableActionGet();
		enableActionSet();
		enableActionSetEnabled();
	}
	

	@Override 
	protected void clear(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("clear" + Utils.getLogText(paramIDvInvocation));
	}
	
	@Override
	protected void set(IDvInvocation paramIDvInvocation, String paramString1, String paramString2, byte[] paramArrayOfByte) {
		log.debug("set" + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected void setEnabled(IDvInvocation paramIDvInvocation, String paramString, boolean paramBoolean) {
		log.debug("setEnable" + Utils.getLogText(paramIDvInvocation));
	}
	
	@Override
	protected Get get(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("get" + Utils.getLogText(paramIDvInvocation));
		byte[] password = new byte[]{17};
		return new Get("UserName", password, true, "Status", "Data");
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


}
