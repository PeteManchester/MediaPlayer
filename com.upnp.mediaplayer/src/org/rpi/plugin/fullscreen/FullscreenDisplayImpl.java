package org.rpi.plugin.fullscreen;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.config.Config;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 */
@PluginImplementation
public class FullscreenDisplayImpl implements FullscreenDisplayInterface, Observer {

    private static final Logger LOGGER = Logger.getLogger(FullscreenDisplayImpl.class);

    private TrackModel model;
    private Boolean debug = Boolean.FALSE;

    public FullscreenDisplayImpl() {
        LOGGER.info("Init FullscreenDisplay");
//        getConfig();
//        init();

        String friendlyName = Config.friendly_name;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = ge.getDefaultScreenDevice();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        FullscreenDisplayView fd = new FullscreenDisplayView(friendlyName, debug);

        if (device.isFullScreenSupported()){

            DisplayMode dm = device.getDisplayMode();
            int screenWidth = dm.getWidth();
            int screenHeight = dm.getHeight();
            int bitDepth = dm.getBitDepth();
            LOGGER.info("display supports " + screenWidth + "x" + screenHeight + ":" + bitDepth);

            if (debug) {
                // for testing purposes
                device.setFullScreenWindow(null);
            } else {
                device.setFullScreenWindow(fd);
            }
        }

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
                    model.setAlbumArtist(track.getAlbumArtist());
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

    /**
     * Read the configuration
     *
     */
    private void getConfig() {
        try {
            String class_name = this.getClass().getName();
            LOGGER.debug("Find Class, ClassName: " + class_name);
            String path = OSManager.getInstance().getFilePath(this.getClass(), false);
            LOGGER.debug("Getting fullscreen.properties from Directory: " + path);
            File props = new File(path + "fullscreen.properties");
            Properties properties = new Properties();
            properties.load(new FileReader(props));

            this.debug = Boolean.valueOf((String)properties.get("fullscreen.debug"));

        } catch (IOException e) {
            LOGGER.error("Error Reading LIRCConfig.xml");
        }

    }

}
