package org.rpi.mplayer;

/**
 * Used to abstract the differences between the different playes
 * with mplayer we start a new instance for each track
 */

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.player.IPlayer;
import org.rpi.player.IPlayerController;

public class MPlayerController extends Observable implements IPlayerController, Observer {

	IPlayer mPlayer = null;
	private static Logger log = Logger.getLogger(MPlayerController.class);

	@Override
	public void preLoadTrack(ChannelBase track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loaded() {
		// TODO Auto-generated method stub

	}

	// @Override
	// public boolean playTrack(CustomTrack track, long volume, boolean mute) {
	// // TODO Auto-generated method stub
	// return false;
	// }

	@Override
	public void openFile(ChannelBase track) {
		mPlayer.openFile(track);
	}

	@Override
	public void playThis(ChannelBase t, long v, boolean bMute) {
		if (t != null) {

			log.debug("Destroy current MPlayer");
			if (mPlayer != null) {
				mPlayer.destroy();
				mPlayer = null;
			}
			mPlayer = new MPlayer();
			mPlayer.addObserver(this);
			mPlayer.playTrack(t, v, bMute);
		}

	}

//	@Override
//	public void playIndex(long index) {
//		// TODO Auto-generated method stub
//
//	}

//	@Override
//	public void play() {
//		// TODO Auto-generated method stub
//
//	}

	@Override
	public void pause(boolean bPause) {
		if(mPlayer!=null)
		{
			mPlayer.pause(bPause);
		}

	}

	@Override
	public void resume() {
		if(mPlayer!=null)
			mPlayer.resume();
	}

	@Override
	public void stop() {
		if(mPlayer !=null)
			mPlayer.stop();

	}

	@Override
	public void destroy() {
		if (mPlayer != null)
		{
			log.debug("Attempt to Destroy MPlayer");
			mPlayer.destroy();
		}
	}

	@Override
	public void setMute(boolean mute) {
		if (mPlayer != null)
			mPlayer.setMute(mute);

	}

	@Override
	public void setVolume(long volume) {
		if (mPlayer != null)
			mPlayer.setVolume(volume);

	}

	@Override
	public void seekAbsolute(long seconds) {
		if (mPlayer != null)
			mPlayer.seekAbsolute(seconds);

	}



	// @Override
	// public void startTrack() {
	// mPlayer.startTrack();
	//
	// }

	@Override
	public boolean isPlaying() {
		if (mPlayer != null) {
			return mPlayer.isPlaying();
		}
		return false;
	}

	@Override
	public String getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Observable arg0, Object obj) {
		setChanged();
		notifyObservers(obj);
	}

	@Override
	public boolean isActive() {
		if (mPlayer != null) {
			return true;
		}
		return false;
	}

}
