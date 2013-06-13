package org.rpi.playlist;

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
		setChanged();
		notifyObservers(obj);
	}

}
