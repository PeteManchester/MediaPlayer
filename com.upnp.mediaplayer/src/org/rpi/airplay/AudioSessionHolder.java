package org.rpi.airplay;
import java.util.Observable;

public class AudioSessionHolder extends Observable {
	
	private static AudioSessionHolder instance = null;
	
	private AudioSession session = null;

	private byte[] hwAddr;
	
	public static AudioSessionHolder getInstance()
	{
		if(instance ==null)
		{
			instance = new AudioSessionHolder();
		}
		return instance;
	}
	
	private AudioSessionHolder()
	{
		
	}

	/**
	 * @return the session
	 */
	public AudioSession getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	public void setSession(AudioSession session) {
		this.session = session;
		fireEvent("Changed");
	}
	
	private void fireEvent(String s) {
		setChanged();
		notifyObservers(s);
	}

	public void setHardWareAddress(byte[] hwAddr) {
		this.hwAddr = hwAddr;
		
	}
	
	public byte[] getHardWareAddress()
	{
		return hwAddr;
	}
	
	
	
	
			
			

}
