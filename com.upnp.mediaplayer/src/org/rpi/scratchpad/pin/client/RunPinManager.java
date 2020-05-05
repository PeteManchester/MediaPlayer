package org.rpi.scratchpad.pin.client;

import org.apache.log4j.BasicConfigurator;
import org.rpi.pins.PinManager;

public class RunPinManager {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		PinManager.getInstance();
		// pm.registerForEvent();

		try {
			for (int i = 1; i < 1000; i++) {
				Thread.sleep(10000);
				PinManager.getInstance().SavePins("{\"pin" + i + "\":\"ok\"}");
			}

			//
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
