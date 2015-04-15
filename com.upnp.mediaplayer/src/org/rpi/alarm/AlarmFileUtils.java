package org.rpi.alarm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.apache.log4j.Logger;

public class AlarmFileUtils {

	private Logger log = Logger.getLogger(this.getClass());
	private String fileName = "Alarm.json";

	public AlarmFileUtils() {
		getJSONFromFile();
	}

	/**
	 * Get a JSON Object from a File
	 */
	private JsonObject getJSONFromFile() {
		Reader reader = null;
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			log.error("Cannot find RadioList.json", e);
			// Bail out here is we can't find the file
			return null;
		}

		return this.getJsonFromReader(reader);
	}

	private JsonObject getJsonFromReader(Reader reader) {
		try {
			JsonReader jsonReader = Json.createReader(reader);
			JsonObject array = jsonReader.readObject();
			jsonReader.close();
			return array;
		} catch (Exception e) {
			log.error("Error Reading RadioList.json from given reader", e);
		}
		return null;
	}

	public JsonObject getAlarms() {
		return getJSONFromFile();
	}

	private JsonArray getAlarms(JsonObject array) {

		boolean enabled = false;
		String name = "No Name";
		String type = "";
		String time = "";
		int volume = 0;
		String channel = "";
		String shuffle = "true";

		if (array == null)
			return null;
		if (array.containsKey("alarms")) {
			JsonArray body = array.getJsonArray("alarms");
			return body;
		}
		return null;
	}

	public void saveJSON(JsonObject array) {
		try {
			FileWriter sw = new FileWriter(fileName);
			Map<String, Object> properties = new HashMap<>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);
			JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
			JsonWriter jsonWriter = writerFactory.createWriter(sw);

			jsonWriter.writeObject(array);
			jsonWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getPrettyString(JsonObject array) {
		try {
			StringWriter sw = new StringWriter(700);
			Map<String, Object> properties = new HashMap<>(1);
			properties.put(JsonGenerator.PRETTY_PRINTING, true);
			JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
			JsonWriter jsonWriter = writerFactory.createWriter(sw);
			jsonWriter.writeObject(array);
			jsonWriter.close();
			String text = sw.toString();
			return text;
		} catch (Exception e) {
			log.error(e);
		}
		return "";
	}

}
