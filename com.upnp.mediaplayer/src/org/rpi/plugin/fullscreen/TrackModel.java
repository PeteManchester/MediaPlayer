package org.rpi.plugin.fullscreen;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
  */
public class TrackModel {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener changeListener) {
        this.pcs.addPropertyChangeListener(changeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener changeListener) {
        this.pcs.removePropertyChangeListener(changeListener);
    }

    private String trackTitle;

    private String albumTitle;

    private String artist;

    private String imageURI;

    private String playingTime;

    private Long trackDuration;

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        String oldValue = this.trackTitle;
        this.trackTitle = trackTitle;
        this.pcs.firePropertyChange("trackTitle", oldValue, trackTitle);
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        String oldValue = this.albumTitle;
        this.albumTitle = albumTitle;
        this.pcs.firePropertyChange("albumTitle", oldValue, albumTitle);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        String oldValue = this.artist;
        this.artist = artist;
        this.pcs.firePropertyChange("artist", oldValue, artist);
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        String oldValue = this.imageURI;
        this.imageURI = imageURI;
        this.pcs.firePropertyChange("imageURI", oldValue, imageURI);
    }

    public Long getTrackDuration() {
        return trackDuration;
    }

    public void setTrackDuration(Long trackDuration) {
        Long oldValue = this.trackDuration;
        this.trackDuration = trackDuration;
        this.pcs.firePropertyChange("trackDuration", oldValue, trackDuration);
    }
}
