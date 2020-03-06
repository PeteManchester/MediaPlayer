package org.rpi.songcast.ohu.sender;

import java.net.Inet4Address;

import org.apache.log4j.BasicConfigurator;
import org.rpi.utils.NetworkUtils;

public class TestMain {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		Inet4Address localInetAddr =  NetworkUtils.getINet4Address();
		OHUSenderConnection con = new OHUSenderConnection("1234", localInetAddr);
		try {
			String myURI = con.run();
			System.out.println(myURI);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
