package org.rpi.scratchpad.pin.client;

import org.apache.log4j.BasicConfigurator;
import org.rpi.pins.PinMangerAccount;

public class RunPinManager {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		PinMangerAccount.getInstance();
		//pm.registerForEvent();
		
		try {
			Thread.sleep(999999999);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
