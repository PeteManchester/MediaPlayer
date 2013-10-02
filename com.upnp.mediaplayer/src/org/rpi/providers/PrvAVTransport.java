package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1;
import org.rpi.player.PlayManager;

public class PrvAVTransport extends DvProviderUpnpOrgAVTransport1 {

	private Logger log = Logger.getLogger(PrvAVTransport.class);


	public PrvAVTransport(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating AvTransport");
		enablePropertyLastChange();
		
		setPropertyLastChange("");
		
		enableActionGetCurrentTransportActions();
		enableActionGetCurrentTransportActions();
		enableActionGetDeviceCapabilities();
		enableActionGetMediaInfo();
		enableActionGetPositionInfo();
		enableActionGetTransportInfo();
		enableActionGetTransportSettings();
		enableActionNext();
		enableActionPause();
		enableActionPlay();
		enableActionPrevious();
		//enableActionRecord();
		enableActionSeek();
		enableActionSetAVTransportURI();
		enableActionSetPlayMode();
		//enableActionSetRecordQualityMode();
		enableActionStop();
		
	}
	
	
	@Override
	protected String getCurrentTransportActions(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Transport Actions");
		return "Play,Pause,Stop,Seek,Next,Previous";
	}
	
	@Override
	protected GetDeviceCapabilities getDeviceCapabilities(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetDevice Capabilities");
		GetDeviceCapabilities dev = new GetDeviceCapabilities("NONE,NETWORK,HDD,CD-DA,UNKNOWN", "NOT_IMPLEMENTED", "NOT_IMPLEMENTED");
		return dev;

	}
	
	@Override
	protected GetMediaInfo getMediaInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetMediaInfo");
		GetMediaInfo info = new GetMediaInfo(0, "", "", "", "", "", "", "", "");
		return info;
	}
	
	@Override
	protected GetPositionInfo getPositionInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Get Position Info");
		GetPositionInfo info = new GetPositionInfo(0, "", "", "", "", "", 0, 0);
		return info;
	}
	
	@Override
	protected GetTransportInfo getTransportInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetTransport Info");
		GetTransportInfo info = new GetTransportInfo("Playing", "OK", "1");
		return info;
	}
	
	@Override
	protected GetTransportSettings getTransportSettings(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetTransportSettings");
		GetTransportSettings settings = new GetTransportSettings("NORMAL", "NOT_IMPLEMENTED");
		return settings;
	}
	
	@Override
	protected void next(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Next");
		PlayManager.getInstance().nextTrack();
	}
	
	@Override
	protected void pause(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Pause");
		PlayManager.getInstance().pause();
	}
	
	@Override
	protected void play(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("Play");
		PlayManager.getInstance().play();
	}
	
	@Override
	protected void previous(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Previous");
		PlayManager.getInstance().previousTrack();
	}
	
	@Override
	protected void record(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Record");
	}
	
	@Override
	protected void seek(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("Seek");
		PlayManager.getInstance().seekAbsolute(paramLong);
	}
	
	@Override
	protected void setAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("SetAVTransport: " + paramLong + " " + paramString1 + " " + paramString2);
	}
	
	@Override
	protected void setNextAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("SetNexAVTransport: " + paramLong + " " + paramString1 + " " + paramString2);
	}
	
	@Override
	protected void setPlayMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetPlayMode: " + paramLong + " " + paramString );
	}
	
	@Override
	protected void setRecordQualityMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetRecordQuality: " + paramLong + " " + paramString );
	}
	
	@Override
	protected void stop(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Stop");
		PlayManager.getInstance().stop();
	}
	
}

