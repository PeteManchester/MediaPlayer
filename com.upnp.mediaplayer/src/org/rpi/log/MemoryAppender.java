package org.rpi.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MemoryAppender extends AppenderSkeleton {
	
	LimitedSizeQueue<String> events = new LimitedSizeQueue<String>(10);
	
	public MemoryAppender()
	{
	}

	@Override
	public void close() {
		events.clear();
	}

	@Override
	public boolean requiresLayout() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
				addEvent(this.layout.format(event));
	}
	
	private synchronized void addEvent(String event)
	{
		events.add(event);
	}
	
	private synchronized LimitedSizeQueue<String>  getEvents()
	{
		return events;
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized String getEventString()
	{
		StringBuffer sb = new StringBuffer();
		try
		{
			for(String s : getEvents())
			{
				sb.append(s);
				sb.append("\r\n");
			}
		}
		catch(Exception e)
		{
			
		}
		return sb.toString();
	}

}
