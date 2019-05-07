package org.rpi.alarm;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
*/

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class AlarmFileUtils {

	private Logger log = Logger.getLogger(this.getClass());
	private String fileName = "Alarm.json";

	public AlarmFileUtils() {
		getJSONFromFile();
	}

	/**
	 * Get a JSON Object from a File
	 */
	private JSONObject getJSONFromFile() {
		JSONObject array = new JSONObject();
		String content;
		try {
			content = new String(Files.readAllBytes(Paths.get(fileName)));
			array = new JSONObject(content);
		} catch (IOException e) {
			log.error("getJSONFromFile",e);
		}		
		return array;
		/*
		Reader reader = null;
		try {
			reader = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			log.error("Cannot find Alarm.json", e);
			// Bail out here is we can't find the file
			return null;
		}

		return this.getJsonFromReader(reader);
		*/
	}

	/*
	private JSONObject getJsonFromReader(Reader reader) {
		try {
			JsonReader jsonReader = Json.createReader(reader);
			JsonObject array = jsonReader.readObject();
			jsonReader.close();
			return array;
		} catch (Exception e) {
			log.error("Error Reading Alarm.json from given reader", e);
		}
		return null;
	}
	*/

	public JSONObject getAlarms() {
		return getJSONFromFile();
	}

	private JSONArray getAlarms(JSONObject array) {

		boolean enabled = false;
		String name = "No Name";
		String type = "";
		String time = "";
		int volume = 0;
		String channel = "";
		String shuffle = "true";

		if (array == null)
			return null;
		if (array.has("alarms")) {
			JSONArray body = array.getJSONArray("alarms");
			return body;
		}
		return null;
	}

	public void saveJSON(JSONObject array) {
		
		try {

			FileWriter fw = new FileWriter(fileName);
			String json = array.toString(2);
			fw.write(json);
			fw.close();

		} catch (Exception e) {
			log.error("Error Saving to: " + fileName, e);
		}
		
		/*
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
		*/
	}

	/*
	public String getPrettyString(JSONObject array) {
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
	*/

}
