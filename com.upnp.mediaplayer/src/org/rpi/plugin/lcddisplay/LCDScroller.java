package org.rpi.plugin.lcddisplay;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.pi4j.system.SystemInfo;
import com.pi4j.wiringpi.Lcd;

public class LCDScroller extends Thread {

	private static Logger log = Logger.getLogger(LCDScroller.class);
	private int lcdHandle = -1;
	public final static int LCD_COLUMNS = 20;
	public final static int LCD_ROWS = 2;
	private boolean bStandBy = false;

	private List<LCDRow> rows = new ArrayList<LCDRow>();

	public LCDScroller() {
		for (int i = 0; i < LCD_ROWS; i++) {
			LCDRow row = new LCDRow();
			row.setLCD_WIDTH(LCD_COLUMNS);
			rows.add(row);
		}
	}

	public void run() {
		while (true) {
			try {
				if (bStandBy) {
					standbyMessage();
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
			log.error("Error Getting CPU Temp",e);
		} 
		rows.get(1).setText(dateStr);
		rows.get(0).setText(cpuTemp);
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
	public void setText(String text, int row) {

		try {
			rows.get(row).setText(text);
		} catch (Exception e) {
			log.error(e);
		}
	}

	public boolean isStandBy() {
		return bStandBy;
	}

	public void setStandBy(boolean bStandBy) {
		this.bStandBy = bStandBy;
	}
}
