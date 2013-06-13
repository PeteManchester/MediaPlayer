package org.rpi.player;

import java.util.Observer;

import org.rpi.playlist.CustomTrack;

public interface IPlayer {
	
	public void preLoadTrack(CustomTrack track);
	public  void loaded();
	
	public boolean playTrack(CustomTrack track,long volume, boolean mute);
	public void openFile(CustomTrack track);
	public void pause(boolean bPause);	
	public void resume();
	public void stop();

	public void destroy();
	
	public void setMute(boolean mute);
	public void setVolume(long volume);
	
	public void seekAbsolute(long seconds);
	
	public void addObserver(Observer obj);

	public void startTrack();
	public boolean isPlaying();
	
	public String getUniqueId();

}
