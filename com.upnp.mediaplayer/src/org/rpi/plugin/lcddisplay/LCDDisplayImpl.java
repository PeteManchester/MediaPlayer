package org.rpi.plugin.lcddisplay;

import java.io.File;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelBase;
import org.rpi.os.OSManager;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventMuteChanged;
import org.rpi.player.events.EventStandbyChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.player.events.EventVolumeChanged;
import org.rpi.player.observers.ObservableVolume;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.wiringpi.Lcd;

/**
 * Added Config options for GPIO Pins:
 * pin_rs     - Default 11
 * pin_strobe - Default 10
 * pin_data_1 - Default 0
 * pin_data_2 - Default 1
 * pin_data_3 - Default 2
 * pin_data_4 - Default 3
 * pin_data_5 - Default 0
 * pin_data_6 - Default 0
 * pin_data_7 - Default 0
 * pin_data_8 - Default 0
 * 
 * @author phoyle
 *
 */

@PluginImplementation
public class LCDDisplayImpl implements LCDDislayInterface, Observer {

	private Logger log = Logger.getLogger(LCDDisplayImpl.class);
	private GpioController gpio = null;

	// provision gpio pin #02 as an input pin with its internal pull down
	// resistor enabled

	// private GpioPinDigitalInput myButton = null;
	// private GpioPinDigitalOutput myMuteLed = null;

	public int LCD_ROWS = 2;
	public ArrayList<RowDefinition> row_definition = new ArrayList<RowDefinition>();
	public ArrayList<RowDefinition> standby_definition = new ArrayList<RowDefinition>();
	public int LCD_COLUMNS = 20;
	public int LCD_PIN_RS = 11;
	public int LCD_PIN_STROBE = 10;
	public int LCD_PIN_DATA_1 = 0;
	public int LCD_PIN_DATA_2 = 1;
	public int LCD_PIN_DATA_3 = 2;
	public int LCD_PIN_DATA_4 = 3;
	public int LCD_PIN_DATA_5 = 0;
	public int LCD_PIN_DATA_6 = 0;
	public int LCD_PIN_DATA_7 = 0;
	public int LCD_PIN_DATA_8 = 0;
	
	public final static int LCD_BITS = 4;
	private int lcdHandle = -1;
	private long mVolume = 100;
	private String mTime = "0:00";
	private boolean isMute = false;
	private LCDScroller scroller = null;

	public LCDDisplayImpl() {
		log.debug("Init LCDDisplayImpl");
		try {
			getConfig();
			PlayManager.getInstance().observeInfoEvents(this);
			PlayManager.getInstance().observeVolumeEvents(this);
			PlayManager.getInstance().observeTimeEvents(this);
			PlayManager.getInstance().observeProductEvents(this);
			try {
				initPi4J();
			} catch (Exception e) {
				log.error("Error Init Pi4J: " + e);
			}
			scroller = new LCDScroller(LCD_ROWS, LCD_COLUMNS, row_definition, standby_definition);
			scroller.setStandBy(PlayManager.getInstance().isStandby());
			scroller.setReset();
			if (lcdHandle != -1) {
				scroller.setLCDHandle(lcdHandle);
				scroller.reset();
				scroller.start();
				// welcomeMessage();
			}
		} catch (Exception e) {
			log.error("Error Init LCDDisplayImpl", e);
		}
	}

	private void initPi4J() {
		try {

			// gpio = GpioFactory.getInstance();
			gpio = OSManager.getInstance().getGpio();
			if (null == gpio)
				throw new IllegalArgumentException("GPIO Not Initialized");
			/*
			lcdHandle = Lcd.lcdInit(LCD_ROWS, // number of row supported by LCD
					LCD_COLUMNS, // number of columns supported by LCD
					LCD_BITS, // number of bits used to communicate to LCD
					11, // LCD RS pin
					10, // LCD strobe pin
					0, // LCD data bit 1
					1, // LCD data bit 2
					2, // LCD data bit 3
					3, // LCD data bit 4
					0, // LCD data bit 5 (set to 0 if using 4 bit communication)
					0, // LCD data bit 6 (set to 0 if using 4 bit communication)
					0, // LCD data bit 7 (set to 0 if using 4 bit communication)
					0); // LCD data bit 8 (set to 0 if using 4 bit
						// communication)
						 * 
						 */
			
			lcdHandle = Lcd.lcdInit(LCD_ROWS, // number of row supported by LCD
					LCD_COLUMNS, // number of columns supported by LCD
					LCD_BITS, // number of bits used to communicate to LCD
					LCD_PIN_RS, // LCD RS pin
					LCD_PIN_STROBE, // LCD strobe pin
					LCD_PIN_DATA_1, // LCD data bit 1
					LCD_PIN_DATA_2, // LCD data bit 2
					LCD_PIN_DATA_3, // LCD data bit 3
					LCD_PIN_DATA_4, // LCD data bit 4
					LCD_PIN_DATA_5, // LCD data bit 5 (set to 0 if using 4 bit communication)
					LCD_PIN_DATA_6, // LCD data bit 6 (set to 0 if using 4 bit communication)
					LCD_PIN_DATA_7, // LCD data bit 7 (set to 0 if using 4 bit communication)
					LCD_PIN_DATA_8); // LCD data bit 8 (set to 0 if using 4 bit
						// communication)
			

			// verify initialization
			if (lcdHandle == -1) {
				log.warn(" ==>> LCD INIT FAILED");
			}

			// clear LCD
			// LCDClear();
			log.info("Finished Configuring LCD");
		} catch (Exception e) {
			log.error("Error Initializing Pi4J" + e);
		}

	}

