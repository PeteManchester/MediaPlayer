package org.rpi.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.rpi.player.PlayManager;
import org.rpi.plugingateway.PluginGateWay;


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
	
	@Path("pauseTrack")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String pauseTrack()
	{
		log.debug("Setting PauseTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().pause();
			sb.append("OK");
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("playTrack")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String playTrack()
	{
		log.debug("Setting PlayTrack: ");
		StringBuilder sb = new StringBuilder();
		try {
			PlayManager.getInstance().play();
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
	
	@Path("changeSource")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String changeSource(@QueryParam("value") String source_name)
	{
		log.debug("Change Source: " + source_name);
		StringBuilder sb = new StringBuilder();
		try {
			String res = PluginGateWay.getInstance().setSourceByname(source_name);
			sb.append(res);
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	
	@Path("getPlayerStatus")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getPlayerStatus()
	{
		log.debug("Get PlayerStatus: " );
		StringBuilder sb = new StringBuilder();
		try {			
			sb.append(PlayManager.getInstance().getPlayerStatus());
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}
	
	@Path("getSourceName")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getSourceName()
	{
		log.debug("Get SourceName: " );
		StringBuilder sb = new StringBuilder();
		try {			
			sb.append(PluginGateWay.getInstance().getSourceName());
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
			sb.append("'pauseTrack' - Pauses the Current Track");
			sb.append(System.getProperty("line.separator"));
			sb.append("'playTrack' - Plays the Current Track, if Paused will resume, if track not playing will attempt to play Next Track ");
			sb.append(System.getProperty("line.separator"));
			sb.append("'incVolume' - Increase the Volume");
			sb.append(System.getProperty("line.separator"));
			sb.append("'decVolume' - Decrease the Volume");
			sb.append(System.getProperty("line.separator"));
			sb.append("'muteVolume?value=<true or false>' - Mute the Volume");
			sb.append(System.getProperty("line.separator"));
			sb.append("'changeSource?value=<name of source>' - Change the Source");
			sb.append(System.getProperty("line.separator"));
			sb.append("'getSourceName' - Source Name");
			sb.append(System.getProperty("line.separator"));
			sb.append("'getPlayerStatus' - Current Status of the Player");			
		} catch (Exception e) {
			sb.append("ERROR: " + e.getMessage());
			log.error("Error creating Status JSON",e);
		}
		return sb.toString();
	}


}
