package org.rpi.airplay;

/**
 * The class that process audio data
 * @author bencall
 *
 */
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
												// to merge it in one place?)

	// Sockets
	private DatagramSocket sock, csock;
	private UDPListener listener = null;
	private Thread listenerThread = null;

	// client address
	private InetAddress rtpClient;

	// Audio infos and datas
	private AudioSession session;

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
		// Init instance var
		this.session = session;
		initRTP();

	}

	public void stop() {
		log.debug("Stop AudioServer");
		if (listener != null) {
			try
			{
			listener.stopThread();
			listener = null;
			}
			catch(Exception e)
			{
				log.error("Error Stopping UDPListener Thread",e);
			}
			listenerThread = null;
		}
		try {
			csock.close();
		} catch (Exception e) {
			log.error("Error Closing Socket", e);
		}
		PlayManager.getInstance().setStatus("Stopped", "AIRPLAY");
	}

	public void setVolume(double vol) {
		// TODO Change Volume
		log.debug("Set Volume: " + vol);
	}

	/**
	 * Return the server port for the bonjour service
	 * 
	 * @return
	 */
	public int getServerPort() {
		return sock.getLocalPort();
	}

	/**
	 * Opens the sockets and begin listening
	 */
	private void initRTP() {
		int port = session.getControlPort();
		while (true) {
			try {
				sock = new DatagramSocket(port);
				csock = new DatagramSocket(port + 1);
			} catch (IOException e) {
				port = port + 2;
				continue;
			}
			break;
		}

		listener = new UDPListener(sock, session);
		listenerThread = new Thread(listener, "UDPListner");
		listenerThread.start();
	}

	/**
	 * Flush the audioBuffer
	 */
	public void flush() {
		listener.flush();
	}

}
