package org.rpi.web.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

@Path("execute")
public class ExecuteRest {

	private Logger log = Logger.getLogger(this.getClass());

	@Path("command")
	@POST
	@Produces("text/html; charset=utf-8")
	public Response execute(String command) {
		try {
			String test = URLDecoder.decode(command, "UTF-8");
			test = test.replace("command=", "");
			log.debug("Rest Request: " + test);
			executeCommand(test);
		} catch (Exception e) {

		}
		return Response.status(200).entity("").build();
	}

	private void executeCommand(String command) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			Process pa = Runtime.getRuntime().exec(command);
			pa.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(pa.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				log.debug("Result of " + command + " : " + line);
				list.add(line);
			}
			reader.close();
			pa.getInputStream().close();
		} catch (Exception e) {
			log.error("Error executing command: " + command, e);
		}
	}

}
