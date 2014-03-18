package org.rpi.plugin.alarmclock;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.plug.interfaces.AlarmClockInterface;
import org.rpi.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@PluginImplementation
public class AlarmClockImpl implements AlarmClockInterface {

	private static Logger log = Logger.getLogger(AlarmClockImpl.class);

	private PlayManager iPlayer = null;
	private Scheduler scheduler;

	public AlarmClockImpl() {
		log.debug("Creating Alarm Clock Plugin");
		try {
			LogManager.getLogger(Class.forName("org.quartz.core.QuartzSchedulerThread")).setLevel(Level.INFO);
			LogManager.getLogger(Class.forName("org.quartz.utils.UpdateChecker")).setLevel(Level.ERROR);
		} catch (ClassNotFoundException e1) {
			log.error(e1);
		}
		log.debug("AlarmClock");
		this.iPlayer = PlayManager.getInstance();
		try {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.start();
		} catch (Exception e) {
			log.error("Error Starting Scheduler");
		}
		getConfig();
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
	private void createSchedule(String name, String time, String type, String channel, String volume, String shuffle) {
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

	public String createSleepTimer() {
		String name = "Sleep";
		String volume = "";
		String type = "OFF";
		String channel = "";
		String shuffle = "";
		Calendar cal = Calendar.getInstance();
		try {
			cal.set(Calendar.SECOND,0);
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
			cal.add(Calendar.MINUTE, 15);
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

	@Override
	public String cancelSleepTimer() {
		String name = "Sleep";
		try {
			TriggerKey tr_key = new TriggerKey(name, "sleepTimer");
			if (scheduler.checkExists(tr_key)) {
				if(scheduler.unscheduleJob(tr_key))
				{
					log.debug("Sleep Timer Cancelled");
				}				
			}
		} catch (Exception e) {
			log.error("Error Cancelling Sleep Timer");
		}
		return "OK";
	}

	@Override
	public String getSleepTimer() {
		String name = "Sleep";
		Calendar cal = Calendar.getInstance();
		try {

			TriggerKey tr_key = new TriggerKey(name, "sleepTimer");

			if (scheduler.checkExists(tr_key)) {

				Trigger sleepTrigger = scheduler.getTrigger(tr_key);
				cal = Calendar.getInstance();
				cal.setTime(sleepTrigger.getNextFireTime());
				return cal.getTime().toString();
			}
			
		}
		catch(Exception e)
		{
			log.error("Error Getting SleepTimer",e);
			return "Error: " + e.getMessage();
		}
		return "";
	}

	/***
	 * So my AlarmClock.xml file is in a subdirectory of Plugins, we have to
	 * find it the read it and schedule the triggers..
	 */
	private void getConfig() {
		try {
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = OSManager.getInstance().getFilePath(this.getClass(), false);
			log.debug("Getting AlarmClock.xml from Directory: " + path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(path + "AlarmClock.xml"));
			NodeList listOfChannels = doc.getElementsByTagName("Alarm");
			int i = 1;
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				boolean status = true;
				String name = null;
				String time = null;
				String type = null;
				String channel = "";
				String volume = "";
				String shuffle = "";

				Node alarm = listOfChannels.item(s);
				if (alarm.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) alarm;
					name = XMLUtils.getStringFromElement(element, "name");
					String mStatus = XMLUtils.getStringFromElement(element, "enabled");
					if (mStatus.equalsIgnoreCase("FALSE")) {
						status = false;
					}
					time = XMLUtils.getStringFromElement(element, "time");
					type = XMLUtils.getStringFromElement(element, "type");
					volume = XMLUtils.getStringFromElement(element, "volume");
					shuffle = XMLUtils.getStringFromElement(element, "shuffle");
					channel = XMLUtils.getStringFromElement(element, "channel");
				}
				if (status) {
					createSchedule(name, time, type, channel, volume, shuffle);
				}
			}
		} catch (Exception e) {
			log.error("Error Reading AlarmClock.xml");
		}
	}

	private String getAlarmTime() {
		Properties pr = new Properties();
		try {
			pr.load(new FileInputStream("app.properties"));
			return pr.getProperty("alarmtime");
		} catch (Exception e) {
			log.error(e);
		}
		return "";
	}

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
	}

	class RemindTask extends TimerTask {
		public void run() {
			log.debug("Time For Next Track");
			iPlayer.nextTrack();
		}
	}

}
