package org.rpi.playlist;

import java.util.Observable;

/**
 * Possible Events
 * EVENTVOLUMECHANGED
 * EVENTMUTECHANGED
 */

public class ObservableVolume extends Observable {
	
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
