package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgReceiver1;
import org.rpi.config.Config;
import org.rpi.playlist.CustomTrack;
import org.rpi.songcast.OHZManager;
import org.rpi.utils.Utils;

public class PrvReceiver extends DvProviderAvOpenhomeOrgReceiver1 implements IDisposableDevice {

    private Logger log = Logger.getLogger(PrvReceiver.class);
    private boolean bPlay = false;
    private CustomTrack track = null;
    private OHZManager manager = null;



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
    protected String protocolInfo(IDvInvocation paramIDvInvocation) {
        log.debug("getProtocolInfo" + Utils.getLogText(paramIDvInvocation));
        return getPropertyProtocolInfo();
    }

    @Override
    protected Sender sender(IDvInvocation paramIDvInvocation) {
        log.debug("Sender" + Utils.getLogText(paramIDvInvocation));
        Sender sender = new Sender("", "");
        // TODO Auto-generated method stub
        return sender;
    }

    @Override
    protected void play(IDvInvocation paramIDvInvocation) {
        log.debug("Play" + Utils.getLogText(paramIDvInvocation));
        bPlay = true;
        // TODO Auto-generated method stub
        //super.play(arg0);
        //Play seems to come before setSender so set a boolean flag that we want to play and then when we
        //get the setSender event start playing??
    }

    @Override
    protected void setSender(IDvInvocation paramIDvInvocation, String uri, String metadata) {
        log.debug("SetSender, URL: " + uri + " MetaData: " + metadata + Utils.getLogText(paramIDvInvocation));
        propertiesLock();
        setPropertyUri(uri);
        propertiesUnlock();
        setPropertyMetadata(metadata);

        if(bPlay = true)
        {
            CustomTrack t = new CustomTrack(uri, metadata, 1);
            track = t;
            if(manager!=null)
            {
                manager.stop();
                manager = null;
            }
            int lastSlash = uri.lastIndexOf("/");
            String songcast_url = uri.substring(0, lastSlash);
            String zoneID = uri.substring(lastSlash+1);
            log.debug("SongCast URL: " + songcast_url + " ZoneID: " + zoneID );
            manager = new OHZManager(songcast_url, zoneID);
            manager.start();
        }
    }

    @Override
    protected void stop(IDvInvocation paramIDvInvocation) {
        log.debug("Stop" + Utils.getLogText(paramIDvInvocation));
        // TODO Auto-generated method stub
        //super.stop(arg0);
    }

    @Override
    protected String transportState(IDvInvocation paramIDvInvocation) {
        log.debug("Transport State" + Utils.getLogText(paramIDvInvocation));
        return getPropertyTransportState();
    }

    @Override
    public String getName() {
        return "Receiver";
    }


}