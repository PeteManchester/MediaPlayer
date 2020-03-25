package org.rpi.utils;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by triplem on 24.02.14.
 */
public class NetworkUtils {

	private static final Logger log = Logger.getLogger(NetworkUtils.class);

	/**
	 * private constructor, this is a utility class
	 */
	private NetworkUtils() {

	}

	// /***
	// * Get the MAC Address.
	// *
	// * The previous implementation was kind of "oldschool", see
	// http://www.mkyong.com/java/how-to-get-mac-address-in-java/
	// * TODO Does not work on Raspi...
	// *
	// * @return
	// */
	// public static String getMacAddress() {
	// InetAddress ip;
	//
	// StringBuilder sb = new StringBuilder();
	//
	// try {
	// ip = InetAddress.getLocalHost();
	// log.debug("InterAdress: " + ip.getHostAddress());
	// NetworkInterface network = NetworkInterface.getByInetAddress(ip);
	//
	// byte[] mac = network.getHardwareAddress();
	//
	// for (int i = 0; i < mac.length; i++) {
	// sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" :
	// ""));
	// }
	//
	// }
	// catch (UnknownHostException uhex) {
	// log.error(uhex);
	// }
	// catch (SocketException sex) {
	// log.error(sex);
	// }
	//
	// return sb.toString();
	// }

	/**
	 * Returns a suitable hardware address. The other method did not work on
	 * raspi wheezy
	 * 
	 * @return a MAC address
	 */
	public static byte[] getMacAddress() {
		try {
			/*
			 * Search network interfaces for an interface with a valid,
			 * non-blocked hardware address
			 */
			for (final NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (iface.isLoopback())
					continue;
				if (iface.isPointToPoint())
					continue;

				try {
					final byte[] ifaceMacAddress = iface.getHardwareAddress();
					if ((ifaceMacAddress != null) && (ifaceMacAddress.length == 6) && !isBlockedHardwareAddress(ifaceMacAddress)) {
						log.info("Hardware address is " + toHexString(ifaceMacAddress) + " (" + iface.getDisplayName() + ")");
						return Arrays.copyOfRange(ifaceMacAddress, 0, 6);
					}
				} catch (final Throwable e) {
					/* Ignore */
				}
			}
		} catch (final Throwable e) {
			/* Ignore */
		}

		/* Fallback to the IP address padded to 6 bytes */
		try {
			final byte[] hostAddress = Arrays.copyOfRange(InetAddress.getLocalHost().getAddress(), 0, 6);
			log.info("Hardware address is " + toHexString(hostAddress) + " (IP address)");
			return hostAddress;
		} catch (final Throwable e) {
			/* Ignore */
		}

		/* Fallback to a constant */
		log.info("Hardware address is 00DEADBEEF00 (last resort)");
		return new byte[] { (byte) 0x00, (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF, (byte) 0x00 };
	}

	/**
	 * Converts an array of bytes to a hexadecimal string
	 * 
	 * @param bytes
	 *            array of bytes
	 * @return hexadecimal representation
	 */
	public static String toHexString(final byte[] bytes) {
		final StringBuilder s = new StringBuilder();
		for (final byte b : bytes) {
			final String h = Integer.toHexString(0x100 | b);
			s.append(h.substring(h.length() - 2, h.length()).toUpperCase());
		}
		return s.toString();
	}

	/**
	 * Decides whether or nor a given MAC address is the address of some virtual
	 * interface, like e.g. VMware's host-only interface (server-side).
	 * 
	 * @param addr
	 *            a MAC address
	 * @return true if the MAC address is unsuitable as the device's hardware
	 *         address
	 */
	public static boolean isBlockedHardwareAddress(final byte[] addr) {
		if ((addr[0] & 0x02) != 0)
			/* Locally administered */
			return true;
		else if ((addr[0] == 0x00) && (addr[1] == 0x50) && (addr[2] == 0x56))
			/* VMware */
			return true;
		else if ((addr[0] == 0x00) && (addr[1] == 0x1C) && (addr[2] == 0x42))
			/* Parallels */
			return true;
		else if ((addr[0] == 0x00) && (addr[1] == 0x25) && (addr[2] == (byte) 0xAE))
			/* Microsoft */
			return true;
		else
			return false;
	}

	/***
	 * Get the HostName, if any problem attempt to get the MAC Address
	 *
	 * @return
	 */
	public static String getHostName() {
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String hostName = iAddress.getHostName();
			// String canonicalHostName = iAddress.getCanonicalHostName();
			return hostName;
		} catch (Exception e) {
			log.error("Error Getting HostName: ", e);
		}

		return toHexString(getMacAddress());
	}

