package org.rpi.airplay;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Observable;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelAirPlay;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.plugingateway.PluginGateWay;
import org.rpi.utils.SecUtils;

//import org.apache.commons.codec.binary.Base64;

/**
 * An primitive RTSP responder for replying iTunes
 * 
 * @author bencall
 * 
 */
public class RTSPResponder extends Thread implements Observer {

	private Logger log = Logger.getLogger(this.getClass());
	private Socket socket; // Connected socket
	private int[] fmtp;
	private byte[] aesiv, aeskey; // ANNOUNCE request infos
	private AudioServer serv; // Audio listener
	byte[] hwAddr;
	private BufferedReader in;
	private String password;
	private RTSPResponse response = null;

	int controlPort = 0;
	int timingPort = 0;

	// Pre-define patterns
	private static final Pattern authPattern = Pattern.compile("Digest username=\"(.*)\", realm=\"(.*)\", nonce=\"(.*)\", uri=\"(.*)\", response=\"(.*)\"");
	private static final Pattern completedPacket = Pattern.compile("(.*)\r\n\r\n");

	// private static final String key = "-----BEGIN RSA PRIVATE KEY-----\n" +
	// "MIIEpQIBAAKCAQEA59dE8qLieItsH1WgjrcFRKj6eUWqi+bGLOX1HL3U3GhC/j0Qg90u3sG/1CUt\n"
	// +
	// "wC5vOYvfDmFI6oSFXi5ELabWJmT2dKHzBJKa3k9ok+8t9ucRqMd6DZHJ2YCCLlDRKSKv6kDqnw4U\n"
	// +
	// "wPdpOMXziC/AMj3Z/lUVX1G7WSHCAWKf1zNS1eLvqr+boEjXuBOitnZ/bDzPHrTOZz0Dew0uowxf\n"
	// +
	// "/+sG+NCK3eQJVxqcaJ/vEHKIVd2M+5qL71yJQ+87X6oV3eaYvt3zWZYD6z5vYTcrtij2VZ9Zmni/\n"
	// +
	// "UAaHqn9JdsBWLUEpVviYnhimNVvYFZeCXg/IdTQ+x4IRdiXNv5hEewIDAQABAoIBAQDl8Axy9XfW\n"
	// +
	// "BLmkzkEiqoSwF0PsmVrPzH9KsnwLGH+QZlvjWd8SWYGN7u1507HvhF5N3drJoVU3O14nDY4TFQAa\n"
	// +
	// "LlJ9VM35AApXaLyY1ERrN7u9ALKd2LUwYhM7Km539O4yUFYikE2nIPscEsA5ltpxOgUGCY7b7ez5\n"
	// +
	// "NtD6nL1ZKauw7aNXmVAvmJTcuPxWmoktF3gDJKK2wxZuNGcJE0uFQEG4Z3BrWP7yoNuSK3dii2jm\n"
	// +
	// "lpPHr0O/KnPQtzI3eguhe0TwUem/eYSdyzMyVx/YpwkzwtYL3sR5k0o9rKQLtvLzfAqdBxBurciz\n"
	// +
	// "aaA/L0HIgAmOit1GJA2saMxTVPNhAoGBAPfgv1oeZxgxmotiCcMXFEQEWflzhWYTsXrhUIuz5jFu\n"
	// +
	// "a39GLS99ZEErhLdrwj8rDDViRVJ5skOp9zFvlYAHs0xh92ji1E7V/ysnKBfsMrPkk5KSKPrnjndM\n"
	// +
	// "oPdevWnVkgJ5jxFuNgxkOLMuG9i53B4yMvDTCRiIPMQ++N2iLDaRAoGBAO9v//mU8eVkQaoANf0Z\n"
	// +
	// "oMjW8CN4xwWA2cSEIHkd9AfFkftuv8oyLDCG3ZAf0vrhrrtkrfa7ef+AUb69DNggq4mHQAYBp7L+\n"
	// +
	// "k5DKzJrKuO0r+R0YbY9pZD1+/g9dVt91d6LQNepUE/yY2PP5CNoFmjedpLHMOPFdVgqDzDFxU8hL\n"
	// +
	// "AoGBANDrr7xAJbqBjHVwIzQ4To9pb4BNeqDndk5Qe7fT3+/H1njGaC0/rXE0Qb7q5ySgnsCb3DvA\n"
	// +
	// "cJyRM9SJ7OKlGt0FMSdJD5KG0XPIpAVNwgpXXH5MDJg09KHeh0kXo+QA6viFBi21y340NonnEfdf\n"
	// +
	// "54PX4ZGS/Xac1UK+pLkBB+zRAoGAf0AY3H3qKS2lMEI4bzEFoHeK3G895pDaK3TFBVmD7fV0Zhov\n"
	// +
	// "17fegFPMwOII8MisYm9ZfT2Z0s5Ro3s5rkt+nvLAdfC/PYPKzTLalpGSwomSNYJcB9HNMlmhkGzc\n"
	// +
	// "1JnLYT4iyUyx6pcZBmCd8bD0iwY/FzcgNDaUmbX9+XDvRA0CgYEAkE7pIPlE71qvfJQgoA9em0gI\n"
	// +
	// "LAuE4Pu13aKiJnfft7hIjbK+5kyb3TysZvoyDnb3HOKvInK7vXbKuU4ISgxB2bB3HcYzQMGsz1qJ\n"
	// +
	// "2gG0N5hvJpzwwhbhXqFKA4zaaSrw622wDniAK5MlIE0tIAKKP4yxNGjoD2QYjhBGuhvkWKaXTyY=\n"
	// + "-----END RSA PRIVATE KEY-----\n";