	@Override
	public void update(Observable o, Object e) {
		// log.debug("Event: " + e.toString());
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTTRACKCHANGED:
			try {
				EventTrackChanged etc = (EventTrackChanged) e;
				ChannelBase track = etc.getTrack();
				if (track != null) {
					String s = track.getFullDetails();
					log.debug("TrackChanged: " + s);
					// UpdateScroller(s, 0);
					scroller.updateValues("[FULL_DETAILS]", s);
					scroller.updateValues("[ARTIST]", track.getArtist());
					scroller.updateValues("[TITLE]", track.getTitle());
					scroller.updateValues("[ALBUM]", track.getAlbum());
					scroller.updateValues("[PERFORMER]", track.getPerformer());
					scroller.updateValues("[COMPOSER]", track.getComposer());
					scroller.updateValues("[CONDUCTOR]", track.getConductor());
					scroller.updateValues("[DATE]", track.getDate());
					scroller.updateValues("[STANDBY]", "");
				} else {
					log.debug("Track was NULL");
				}
			} catch (Exception ex) {
				log.error("TrackChanged", ex);
			}

			break;
		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText et = (EventUpdateTrackMetaText) e;
			try {
				log.debug("TrackMetaText Changed: " + et.getTitle() + " : " + et.getArtist());
				if (scroller != null) {
					// UpdateScroller(et.getTitle() + " - " + et.getArtist(),
					// 0);
					scroller.updateValues("[TITLE]", et.getTitle());
					scroller.updateValues("[ARTIST]", et.getArtist());
					scroller.updateValues("[PERFORMER]", et.getArtist());
				}
			} catch (Exception ex) {
				log.error("UpdateMetaData", ex);
			}
			break;
		case EVENTVOLUMECHANGED:
			try {
				EventVolumeChanged ev = (EventVolumeChanged) e;
				mVolume = ev.getVolume();
				// updateVolume();
				if(scroller !=null)
				{
					scroller.updateValues("[VOLUME]", "" + mVolume);
				}
			} catch (Exception ex) {
				log.error("VolumeChanged", ex);
			}
			break;
		case EVENTMUTECHANGED:
			if (o instanceof ObservableVolume) {
				try {
					EventMuteChanged em = (EventMuteChanged) e;
					log.debug("MuteStateChanged: " + em.isMute());
					isMute = em.isMute();
					if (em.isMute()) {
						if(scroller !=null)
						{
							scroller.updateValues("[VOLUME]", "Mute");
						}
					} else {
						if(scroller !=null)
						{
							scroller.updateValues("[VOLUME]", "" + mVolume);
						}
					}
				} catch (Exception ex) {
					log.error("MuteChanged", ex);
				}
			}
			break;
		case EVENTSTANDBYCHANGED:
			try {
				EventStandbyChanged es = (EventStandbyChanged) e;
				scroller.setReset();
				String sStandby = "false";
				if (es.isStandby()) {
					if(scroller !=null)
					{
						scroller.setStandBy(true);
					}
					sStandby = "true";
				} else {
					if(scroller !=null)
					{
						scroller.setStandBy(false);
					}
					try {
					} catch (Exception ex) {

					}
				}
				if(scroller !=null)
				{
					scroller.updateValues("[STANDBY", sStandby);
				}
			} catch (Exception ex) {
				log.error("StandbyChanged", ex);
			}
			break;
		case EVENTTIMEUPDATED:
			try {
				EventTimeUpdate etime = (EventTimeUpdate) e;
				if (mTime != null) {
					mTime = ConvertTime(etime.getTime());
				}
				// updateVolume();
				if(scroller !=null)
				{
				scroller.updateValues("[TIME]", mTime);
				}
			} catch (Exception ex) {
				log.error("TimeUpdated", ex);
			}
			break;

		}
	}

	/***
	 * Convert seconds to Hours:Seconds
	 * 
	 * @param lTime
	 * @return
	 */
	private String ConvertTime(long lTime) {
		if (lTime == 0)
			return "0:00";
		try {
			if (lTime <= Integer.MAX_VALUE) {
				int minutes = (int) lTime / 60;
				int seconds = (int) lTime % 60;
				String sSeconds = "";
				if (seconds < 10) {
					sSeconds = "0" + seconds;
				} else {
					sSeconds = "" + seconds;
				}
				return "" + minutes + ":" + sSeconds;
			}
		} catch (Exception e) {

		}
		return "" + lTime;
	}
	
	private int getIntValue(String path, Document doc)
	{
		int res = -1;
		try
		{
			XPath xPath = XPathFactory.newInstance().newXPath();
			String sColumns = xPath.compile(path).evaluate(doc);
			log.debug("Value of '" + path + "' : " + sColumns);
			res = Integer.parseInt(sColumns);
		}
		catch(Exception e)
		{
			log.error("Error, Config Option '" + "' Not Found", e  );
		}
		return res;
	}

	private void getConfig() {
		try {
			String class_name = this.getClass().getName();
			log.debug("Find Class, ClassName: " + class_name);
			String path = OSManager.getInstance().getFilePath(this.getClass(), false);
			log.debug("Getting LCD.xml from Directory: " + path);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new File(path + "LCD.xml"));
			
			int value = getIntValue("/LCD/@columns",doc);
			if(value >= 0 )
			{
				log.info("String LCD Colums: " + value);
				LCD_COLUMNS = value;
			}
			
			value = getIntValue("/LCD/@pin_rs",doc);
			if(value >=0)
			{
				log.info("String LCD pin_rs: " + value);
				LCD_PIN_RS = value;
			}
			
			value = getIntValue("/LCD/@pin_strobe",doc);
			if(value >=0)
			{
				log.info("String LCD pin_strobe: " + value);
				LCD_PIN_RS = value;
			}
			
			for(int i=1; i<=8;i++)
			{
				value = getIntValue("/LCD/@pin_data_"+ i,doc);
				if(value >=0)
				{
					log.info("String LCD pin_data_" + i + ": " + value);
					switch(i)
					{
					case 1:
						LCD_PIN_DATA_1 = value;
						break;
					case 2:
						LCD_PIN_DATA_2 = value;
						break;
					case 3:
						LCD_PIN_DATA_3 = value;
						break;
					case 4:
						LCD_PIN_DATA_4 = value;
						break;
					case 5:
						LCD_PIN_DATA_5 = value;
						break;
					case 6:
						LCD_PIN_DATA_6 = value;
						break;
					case 7:
						LCD_PIN_DATA_7 = value;
						 break;
					case 8:
						LCD_PIN_DATA_8 = value;
						break;
					}
				}
			}
		
			
			/*
			try {
				String ex_columns = "/LCD/@columns";
				XPath xPath = XPathFactory.newInstance().newXPath();
				String sColumns = xPath.compile(ex_columns).evaluate(doc);
				log.debug("Number of Columns: " + sColumns);
				LCD_COLUMNS = Integer.parseInt(sColumns);
			} catch (Exception e) {
				log.debug("Error getting Number of Columns:", e);
			}
			*/
			NodeList listOfRows = doc.getElementsByTagName("lcdrow");
			log.debug("Number of Rows: " + listOfRows.getLength());
			LCD_ROWS = listOfRows.getLength();
			for (int s = 0; s < listOfRows.getLength(); s++) {
				Node row = listOfRows.item(s);
				if (row.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) row;
					String text = getElement(element, "text");
					log.debug(text);
					RowDefinition rd = new RowDefinition();
					rd.setText(text);
					row_definition.add(rd);
				}
			}

			NodeList listOfStandby = doc.getElementsByTagName("lcdstandby");
			log.debug("Number of Standby Rows: " + listOfStandby.getLength());
			if (LCD_ROWS < listOfStandby.getLength())
				LCD_ROWS = listOfStandby.getLength();
			for (int s = 0; s < listOfStandby.getLength(); s++) {
				Node row = listOfStandby.item(s);
				if (row.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) row;
					String text = getElement(element, "text");
					log.debug(text);
					RowDefinition rd = new RowDefinition();
					rd.setText(text);
					standby_definition.add(rd);
				}
			}

		} catch (Exception e) {
			log.error("Error Getting LCD.xml", e);
		}
	}

	/***
	 * 
	 * @param element
	 * @param name
	 * @return
	 */
	private String getElement(Element element, String name) {
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

	@Shutdown
	public void bye() {
		log.debug("ShutDown Called");
		if (scroller != null) {
			scroller = null;
		}

		if (lcdHandle != -1) {
			int i = 0;
			for (int iRow = 0; iRow <= LCD_ROWS; iRow++) {
				Lcd.lcdPuts(i, "");
			}
			Lcd.lcdClear(lcdHandle);
		}
	}

}
