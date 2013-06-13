package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgVolume1;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.playlist.PlayManager;

public class PrvVolume extends DvProviderAvOpenhomeOrgVolume1 implements Observer {

	private Logger log = Logger.getLogger(PrvVolume.class);

	private PlayManager iPlayer = PlayManager.getInstance();

	public PrvVolume(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomVolume");

		enablePropertyVolume();
		enablePropertyMute();
		enablePropertyBalance();
		enablePropertyFade();
		enablePropertyVolumeLimit();
		enablePropertyVolumeMax();
		enablePropertyVolumeUnity();
		enablePropertyVolumeSteps();
		enablePropertyVolumeMilliDbPerStep();
		enablePropertyBalanceMax();
		enablePropertyFadeMax();

		propertiesLock();
		setPropertyVolume(iPlayer.getVolume());
		setPropertyMute(false);
		setPropertyBalance(0);
		setPropertyFade(0);
		setPropertyVolumeLimit(100);
		setPropertyVolumeMax(100);
		setPropertyVolumeUnity(80);
		setPropertyVolumeSteps(100);
		setPropertyVolumeMilliDbPerStep(1024);
		setPropertyBalanceMax(0);
		setPropertyFadeMax(0);
		propertiesUnlock();

		enableActionCharacteristics();
		enableActionSetVolume();
		enableActionVolumeInc();
		enableActionVolumeDec();
		enableActionVolume();
		enableActionSetMute();
		enableActionMute();
		enableActionVolumeLimit();
		PlayManager.getInstance().observVolumeEvents(this);
	}
	
	private void updateVolume(long volume)
	{
		setPropertyVolume(volume);
	}

	protected void setVolume(IDvInvocation arg0, long volume) {
		log.debug("Volume Changed: " + volume);
		iPlayer.setVolume(volume);

	}

	protected void volumeInc(IDvInvocation arg0) {
		long volume = iPlayer.incVolume();
		//propertiesLock();
		//setPropertyVolume(volume);
		//propertiesUnlock();
		//iPlayer.setVolume(volume);
		log.debug("VolumeInc: " + volume);
	}

	protected void volumeDec(IDvInvocation arg0) {
		long volume = iPlayer.decVolume();
		//propertiesLock();
		//setPropertyVolume(volume);
		//propertiesUnlock();
		//iPlayer.setVolume(volume);
		log.debug("VolumeDec: " + volume);
	}

	protected void setMute(IDvInvocation arg0, boolean mute) {
		iPlayer.setMute(mute);
	}
	
	private void updateMute(boolean mute)
	{
		//propertiesLock();
		setPropertyMute(mute);
		//propertiesUnlock();
	}

	protected long volume(IDvInvocation paramIDvInvocation) {
		long volume = iPlayer.getVolume();
		log.debug("GetVolume: " + volume);
		return volume;
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase e = (EventBase) obj;
		switch(e.getType())
		{
		case EVENTVOLUMECHNANGED:
			EventVolumeChanged ev = (EventVolumeChanged)e;
			updateVolume(ev.getVolume());
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged)e;
			updateMute(em.isMute());
			break;
		}
	}
}
