package org.rpi.songcast.ohm;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventTimeUpdate;

public class SongcastTimer implements Runnable {

	private boolean run = true;

	private Logger log = Logger.getLogger(this.getClass());
	private long started = 0;

	public SongcastTimer() {
		started = System.nanoTime();
	}

	@Override
	public void run() {
		while (run) {
			try {
				long now = System.nanoTime();
				long time = (now - started) / 1000000000;
				if (time >= 0) {
					EventTimeUpdate e = new EventTimeUpdate();
					e.setTime(time);
					PlayManager.getInstance().updateTime(e);
				}
				Thread.sleep(990);
			} catch (Exception e) {

			}
		}

	}

	public void setRun(boolean run) {
		this.run = run;
	}
}
