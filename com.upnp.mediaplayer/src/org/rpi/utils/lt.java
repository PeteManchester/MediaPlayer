package org.rpi.utils;

import org.apache.log4j.Logger;
import org.openhome.net.device.IDvInvocation;

public class lt {

	private static Logger log = Logger.getLogger(lt.class);

	public static String getLogText(IDvInvocation paramIDvInvocation) {
		if (!log.isDebugEnabled())
			return "";
		String sp = " ";
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(sp);
			sb.append("Adapter: ");
			sb.append(getAdapterIP(paramIDvInvocation.getAdapter()));
			sb.append(sp);
			sb.append("uriPrefix: ");
			sb.append(paramIDvInvocation.getResourceUriPrefix());
			sb.append(sp);
			sb.append("Version:");
			sb.append(paramIDvInvocation.getVersion());
			sb.append(sp);
		} catch (Exception e) {
		}
		return sb.toString();
	}

	private static String getAdapterIP(int ip) {
		String ipStr = String.format("%d.%d.%d.%d", (ip >>> 24 & 0xff), (ip >>> 16 & 0xff), (ip >>> 8 & 0xff), (ip & 0xff));
		return ipStr;
	}

}
