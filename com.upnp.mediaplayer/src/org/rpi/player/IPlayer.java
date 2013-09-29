package org.rpi.player;

/**
 * A general implementation of a Player
 */

import java.util.Observer;

import org.rpi.player.events.EventBase;
import org.rpi.playlist.CustomTrack;

public interface IPlayer {
	
	public void preLoadTrack(CustomTrack track);
	public  void loaded();
	
	//Player control
	public boolean playTrack(CustomTrack track,long volume, boolean mute);
	public void openFile(CustomTrack track);
	public void pause(boolean bPause);	
	public void resume();
	public void stop();
	public void seekAbsolute(long seconds);
	public void startTrack();
	public boolean isPlaying();

	//Volume Control
	public void setMute(boolean mute);
	public void setVolume(long volume);
	

	//Event Handling
	public void addObserver(Observer obj);
	public  void fireEvent(EventBase ev);
	public void updateInfo(String artist, String title);
	public void setStatus(String status);
	
	//Probably not used..
	public String getUniqueId();
	
	//Tidy up
	public void destroy();

}
