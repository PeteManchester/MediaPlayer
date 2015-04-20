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
	
	@Path("nextTrack")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String nextTrack()
	{
		log.debug("Setting NextTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().nextTrack();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("previousTrack")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String previousTrack()
	{
		log.debug("Setting PreviousTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().previousTrack();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("stopTrack")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String stopTrack()
	{
		log.debug("Setting StopTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().stop();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("incVolume")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String incVolume()
	{
		log.debug("IncVolume: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().incVolume();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("decVolume")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String decVolume()
	{
		log.debug("Decreace Volume: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().decVolume();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("muteVolume")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String muteVolume(@QueryParam("value") String value)
	{
		log.debug("Mute Volume: ");
		StringBuilder sb = new StringBuilder();
		try {
			boolean mute = false;
			if(value.equalsIgnoreCase("TRUE"))
			{
				mute = true;
			}
			PlayManager.getInstance().setMute(mute);
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
	
	
	@Path("help")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getHelp()
	{
		log.debug("Setting PreviousTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			sb.append("Help for MediaPlayer Commands");
			sb.append(System.getProperty("line.separator"));
			sb.append("'setStandbyMode?value=<true or false>' - Set the StandyMode");
			sb.append(System.getProperty("line.separator"));
			sb.append("'getStandbyMode' - Get the Current StandbyMode");
			sb.append(System.getProperty("line.separator"));
			sb.append("'nextTrack' - Play the Next Track in Playlist");
			sb.append(System.getProperty("line.separator"));
			sb.append("'previousTrack' - Play the Previous Track, if track has played for more than 5 secs current track is started again");
			sb.append(System.getProperty("line.separator"));
			sb.append("'stopTrack' - Stop the Current Track");
			sb.append(System.getProperty("line.separator"));
			sb.append("'incVolume' - Increase the Volume");
			sb.append(System.getProperty("line.separator"));
			sb.append("'decVolume' - Decrease the Volume");
			sb.append(System.getProperty("line.separator"));
			sb.append("'muteVolume?value=<true or false>' - Mute the Volume");
			
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}


}
