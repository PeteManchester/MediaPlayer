package org.rpi.alarm;

import org.apache.log4j.BasicConfigurator;

public class RunAlarm {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		AlarmFileUtils alarm = new AlarmFileUtils();		
	}

}
