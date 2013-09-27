package org.rpi.player.observers;

import java.util.Observable;

/**
 * Possible Events
 * EVENTRADIOSTATUSCHANGED
 * EVENTTRADIOPLAYINGTRACKID
 */

public class ObservableRadio extends Observable {
	
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
		}
		
	}

}
