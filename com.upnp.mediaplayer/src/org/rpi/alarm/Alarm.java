package org.rpi.alarm;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonValue;
//import javax.json.JsonValue.ValueType;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.rpi.player.PlayManager;

public class Alarm {

	private Logger log = Logger.getLogger(Alarm.class);

	private PlayManager iPlayer = null;
	private Scheduler scheduler;
	private AlarmFileUtils utils = null;
	private static Alarm instance = null;

	public static Alarm getInstance() {
		if (instance == null) {
			instance = new Alarm();
		}
		return instance;
	}

	private Alarm() {
		utils = new AlarmFileUtils();
		try {
			LogManager.getLogger(Class.forName("org.quartz.core.QuartzSchedulerThread")).setLevel(Level.INFO);
			LogManager.getLogger(Class.forName("org.quartz.utils.UpdateChecker")).setLevel(Level.ERROR);
		} catch (ClassNotFoundException e1) {
			log.error(e1);
		}
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
			
		} catch (Exception e) {
			log.error("Error Starting Scheduler");
		}
		JSONObject alarms = getAlarmObjects();
		refreshAlarms(alarms);
	}
	
	private JSONObject getAlarmObjects()
	{
		utils = new AlarmFileUtils();
		JSONObject array = utils.getAlarms();
		return array;
	}

	public String getAlarmConfig() {
		JSONObject array = getAlarmObjects();
		return array.toString(2);
		//return utils.getPrettyString(array);
	}

	public void updateAlarms(JSONObject jsonObject) {
		try {
			refreshAlarms(jsonObject);
			utils.saveJSON(jsonObject);
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void refreshAlarms(JSONObject jsonObject) {
		log.debug("Refreshing Alarms");
		boolean enabled = false;
		String name = "No Name";
		String type = "";
		String time = "";
		int volume = 0;
		String channel = "";
		boolean shuffle = true;

		if (jsonObject == null)
			return;
		clearSchedule();
		if (jsonObject.has("alarms")) {
			JSONArray body = jsonObject.getJSONArray("alarms");
			
			Iterator<Object> l = body.iterator();
			
			while (l.hasNext()) {
				JSONObject object = (JSONObject) l.next();
				if (object.has("enabled")) {
					enabled = object.getBoolean("enabled");
				}
				if (object.has("name")) {
					name = object.getString("name");
				}
				if (object.has("type")) {
					type = object.getString("type");
				}
				if (object.has("time")) {
					time = object.getString("time");
				}
				if (object.has("volume")) {
					volume = object.getInt("volume");
				}
				if (object.has("channel")) {
					channel = object.getString("channel");
				}
				if (object.has("shuffle")) {
					shuffle = object.getBoolean("shuffle");
				}
				if (enabled) {
					createSchedule(name, time, type, channel, "" + volume, String.valueOf(shuffle));
				}
			}
			
			/*
			for (JsonValue jsonValue : body) {
				if (jsonValue.getValueType() == ValueType.OBJECT) {
					JsonObject object = (JsonObject) jsonValue;
					if (object.containsKey("enabled")) {
						enabled = object.getBoolean("enabled");
					}
					if (object.containsKey("name")) {
						name = object.getString("name");
					}
					if (object.containsKey("type")) {
						type = object.getString("type");
					}
					if (object.containsKey("time")) {
						time = object.getString("time");
					}
					if (object.containsKey("volume")) {
						volume = object.getInt("volume");
					}
					if (object.containsKey("channel")) {
						channel = object.getString("channel");
					}
					if (object.containsKey("shuffle")) {
						shuffle = object.getBoolean("shuffle");
					}
					if (enabled) {
						createSchedule(name, time, type, channel, "" + volume, String.valueOf(shuffle));
					}
				}
			}
			*/
		}
	}

	/***
	 * User Quartz to set up a schedule
	 * 
	 * @param name
	 * @param time
	 * @param type
	 * @param channel
	 * @param volume
	 * @param shuffle
	 */
	public void createSchedule(String name, String time, String type, String channel, String volume, String shuffle) {
		try {
			TriggerKey tr_key = new TriggerKey(name, "radioPlugin");
			JobDetail job = JobBuilder.newJob(AlarmClockJob.class).withIdentity(name, "group1").build();
			Map dataMap = job.getJobDataMap();
			dataMap.put("id", name);
			dataMap.put("Volume", volume);
			dataMap.put("Shuffle", shuffle);
			dataMap.put("type", type);
			dataMap.put("channel", channel);
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(tr_key).withSchedule(CronScheduleBuilder.cronSchedule(time)).forJob(job).build();
			if (scheduler.checkExists(tr_key)) {
				Date next_time = scheduler.rescheduleJob(tr_key, trigger);
				log.info("Schedule has been changed, next execution time : " + next_time.toString() + " -- Job: " + name);
			} else {
				Date next_time = scheduler.scheduleJob(job, trigger);
				log.info("Job has been scheduled, next execution time : " + next_time.toString() + " -- Job: " + name);
			}
		} catch (Exception e) {
			log.error("Error Creating Job: ", e);
		}
	}

	/**
	 * Clear all schedules except for Sleep timer
	 */
	public void clearSchedule() {
		log.debug("Clearing Schedules");
		try {
			for (String groupName : scheduler.getJobGroupNames()) {

				for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

					String jobName = jobKey.getName();
					String jobGroup = jobKey.getGroup();

					// get job's trigger
					List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);

					Date nextFireTime = triggers.get(0).getNextFireTime();
					log.debug("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);
					if (!jobName.equalsIgnoreCase("Sleep")) {
						TriggerKey tr_key = triggers.get(0).getKey();
						boolean res = scheduler.unscheduleJob(tr_key);
						log.debug("Result of Unschedule: " + res);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create a Sleep Timer
	 * @param value
	 * @return
	 */
	public String createSleepTimer(String value) {
		String name = "Sleep";
		String volume = "";
		String type = "OFF";
		String channel = "";
		String shuffle = "";
		Calendar cal = Calendar.getInstance();
		try {
			int iTime = Integer.parseInt(value);
			if (iTime == 0) {
				return "Not Scheduled";
			}
			cal.set(Calendar.SECOND, 0);
			TriggerKey tr_key = new TriggerKey(name, "sleepTimer");

			if (scheduler.checkExists(tr_key)) {

				Trigger sleepTrigger = scheduler.getTrigger(tr_key);
				cal = Calendar.getInstance();
				cal.setTime(sleepTrigger.getNextFireTime());
				log.info("Sleep Already Scheduled : " + cal.toString());
			}

			JobDetail job = JobBuilder.newJob(AlarmClockJob.class).withIdentity(name, "group1").build();
			Map dataMap = job.getJobDataMap();
			dataMap.put("id", name);
			dataMap.put("Volume", volume);
			dataMap.put("Shuffle", shuffle);
			dataMap.put("type", type);
			dataMap.put("channel", channel);
			cal.add(Calendar.MINUTE, iTime);
			Trigger trigger = TriggerBuilder.newTrigger().withIdentity(tr_key).startAt(cal.getTime()).forJob(job).build();
			if (scheduler.checkExists(tr_key)) {
				Date next_time = scheduler.rescheduleJob(tr_key, trigger);
				log.info("Schedule has been changed, next execution time : " + next_time.toString() + " -- Job: " + name);
				return next_time.toString();
			} else {
				Date next_time = scheduler.scheduleJob(job, trigger);
				log.info("Job has been scheduled, next execution time : " + next_time.toString() + " -- Job: " + name);
				return next_time.toString();
			}
		} catch (Exception e) {
			log.error("Error Creating Job: ", e);
			return e.getMessage();
		}
	}

	/**
	 * Cancel Sleep Timer
	 * @return
	 */
	public String cancelSleepTimer() {
		String name = "Sleep";
		try {
			TriggerKey tr_key = new TriggerKey(name, "sleepTimer");
			if (scheduler.checkExists(tr_key)) {
				if (scheduler.unscheduleJob(tr_key)) {
					log.debug("Sleep Timer Cancelled");
				}
			}
		} catch (Exception e) {
			log.error("Error Cancelling Sleep Timer");
		}
		return "OK";
	}

	/**
	 * Get the Sleep Timer
	 * @return
	 */
	public String getSleepTimer() {
		String name = "Sleep";
		Calendar cal = Calendar.getInstance();
		try {

			TriggerKey tr_key = new TriggerKey(name, "sleepTimer");

			if (scheduler.checkExists(tr_key)) {
				Trigger sleepTrigger = scheduler.getTrigger(tr_key);
				if (sleepTrigger != null) {
					cal = Calendar.getInstance();
					cal.setTime(sleepTrigger.getNextFireTime());
					return cal.getTime().toString();
				}
			}
		} catch (Exception e) {
			log.error("Error Getting SleepTimer: " + e.getMessage());
			return "Error: " + e.getMessage();
		}
		return "";
	}

}
