package org.rpi.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.rpi.plugingateway.PluginGateWay;

@Path("alarm")
public class AlarmRest {
	
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Path("setSleepTimer")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String setSleepTimer(@QueryParam("value") String value)
	{
		log.debug("Setting SleepTimer: " + value);
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(PluginGateWay.getInstance().setSleepTimer(value));
		} catch (Exception e) {
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("cancelSleepTimer")
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String cancelSleepTimer() {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(PluginGateWay.getInstance().cancelSleepTimer());
		} catch (Exception e) {
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("getSleepTimer")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getSleepTimer() {
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(PluginGateWay.getInstance().getSleepTimer());
		} catch (Exception e) {
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}

}
