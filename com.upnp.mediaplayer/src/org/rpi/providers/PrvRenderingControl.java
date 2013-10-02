package org.rpi.providers;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgRenderingControl1;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;

public class PrvRenderingControl extends DvProviderUpnpOrgRenderingControl1 implements Observer  {

	private Logger log = Logger.getLogger(PrvRenderingControl.class);


	public PrvRenderingControl(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomRenderingControl");	
		enablePropertyLastChange();
		
		setPropertyLastChange("");
		//enableActionGetBlueVideoBlackLevel();
		//enableActionGetBlueVideoGain();
		//enableActionGetBrightness();
		//enableActionGetColorTemperature();
		//enableActionGetContrast();
		//enableActionGetGreenVideoBlackLevel();
		//enableActionGetGreenVideoGain();
		//enableActionGetHorizontalKeystone();
		enableActionGetLoudness();
		enableActionGetMute();
		//enableActionGetRedVideoBlackLevel();
		//enableActionGetRedVideoGain();
		//enableActionGetSharpness();
		//enableActionGetVerticalKeystone();
		enableActionGetVolume();
		//enableActionGetVolumeDB();
		//enableActionGetVolumeDBRange();
		//enableActionListPresets();
		//enableActionSelectPreset();
		//enableActionSetBlueVideoBlackLevel();
		//enableActionSetBlueVideoGain();
		//enableActionSetBrightness();
		//enableActionSetColorTemperature();
		//enableActionSetContrast();
		//enableActionSetGreenVideoBlackLevel();
		//enableActionSetGreenVideoGain();
		//enableActionSetHorizontalKeystone();
		enableActionSetLoudness();
		enableActionSetMute();
		//enableActionSetRedVideoBlackLevel();
		//enableActionSetRedVideoGain();
		//enableActionSetSharpness();
		//enableActionSetVerticalKeystone();
		enableActionSetVolume();
		//enableActionSetVolumeDB();
		PlayManager.getInstance().observVolumeEvents(this);
	}
	
	public long getVolume(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		return PlayManager.getInstance().getVolume();
	}
	
	protected void setVolume(IDvInvocation paramIDvInvocation, long paramLong1, String mixer, long volume) {
		PlayManager.getInstance().setVolume(volume);
	}
	
	public boolean getMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		return PlayManager.getInstance().getMute();
	}

	public void setMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString, boolean bMute) {
		PlayManager.getInstance().setMute(bMute);
	}
	
	private void updateVolume()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<Volume channel=\"Master\" val=\"");
		sb.append("" + PlayManager.getInstance().getVolume() );
		sb.append("\"/>");
		//sb.append("");
		sb.append("</InstanceID></Event>");		
		//log.debug("VolumeString: " + sb.toString());
		setPropertyLastChange(sb.toString());
	}
	
	private void updateMute()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<Mute channel=\"Master\" val=\"");
		String mute = "0";
		if(PlayManager.getInstance().getMute())
		{
			mute = "1";
		}
		sb.append(mute);
		sb.append("\"/>");
		//sb.append("");
		sb.append("</InstanceID></Event>");		
		//log.debug("MuteString: " + sb.toString());
		setPropertyLastChange(sb.toString());
	}


	@Override
	public void update(Observable arg0, Object obj) {
		EventBase e = (EventBase) obj;
		switch(e.getType())
		{
		case EVENTVOLUMECHNANGED:
			EventVolumeChanged ev = (EventVolumeChanged)e;
			//updateVolume(ev.getVolume());
			updateVolume();
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged)e;
			//updateMute(em.isMute());
			updateMute();
			break;
		}
		
	}

}