	public RTSPResponder(byte[] hwAddr, Socket socket) throws Exception {
		log.debug("Create RTSPResponder");
		PlayManager.getInstance().observeAirPlayEvents(this);
		this.hwAddr = hwAddr;
		this.socket = socket;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		log.debug("Created RTSPResponder");
	}

	public RTSPResponder(byte[] hwAddr, Socket socket, String pass) throws Exception {
		log.debug("Create RTSPResponder");
		PlayManager.getInstance().observeAirPlayEvents(this);
		this.hwAddr = hwAddr;
		this.socket = socket;
		this.password = pass;
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		log.debug("Created RTSPResponder");
	}

	public RTSPResponse handlePacket(RTSPPacket packet) {
		try {

			if (password == null) {
				// No pass = ok!
				response = new RTSPResponse("RTSP/1.0 200 OK");
				response.append("Audio-Jack-Status", "connected; type=analog");
				response.append("CSeq", packet.valueOfHeader("CSeq"));
			} else {
				// Default response (deny, deny, deny!)
				response = new RTSPResponse("RTSP/1.0 401 UNAUTHORIZED");
				response.append("WWW-Authenticate", "Digest realm=\"*\" nonce=\"*\"");
				response.append("Method", "DENIED");

				String authRaw = packet.valueOfHeader("Authorization");

				// If supplied, check response
				if (authRaw != null) {
					Matcher auth = authPattern.matcher(authRaw);

					if (auth.find()) {
						String username = auth.group(1);
						String realm = auth.group(2);
						String nonce = auth.group(3);
						String uri = auth.group(4);
						String resp = auth.group(5);
						String method = packet.getReq();

						String hash1 = md5Hash(username + ":" + realm + ":" + password).toUpperCase();
						String hash2 = md5Hash(method + ":" + uri).toUpperCase();
						String hash = md5Hash(hash1 + ":" + nonce + ":" + hash2).toUpperCase();

						// Check against password
						if (hash.equals(resp)) {
							// Success!
							response = new RTSPResponse("RTSP/1.0 200 OK");
							response.append("Audio-Jack-Status", "connected; type=analog");
							response.append("CSeq", packet.valueOfHeader("CSeq"));
						}
					}
				}
			}

			// Apple Challenge-Response field if needed
			String challenge;
			if ((challenge = packet.valueOfHeader("Apple-Challenge")) != null) {
				// BASE64 DECODE
				byte[] decoded = Base64.decode(challenge);

				// IP byte array
				// byte[] ip = socket.getLocalAddress().getAddress();
				SocketAddress localAddress = socket.getLocalSocketAddress(); // .getRemoteSocketAddress();

				byte[] ip = ((InetSocketAddress) localAddress).getAddress().getAddress();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				// Challenge
				try {
					out.write(decoded);
					// IP-Address
					out.write(ip);
					// HW-Addr
					out.write(hwAddr);

					// Pad to 32 Bytes
					int padLen = 32 - out.size();
					for (int i = 0; i < padLen; ++i) {
						out.write(0x00);
					}

				} catch (Exception e) {
					log.error(e);
				}

				// RSA
				byte[] crypted = SecUtils.encryptRSA(out.toByteArray());

				// Encode64
				String ret = Base64.encode(crypted);

				// On retire les ==
				ret = ret.replace("=", "").replace("\r", "").replace("\n", "");

				// Write
				response.append("Apple-Response", ret);
			}

			// Paquet request
			String REQ = packet.getReq();
			if (REQ.contentEquals("OPTIONS")) {
				// The response field
				response.append("Public", "ANNOUNCE, SETUP, RECORD, PAUSE, FLUSH, TEARDOWN, OPTIONS, GET_PARAMETER, SET_PARAMETER");

			} else if (REQ.contentEquals("ANNOUNCE")) {
				// Nothing to do here. Juste get the keys and values
				// Here is our Client Name we could use that in the MetaData of
				// MediaPlayer
				String client_name = packet.valueOfHeader("X-Apple-Client-Name");
				ChannelAirPlay channel = new ChannelAirPlay("", "", 1, client_name);
				PlayManager.getInstance().playAirPlayer(channel);
				Pattern p = Pattern.compile("^a=([^:]+):(.+)", Pattern.MULTILINE);
				Matcher m = p.matcher(packet.getContent());
				while (m.find()) {
					if (m.group(1).contentEquals("fmtp")) {
						// Parse FMTP as array
						String[] temp = m.group(2).split(" ");
						fmtp = new int[temp.length];
						for (int i = 0; i < temp.length; i++) {
							fmtp[i] = Integer.valueOf(temp[i]);
						}

					} else if (m.group(1).contentEquals("rsaaeskey")) {
						aeskey = SecUtils.decryptRSA(Base64.decode(m.group(2)));
					} else if (m.group(1).contentEquals("aesiv")) {
						aesiv = Base64.decode(m.group(2));
					}

				}
				AudioSession session = new AudioSession(aesiv, aeskey, fmtp, controlPort, timingPort);
				AudioSessionHolder.getInstance().setSession(session);

			} else if (REQ.contentEquals("SETUP")) {
				// TODO SETUP We should let MediaPlayer know we are starting up

				String value = packet.valueOfHeader("Transport");

				// Control port
				Pattern p = Pattern.compile(";control_port=(\\d+)");
				Matcher m = p.matcher(value);
				if (m.find()) {
					controlPort = Integer.valueOf(m.group(1));
				}

				// Timing port
				p = Pattern.compile(";timing_port=(\\d+)");
				m = p.matcher(value);
				if (m.find()) {
					timingPort = Integer.valueOf(m.group(1));
				}

				AudioSession as = new AudioSession(aesiv, aeskey, fmtp, controlPort, timingPort);
				AudioSessionHolder.getInstance().setSession(as);
				// Launching audioserver
				serv = new AudioServer(new AudioSession(aesiv, aeskey, fmtp, controlPort, timingPort));
				PlayManager.getInstance().setStatus("Playing", "AIRPLAY");
				PluginGateWay.getInstance().setSourceId("AirPlay","AirPlay");
				response.append("Transport", packet.valueOfHeader("Transport") + ";server_port=" + serv.getServerPort());

				// ??? Why ???
				response.append("Session", "DEADBEEF");
			} else if (REQ.contentEquals("RECORD")) {
				// Headers
				// Range: ntp=0-
				// RTP-Info: seq={Note 1};rtptime={Note 2}
				// Note 1: Initial value for the RTP Sequence Number, random 16
				// bit
				// value
				// Note 2: Initial value for the RTP Timestamps, random 32 bit
				// value

			} else if (REQ.contentEquals("FLUSH")) {
				serv.flush();

			} else if (REQ.contentEquals("TEARDOWN")) {
				log.debug("TEARDOWN");
				PlayManager.getInstance().setStatus("Stoped", "AIRPLAY");
				response.append("Connection", "close");
				tearDown();
			} else if (REQ.contentEquals("SET_PARAMETER")) {
				// Timing port
				Pattern p = Pattern.compile("volume: (.+)");
				Matcher m = p.matcher(packet.getContent());
				if (m.find()) {
					double volume = (double) Math.pow(10.0, 0.05 * Double.parseDouble(m.group(1)));
					serv.setVolume(65536.0 * volume);
				}

			} else {
				log.debug("REQUEST(" + REQ + "): Not Supported Yet!");
				log.debug(packet.getRawPacket());
			}

			// We close the response
			response.finalize();
		} catch (Exception e) {
			log.error(e);
		}
		return response;
	}

