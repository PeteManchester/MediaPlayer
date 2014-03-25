package org.rpi.config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Level;
import org.rpi.utils.Utils;

public class Config {
	
	
	public static String friendly_name = "Default Room";
	public static List<String> playlists = new ArrayList<String>();
	public static String debug = "None";
	public static String mplayer_path= "/usr/bin/mplayer";
	public static boolean save_local_playlist = false;
    public static boolean startHttpDaemon = true;
	public static String version = "0.0.0.8";
	public static String logfile = "mediaplayer.log";
	public static int port = -99;
	public static int mplayer_cache = 500;
	public static int mplayer_cache_min = 70;
	public static String loglevel;
	public static String logconsole;
	public static int playlist_max = 1000;
	public static String mpd_host = "";
	public static int mpd_port = 6600;
	public static String player = "mpd";
	public static int mpd_preload_timer = 10;
	public static boolean enableAVTransport =true;
	public static boolean enableReceiver = true;
	public static String songcastNICName = "";
	public static String songcastSoundCardName = "";
	public static String radio_tunein_username = "";
	
	private static Calendar cal = Calendar.getInstance();
	public static boolean songcastLatencyEnabled = false;
	public static String webHttpPort = "8088";	
	
	public static String getProtocolInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append("http-get:*:audio/x-flac:*,");
		sb.append("http-get:*:audio/wav:*,");
		sb.append("http-get:*:audio/wave:*,");
		sb.append("http-get:*:audio/x-wav:*,");
		sb.append("http-get:*:audio/mpeg:*,");
		sb.append("http-get:*:audio/x-mpeg:*,");
		sb.append("http-get:*:audio/mp1:*,");
		sb.append("http-get:*:audio/aiff:*,");
		sb.append("http-get:*:audio/x-aiff:*,");
		sb.append("http-get:*:audio/x-m4a:*,");
		sb.append("http-get:*:audio/x-ms-wma:*,");
		sb.append("rtsp-rtp-udp:*:audio/x-ms-wma:*,");
		sb.append("http-get:*:audio/x-scpls:*,");
		sb.append("http-get:*:audio/x-mpegurl:*,");
		sb.append("http-get:*:audio/x-ms-asf:*,");
		sb.append("http-get:*:audio/x-ms-wax:*,");
		sb.append("http-get:*:audio/x-ms-wvx:*,");
		sb.append("http-get:*:text/xml:*,");
		sb.append("http-get:*:audio/aac:*,");
		sb.append("http-get:*:audio/aacp:*,");
		sb.append("http-get:*:audio/mp4:*,");
		sb.append("http-get:*:audio/ogg:*,");
		sb.append("http-get:*:audio/x-ogg:*,");
		sb.append("http-get:*:application/ogg:*,");
		sb.append("http-get:*:video/mpeg:*,");
		sb.append("http-get:*:video/mp4:*,");
		sb.append("http-get:*:video/quicktime:*,");
		sb.append("http-get:*:video/webm:*,");
		sb.append("http-get:*:video/x-ms-wmv:*,");
		sb.append("http-get:*:video/x-ms-asf:*,");
		sb.append("http-get:*:video/x-msvideo:*,");
		sb.append("http-get:*:video/x-ms-wax:*,");
		sb.append("http-get:*:video/x-ms-wvx:*,");
		sb.append("http-get:*:video/x-m4v:*,");
		sb.append("http-get:*:video/x-matroska:*,");
		sb.append("http-get:*:application/octet-stream:*");
		return sb.toString();
	}
	
	public static Level getLogFileLevel()
	{
		return getLogLevel(loglevel);
	}
	
	public static Level getLogConsoleLevel()
	{
		return getLogLevel(logconsole);
	}

    protected static Level getLogLevel(String s) {
        return Level.toLevel(s, Level.DEBUG);
    }

	public static void setSaveLocalPlayList(String property) {
		if(property.equalsIgnoreCase("TRUE"))
			save_local_playlist = true;
	}

    public static void setStartHttpDaemon(String property) {
        startHttpDaemon = convertStringToBoolean(property, true);
    }
	
	public static int convertStringToInt(String s)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch(Exception e)
		{
			
		}
		return -99;
	}

	public static int convertStringToInt(String s, int iDefault) {
		try
		{
			return Integer.parseInt(s);
		}
		catch(Exception e)
		{
			
		}
		return iDefault;
	}
	
	public static boolean convertStringToBoolean(String s, boolean bDefault)
	{
		if(s==null || s.equalsIgnoreCase(""))
			return bDefault;
		if(s.equalsIgnoreCase("TRUE"))
			return true;
		if(s.equalsIgnoreCase("YES"))
			return true;
		if(s.equalsIgnoreCase("1"))
			return true;
		return false;
	}
	
	public static void setStartTime()
	{
		try
		{
		Date date = new Date();
		cal.setTime(date);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Date getStartTime() {
		return cal.getTime();
	}
}
