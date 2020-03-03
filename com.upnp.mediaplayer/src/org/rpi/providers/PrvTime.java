package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgTime1;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventDurationUpdate;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.utils.Utils;

import java.util.Observable;
import java.util.Observer;

public class PrvTime extends DvProviderAvOpenhomeOrgTime1 implements Observer, IDisposableDevice {

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
		PlayManager.getInstance().observeTimeEvents(this);

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

	public synchronized void setSeconds(long seconds) {
		try {
			// if ((seconds % 2) == 0) {
			setPropertySeconds(seconds);
			// }
		} catch (Exception e) {
			log.error("Error: setSeconds", e);
		}
	}

	@Override
	protected Time time(IDvInvocation paramIDvInvocation) {
		log.debug("time" + Utils.getLogText(paramIDvInvocation));
		try {
			long trackCount = getPropertyTrackCount();
			long duration = getPropertyDuration();
			long seconds = getPropertySeconds();
			Time time = new Time(trackCount, duration, seconds);
			return time;
		} catch (Exception e) {
			log.error("Erorr Time", e);
		}
		return new Time(0, 0, 0);
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase eb = (EventBase) obj;
		switch (eb.getType()) {
		case EVENTTIMEUPDATED:
			EventTimeUpdate et = (EventTimeUpdate) eb;
			setSeconds(et.getTime());
			break;
		case EVENTDURATIONUPDATE:
			EventDurationUpdate ed = (EventDurationUpdate) eb;
			setDuration(ed.getDuration());
			break;
		}

	}

	@Override
	public String getName() {
		return "Time";
	}


}
