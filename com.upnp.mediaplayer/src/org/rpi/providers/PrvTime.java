package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgTime1;

public class PrvTime extends DvProviderAvOpenhomeOrgTime1 {

	private Logger log = Logger.getLogger(PrvTime.class);

	public PrvTime(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomTime");
		enablePropertyTrackCount();
		enablePropertyDuration();
		enablePropertySeconds();

		setPropertyTrackCount(0);
		setPropertyDuration(0);
		setPropertySeconds(0);

		enableActionTime();

	}

	public void setDuration(long duration) {
		try {
			long trackCount = getPropertyTrackCount();
			trackCount++;
			propertiesLock();
			setPropertyTrackCount(trackCount);
			setPropertyDuration(duration);
			propertiesUnlock();
		} catch (Exception e) {
			log.error("Error: setDuration", e);
		}
	}

	public void setSeconds(long seconds) {
		try {
			setPropertySeconds(seconds);
		} catch (Exception e) {
			log.error("Error: setDetails", e);
		}
	}

	@Override
	protected Time time(IDvInvocation paramIDvInvocation) {
		long trackCount = getPropertyTrackCount();
		long duration = getPropertyDuration();
		long seconds = getPropertySeconds();
		Time time = new Time(trackCount, duration, seconds);
		return time;
	}

}
