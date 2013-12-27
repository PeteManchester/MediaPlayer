package org.rpi.plugin.lcddisplay;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RowDefinition {

	private static Logger log = Logger.getLogger(RowDefinition.class);

	private String text = "";
	private boolean first_time = true;
	private boolean isFormat = false;
	private String format = "";
	private ArrayList<String> keys = new ArrayList<String>();
	private ArrayList<String> system_keys = new ArrayList<String>();

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		if (first_time) {
			grabKeys();
			first_time = false;
		}
	}

	private void grabKeys() {
		log.debug("GrabKeys: " + text);
		// Find all text in Square Brackets
		String p = "\\[([^]]+)\\]";
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			String value = matcher.group();
			if (value.contains("(")) {
				String sFormat = getFormat(value);
				String oldValue = value;
				value = value.replace(sFormat, "");
				value = text.replace(oldValue, value);
				value = text.trim();
				if(value.startsWith("("))
					value = value.replaceFirst("(", "");
				if(value.endsWith(")"))
					value = value.substring(0, value.length()-1);
						
				log.debug("Setting Format: " + value + " : " + sFormat);
				setFormat(sFormat);
			}
			if (value.startsWith("[SYS")) {
				log.debug("Adding System Key: " + value);
				getSystemKeys().add(value);
			} else {
				log.debug("Adding Key: " + value);
				keys.add(value);
			}
		}
	}

	private String getFormat(String text) {
		String res = "";
		String p = "\\(([^]]+)\\)";
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res = matcher.group();
			isFormat = true;
			return res;
		}
		return res;
	}

	public ArrayList<String> getKeys() {
		//log.debug("Returning Keys: " + keys);
		return keys;
	}

	public boolean isFormat() {
		return isFormat;
	}

	public String getFormat() {
		
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public ArrayList<String> getSystemKeys() {
		return system_keys;
	}
}
