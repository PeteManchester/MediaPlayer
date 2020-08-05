package org.rpi.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;

//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.rpi.config.Config;

/**
 * Root resource (exposed at "myresource" path)
 * http://localhost:8090/myapp/myresource
 */
@Path("config")
public class ConfigRest {

	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 * 
	 * @return String that will be returned as a text/plain response.
	 */
	@Path("getConfig")
	@GET
	@Produces("text/html; charset=utf-8")

	public String getIt() {
		JSONObject sb = new JSONObject();
		try {
			Properties pr = new Properties();
			try (InputStream in = new FileInputStream(new File("app.properties")); InputStreamReader writer = new InputStreamReader(in, Charset.forName("UTF-8"))) {
				pr = new Properties();
				pr.load(writer);
				Enumeration<?> e = pr.propertyNames();
				while (e.hasMoreElements()) {
					String key = (String) e.nextElement();
					String value = pr.getProperty(key);
					String s = value;
					try {
						s = StringEscapeUtils.escapeJson(value);
					} catch (Exception ex) {
						log.error("Error: ", ex);
					}

					sb.put(key, s);
				}
			} catch (Exception e) {
				log.error("Error Loading Config", e);
			}

		} catch (Exception e) {
			log.error("Error Loading Config", e);
		}

		return sb.toString();
	}

	/*
	 * public String getIt() { StringBuilder sb = new StringBuilder(); try {
	 * Properties pr = new Properties(); pr.load(new
	 * FileInputStream("app.properties"));
	 * 
	 * sb.append("{"); String q = "\""; String colon = ":"; String space = " ";
	 * String comma = ""; Enumeration<?> e = pr.propertyNames(); while
	 * (e.hasMoreElements()) { String key = (String) e.nextElement(); String
	 * value = pr.getProperty(key); sb.append(comma + q + key + q + colon +
	 * space + q + converValues(value) + q); comma = ","; }
	 * 
	 * sb.append("}"); } catch (Exception e) { e.printStackTrace(); } return
	 * sb.toString(); }
	 */

	// private String convertKey(String key) {
	// key = key.replaceAll("\\.", "_");
	// return key;
	//
	// }

	private String converValues(String value) {
		try {
			value = URLEncoder.encode(value, "UTF-8");
		} catch (Exception e) {
			log.error("Error creating Encoding JSON", e);
		}
		return value;
	}

	@Path("setConfig")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String setConfig(String msg) {
		try {
			msg = URLDecoder.decode(msg, "UTF-8");
			if (msg.startsWith("=")) {
				msg = msg.substring(1);
			}
			// msg = unescape(msg);
			log.debug("setConfig: " + msg);
			// JsonReader reader = Json.createReader(new StringReader(msg));
			// JsonObject configObject = reader.readObject();
			// reader.close();
			JSONObject configObject = new JSONObject(msg);
			log.debug("setConfig: " + configObject.toString(2));
			if (configObject != null) {
				Config.getInstance().updateConfig(configObject);
			}
			log.debug("Name: " + configObject.getString("mediaplayer_friendly_name"));
			// Config.friendly_name = configObject.getString("friendly_name");
			log.debug("ConsoleLogLevel: " + configObject.getString("log_console_level"));

		} catch (Exception e) {
			log.error("Error creating Status JSON", e);
			return "Error :" + e.getMessage();
		}
		// return Response.status(200).entity("HELLO").build();
		return "Saved";
	}

	private String getStringValue(JSONObject configObject, String key) {
		if (configObject.has(key)) {
			return configObject.getString(key);
		}
		return null;
	}

	private Boolean getBooleanValue(JSONObject configObject, String key) {
		if (configObject.has(key)) {
			return configObject.getBoolean(key);
		}
		return null;
	}

}
