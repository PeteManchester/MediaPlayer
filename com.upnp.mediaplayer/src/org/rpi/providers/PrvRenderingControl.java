package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.ActionError;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgRenderingControl1;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.utils.Utils;

public class PrvRenderingControl extends DvProviderUpnpOrgRenderingControl1 implements Observer {

	private Logger log = Logger.getLogger(PrvRenderingControl.class);
	private String isMute = "0";
	private String volume = "100";

	public PrvRenderingControl(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomRenderingControl");
		enablePropertyLastChange();
		createEvent();
		// setPropertyLastChange("<Event xmlns = \"urn:schemas-upnp-org:metadata-1-0/RCS/\"> <InstanceID val=\"0\"> <Volume val=\"100\" Channel=\"RF\"/> <Volume val=\"100\" Channel=\"LF\"/> </InstanceID></Event>");
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

	private void createEvent() {
		getMuteAsString(PlayManager.getInstance().getMute());
		long v = PlayManager.getInstance().getVolume();
		if(v < 0)
			v = 0;
		volume = "" + v;
		StringBuilder sb = new StringBuilder();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<VerticalKeystone val=\"\" />");
		sb.append("<Loudness val=\"\" />");
		sb.append("<HorizontalKeystone val=\"\" />");
		sb.append("<BlueVideoBlackLevel val=\"\" />");
		sb.append("<RedVideoGain val=\"\" />");
		sb.append("<GreenVideoBlackLevel val=\"\" />");
		sb.append("<Volume channel=\"Master\" val=\"" + volume + "\" />");
		sb.append("<Mute channel=\"Master\" val=\"" + isMute + "\" />");
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

	// #####################################################################################################################
	protected String listPresets(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("listPresets: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return "FactoryDefaults,InstallationDefaults";
	}

	@Override
	protected void selectPreset(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SelectPreset: " + paramString + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getBrightness(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getBrightness: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setBrightness(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setBrightness: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getContrast(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getContrast: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setContrast(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setContrast: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getSharpness(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getSharpness: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setSharpness(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setSharpness: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getRedVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getRedVideoGain: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setRedVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setRedVideoGain: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getGreenVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getGreenVideoGain: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setGreenVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setGreenVideoGain: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getBlueVideoGain(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getBlueVideoGain: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setBlueVideoGain(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setBlueVideoGain: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getRedVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getRedVideoBlackLevel: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setRedVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setRedVideoBlackLevel: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getGreenVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getGreenVideoBlackLevel: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setGreenVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setGreenVideoBlackLevel: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getBlueVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getBlueVideoBlackLevel: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setBlueVideoBlackLevel(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setBlueVideoBlackLevel: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected long getColorTemperature(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getColorTemperature: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setColorTemperature(IDvInvocation paramIDvInvocation, long paramLong1, long paramLong2) {
		checkValue(paramLong2);
		log.debug("setColorTemperature: " + paramLong2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected int getHorizontalKeystone(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getHorizontalKeystone: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setHorizontalKeystone(IDvInvocation paramIDvInvocation, long paramLong, int paramInt) {
		checkValue(paramInt);
		log.debug("setHorizontalKeystone: " + paramInt + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected int getVerticalKeystone(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("getVerticalKeystone: " + paramLong + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setVerticalKeystone(IDvInvocation paramIDvInvocation, long paramLong, int paramInt) {
		checkValue(paramInt);
		log.debug("setVerticalKeystone: " + paramInt + Utils.getLogText(paramIDvInvocation));
	}

	// protected boolean getMute(IDvInvocation paramIDvInvocation, long
	// paramLong, String paramString) {
	// throw new ActionDisabledError();
	// }

	// protected void setMute(IDvInvocation paramIDvInvocation, long paramLong,
	// String paramString, boolean paramBoolean) {
	// throw new ActionDisabledError();
	// }

	// protected long getVolume(IDvInvocation paramIDvInvocation, long
	// paramLong, String paramString) {
	// throw new ActionDisabledError();
	// }

	// protected void setVolume(IDvInvocation paramIDvInvocation, long
	// paramLong1, String paramString, long paramLong2) {
	// throw new ActionDisabledError();
	// }

	@Override
	protected int getVolumeDB(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("getVolumeDB" + Utils.getLogText(paramIDvInvocation));
		return 0;
	}

	@Override
	protected void setVolumeDB(IDvInvocation paramIDvInvocation, long paramLong, String paramString, int paramInt) {
		checkValue(paramInt);
		log.debug("setVolumeDB: " + paramInt + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected GetVolumeDBRange getVolumeDBRange(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("getVolumeDBRange" + Utils.getLogText(paramIDvInvocation));
		GetVolumeDBRange dbr = new GetVolumeDBRange(0, 0);
		return dbr;
	}

	@Override
	protected boolean getLoudness(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("getLoudness" + Utils.getLogText(paramIDvInvocation));
		return false;
	}

	@Override
	protected void setLoudness(IDvInvocation paramIDvInvocation, long paramLong, String paramString, boolean paramBoolean) {
		log.debug("setLoudness: " + paramBoolean + Utils.getLogText(paramIDvInvocation));
	}

	// #####################################################################################################################

	@Override
	public long getVolume(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("GetVolume: " + paramString + Utils.getLogText(paramIDvInvocation));
		try {
			long v = PlayManager.getInstance().getVolume();
			if(v<0)
				v=0;
			volume = "" + v;
			log.debug("Returning Volume: " + v);
			return v;
		} catch (Exception e) {
			log.error("Error GetVolume", e);
		}
		return 100;
	}

	@Override
	protected void setVolume(IDvInvocation paramIDvInvocation, long paramLong1, String mixer, long volume) {
		checkValue(volume);
		log.debug("setVolume" + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().setVolume(volume);

	}


	@Override
	public boolean getMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("getMute" + Utils.getLogText(paramIDvInvocation));
		return PlayManager.getInstance().getMute();
	}

	@Override
	public void setMute(IDvInvocation paramIDvInvocation, long paramLong, String paramString, boolean bMute) {
		log.debug("setVolume" + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().setMute(bMute);
	}

	private void updateVolume() {
		StringBuffer sb = new StringBuffer();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<Volume Channel=\"Master\" val=\"");
		sb.append(volume);
		sb.append("\"/>");
		sb.append("<VolumeDB Channel=\"Master\" val=\"0\" />");
		sb.append("</InstanceID></Event>");
		log.debug("VolumeString: " + sb.toString());
		setPropertyLastChange(sb.toString());
	}

	private void updateMute() {
		StringBuffer sb = new StringBuffer();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/RCS/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<Mute Channel=\"Master\" val=\"");
		sb.append(isMute);
		sb.append("\"/>");
		sb.append("</InstanceID></Event>");
		log.debug("MuteString: " + sb.toString());
		setPropertyLastChange(sb.toString());
	}

	@Override
	public void update(Observable arg0, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTVOLUMECHANGED:
			EventVolumeChanged ev = (EventVolumeChanged) e;
			String mVolume = "" + ev.getVolume();
			if (!mVolume.equalsIgnoreCase(volume)) {
				volume = mVolume;
				// updateVolume();
				createEvent();
			}
			// updateVolume(ev.getVolume());
			// updateVolume();
			break;
		case EVENTMUTECHANGED:
			EventMuteChanged em = (EventMuteChanged) e;
			// updateMute();
			String test = getMuteAsString(em.isMute());
			if (!test.equalsIgnoreCase(isMute)) {
				isMute = test;
				// updateMute();
				createEvent();
			}

			break;
		}

	}

	private String getMuteAsString(boolean mute) {
		String mIsMute = "0";
		if (mute) {
			mIsMute = "1";
		}
		return mIsMute;
	}
	
	private void checkValue(int value)
	{
		long l = (long)value;
		checkValue(l);
	}
	private void checkValue(long value) {
		if (value < 0 || value > 100)
			throw new ActionError("Specified Value: " + value + " Must be &lt;= 100");		
	}

}
