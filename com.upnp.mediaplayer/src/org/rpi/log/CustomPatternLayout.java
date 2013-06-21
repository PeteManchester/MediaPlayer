package org.rpi.log;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.PatternConverter;
import org.apache.log4j.helpers.PatternParser;
import org.apache.log4j.spi.LoggingEvent;
import org.rpi.config.Config;



public class CustomPatternLayout extends Layout {

	private String header;
	
	public CustomPatternLayout()
    {
        this("%m%n");
        setHeader();
    }

    public CustomPatternLayout(String pattern)
    {
        sbuf = new StringBuffer(256);
        this.pattern = pattern;
        head = createPatternParser(pattern != null ? pattern : "%m%n").parse();
        setHeader();
    }

    public void setConversionPattern(String conversionPattern)
    {
        pattern = conversionPattern;
        head = createPatternParser(conversionPattern).parse();
    }

    public String getConversionPattern()
    {
        return pattern;
    }

    public void activateOptions()
    {
    }

    public boolean ignoresThrowable()
    {
        return true;
    }

    protected PatternParser createPatternParser(String pattern)
    {
        return new PatternParser(pattern);
    }

    public String format(LoggingEvent event)
    {
        if(sbuf.capacity() > 1024)
            sbuf = new StringBuffer(256);
        else
            sbuf.setLength(0);
        for(PatternConverter c = head; c != null; c = c.next)
            c.format(sbuf, event);

        return sbuf.toString();
    }

    public static final String DEFAULT_CONVERSION_PATTERN = "%m%n";
    public static final String TTCC_CONVERSION_PATTERN = "%r [%t] %p %c %x - %m%n";
    protected final int BUF_SIZE = 256;
    protected final int MAX_CAPACITY = 1024;
    private StringBuffer sbuf;
    private String pattern;
    private PatternConverter head;


	
	public String getHeader()
	{
		StringBuffer sb = new StringBuffer();
		String nl = System.getProperty("line.separator");
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int dstOffset = cal.getTimeZone().getDSTSavings();
		if(dstOffset>0)
		{
			dstOffset = dstOffset/3600000;
		}
		sb.append(header);
		sb.append("LocalTime :\t\t\t" +cal.getTime());
		sb.append(nl);
		sb.append("DST  :\t\t\t\tOffset = " + dstOffset);
		sb.append(nl);
		sb.append("Timezone :\t\t\t" + cal.getTimeZone().getID() + ", " +cal.getTimeZone().getDisplayName() + ", " + cal.getTimeZone().getDisplayName(true, 1));
		sb.append(nl);
		sb.append(nl);
		sb.append(nl);
		return sb.toString();
	}
	
	private void setHeader()
	{
		StringBuffer sb = new StringBuffer();
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int dstOffset = cal.getTimeZone().getDSTSavings();
		if(dstOffset>0)
		{
			dstOffset = dstOffset/3600000;
		}
		String nl = System.getProperty("line.separator");
		sb.append("Friendly Name :\t\t" + Config.friendly_name);
		sb.append(nl);
		sb.append("Version :\t\t\t" + Config.version);
		sb.append(nl);
		sb.append("Host name  : \t\t\t" + getHostName().trim());
		sb.append(nl);		
		sb.append("StartTime :\t\t\t" + Config.getStartTime());
		sb.append(nl);
		header = sb.toString();
	}
	
	private String getHostName() {
		try {
			java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
			return localMachine.getHostName();
		} catch (java.net.UnknownHostException uhe) {
			return "localhost";
		}
	}
	
	public void setHeader(String header)
	{
		this.header = header;
	}
}
