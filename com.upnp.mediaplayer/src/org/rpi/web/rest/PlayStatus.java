package org.rpi.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.rpi.web.longpolling.PlayerStatus;

@Path("player")
public class PlayStatus {
	@Path("getStatus")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String getStatus() {
		try {
			return PlayerStatus.getInstance().getJSON();
		} catch (Exception e) {

		}
		return "{Error}";
	}
}
