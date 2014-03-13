package org.rpi.plugin.fullscreen;

import org.apache.log4j.Logger;

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
        String newValue = (String)e.getNewValue();

        System.out.println("PropertyName: " + propertyName);
        if (propertyName.equals("albumTitle")) {
            view.getAlbumPanel().setText(newValue);
        }
        else if (propertyName.equals("artist")) {
            view.getArtistPanel().setText(newValue);
        }
        else if (propertyName.equals("trackTitle")) {
            view.getTrackPanel().setText(newValue);
        }
        else if (propertyName.equals("trackDuration")) {
            view.getTrackDurationLabel().setText(newValue);
        }
        else if (propertyName.equals("imageURI")) {
            try {
                view.setImage(newValue);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}
