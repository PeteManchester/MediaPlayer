package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgPins1;

public class PrvPins extends DvProviderAvOpenhomeOrgPins1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvPins.class);
	//GetDeviceAccountMax iDeviceAccountMax = new GetDeviceAccountMax(0, 0);
	long iDeviceMax = 0;
	long iAccountMax = 0;
	String idArray = "";
	String modes = "";

	public PrvPins(DvDevice iDevice) {
		super(iDevice);
		enablePropertyAccountMax();
		enablePropertyDeviceMax();
		enablePropertyCloudConnected();
		enablePropertyIdArray();
		enablePropertyModes();

		
		propertiesLock();
		setPropertyAccountMax(iAccountMax);
		setPropertyDeviceMax(iDeviceMax);
		setPropertyCloudConnected(false);
		setPropertyIdArray("");
		//setPropertyModes("[\"openhome.me\"]");
		setPropertyModes("");		
		propertiesUnlock();

		enableActionGetDeviceMax();		
		enableActionGetModes();
		enableActionGetIdArray();
		enableActionReadList();
		enableActionInvokeId();
		enableActionInvokeIndex();
		enableActionSetDevice();
		enableActionSetAccount();
		enableActionClear();
		enableActionSwap();
		enableActionGetCloudConnected();
	}

	
	protected long getDeviceMax(IDvInvocation var1) {
		return iDeviceMax;
	}
	

	protected String getModes(IDvInvocation var1) {
		return getPropertyModes();
	}
	

	protected String getIdArray(IDvInvocation var1) {

		return getPropertyIdArray();
	}
	

	protected String readList(IDvInvocation var1, String var2) {
		return "";
	}
	

	protected void invokeId(IDvInvocation var1, long var2) {
		log.debug("InvokeId: " + var1 + " Var2: " + var2);
	}
	

	protected void invokeIndex(IDvInvocation var1, long var2) {
		log.debug("InvokeIndex: " + var1 + " Var2: " + var2);
	}
	

	protected void setDevice(IDvInvocation var1, long var2, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10) {
		log.debug("InvokeDevice: " + var1 + " Var2: " + var2);
	}
	

	protected void setAccount(IDvInvocation var1, long var2, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10) {
		log.debug("SetAccount: " + var1 + " Var2: " + var2);
	}
	

	protected void clear(IDvInvocation var1, long var2) {
		log.debug("Clear: " + var1 + " Var2: " + var2);
	}
	

	protected void swap(IDvInvocation var1, long var2, long var4) {
		log.debug("Swap: " + var1 + " Var2: " + var2);
	}
	

	protected boolean getCloudConnected(IDvInvocation var1) {
		return true;
	}

	
	//Actions End//
	
	
	//Set Properties Start//
	/*
	
	@Override
	public boolean setPropertyDeviceMax(long var1) {
		log.debug("SetDeviceMax: " + var1);
		return super.setPropertyDeviceMax(var1);
	}
	
	@Override
	public boolean setPropertyAccountMax(long var1) {
		iAccountMax = var1;
		return super.setPropertyAccountMax(var1);

	}
	
	@Override
	public boolean setPropertyIdArray(String var1) {
		idArray = var1;
		return super.setPropertyIdArray(var1);
	}
	
	@Override
	public boolean setPropertyModes(String var1) {
		modes = var1;
		return super.setPropertyModes(var1);
	}
	*/
	//Set Properties End//

	@Override
	public String getName() {
		return "Pins";
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub

	}
	
	
	

}
