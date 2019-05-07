package org.openhome.net.device.tests;

import java.io.File;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.openhome.net.controlpoint.CpAttribute;
import org.openhome.net.controlpoint.CpDevice;
import org.openhome.net.controlpoint.CpDeviceListUpnpAll;
import org.openhome.net.controlpoint.CpDeviceListUpnpServiceType;
import org.openhome.net.controlpoint.ICpDeviceListListener;
import org.openhome.net.controlpoint.proxies.CpProxyAvOpenhomeOrgPlaylist1;
import org.openhome.net.controlpoint.tests.TestBasicCp;
import org.openhome.net.core.InitParams;
import org.openhome.net.core.Library;
import org.openhome.net.core.NetworkAdapter;
import org.openhome.net.core.SubnetList;
import org.rpi.os.OSManager;


public class TestDvDevice implements ICpDeviceListListener
{
	private List<CpDevice> iDeviceList;
	//private DeviceBasic iDevice;
	private Semaphore iSem;
	
	public TestDvDevice()
	{
		File f = null;
		try
		{
		//f = new File("C:\\Keep\\git\\repository\\MediaPlayer\\com.upnp.mediaplayer\\bin\\mediaplayer_lib\\ohNet\\windows\\x86\\ohNet.dll");
			//f = "C:\\\\Keep\\\\git\\\\repository\\\\MediaPlayer\\\\com.upnp.mediaplayer\\bin\\mediaplayer_lib\\ohNet\\windows\\x86\\ohNet.dll";
		}
		catch (Exception e)
		{
			System.out.println(e);
		}
		//System.loadLibrary(f.getAbsolutePath());
		System.out.println("TestDvDeviceJava - starting");
		//iDevice = new DeviceBasic();
		iDeviceList = new ArrayList<CpDevice>();
		//CpDeviceListUpnpServiceType list = new CpDeviceListUpnpServiceType("openhome.org", "ConnectionManager", 1, this);
		CpDeviceListUpnpServiceType list = new CpDeviceListUpnpServiceType("upnp.org", "ContentDirectory", 1, this);
		
		iSem = new Semaphore(1);
		iSem.acquireUninterruptibly();
		try {
			iSem.tryAcquire(30*1000, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException ie)
		{
			ie.printStackTrace();
		}
		assert(iDeviceList.size() == 1);
		System.out.println("iDeviceList size: " + iDeviceList.size());
		//TestBasicCp cp = new TestBasicCp(iDeviceList.get(0));
		//cp.testActions();
		//cp.testSubscriptions();
		CpDeviceListUpnpAll myList = new CpDeviceListUpnpAll(this);
		myList.refresh();
		
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list.destroy();
		synchronized (this)
		{
			for (CpDevice d : iDeviceList)
			{
				d.removeRef();
			}
		}
		//iDevice.dispose();
		
		System.out.println("TestDvDeviceJava - completed");
	}
	
	public void deviceAdded(CpDevice aDevice)
	{
		synchronized (this)
		{
			//if (aDevice.getUdn().equals(iDevice.getUdn()))
			//{
                aDevice.addRef();
                CpAttribute xml = aDevice.getAttribute("Upnp.DeviceXml");
                CpAttribute modelName = aDevice.getAttribute("Upnp.ModelName");
                String udn = aDevice.getUdn();
                System.out.println(udn);
                if(udn.equalsIgnoreCase("652256b90ef958c66abcf3292f5f2412")) {
                	System.out.println("Bingo!!!!!");
                }
				iDeviceList.add(aDevice);
				iSem.release();
			//}
		}
		
	}

	public void deviceRemoved(CpDevice aDevice)
	{
		synchronized (this)
		{
			String udn = aDevice.getUdn();
			int i = 0;
			for (CpDevice n : iDeviceList)
			{
				if (n.getUdn() == udn)
				{
					n.removeRef();
					iDeviceList.remove(i);
				}
				i++;
			}
		}
	}
	
	public static void main(String[] args)
	{
		OSManager.getInstance();
		InitParams initParams = new InitParams();
		initParams.setMsearchTimeSecs(1);
		//initParams.setUseLoopbackNetworkAdapter();
        initParams.setDvServerPort(0);
		Library lib = new Library();
		lib.initialise(initParams);
		SubnetList subnetList = new SubnetList();
		NetworkAdapter nif = subnetList.getSubnet(2);
		Inet4Address subnet = nif.getSubnet();
		subnetList.destroy();
		//lib.startCombined(subnet);
		lib.startCp(subnet);
		new TestDvDevice();
		lib.close();
	}
}