package org.rpi.volume;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.log4j.Logger;

public class VolumeWriter extends Thread {

	private int counter = 0;
	private Logger log = Logger.getLogger(VolumeWriter.class);
	private long volume = 0;
	private long oldVolume = 0;
	private boolean save = false;

	/***
	 * Trigger saving the volume.
	 * @param volume
	 */
	public void trigger(long volume) {
		if(oldVolume == volume) {
			return;
		}
		this.volume = volume;		
		counter = 0;
		save = true;
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (save) {
					if (counter < 5) {
						counter++;
					} else {

						log.debug("Saving Volume: " + volume);
						try (InputStream in = new FileInputStream(new File("app.properties"));
							    InputStreamReader writer = new InputStreamReader(in,Charset.forName("UTF-8"))) {
								Properties properties = new Properties();
							    properties.load(writer);
							    
							    try (OutputStream outputStream = new FileOutputStream("app.properties")) {
									properties.setProperty("mediaplayer_startup_volume", "" + volume);
									properties.store(outputStream, null);
									save = false;
									oldVolume = volume;
								} catch (IOException e) {
									log.error("Error Saving Volume: " + volume);
								}
							}
						catch(Exception e) {
							log.error("Error: SaveVolume: " + volume, e);
						}
					}
				}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			log.error("Error: SaveVolume", e);
		}
		
		

		
		
	}
}
