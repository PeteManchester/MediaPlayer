package org.rpi.web.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

@Path("config")
public class ExecuteRest {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Path("execute")
	@POST
	@Produces("text/html; charset=utf-8")
	public Response execute(String command) {
		log.debug("Rest Request: " + command);
		return Response.status(200).entity("").build();
	}

}
