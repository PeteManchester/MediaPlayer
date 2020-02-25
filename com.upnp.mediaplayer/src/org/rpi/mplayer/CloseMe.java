package org.rpi.mplayer;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;



public class CloseMe {
	
	private static Logger log = Logger.getLogger(CloseMe.class);
	
	   public static void close(Closeable closable) {
	        if (closable != null) {
	            try {
	                closable.close();
	            } catch (IOException e) {
	            	log.error("Error Closing: ", e);
	            }
	        }
	    }

}
