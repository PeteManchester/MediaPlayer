package org.rpi.plugin.fullscreen;

import org.apache.log4j.Logger;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.utils.Utils;

import java.util.Timer;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimerTask;

/**
 *
 */
public class FullscreenDisplayController implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(FullscreenDisplayController.class);

    private TrackModel model;
    private FullscreenDisplayView view;

    public FullscreenDisplayController(FullscreenDisplayView view, TrackModel model) {
        this.model = model;
        this.view = view;

        this.model.addPropertyChangeListener(this);

        Clock clock = new Clock(this.view.getCurrentTimeLabel());
        clock.start();
    }

    public void propertyChange(PropertyChangeEvent e) {
        String propertyName = e.getPropertyName();
        Object newValue = (Object)e.getNewValue();

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
            LOGGER.debug("duration: " + value);
            view.getTrackDurationLabel().setText(value);
        }
        else if (propertyName.equals("playTime")) {
            // playtime has to be a Long value ;-)
            Long time = (Long)newValue;
            time = time * 1000;
            String value = Utils.printTimeString(time);
            LOGGER.debug("playTime: " + value);
            view.getPlayTimeLabel().setText(value);
        }
        else if (propertyName.equals("imageURI")) {
            view.setImage(newValue.toString());
        }
    }

    private class Clock {

        private int currentSecond;
        private Calendar calendar;
        private JLabel label;

        public Clock(JLabel label) {
            this.label = label;
        }

        private void reset(){
            calendar = Calendar.getInstance();
            currentSecond = calendar.get(Calendar.SECOND);
        }

        public void start(){
            reset();
            Timer timer = new Timer();
            timer.scheduleAtFixedRate( new TimerTask(){
                public void run(){
                    if( currentSecond == 60 ) {
                        reset();
                        label.setText(LocalTime.now().toString("HH:mm"));
                    }
                    currentSecond++;
                }
            }, 0, 1000 );
        }
    }

}
