package org.rpi.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;


@Path("mediaplayer")
public class MediaPlayerRest {
	
	
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Path("setStandbyMode")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String setStandbyMode(@QueryParam("value") String value)
	{
		log.debug("Setting StandbyMode: " + value);
		StringBuilder sb = new StringBuilder();
		try {
			boolean standby = false;
			if(value.equalsIgnoreCase("TRUE"))
			{
				standby = true;
			}
			PlayManager.getInstance().setStandby(standby);
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("getStandbyMode")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getStandbyMode()
	{
		log.debug("Get StandbyMode: " );
		StringBuilder sb = new StringBuilder();
		try {			
			sb.append(PlayManager.getInstance().isStandby());
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}


}
