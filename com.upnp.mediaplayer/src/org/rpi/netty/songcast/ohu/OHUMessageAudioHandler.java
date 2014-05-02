package org.rpi.netty.songcast.ohu;

/**
 * OHUMessageAudioHandler
 * Handles the OHUMessageAudio
 * Create an Player and passes the sound bytes
 */

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.apache.log4j.Logger;
import org.rpi.mplayer.TrackInfo;
import org.rpi.netty.songcast.ohz.SongcastPlayerJavaSound;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.songcast.core.AudioInformation;

public class OHUMessageAudioHandler extends SimpleChannelInboundHandler<OHUMessageAudio> {
	
	private Logger log = Logger.getLogger(this.getClass());
	private SongcastPlayerJavaSound player =null;
	private AudioInformation audioInformation = null;
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageAudio msg) throws Exception {
		if(msg instanceof OHUMessageAudio)
		{
			if(player !=null)
			{
				//log.debug("Audio" + msg.getAudio().length);
				AudioInformation ai = msg.getAudioInformation();
				if(ai !=null && !ai.compare(audioInformation))
				{
					player.createSoundLine(ai);
					setAudioInformation(ai);
				}
				ai=null;
				player.put(msg);				
			}
			else
			{
				player= new SongcastPlayerJavaSound();
				setAudioInformation(msg.getAudioInformation());
				player.createSoundLine(audioInformation);
			}
			msg = null;
		}		
	}
	
	private void setAudioInformation(AudioInformation ai)
	{
		audioInformation = ai;
		try
		{
			TrackInfo info = new TrackInfo();
			info.setBitDepth(ai.getBitDepth());
			info.setCodec(ai.getCodec());
			info.setBitrate(ai.getBitRate());
			info.setSampleRate((long) ai.getSampleRate());
			info.setDuration(0);
			EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
			ev.setTrackInfo(info);
			if (ev != null) {
				PlayManager.getInstance().updateTrackInfo(ev);
			}
		}
		catch(Exception e)
		{
			
		}
	}
}
