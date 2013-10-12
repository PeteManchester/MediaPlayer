package org.rpi.providers;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.ActionDisabledError;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgRenderingControl1;
import org.openhome.net.device.providers.DvProviderUpnpOrgRenderingControl1.GetVolumeDBRange;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;

public class PrvRenderingControl extends DvProviderUpnpOrgRenderingControl1 implements Observer  {

	private Logger log = Logger.getLogger(PrvRenderingControl.class);
	private String isMute = "0";
	private String volume = "100";


	public PrvRenderingControl(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomRenderingControl");	
		enablePropertyLastChange();
		createEvent();
		//setPropertyLastChange("<Event xmlns = \"urn:schemas-upnp-org:metadata-1-0/RCS/\"> <InstanceID val=\"0\"> <Volume val=\"100\" channel=\"RF\"/> <Volume val=\"100\" channel=\"LF\"/> </InstanceID></Event>");
		enableActionSetBlueVideoBlackLevel();
		enableActionSetBlueVideoGain();
		enableActionSetBrightness();
		enableActionSetColorTemperature();
		enableActionSetContrast();
		enableActionSetGreenVideoBlackLevel();
		enableActionSetGreenVideoGain();
		enableActionSetHorizontalKeystone();
		enableActionSetLoudness();
		enableActionSetMute();
		enableActionSetRedVideoBlackLevel();
		enableActionSetRedVideoGain();
		enableActionSetSharpness();
		enableActionSetVerticalKeystone();
		enableActionSetVolume();
		enableActionSetVolumeDB();
		enableActionGetBlueVideoBlackLevel();
		enableActionGetBlueVideoGain();
		enableActionGetBrightness();
		enableActionGetColorTemperature();
		enableActionGetContrast();
		enableActionGetGreenVideoBlackLevel();
		enableActionGetGreenVideoGain();
		enableActionGetHorizontalKeystone();
		enableActionGetLoudness();
		enableActionGetMute();
		enableActionGetRedVideoBlackLevel();
		enableActionGetRedVideoGain();
		enableActionGetSharpness();
		enableActionGetVerticalKeystone();
		enableActionGetVolume();
		enableActionGetVolumeDB();
		enableActionGetVolumeDBRange();
		enableActionListPresets();
		enableActionSelectPreset();
		PlayManager.getInstance().observVolumeEvents(this);
		
	}
	
	//#####################################################################################################################
	protected String listPresets(IDvInvocation paramIDvInvocation, long paramLong) {
		return "FactoryDefaults,InstallationDefaults";
	}

	protected void selectPreset(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SelectPreset: " + paramString);
	}

	protected long getBrightness(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setBrightness(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setBrightness: " + paramLong2);
	}

	protected long getContrast(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setContrast(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setContrast: " + paramLong2);
	}

	protected long getSharpness(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setSharpness(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setSharpness: " + paramLong2);
	}

	protected long getRedVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setRedVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setRedVideoGain: " + paramLong2);
	}

	protected long getGreenVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setGreenVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setGreenVideoGain: " + paramLong2);
	}

	protected long getBlueVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setBlueVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setBlueVideoGain: " + paramLong2);
	}

	protected long getRedVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setRedVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setRedVideoBlackLevel: " + paramLong2);
	}

	protected long getGreenVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setGreenVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setGreenVideoBlackLevel: " + paramLong2);
	}

	protected long getBlueVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setBlueVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setBlueVideoBlackLevel: " + paramLong2);
	}

	protected long getColorTemperature(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setColorTemperature(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		log.debug("setColorTemperature: " + paramLong2);
	}

	protected int getHorizontalKeystone(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setHorizontalKeystone(IDvInvocation paramIDvInvocation, long paramLong, int paramInt) {
		log.debug("setHorizontalKeystone: " + paramInt);
	}

	protected int getVerticalKeystone(IDvInvocation paramIDvInvocation, long paramLong) {
		return 0;
	}

	protected void setVerticalKeystone(IDvInvocation paramIDvInvocation, long paramLong, int paramInt) {
		log.debug("setVerticalKeystone: " + paramInt);
	}

	//protected boolean getMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
	//	throw new ActionDisabledError();
	//}

	//protected void setMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString, boolean paramBoolean) {
	//	throw new ActionDisabledError();
	//}

	//protected long getVolume(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
	//	throw new ActionDisabledError();
	//}

	//protected void setVolume(IDvInvocation paramIDvInvocation, long paramLong1, String paramString, long paramLong2) {
	//	throw new ActionDisabledError();
	//}

	protected int getVolumeDB(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		return 0;
	}

	protected void setVolumeDB(IDvInvocation paramIDvInvocation, long paramLong, String paramString, int paramInt) {
		log.debug("setVolumeDB: " + paramInt);
	}

	protected GetVolumeDBRange getVolumeDBRange(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		GetVolumeDBRange dbr = new GetVolumeDBRange(0, 0);
		return dbr;
	}

	protected boolean getLoudness(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		return false;
	}

	protected void setLoudness(IDvInvocation paramIDvInvocation, long paramLong, String paramString, boolean paramBoolean) {
		log.debug("setLoudness: " + paramBoolean);
	}
	
	//#####################################################################################################################
	
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
			volume = ""+ev.getVolume();
			createEvent();
			//updateVolume(ev.getVolume());
			//updateVolume();
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged)e;
			//updateMute();
			if(em.isMute())
			{
				isMute  = "1";
			}
			else
			{
				isMute = "0";
			}
			createEvent();
			break;
		}
		
	}
	
	private void createEvent()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<VerticalKeystone val=\"\" />");
		sb.append("<Loudness val=\"\" />");
		sb.append("<HorizontalKeystone val=\"\" />");
		sb.append("<BlueVideoBlackLevel val=\"\" />");
		sb.append("<RedVideoGain val=\"\" />");
		sb.append("<GreenVideoBlackLevel val=\"\" />");
		sb.append("<Volume channel=\"Master\" val=\"100\" />");
		sb.append("<Mute channel=\"Master\" val=\"0\" />");
		sb.append("<Brightness val=\"\" />");
				sb.append("<PresetNameList val=\"FactoryDefaults\" />");
				sb.append("<ColorTemperature val=\"\" />");
				sb.append("<VolumeDB channel=\"Master\" val=\"0\" />");
				sb.append("<Contrast val=\"\" />");
				sb.append("<GreenVideoGain val=\"\" />");
				sb.append("<RedVideoBlackLevel val=\"\" />");
				sb.append("<BlueVideoGain val=\"\" />");
				sb.append("<Sharpness val=\"\" />");
				sb.append("</InstanceID>");
				sb.append("</Event>");
		setPropertyLastChange(sb.toString());
	}

}
