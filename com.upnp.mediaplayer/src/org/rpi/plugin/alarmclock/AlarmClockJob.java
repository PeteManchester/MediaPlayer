package org.rpi.plugin.alarmclock;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.rpi.player.PlayManager;

public class AlarmClockJob implements Job {

	private static Logger log = Logger.getLogger(AlarmClockJob.class);

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		String job_name = "DefaultJob";
		String volume = "100";
		String shuffle = "false";
		JobDataMap map = context.getJobDetail().getJobDataMap();

		if (map.containsKey("id")) {
			try {
				job_name = (String) map.get("id");
			} catch (Exception e) {

			}
		}

		if (map.containsKey("Volume")) {
			volume = map.getString("Volume");
			log.debug("Volume is: " + volume);

		}

		try {
			PlayManager.getInstance().setVolume(Long.parseLong(volume));
		} catch (Exception e) {

		}

		if (map.containsKey("Shuffle")) {
			shuffle = map.getString("Shuffle");
		}

		String type = "PlayList";

		if (map.containsKey("type")) {
			type = map.getString("type");
		}

		if (type.equalsIgnoreCase("Radio")) {
			log.info("ALARM# Playing Radio " + job_name);
			String channel = map.getString("channel");
			PlayManager.getInstance().setStandby(false);
			PlayManager.getInstance().playRadio(channel);
		} else if (type.equalsIgnoreCase("OFF")) {
			log.info("ALARM# TURN OFF " + job_name);
			PlayManager.getInstance().stop();
			PlayManager.getInstance().setStandby(true);
		} else {
			log.info("ALARM# StartPlayList " + job_name);
			if (shuffle.equalsIgnoreCase("TRUE")) {
				PlayManager.getInstance().updateShuffle(true);
			} else {
				PlayManager.getInstance().updateShuffle(false);
			}
			PlayManager.getInstance().setStandby(false);
			PlayManager.getInstance().playIndex(0);
		}
	}
}
