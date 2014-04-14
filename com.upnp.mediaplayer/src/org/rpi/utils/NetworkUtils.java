package org.rpi.utils;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

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

//    /***
//     * Get the MAC Address.
//     *
//     * The previous implementation was kind of "oldschool", see http://www.mkyong.com/java/how-to-get-mac-address-in-java/
//     * TODO Does not work on Raspi...
//     *
//     * @return
//     */
//    public static String getMacAddress() {
//        InetAddress ip;
//
//        StringBuilder sb = new StringBuilder();
//
//        try {
//            ip = InetAddress.getLocalHost();
//            log.debug("InterAdress: " + ip.getHostAddress());
//            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
//
//            byte[] mac = network.getHardwareAddress();
//
//            for (int i = 0; i < mac.length; i++) {
//                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
//            }
//
//        }
//        catch (UnknownHostException uhex) {
//            log.error(uhex);
//        }
//        catch (SocketException sex) {
//            log.error(sex);
//        }
//
//        return sb.toString();
//    }
    
    /**
	 * Returns a suitable hardware address.
	 * The other method did not work on raspi wheezy
	 * @return a MAC address
	 */
	public static byte[] getMacAddress() {
		try {
			/* Search network interfaces for an interface with a valid, non-blocked hardware address */
	    	for(final NetworkInterface iface: Collections.list(NetworkInterface.getNetworkInterfaces())) {
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
	    		}
	    		catch (final Throwable e) {
	    			/* Ignore */
	    		}
	    	}
		}
		catch (final Throwable e) {
			/* Ignore */
		}

		/* Fallback to the IP address padded to 6 bytes */
		try {
			final byte[] hostAddress = Arrays.copyOfRange(InetAddress.getLocalHost().getAddress(), 0, 6);
			log.info("Hardware address is " + toHexString(hostAddress) + " (IP address)");
			return hostAddress;
		}
		catch (final Throwable e) {
			/* Ignore */
		}

		/* Fallback to a constant */
		log.info("Hardware address is 00DEADBEEF00 (last resort)");
		return new byte[] {(byte)0x00, (byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF, (byte)0x00};
	}
	
	/**
	 * Converts an array of bytes to a hexadecimal string
	 * 
	 * @param bytes array of bytes
	 * @return hexadecimal representation
	 */
	public static String toHexString(final byte[] bytes) {
		final StringBuilder s = new StringBuilder();
		for(final byte b: bytes) {
			final String h = Integer.toHexString(0x100 | b);
			s.append(h.substring(h.length() - 2, h.length()).toUpperCase());
		}
		return s.toString();
	}
	
	
	/**
	 * Decides whether or nor a given MAC address is the address of some
	 * virtual interface, like e.g. VMware's host-only interface (server-side).
	 * 
	 * @param addr a MAC address
	 * @return true if the MAC address is unsuitable as the device's hardware address
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
		else if ((addr[0] == 0x00) && (addr[1] == 0x25) && (addr[2] == (byte)0xAE))
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
    
    public static String getNICName(String displayName)
    {
    	String res = "";
    	try
    	{
    	Enumeration e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			//Enumeration ee = n.getInetAddresses();
			if(n.getDisplayName().equals(displayName))
			{
				return n.getName();
			}
		}
    	}
    	catch(Exception e)
    	{
    		log.error("Error Getting NIC Name. DisplayName: " + displayName,e);
    	}
    	return res;
    }
}