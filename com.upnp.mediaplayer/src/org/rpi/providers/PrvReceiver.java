package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgReceiver1;
import org.rpi.config.Config;
import org.rpi.playlist.CustomTrack;

public class PrvReceiver extends DvProviderAvOpenhomeOrgReceiver1 {

	private Logger log = Logger.getLogger(PrvReceiver.class);
	private boolean bPlay = false;



	public PrvReceiver(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Template");
		enablePropertyMetadata();
		enablePropertyProtocolInfo();
		enablePropertyTransportState();
		enablePropertyUri();
		
		setPropertyMetadata("");
		setPropertyProtocolInfo(Config.getProtocolInfo());
		setPropertyTransportState("Stopped");
		setPropertyUri("");
		
		enableActionPlay();
		enableActionProtocolInfo();
		enableActionSender();
		enableActionSetSender();
		enableActionStop();
		enableActionTransportState();
		
	}
	
	@Override
	protected String protocolInfo(IDvInvocation arg0) {
		log.debug("getProtocolInfo");
		return getPropertyProtocolInfo();
	}
	
	@Override
	protected Sender sender(IDvInvocation arg0) {
		log.debug("Sender");
		Sender sender = new Sender("", "");
		// TODO Auto-generated method stub
		return sender;
	}
	
	@Override
	protected void play(IDvInvocation arg0) {
		log.debug("Play");
		bPlay = true;
		// TODO Auto-generated method stub
		//super.play(arg0);
		//Play seems to come before setSender so set a boolean flag that we want to play and then when we 
		//get the setSender event start playing??
	}
	
	@Override
	protected void setSender(IDvInvocation arg0, String uri, String metadata) {
		log.debug("SetSender, URL: " + uri + " MetaData: " + metadata);
		propertiesLock();
		setPropertyUri(uri);
		propertiesUnlock();
		setPropertyMetadata(metadata);
		if(bPlay = true)
		{
			CustomTrack t = new CustomTrack(uri, metadata, 1);
			//TODO sort out how to play SongCast channel
		}
	}
	
	@Override
	protected void stop(IDvInvocation arg0) {
		log.debug("Stop");
		// TODO Auto-generated method stub
		//super.stop(arg0);
	}
	
	@Override
	protected String transportState(IDvInvocation arg0) {
		log.debug("Transport State");
		return getPropertyTransportState();
	}



}