	public static String getIPAddress2() {
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String addr = iAddress.getHostAddress();
			// String canonicalHostName = iAddress.getCanonicalHostName();
			return addr;
		} catch (Exception e) {
			log.error("Error Getting IPAddress: ", e);
		}
		return "127.0.0.1";
	}

	public static String getIPAddress() {
		String res = "127.0.0.1";
		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String addr = iAddress.getHostAddress();
			log.debug("PETE!!!!!!!! InetAddress: " + addr);
			if ((!res.equalsIgnoreCase(addr)) && (!addr.equalsIgnoreCase("127.0.1.1"))) {
				log.debug("PETE!!!!!!!! Returning InetAddress: " + addr);
				return addr;
			}
		} catch (Exception e) {
			log.error("Error getHostAddress", e);
		}

		try {
			log.debug("PETE!!!!!!!! Interate InetAddress Interfaces: ");
			// Enumeration<NetworkInterface> nets =
			// NetworkInterface.getNetworkInterfaces();
			// for (final NetworkInterface iface :
			// Collections.list(NetworkInterface.getNetworkInterfaces())) {
			for (final NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				String addr = getIPAddressInfo(netint);
				if (!addr.equalsIgnoreCase(res)) {
					return addr;
				}
			}
		} catch (Exception e) {
			log.error("Error getNetworkInterfaces", e);
		}
		return res;
	}

	static String getIPAddressInfo(NetworkInterface netint) throws SocketException {
		String res = "127.0.0.1";
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			if (inetAddress != null && !inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.getHostAddress().equals("127.0.1.1") && inetAddress instanceof Inet4Address && netint.isUp()) {
				log.debug("Display name: " + netint.getDisplayName() + " Name: " + netint.getName() + " InetAddress: " + inetAddress);
				return inetAddress.getHostAddress();
			}
		}
		return res;
	}

	public static Inet4Address getINet4Address() {
		Map<String, String> resList = new HashMap<String, String>();
		// IP Addresses to exclude.
		resList.put("127.0.0.1", "127.0.0.1");
		resList.put("192.168.116.1", "192.168.116.1");
		resList.put("192.168.32.1", "192.168.32.1");
		boolean isNetworkFound = false;
		while(!isNetworkFound) {
			try {
				
				log.debug("Interate InetAddress Interfaces: ");
				Map<String, Inet4Address> nics = new HashMap<String, Inet4Address>();
				// Enumeration<NetworkInterface> nets =
				// NetworkInterface.getNetworkInterfaces();
				// for (NetworkInterface netint : Collections.list(nets)) {
				for (final NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
					InetAddress addr = getInetAddressInfo(netint);
					String hostAddress = "BAD";
					if (addr != null) {
						hostAddress = addr.getHostAddress();
					}

					if (addr instanceof Inet4Address && !resList.containsKey(addr.getHostAddress())) {
						// return (Inet4Address) addr;
						log.debug("Adding NetworkInterface to List: " + netint.getName());
						nics.put(netint.getName(), (Inet4Address) addr);
					}
				}

				if (nics.containsKey("eth0")) {
					log.debug("On a Raspi, prefer the wired lan (eth0) if it is active");
					isNetworkFound = true;
					return nics.get("eth0");
				}
				
				for(String key: nics.keySet()) {
					if(key.toUpperCase().startsWith("ETH")) {
						log.debug("No eth0 but using: " + key);
						isNetworkFound = true;
						return nics.get(key);
					}
				}
				
				if(nics.size() > 0) {
					log.debug("On a Raspi, couldn't get the wired lan, use other instead: " + nics.size());
					isNetworkFound = true;
				return nics.values().stream().findFirst().get();
				}
				log.info("No Network found, waiting for Network to start");
				TimeUnit.SECONDS.sleep(2);

			} catch (Exception e) {

			}
			
		}
		

		return null;

	}

	public static Inet4Address getINetOld4Address() {
		// String res = "127.0.0.1,192.168.116.1,192.168.32.1";
		Map<String, String> resList = new HashMap<String, String>();
		// IP Addresses to exclude.
		resList.put("127.0.0.1", "127.0.0.1");
		resList.put("192.168.116.1", "192.168.116.1");
		resList.put("192.168.32.1", "192.168.32.1");

		try {
			InetAddress iAddress = InetAddress.getLocalHost();
			String addr = iAddress.getHostAddress();
			log.debug("PETE!!!!!!!! InetAddress: " + addr);
			if ((!resList.containsKey(addr)) && (!addr.equalsIgnoreCase("127.0.1.1") && !addr.equalsIgnoreCase("192.168.116.1") && iAddress instanceof Inet4Address)) {
				log.debug("PETE!!!!!!!! Returning InetAddress: " + addr);
				return (Inet4Address) iAddress;
			}
		} catch (Exception e) {

		}

		try {
			log.debug("PETE!!!!!!!! Interate InetAddress Interfaces: ");
			// Enumeration<NetworkInterface> nets =
			// NetworkInterface.getNetworkInterfaces();
			// for (NetworkInterface netint : Collections.list(nets)) {
			for (final NetworkInterface netint : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				InetAddress addr = getInetAddressInfo(netint);
				String hostAddress = "BAD";
				if (addr != null) {
					hostAddress = addr.getHostAddress();
				}

				if (addr instanceof Inet4Address && !resList.containsKey(addr.getHostAddress())) {
					return (Inet4Address) addr;
				}
			}
		} catch (Exception e) {

		}
		return null;
	}

	static InetAddress getInetAddressInfo(NetworkInterface netint) throws SocketException {
		String res = "127.0.0.1";
		Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		for (InetAddress inetAddress : Collections.list(inetAddresses)) {
			if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress() && !inetAddress.getHostAddress().equals("127.0.1.1") && inetAddress instanceof Inet4Address) {
				log.debug("Display name: " + netint.getDisplayName() + " Name: " + netint.getName() + " InetAddress: " + inetAddress);
				return inetAddress;
			}
		}
		return null;
	}

	public static String getNICName(String displayName) {
		String res = "";
		try {
			// Enumeration<NetworkInterface> e =
			// NetworkInterface.getNetworkInterfaces();
			// while (e.hasMoreElements()) {
			for (final NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				// NetworkInterface n = (NetworkInterface) e.nextElement();
				// Enumeration ee = n.getInetAddresses();
				if (n.getDisplayName().equals(displayName)) {
					return n.getName();
				}
			}
		} catch (Exception e) {
			log.error("Error Getting NIC Name. DisplayName: " + displayName, e);
		}
		return res;
	}

	/***
	 * Find the name of the NIC from the IPAddress.
	 * 
	 * @param ip
	 * @return
	 */
	public static String getNICNameForIPAddress(String ip) {
		String nic = "";
		try {
			// Enumeration<NetworkInterface> e =
			// NetworkInterface.getNetworkInterfaces();
			// while (e.hasMoreElements()) {
			// NetworkInterface n = (NetworkInterface) e.nextElement();
			for (final NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				Enumeration<InetAddress> ee = n.getInetAddresses();
				// log.info("Network Interface Display Name: '" +
				// n.getDisplayName() + "'");
				// log.info("NIC Name: '" + n.getName() + "'");
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					if (i.getHostAddress().equalsIgnoreCase(ip)) {
						log.info("IPAddress for Network Interface: " + n.getDisplayName() + " : " + i.getHostAddress());
						nic = n.getName();
					}
				}
			}
		} catch (Exception e) {
			log.error("Error getNICNameForIPAddress", e);
		}
		return nic;
	}

	/***
	 * Print out each Network Card and IPAddress.
	 */
	public static void printNetworkInterfaceDetails() {
		log.info("Getting Network Interfaces");
		try {
			// Enumeration<NetworkInterface> e =
			// NetworkInterface.getNetworkInterfaces();
			// while (e.hasMoreElements()) {
			// NetworkInterface n = (NetworkInterface) e.nextElement();
			for (final NetworkInterface n : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				Enumeration<InetAddress> ee = n.getInetAddresses();
				log.info("Network Interface Display Name: '" + n.getDisplayName() + "'");
				log.info("NIC Name: '" + n.getName() + "'");
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					log.info("IPAddress for Network Interface: " + n.getDisplayName() + " : " + i.getHostAddress());
				}
			}
		} catch (Exception e) {
			log.error("Error Getting IPAddress", e);
		}
		log.info("End Of Network Interfaces");
	}

}