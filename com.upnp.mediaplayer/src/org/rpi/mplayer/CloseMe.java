package org.rpi.mplayer;

import java.io.Closeable;
import java.io.IOException;



public class CloseMe {
	
	   public static void close(Closeable closable) {
	        if (closable != null) {
	            try {
	                closable.close();
	            } catch (IOException e) {
	            	e.printStackTrace();
	            }
	        }
	    }

}
