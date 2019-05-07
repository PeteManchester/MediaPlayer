package org.rpi.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.apache.log4j.Logger;
import org.openhome.net.controlpoint.CpDeviceListUpnpServiceType;
import org.openhome.net.controlpoint.ICpDeviceListListener;

public class CpDeviceImpl implements ICpDeviceListListener {
	
	private Logger log = Logger.getLogger(CpDeviceImpl.class);
	
	//private List<CpDeviceImpl> iDeviceList;
	private Semaphore iSem;
	
	
	
	public void initCpDevice()
	{
		//iDeviceList = new ArrayList<CpDeviceImpl>();
		//CpDeviceListUpnpServiceType list = new CpDeviceListUpnpServiceType("openhome.org", "ConnectionManager", 1, this);
		CpDeviceListUpnpServiceType list = new CpDeviceListUpnpServiceType("upnp.org", "ContentDirectory", 1, this);
	}



	@Override
	public void deviceAdded(org.openhome.net.controlpoint.CpDevice arg0) {
		log.debug("Added");
		
	}



	@Override
	public void deviceRemoved(org.openhome.net.controlpoint.CpDevice arg0) {
		log.debug("Removed");
		
	}

}
