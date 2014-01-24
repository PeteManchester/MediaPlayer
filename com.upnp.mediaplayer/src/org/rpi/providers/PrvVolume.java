package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgVolume1;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.utils.Utils;

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
		long v = iPlayer.getVolume();
		if(v<0)
			v=0;
		setPropertyVolume(v);
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
	
	@Override
	protected Characteristics characteristics(IDvInvocation paramIDvInvocation) {
		log.debug("characteristics" + Utils.getLogText(paramIDvInvocation));
		Characteristics charistics = new Characteristics(100, 0, 0, 0, 0, 0);
		return charistics;
	}
	
	@Override
	protected boolean mute(IDvInvocation paramIDvInvocation) {
		log.debug("mute" + Utils.getLogText(paramIDvInvocation));
		return PlayManager.getInstance().getMute();
	}
	
	private void updateVolume(long volume)
	{
		if(volume < 0)
			volume = 0;
		setPropertyVolume(volume);
	}

	protected void setVolume(IDvInvocation paramIDvInvocation, long volume) {

		log.debug("setVolume: " + volume +Utils.getLogText(paramIDvInvocation));
		iPlayer.setVolume(volume);

	}

	protected void volumeInc(IDvInvocation paramIDvInvocation) {
		log.debug("volumeInc: "  +Utils.getLogText(paramIDvInvocation));
		long volume = iPlayer.incVolume();
		//propertiesLock();
		//setPropertyVolume(volume);
		//propertiesUnlock();
		//iPlayer.setVolume(volume);
		//log.debug("VolumeInc: " + volume);
	}

	protected void volumeDec(IDvInvocation paramIDvInvocation) {
		log.debug("volumeDec: "  +Utils.getLogText(paramIDvInvocation));
		long volume = iPlayer.decVolume();
		//propertiesLock();
		//setPropertyVolume(volume);
		//propertiesUnlock();
		//iPlayer.setVolume(volume);
		//log.debug("VolumeDec: " + volume);
	}

	protected void setMute(IDvInvocation paramIDvInvocation, boolean mute) {
		log.debug("vsetMute: "  + mute  +Utils.getLogText(paramIDvInvocation));
		iPlayer.setMute(mute);
	}
	
	private void updateMute(boolean mute)
	{
		//propertiesLock();
		setPropertyMute(mute);
		//propertiesUnlock();
	}

	protected long volume(IDvInvocation paramIDvInvocation) {
		log.debug("volume: "  +Utils.getLogText(paramIDvInvocation));
		long volume = iPlayer.getVolume();
		log.debug("GetVolume: " + volume);
		if(volume < 0)
			volume = 0;
		return volume;
	}
	
	@Override
	protected long volumeLimit(IDvInvocation paramIDvInvocation) {
		log.debug("volumeLimit: "  +Utils.getLogText(paramIDvInvocation));
		return 100;
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase e = (EventBase) obj;
		switch(e.getType())
		{
		case EVENTVOLUMECHANGED:
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
