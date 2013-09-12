package org.rpi.plugin.lcddisplay;

import org.apache.log4j.Logger;

import com.pi4j.util.StringUtil;

public class LCDRow {
	
	private static Logger log = Logger.getLogger(LCDRow.class);

	private int LCD_WIDTH = 20;
	private String current_text = "";
	private String next_text = "";
	private int position = 0;

	public int getLCD_WIDTH() {
		return LCD_WIDTH;
	}

	/***
	 * Set the Width of the Display
	 * @param lCD_WIDTH
	 */
	public void setLCD_WIDTH(int lCD_WIDTH) {
		LCD_WIDTH = lCD_WIDTH;
	}

	/***
	 * Get the Text to Display
	 * @return
	 */
	public String getText() {
		UpdateText();
		if (current_text.length() <= LCD_WIDTH)
			return current_text;
		else
			return getNextString(current_text);
	}

	/***
	 * Set the Text
	 * @param text
	 */
	public void setText(String text) {
		if (text.length() <= LCD_WIDTH) {
			next_text = StringUtil.padCenter(text, LCD_WIDTH);
		} else {
			next_text = StringUtil.padLeft("", LCD_WIDTH) + text + StringUtil.padRight("", LCD_WIDTH);
		}
	}

	/***
	 * Change over from the existing Text to the new Text
	 */
	public void UpdateText() {
		if (current_text != next_text) {
			if (next_text.length() <= LCD_WIDTH) {
				position = 0;
			} else {
			}
			current_text = next_text;
		}
	}

	/***
	 * If text if wider then the screen width scroll through the text
	 * @param in
	 * @return
	 */
	private String getNextString(String in) {
		String res = "";
		position++;
		if (position + LCD_WIDTH > in.length()) {
			position = 0;
		}
		if (in.length() > LCD_WIDTH) {
			try {
				res = in.substring(position, position + LCD_WIDTH);
			} catch (Exception e) {
				log.error("Error getNextString. Position: " + position + " Text:" + in, e);
				position = 0;
			}
		}
		return res;
	}

}
