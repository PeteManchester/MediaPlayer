package org.rpi.plugin.lcddisplay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	private boolean bStandBy = true;
	private boolean bStartup = true;
	private boolean bReset = true;

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
				if(isReset())
				{
					if (lcdHandle != -1) {
						Lcd.lcdClear(lcdHandle);
					}
					setText("", 0);
					setText("", 1);
					bReset=false;
				}
				if (bStandBy) {
					if(bStartup)
					{
						welcomeMessage();
					}
					else
					{
						standbyMessage();
					}
				}
				else
				{
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
			log.error("Error Getting CPU Temp",e);
		} 
		setText(cpuTemp,0);
		setText(dateStr,1);
	}
	
	/***
	 * Create the Welcome Message
	 */
	private void welcomeMessage() {
		//log.debug("Welcome Message");
		String sWelcome = "Welcome";
		String sStatus = "";
		try {
			sStatus = "CPU Temp:" + SystemInfo.getCpuTemperature() + " Memory Free:" + convertBytes(SystemInfo.getMemoryFree()) + " Memory Used:" + convertBytes(SystemInfo.getMemoryUsed());
		} catch (Exception e) {
			log.error(e);
		}
		setText(sWelcome,0);
		setText(sStatus,1);
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
	public synchronized void  setReset() {
		this.bReset = true;
	}
}
