package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.providers.DvProviderUpnpOrgRenderingControl1;;

public class PrvRenderingControl extends DvProviderUpnpOrgRenderingControl1 {

	private Logger log = Logger.getLogger(Template.class);


	public PrvRenderingControl(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomRenderingControl");	
		enablePropertyLastChange();
		
		setPropertyLastChange("");
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
	}



}
