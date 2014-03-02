package org.rpi.utils;

import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
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

    /***
     * Get the MAC Address.
     *
     * The previous implementation was kind of "oldschool", see http://www.mkyong.com/java/how-to-get-mac-address-in-java/
     *
     * @return
     */
    public static String getMacAddress() {
        InetAddress ip;

        StringBuilder sb = new StringBuilder();

        try {
            ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }

        }
        catch (UnknownHostException uhex) {
            log.error(uhex);
        }
        catch (SocketException sex) {
            log.error(sex);
        }

        return sb.toString();
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

        return getMacAddress();
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