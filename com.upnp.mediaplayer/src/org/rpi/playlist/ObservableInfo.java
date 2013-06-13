package org.rpi.playlist;

import java.util.Observable;

/**
 * Possible Events
 * EVENTUPDATETRACKINFO
 * EVENTUPDATETRACKMETADATA
 * EVENTTRACKCHANGED
 */

public class ObservableInfo extends Observable {
	
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
