package org.rpi.airplay;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.rpi.utils.NetworkUtils;
import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

/**
 * LaunchThread class which starts services
 * 
 * @author bencall
 * 
 */
public class AirPlayThread extends Thread {
	private Logger log = Logger.getLogger(this.getClass());
	private List<BonjourEmitter> emitter = new ArrayList<BonjourEmitter>();
	private ServerSocket servSock = null;
	private String name;
	private String password;
	private boolean stopThread = false;

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public AirPlayThread(String name) {
		super();
		this.name = name;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public AirPlayThread(String name, String pass) {
		super();
		this.name = name;
		this.password = pass;
	}

	private byte[] getHardwareAdress() {
		byte[] hwAddr = null;
		InetAddress local;

		try {
			hwAddr = NetworkInterface.getByName("eth0").getHardwareAddress();
			if (hwAddr != null) {
				log.debug("eth0 Address: " + hwAddr.toString());
				return hwAddr;
			}
		} catch (Exception e) {
			log.error(e);
		}

		try {
			local = InetAddress.getLocalHost();
			// local = InetAddress.getByName("eth0");
			log.debug("LocalAddhress: " + local.getHostAddress());
			NetworkInterface ni = NetworkInterface.getByInetAddress(local);

			if (ni != null)
				hwAddr = ni.getHardwareAddress();
		} catch (UnknownHostException e) {
			log.error(e);
		} catch (SocketException e) {
			log.error(e);
		}

		return hwAddr;
	}

	private String getStringHardwareAdress(byte[] hwAddr) {
		StringBuilder sb = new StringBuilder();

		for (byte b : hwAddr)
			sb.append(String.format("%02x", b));

		return sb.toString();
	}

	public void run() {
		log.debug("Starting AirPlay Service...");
		//For the Raspi we have to do this now, because for some reason it is very slow the first time it is run and if we run it when we get an AirPlay connection the connection times out.
		log.debug("Create BouncyCastleProvider");
		Security.addProvider(new BouncyCastleProvider());
		log.debug("Created BouncyCastleProvider");
		log.debug("Initiate an encrypt");
		byte[] test = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0, 0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte) 0x9d };
		SecUtils.encryptRSA(test);

		int port = 5004;
		try {
			// DNS Emitter (Bonjour)
			byte[] hwAddr = getHardwareAdress();
			// for(final NetworkInterface iface:
			// Collections.list(NetworkInterface.getNetworkInterfaces())) {
			// if (iface.isLoopback())
			// continue;
			// if (iface.isPointToPoint())
			// continue;
			// if (!iface.isUp())
			// continue;
			//
			// for(final InetAddress addr:
			// Collections.list(iface.getInetAddresses())) {
			// if (!(addr instanceof Inet4Address) && !(addr instanceof
			// Inet6Address))
			// continue;
			//
			// try {
			// // Check if password is set
			// log.debug("Registering for Interface: " +
			// iface.getDisplayName());
			log.debug("Check if Passsword is set");
			boolean bPassword = false;
			if (!Utils.isEmpty(password)) {
				bPassword = true;
			}

			BonjourEmitter be = new BonjourEmitter(name, getStringHardwareAdress(hwAddr), port, bPassword);
			emitter.add(be);

			// }
			// catch (final Throwable e) {
			// log.error("Failed to publish service on " , e);
			// }
			// }
			// }

			// log.debug("announced [" + name + " @ " +
			// getStringHardwareAdress(hwAddr) + "]");

			// We listen for new connections
			log.debug("Starting ServerSocket on Port: " + port);
			try {
				servSock = new ServerSocket(port);
			} catch (Exception e) {
				log.debug("port busy, using default.");
				servSock = new ServerSocket();
			}

			servSock.setSoTimeout(8000);

			log.debug("SocketServer Started, now entering Run Loop.");

			while (!stopThread) {
				try {
					Socket socket = servSock.accept();
					log.debug("Accepted Connection From " + socket.toString());
					// InetAddress addr = socket.getInetAddress();
					// NetworkInterface nic =
					// NetworkInterface.getByInetAddress(addr);
					// byte[] hwAddr = nic.getHardwareAddress();

					// Check if password is set
					try {
						if (!bPassword) {

							log.debug("Create new RTSPResponder");
							RTSPResponder res = new RTSPResponder(hwAddr, socket);
							res.start();
							log.debug("Created new RTSPResponder");

						} else {
							log.debug("Create new RTSPResponder");
							RTSPResponder res = new RTSPResponder(hwAddr, socket, password);
							res.start();
							log.debug("Created new RTSPResponder");
						}
					} catch (Exception e) {
						log.error("Error creating RTSPResponder", e);
					}
				} catch (SocketTimeoutException e) {
					//
				} catch (Exception e) {
					log.error("Error", e);
				}
			}

		} catch (Exception e) {
			log.error(e);

		} finally {
			try {
				closeBonjourServices();
				servSock.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	private synchronized void closeBonjourServices() {
		try {
			for (BonjourEmitter be : emitter) {
				try {
					be.stop();
					log.debug("Close Socket");
					servSock.close();
				} catch (Exception e) {
					log.error("Error Stopping BonjourService", e);
				}
			}

			log.info("Bonjur Service stopped.");
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * Stop Our Thread
	 */
	public synchronized void stopThread() {

		log.debug("AirplayThread Shutdown...");
		stopThread = true;
		closeBonjourServices();
	}
}
