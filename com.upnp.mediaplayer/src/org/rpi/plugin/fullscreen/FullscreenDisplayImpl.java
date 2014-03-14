package org.rpi.plugin.fullscreen;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;

import javax.swing.*;

/**
 *
 */
@PluginImplementation
public class FullscreenDisplayImpl implements FullscreenDisplayInterface, Observer {

    private static final Logger LOGGER = Logger.getLogger(FullscreenDisplayImpl.class);

    private TrackModel model;

    public FullscreenDisplayImpl() {
        LOGGER.info("Init LastFmPluginImpl");
//        getConfig();
//        init();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();

        if (device.isFullScreenSupported()){

            DisplayMode dm = device.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            int bitDepth = dm.getBitDepth();
            LOGGER.info("display supports " + screenWidth + "x" + screenHeight + ":" + bitDepth);

//            device.setFullScreenWindow(this);
            // for testing purposes
            device.setFullScreenWindow(null);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { e.printStackTrace(); }

        FullscreenDisplayView fd = new FullscreenDisplayView();
        model = new TrackModel();
        FullscreenDisplayController controller = new FullscreenDisplayController(fd, model);
        fd.setVisible(true);

        LOGGER.info("is dispatch thread?" + SwingUtilities.isEventDispatchThread());

        PlayManager.getInstance().observeInfoEvents(this);
        PlayManager.getInstance().observeProductEvents(this);
        PlayManager.getInstance().observeTimeEvents(this);

        LOGGER.info("Finished FullscreenDisplay Plugin Init");
    }

    @Override
    public void update(Observable o, Object e) {
        EventBase base = (EventBase) e;
        switch (base.getType()) {
            case EVENTTRACKCHANGED:
                EventTrackChanged etc = (EventTrackChanged) e;
                ChannelBase track = etc.getTrack();
                if (track != null) {
                    model.setAlbumTitle(track.getAlbum());
                    model.setArtist(track.getArtist());
                    model.setTrackTitle(track.getTitle());
                    model.setImageURI(track.getAlbumArtUri());
                    model.setTrackDuration(track.getDuration());
                    model.setGenre(track.getGenre());
                } else {
                    LOGGER.info("Track was NULL");
                }

                break;
            case EVENTTIMEUPDATED:
                EventTimeUpdate ed = (EventTimeUpdate) e;
                model.setPlayTime(ed.getTime());
                break;
        }
    }

}