	/**
	 * Generates md5 hash of a string.
	 * 
	 * @param plaintext
	 *            string
	 * @return hash string
	 */
	public String md5Hash(String plaintext) {
		String hashtext = "";

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plaintext.getBytes());
			byte[] digest = md.digest();

			BigInteger bigInt = new BigInteger(1, digest);
			hashtext = bigInt.toString(16);

			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
		} catch (java.security.NoSuchAlgorithmException e) {
			log.error(e);
		}

		return hashtext;
	}

	// /**
	// * Crypts with private key
	// *
	// * @param array
	// * data to encrypt
	// * @return encrypted data
	// */
	// public byte[] encryptRSA(byte[] array) {
	// log.debug("Start of EncryptRSA");
	// try {
	// //Security.addProvider(new BouncyCastleProvider());
	// log.debug("Create pemReader");
	// PEMReader pemReader = new PEMReader(new StringReader(key));
	// log.debug("Create pemReader");
	// log.debug("ReadObject");
	// KeyPair pObj = (KeyPair) pemReader.readObject();
	//
	// // Encrypt
	// log.debug("getInstancer");
	// Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
	// log.debug("Cipher");
	// cipher.init(Cipher.ENCRYPT_MODE, pObj.getPrivate());
	// log.debug("End of EncryptRSA");
	// return cipher.doFinal(array);
	//
	// } catch (Exception e) {
	// log.error(e);
	// }
	//
	// return null;
	// }

	// /**
	// * Decrypt with RSA priv key
	// *
	// * @param array
	// * @return
	// */
	// public byte[] decryptRSA(byte[] array) {
	// log.debug("Start of decryptRSA");
	// try {
	//
	// // La clef RSA
	// PEMReader pemReader = new PEMReader(new StringReader(key));
	// KeyPair pObj = (KeyPair) pemReader.readObject();
	//
	// // Encrypt
	// Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
	// cipher.init(Cipher.DECRYPT_MODE, pObj.getPrivate());
	// log.debug("End of decryptRSA");
	// return cipher.doFinal(array);
	//
	// } catch (Exception e) {
	// log.error(e);
	// }
	//
	// return null;
	// }

	/**
	 * Thread to listen packets
	 */
	public void run() {
		try {
			log.debug("Start of Run");
			do {
				// log.debug("Listening packets ... ");
				// feed buffer until packet completed
				StringBuffer packet = new StringBuffer();
				int ret = 0;
				do {
					char[] buffer = new char[4096];
					ret = in.read(buffer);
					packet.append(new String(buffer));
				} while (ret != -1 && !completedPacket.matcher(packet.toString()).find());

				if (ret != -1) {
					// We handle the packet
					RTSPPacket request = new RTSPPacket(packet.toString());
					RTSPResponse response = this.handlePacket(request);
					String REQ = request.getReq();
					if (!REQ.contentEquals("OPTIONS")) {
						log.debug("Request: \r\n" + request.toString());
						// log.debug("Response: \r\n" + response.toString());
					}
					// log.debug("Request: \r\n" + request.toString());
					// log.debug("Response: \r\n" + response.toString());

					// Write the response to the wire
					try {
						BufferedWriter oStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
						oStream.write(response.getRawPacket());
						oStream.flush();
					} catch (Exception e) {
						log.error(e);
					}

					if ("TEARDOWN".equals(request.getReq())) {
						// log.debug("Request: \r\n" + request.toString());
						// socket.close();
						// socket = null;
						tearDown();
					}
				} else {
					socket.close();
					socket = null;
				}
			} while (socket != null);

		} catch (Exception e) {
			log.error("Error Run", e);

		} finally {
			try {
				if (in != null)
					in.close();
			} catch (Exception e) {
				log.error(e);
			} finally {
				try {
					if (socket != null)
						socket.close();
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
		// TODO Here is where the connection ends
		log.debug("connection ended.");
	}

	private void tearDown() {
		log.debug("Attempting to Stop AirPlay Channel");
		if (serv != null) {
			try {
				serv.stop();
			} catch (Exception e) {
				log.error(e);
			}
		}
		try {
			if (socket != null) {
				socket.close();
				socket = null;
			}
		} catch (Exception e) {
			log.error("Error TearDown", e);
		}
	}

	/**
	 * Used to indicate that we need to stop playing
	 */
	@Override
	public void update(Observable o, Object obj) {
		EventBase e = (EventBase) obj;
		switch (e.getType()) {
		case EVENTAIRPLAYERSTOP:
			tearDown();
			break;
		}
	}

}