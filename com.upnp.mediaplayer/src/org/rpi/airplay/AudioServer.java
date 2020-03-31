package org.rpi.airplay;

/**
 * The class that process audio data
 * @author bencall
 *
 */
import java.net.InetAddress;

import org.apache.log4j.Logger;
import org.rpi.airplay.audio.ProcessAirplayAudio;
import org.rpi.config.Config;
import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IJavaSoundPlayer;
import org.rpi.java.sound.JavaSoundPlayerBasic;
import org.rpi.java.sound.JavaSoundPlayerLatency;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;

/**
 * Main class that listen for new packets.
 * 
 * @author bencall
 * 
 */
public class AudioServer {
	Logger log = Logger.getLogger(this.getClass());
	// Constantes
	public static final int BUFFER_FRAMES = 512; // Total buffer size (number of
													// frame)
	public static final int START_FILL = 282; // Alac will wait till there are
												// START_FILL frames in buffer
	public static final int MAX_PACKET = 2048; // Also in UDPListener (possible

	
	private ProcessAirplayAudio processQueue = null;
	private Thread threadProcessor = null;
	
	private SourceTimer timer = null;
	private Thread timerThread = null;
	
	

	// client address
	private InetAddress rtpClient;

	// Audio infos and datas
	private AudioSession session;
	
	private AudioChannel control = null;

	/**
	 * Constructor. Initiate instances vars
	 * 
	 * @param aesiv
	 * @param aeskey
	 * @param fmtp
	 * @param controlPort
	 * @param timingPort
	 */
	public AudioServer(AudioSession session) {
		this.session = session;
		initRTP();
	}

	public void stop() {
		log.debug("Stop AudioServer");
		stopTimer();
		if(processQueue !=null)
		{
			processQueue.stop();
		}
		threadProcessor = null;
		
		if(control !=null)
		{
			try
			{
			control.close();
			}
			catch(Exception e)
			{
				log.error("Error Closing AudioChannel",e);
			}finally
			{
				control = null;
			}
		}
		PlayManager.getInstance().setStatus("Stopped", "AIRPLAY");
	}

	/**
	 * Opens the sockets and begin listening
	 */
	private void initRTP() {
		AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);		
		//if (Config.getInstance().isAirPlayLatencyEnabled()) {
			// With Latency
			processQueue = new ProcessAirplayAudio();
			threadProcessor = new Thread(processQueue, "AirplayProcessAudio");
		//} else {
		//	player = new JavaSoundPlayerBasic();
		//	threadPlayer = new Thread(player, "SongcastPlayerJavaSound");
		//}
		//player.createSoundLine(audioInf);
		threadProcessor.start();
		/*
		TrackInfo info = new TrackInfo();
		info.setBitDepth(audioInf.getBitDepth());
		info.setCodec(audioInf.getCodec());
		info.setBitrate(audioInf.getBitRate());
		info.setSampleRate((long) audioInf.getSampleRate());
		info.setDuration(0);
		EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
		ev.setTrackInfo(info);
		if (ev != null) {
			PlayManager.getInstance().updateTrackInfo(ev);
		}	
		*/		
		control = new AudioChannel(session.getLocalAddress(), session.getRemoteAddress(),session.getControlPort(), processQueue);
		startTimer();
	}


	/**
	 * Flush the audioBuffer
	 */
	public void flush() {
		processQueue.clear();
	}
	
	private void startTimer() {
		timer = new SourceTimer();
		timerThread = new Thread(timer, "SongcastTimer");
		timerThread.start();
	}
	
	private void stopTimer() {
		if (timer != null) {
			timer.setRun(false);
			timer = null;
		}
		if (timerThread != null) {
			timerThread = null;
		}
	}

}
