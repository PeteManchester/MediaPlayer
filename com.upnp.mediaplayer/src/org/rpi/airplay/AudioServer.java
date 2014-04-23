package org.rpi.airplay;

/**
 * The class that process audio data
 * @author bencall
 *
 */
import java.net.InetAddress;
import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;

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

	
	private AudioEventQueue queue = null;
	private Thread threadMessageQueue = null;

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
		if(queue !=null)
		{
			queue.stop();
		}
		threadMessageQueue = null;
		
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
		queue = new AudioEventQueue(session);
		threadMessageQueue = new Thread(queue, "AudioEventQueue");
		threadMessageQueue.start();		
		control = new AudioChannel(session.getLocalAddress(), session.getRemoteAddress(),session.getControlPort(),queue);
	}


	/**
	 * Flush the audioBuffer
	 */
	public void flush() {
		queue.clear();
	}

}
