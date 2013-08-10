package org.rpi.mplayer;

import org.apache.log4j.Logger;

public class PositionThread extends Thread {

	private MPlayer mPlayer = null;
	private Logger log = Logger.getLogger(PositionThread.class);
	private boolean bNewTrack = true;
		
	public PositionThread(MPlayer mPlayer) {
		this.setName("PT-" + mPlayer.getUniqueId());
		setNewTrack(true);
		this.mPlayer= mPlayer;
	}
	

	@Override
    public void run() {
        try {
            while (!isInterrupted()) {
				if (!mPlayer.isbPaused()) {
					try
					{
					mPlayer.getCommandWriter().sendCommand("get_time_pos");
					if (!mPlayer.getTrackInfo().isSet()) {
						mPlayer.getCommandWriter().sendCommand("get_time_length");
						mPlayer.getCommandWriter().sendCommand("get_audio_bitrate");
						mPlayer.getCommandWriter().sendCommand("get_audio_codec");
						mPlayer.getCommandWriter().sendCommand("get_audio_samples");
					}
					}
					catch(Exception e)
					{
						log.error("Error Writing Command: ", e);
					}
				}
				Thread.sleep(1000);
			}
		} catch (InterruptedException e) {
			log.debug("Error in Postion Thread: " + e.getMessage());
		} finally {
			log.debug("Closing Position Thread");
		}
	}

	/**
	 * @return the bNewTrack
	 */
//	private boolean isNewTrack() {
//		return bNewTrack;
//	}

	/**
	 * @param bNewTrack
	 *            the bNewTrack to set
	 */
	public void setNewTrack(boolean bNewTrack) {
		this.bNewTrack = bNewTrack;
	}

}
