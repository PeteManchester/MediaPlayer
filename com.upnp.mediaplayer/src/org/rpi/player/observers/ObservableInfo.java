package org.rpi.player.observers;

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
