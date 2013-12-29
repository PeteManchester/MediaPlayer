package org.rpi.plugin.lcddisplay;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RowDefinition {

	private static Logger log = Logger.getLogger(RowDefinition.class);

	private String text = "";
	private boolean first_time = true;
	private ArrayList<KeyDefinition> keys = new ArrayList<KeyDefinition>();
	private ArrayList<KeyDefinition> system_keys = new ArrayList<KeyDefinition>();

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
			KeyDefinition kd = new KeyDefinition();
			kd.setName(value);
			
			if (value.startsWith("[SYS")) {
				log.debug("Adding System Key: " + value);
				getSystemKeys().add(kd);
			} else {
				log.debug("Adding Key: " + value);
				keys.add(kd);
			}
		}
	}

	public ArrayList<KeyDefinition> getKeys() {
		return keys;
	}

	public ArrayList<KeyDefinition> getSystemKeys() {
		return system_keys;
	}
}
