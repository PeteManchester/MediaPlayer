package org.rpi.mpdplayer;
/**
 * Used to abstract the differences between the different playes
 * with MPD we keep the same instance all the time
 */

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.player.IPlayerController;
import org.rpi.player.events.EventBase;


public class MPDPlayerController extends Observable implements IPlayerController, Observer {

	private Logger log = Logger.getLogger(MPDPlayerController.class);
	private MPDPlayer mpdPlayer = null;

	public MPDPlayerController() {
		mpdPlayer = new MPDPlayer();
		mpdPlayer.addObserver(this);
	}

	@Override
	public void preLoadTrack(ChannelBase  track) {
		mpdPlayer.preLoadTrack(track);
	}

	@Override
	public void loaded() {
		// TODO Auto-generated method stub
	}

	@Override
	public void openFile(ChannelBase track) {
		// TODO Auto-generated method stub
	}

	@Override
	public void playThis(ChannelBase t, long v, boolean bMute , boolean isStopped) {
		mpdPlayer.playTrack(t, v, bMute, isStopped);
	}

	@Override
	public void pause(boolean bPause) {
		mpdPlayer.pause(bPause);
	}

	@Override
	public void resume() {
		mpdPlayer.resume();
	}

	@Override
	public void stop() {
		mpdPlayer.stop();
	}

	@Override
	public void destroy() {
		mpdPlayer.destroy();
	}

	@Override
	public void setMute(boolean mute) {
		mpdPlayer.setMute(mute);
	}

	@Override
	public void setVolume(long volume) {
		mpdPlayer.setVolume(volume);
	}

	@Override
	public void seekAbsolute(long seconds) {
		mpdPlayer.seekAbsolute(seconds);
	}

	@Override
	public boolean isPlaying() {
		return mpdPlayer.isPlaying();
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public String getUniqueId() {
		return mpdPlayer.getUniqueId();
	}

	@Override
	public void update(Observable arg0, Object obj) {
		EventBase e = (EventBase)obj;
		setChanged();
		notifyObservers(obj);
	}
}
