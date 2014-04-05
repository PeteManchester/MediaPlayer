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
	private UDPListener l1;

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
		l1.stopThread();
		// synchronized(sock){
		// sock.close();
		// }
		try {
			csock.close();
		} catch (Exception e) {
			log.error("Error Closing Socket", e);
		}

	}

	public void setVolume(double vol) {
		// player.setVolume(vol);
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
		int port = 6000;
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

		l1 = new UDPListener(sock);
	}

	/**
	 * Ask iTunes to resend packet FUNCTIONAL??? NO PROOFS
	 * 
	 * @param first
	 * @param last
	 */
	// public void request_resend(int first, int last) {
	// log.debug("Resend Request: " + first + "::" + last);
	// if(last<first){
	// return;
	// }
	//
	// int len = last - first + 1;
	// byte[] request = new byte[] { (byte) 0x80, (byte) (0x55|0x80), 0x01,
	// 0x00, (byte) ((first & 0xFF00) >> 8), (byte) (first & 0xFF), (byte) ((len
	// & 0xFF00) >> 8), (byte) (len & 0xFF)};
	//
	// try {
	// DatagramPacket temp = new DatagramPacket(request, request.length,
	// rtpClient, session.getControlPort());
	// csock.send(temp);
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	//
	// }

	/**
	 * Flush the audioBuffer
	 */
	public void flush() {
		l1.flush();
	}

}
