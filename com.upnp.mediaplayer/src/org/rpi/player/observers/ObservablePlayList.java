package org.rpi.player.observers;

/**
 * Possible Events
 * EVENTPLAYLISTSTATUSCHANGED
 * EVENTPLAYLISTPLAYINGTRACKID
 * EVENTPLAYLISTUPDATESHUFFLE
 */

import java.util.Observable;

public class ObservablePlayList extends Observable {
	
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
