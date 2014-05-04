package org.rpi.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class MemoryAppender extends AppenderSkeleton {

	LimitedSizeQueue<String> events = new LimitedSizeQueue<String>(50);

	public MemoryAppender() {
	}

	@Override
	public void close() {
		events.clear();
	}

	@Override
	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		addEvent(this.layout.format(event));
	}

	private synchronized void addEvent(String event) {
		events.add(event);
	}

	private synchronized LimitedSizeQueue<String> getEvents() {
		return events;
	}

	/**
	 * 
	 * @return
	 */
	public synchronized String getEventString() {
		String text = "";
		try {
			for (String s : getEvents()) {
				if (!s.trim().equalsIgnoreCase("")) {
					s = s + text;
					text = s ;
				}
			}
		} catch (Exception e) {

		}
		return text;
	}

}
