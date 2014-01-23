package org.rpi.plugin.alarmclock;

import java.io.File;
import java.io.FileInputStream;
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
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
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

	/***
	 * So my AlarmClock.xml file is in a subdirectory of Plugins, we have to
	 * find it the read it and schedule the triggers..
	 */
	private void getConfig() {
		try {
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = OSManager.getInstance().getFilePath(this.getClass(),false);
			log.debug("Getting AlarmClock.xml from Directory: " + path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(path + "AlarmClock.xml"));
			NodeList listOfChannels = doc.getElementsByTagName("Alarm");
			int i = 1;
			for (int s = 0; s < listOfChannels.getLength(); s++) {
				String name = null;
				String time = null;
				String type = null;
				String channel = "";
				String volume = "";
				String shuffle = "";

				Node alarm = listOfChannels.item(s);
				if (alarm.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) alarm;
					name = getElementTest(element, "name");
					time = getElementTest(element, "time");
					type = getElementTest(element, "type");
					volume = getElementTest(element, "volume");
					shuffle = getElementTest(element, "shuffle");
					channel = getElementTest(element, "channel");
				}

				createSchedule(name, time, type, channel, volume, shuffle);
			}
		} catch (Exception e) {
			log.error("Error Reading AlarmClock.xml");
		}
	}


	/***
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	private String getElementTest(Element element, String name) {
		String res = "";
		NodeList nid = element.getElementsByTagName(name);
		if (nid != null) {
			Element fid = (Element) nid.item(0);
			if (fid != null) {
				res = fid.getTextContent();
				// log.debug("ElementName: " + name + " Value: " + res);
				return res;

			}
		}
		return res;
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
