package org.rpi.playlist;

import java.util.Observable;

/**
 * Possible Events
 * EVENTTIMEUPDATED
 * EVENTDURATIONUPDATE
 * 
 *
 */

public class ObservsableTime extends Observable {
	
	/**
	 * Let the Observers know something has changed..
	 * 
	 * @param obj
	 */
	public void notifyChange(Object obj) {
		try
		{
			setChanged();
			notifyObservers(obj);
		}
		catch(Exception e)
		{
			String s = "";
		}	
	}

}
