package org.rpi.web.rest;

import java.io.StringReader;
import java.net.URLDecoder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.rpi.alarm.Alarm;

@Path("alarmconfig")
public class AlarmConfig {

	private Logger log = Logger.getLogger(this.getClass());

	@Path("getAlarms")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus() {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(Alarm.getInstance().getAlarmConfig());
		} catch (Exception e) {
			log.error("Error creating Status JSON", e);
		}
		return sb.toString();
	}

	@Path("update")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public String setConfig(String msg) {
		try {
			msg = URLDecoder.decode(msg, "UTF-8");
			if (msg.startsWith("=")) {
				msg = msg.substring(1);
			}
			log.debug("setConfig: " + msg);
			JsonReader reader = Json.createReader(new StringReader(msg));
			JsonObject alarmObject = reader.readObject();
			reader.close();
			if (alarmObject != null) {
				Alarm.getInstance().updateAlarms(alarmObject);
			}

		} catch (Exception e) {
			log.error("Error creating Status JSON", e);
			return "Error :" + e.getMessage();
		}
		// return Response.status(200).entity("HELLO").build();
		return "Saved";
	}

}
