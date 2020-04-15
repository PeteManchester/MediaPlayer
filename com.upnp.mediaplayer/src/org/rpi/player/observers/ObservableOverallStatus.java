package org.rpi.player.observers;

import java.util.Observable;

public class ObservableOverallStatus extends Observable {
	
	/***
	 * Used by the Songcast Sender to determine if it should stop sending packets.
	 * 
	 * Possible Events:
	 * Playing
	 * Stopped
	 */
	
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
            e.printStackTrace();
		}
		
	}

}
