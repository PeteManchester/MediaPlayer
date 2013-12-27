package org.rpi.plugin.lcddisplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.rpi.os.OSManager;

import com.pi4j.system.SystemInfo;
import com.pi4j.wiringpi.Lcd;

public class LCDScroller extends Thread {

	private static Logger log = Logger.getLogger(LCDScroller.class);
	private int lcdHandle = -1;
	private int LCD_COLUMNS = 20;
	private int LCD_ROWS = 2;
	private boolean bStandBy = true;
	private boolean bStartup = true;
	private boolean bReset = true;
	private ConcurrentHashMap<String, String> values = new ConcurrentHashMap<String, String>();
	private ArrayList<RowDefinition> row_definition = new ArrayList<RowDefinition>();
	private List<LCDRow> rows = new ArrayList<LCDRow>();

	/**
	 * 
	 * @param lcd_rows
	 * @param lcd_columns
	 * @param row_definition
	 */
	public LCDScroller(int lcd_rows, int lcd_columns, ArrayList<RowDefinition> row_definition) {
		LCD_ROWS = lcd_rows;
		LCD_COLUMNS = lcd_columns;
		for (int i = 0; i < LCD_ROWS; i++) {
			LCDRow row = new LCDRow();
			row.setLCD_WIDTH(LCD_COLUMNS);
			rows.add(row);
		}
		this.row_definition = row_definition;
		init();
	}

	/**
	 * Initialize the values..
	 */
	private void init() {
		log.debug("Initialize Values");
		updateValues("[VOLUME]", "100");
		updateValues("[TIME]", "0:00");
		updateValues("[FULL_DETAILS]", "");
		updateValues("[ALBUM]", "");
		updateValues("[ARTIST]", "");
		updateValues("[TRACK]", "");
		updateValues("[TITLE]", "");
		updateValues("[COMPOSER]", "");
		updateValues("[DATE]", "");
	}

	public void run() {
		while (true) {
			try {
				if (isReset()) {
					if (lcdHandle != -1) {
						Lcd.lcdClear(lcdHandle);
					}
					setText("", 0);
					setText("", 1);
					bReset = false;
				}
				if (bStandBy) {
					if (bStartup) {
						welcomeMessage();
					} else {
						standbyMessage();
					}
				} else {
					bStartup = false;
				}
				for (int i = 0; i < LCD_ROWS; i++) {
					LCDRow row = rows.get(i);
					LCDWrite(row.getText(), i);
				}

			} catch (Exception e) {
				log.error(e);
			}
			try {

				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
	}

	private void standbyMessage() {
		Date now = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd  hh:mm:ss");
		String dateStr = formatter.format(now);
		String cpuTemp = "";
		try {
			cpuTemp = "CPU Temp:" + SystemInfo.getCpuTemperature() + "C";
		} catch (Exception e) {
			log.error("Error Getting CPU Temp", e);
		}
		setText(cpuTemp, 0);
		setText(dateStr, 1);
	}

	/***
	 * Create the Welcome Message
	 */
	private void welcomeMessage() {
		// log.debug("Welcome Message");
		String sWelcome = "Welcome";
		String sStatus = "";
		try {
			sStatus = "CPU Temp:" + SystemInfo.getCpuTemperature() + " Memory Free:" + convertBytes(SystemInfo.getMemoryFree()) + " Memory Used:" + convertBytes(SystemInfo.getMemoryUsed());
		} catch (Exception e) {
			log.error(e);
		}
		setText(sWelcome, 0);
		setText(sStatus, 1);
	}

	private final String[] Q = new String[] { "", "Kb", "Mb", "Gb", "Tb", "Pb", "Eb" };

	public String convertBytes(long bytes) {
		for (int i = 6; i > 0; i--) {
			double step = Math.pow(1024, i);
			if (bytes > step)
				return String.format("%3.1f %s", bytes / step, Q[i]);
		}
		return Long.toString(bytes);
	}

	/***
	 * 
	 * @return
	 */
	public int getLCDHandle() {
		return lcdHandle;
	}

	/***
	 * 
	 * @param lcdHandle
	 */
	public void setLCDHandle(int lcdHandle) {
		this.lcdHandle = lcdHandle;
	}

	/***
	 * 
	 * @param s
	 * @param Row
	 */
	public synchronized void LCDWrite(String s, int Row) {
		if (lcdHandle != -1) {
			try {
				Lcd.lcdPosition(lcdHandle, 0, Row);
				byte[] data = s.getBytes("UTF-8");
				for (byte b : data)
					Lcd.lcdPutchar(lcdHandle, b);
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	/***
	 * 
	 * @param text
	 * @param row
	 */
	public synchronized void setText(String text, int row) {

		try {
			rows.get(row).setText(text);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public synchronized boolean isStandBy() {
		return bStandBy;
	}

	public synchronized void setStandBy(boolean bStandBy) {
		this.bStandBy = bStandBy;
	}

	public synchronized boolean isReset() {
		return bReset;
	}

	/***
	 * Reset the LCD Display
	 */
	public synchronized void setReset() {
		this.bReset = true;
	}

	/**
	 * Update the Values
	 * 
	 * @param name
	 * @param value
	 */
	public synchronized void updateValues(String name, String value) {
		if (values.containsKey(name)) {
			values.remove(name);
		}
		values.put(name, value);
		updateRows();
	}

	/**
	 * 
	 */
	private void updateRows() {
		int i = 0;
		for (RowDefinition def : row_definition) {
			String text = def.getText();
			//log.debug("Got Row Definition: " + def.getText());
			for (String rd : def.getKeys()) {
				//log.debug("Getting Value for: " + rd);
				String value = "";
				//log.debug(values);
				if (values.containsKey(rd)) {
					//log.debug("Setting Value to Be");
					value = values.get(rd);
				}
				if (def.isFormat()) {
					try {
						log.debug("Tring to Parse: " + value);
						Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
						value = new SimpleDateFormat(def.getFormat()).format(date);
						//log.debug(value);
						//log.debug(text);
					} catch (Exception e) {
						log.error(e);
					}
				}
				// log.debug("Key:  " + key + " : " + values.get(key));
				text = text.replace(rd, value);
			}

			if (OSManager.getInstance().isRaspi()) {
				for (String rd : def.getSystemKeys()) {
					try {
						if (rd.contains("[SYS_MEMORY_USED]")) {
							text = text.replace("[SYS_MEMORY_USED]", convertBytes(SystemInfo.getMemoryUsed()));
						}
					} catch (Exception e) {
						log.error("Error gettting SystimInfo:", e);
					}
				}
			}
			// log.debug("Row: " + i + " Text: " + def);
			rows.get(i).setText(text);
			i++;
		}

	}
}
