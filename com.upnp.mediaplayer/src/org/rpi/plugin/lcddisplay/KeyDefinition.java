package org.rpi.plugin.lcddisplay;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class KeyDefinition {
	private String name="";
	private String key = "";
	private String format = "";
	private boolean bFormatted =false;
	
	private Logger log = Logger.getLogger(KeyDefinition.class);
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
		String sKey = name;
		if (name.contains("{")) {
			String sFormat = getFormat(name);
			sKey = sKey.replace(sFormat, "");
			sKey = sKey.trim();
			if(sFormat.startsWith("{"))
				sFormat = sFormat.substring(1);
			if(sFormat.endsWith("}"))
				sFormat = sFormat.substring(0, sFormat.length()-1);
			log.debug("Setting Format: " + name + " : " + sFormat);
			setFormat(sFormat);
		}
		setKey(sKey);	
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public boolean isFormatted() {
		return bFormatted;
	}
	public void setFormatted(boolean bFormatted) {
		this.bFormatted = bFormatted;
	}
	
	/**
	 * Check For a format parameter
	 * @param text
	 * @return
	 */
	private String getFormat(String text) {
		String res = "";
		String p = "\\{([^]]+)\\}";
		Pattern pattern = Pattern.compile(p);
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			res = matcher.group();
			bFormatted = true;
			return res;
		}
		return res;
	}

}
