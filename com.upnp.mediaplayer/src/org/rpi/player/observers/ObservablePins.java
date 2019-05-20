package org.rpi.player.observers;

import java.util.Observable;

import org.rpi.pins.EventPinsChanged;

public class ObservablePins extends Observable {
	
	/**
	 * Let the Observers know something has changed..
	 * 
	 * @param obj
	 */
	public void notifyChange(EventPinsChanged obj) {
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
