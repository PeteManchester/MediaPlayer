package org.rpi.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.openhome.net.device.IDvInvocation;

public class Utils {

    private static Logger log = Logger.getLogger(Utils.class);
    private static Pattern pattern = Pattern.compile("(\\d{1}):(\\d{2}):(\\d{2}).(\\d{3})");

    private static PeriodFormatter pfWithSeconds = new PeriodFormatterBuilder()
            .printZeroAlways()
            .maximumParsedDigits(2)
            .minimumPrintedDigits(2)
            .appendHours()
            .appendSeparator(":")
            .maximumParsedDigits(2)
            .minimumPrintedDigits(2)
            .appendMinutes()
            .appendSeparator(":")
            .maximumParsedDigits(2)
            .minimumPrintedDigits(2)
            .appendSeconds()
            .appendSeparator(".")
            .printZeroRarelyFirst()
            .maximumParsedDigits(3)
            .appendMillis()
            .toFormatter();

    public static String getLogText(IDvInvocation paramIDvInvocation) {
        if (!log.isDebugEnabled())
            return "";
        String sp = " ";
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(sp);
            sb.append("Adapter: ");
            sb.append(getAdapterIP(paramIDvInvocation.getAdapter()));
            sb.append(sp);
            sb.append("uriPrefix: ");
            sb.append(paramIDvInvocation.getResourceUriPrefix());
            sb.append(sp);
            sb.append("Version:");
            sb.append(paramIDvInvocation.getVersion());
            sb.append(sp);
        } catch (Exception e) {
        }
        return sb.toString();
    }

    public static String getAdapterIP(int ip) {
        String ipStr = String.format("%d.%d.%d.%d", (ip >>> 24 & 0xff), (ip >>> 16 & 0xff), (ip >>> 8 & 0xff), (ip & 0xff));
        return ipStr;
    }

    /**
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean containsString(String source, ArrayList<String> target) {
        if (source == null || target == null)
            return false;
        for (String s : target) {
            if (containsString(source, s)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean containsString(String source, String target) {
        if (source.toUpperCase().contains(target.toUpperCase())) {
            return true;
        }
        return false;
    }

    public static String[] execute(String command) throws Exception {
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
        return list.toArray(new String[list.size()]);
    }

    /**
     * Checks if the given string is empty. Empty means, it is null, has no content or has not length.
     *
     * @param string
     * @return
     */
    public static boolean isEmpty(String string) {
        if (string == null || string.equals("") || string.length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Parses the given duration String and returns the milliseconds value.
     *
     * This method uses the statically initialized Formatter pf.
     *
     * @param duration
     * @return
     */
//    public static Long parseDurationString(String duration) {
//        Period result = Period.parse(duration, pfWithSeconds);
//        return result.toStandardDuration().getMillis();
//    }

    /**
     * Prints the given time value as a String
     *
     * @param time
     * @return
     */
    public static String printTimeString(Long time) {
    	try
    	{
	    	long seconds = TimeUnit.MILLISECONDS.toSeconds(time)
	                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(time));
	        long minutes = TimeUnit.MILLISECONDS.toMinutes(time)
	                - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(time));
	        long hours = TimeUnit.MILLISECONDS.toHours(time);
	
	        StringBuilder b = new StringBuilder();
	        b.append(hours == 0 ? "00" : hours < 10 ? String.valueOf("0" + hours) : 
	        String.valueOf(hours));
	        b.append(":");
	        b.append(minutes == 0 ? "00" : minutes < 10 ? String.valueOf("0" + minutes) : 
	        String.valueOf(minutes));
	            b.append(":");
	        b.append(seconds == 0 ? "00" : seconds < 10 ? String.valueOf("0" + seconds) : 
	        String.valueOf(seconds));
	        return b.toString(); 
    	}
    	catch(Exception e)
    	{
    		log.error("Error Converting mille to HH:mm:ss",e);
    	}
    	return "00:00:00";
    }
//    public static String printTimeString(Long time) {
//        Duration duration = new Duration(time.longValue());
//        return pfWithSeconds.print(duration.toPeriod());
//    }
    
    /**
     * Parses the given duration String and returns the milliseconds value.
     *
     * 
     *
     * @param duration
     * @return
     */
    public static long parseDurationString(String duration) {
    	try
    	{
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
	        Date date = sdf.parse("1970-01-01 " + duration);
	        return date.getTime();
    	}
    	catch(Exception e)
    	{
    		log.error("Error Converting HH:mm:ss to millis",e);
    	}
    	return 0;
    }

}
