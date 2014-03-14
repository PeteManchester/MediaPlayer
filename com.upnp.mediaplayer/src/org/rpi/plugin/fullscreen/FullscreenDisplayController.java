package org.rpi.plugin.fullscreen;

import org.apache.log4j.Logger;
import org.joda.time.Period;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.utils.Utils;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

/**
 *
 */
public class FullscreenDisplayController implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(FullscreenDisplayController.class);

    private TrackModel model;
    private FullscreenDisplayView view;

    private SwingWorker<Void, Void> worker;

    public FullscreenDisplayController(FullscreenDisplayView view, TrackModel model) {
        this.model = model;
        this.view = view;

        this.model.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        Object newValue = (Object)e.getNewValue();

        System.out.println("PropertyName: " + propertyName);
        if (propertyName.equals("albumTitle")) {
            view.getAlbumPanel().setText(newValue.toString());
        }
        else if (propertyName.equals("artist")) {
            view.getArtistPanel().setText(newValue.toString());
        }
        else if (propertyName.equals("trackTitle")) {
            view.getTrackPanel().setText(newValue.toString());
        }
        else if (propertyName.equals("genre")) {
            view.getGenrePanel().setText(newValue.toString());
        }
        else if (propertyName.equals("trackDuration")) {
            // duration has to be a Long value ;-)
            String value = Utils.printTimeString((Long) newValue);
            LOGGER.info("duration: " + value);
            view.getTrackDurationLabel().setText(value);
        }
        else if (propertyName.equals("playTime")) {
            // playtime has to be a Long value ;-)
            Long time = (Long)newValue;
            time = time * 1000;
            String value = Utils.printTimeString(time);
            LOGGER.info("playTime: " + value);
            view.getPlayTimeLabel().setText(value);
        }
        else if (propertyName.equals("imageURI")) {
            try {
                view.setImage(newValue.toString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
