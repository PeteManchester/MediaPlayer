package org.rpi.player;

/**
 * Used to abstract the differences between the different playes
 */

import java.util.Observer;

import org.rpi.playlist.CustomTrack;

public interface IPlayerController {
	
	public void preLoadTrack(CustomTrack track);
	public  void loaded();
	
	//public boolean playTrack(CustomTrack track,long volume, boolean mute);
	public void openFile(CustomTrack track);
	public void playThis(CustomTrack t,long v,boolean bMute);
	//public void playIndex(long index);
	//public void play();
	public void pause(boolean bPause);	
	public void resume();
	public void stop();

	public void destroy();
	
	public void setMute(boolean mute);
	public void setVolume(long volume);
	
	public void seekAbsolute(long seconds);
	public void addObserver(Observer obj);
	//public void startTrack();
	public boolean isPlaying();
	public boolean isActive();
	
	public String getUniqueId();

}
