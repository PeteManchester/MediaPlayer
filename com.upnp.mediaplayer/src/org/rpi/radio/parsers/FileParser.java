package org.rpi.radio.parsers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import org.apache.log4j.Logger;

public class FileParser {
	
	private static Logger log = Logger.getLogger(FileParser.class);
	public String getURL(String url)
	{
		String uURL = url.toUpperCase();
		if(uURL.endsWith(".FLAC"))
		{
			return url;
		}
		else if(uURL.endsWith(".MP3"))
		{
			return url;
		}
		else if(uURL.endsWith(".WAV"))
		{
			return url;
		}
		else if (uURL.endsWith(".M4A"))
		{
			return url;
		}
		else if(uURL.endsWith(".PLS"))
		{
			PLSParser pls = new PLSParser();
			LinkedList<String> urls = pls.getStreamingUrl(url);
			if(urls.size()>0)
			{
				return urls.get(0);
			}
		}
		else if(uURL.endsWith(".M3U"))
		{
			M3UParser m3u = new M3UParser();
			LinkedList<String> urls = m3u.getStreamingUrl(url);
			if((urls.size()>0))
			{
				return urls.get(0);
			}
		}
		else if(uURL.endsWith(".ASX"))//|| url.toUpperCase().endsWith("WMA_UK_CONCRETE"))
		{
			ASXParser asx = new ASXParser(); 
			log.debug("ASX File: " + url);
			LinkedList<String> urls = asx.getStreamingUrl(url);
			if((urls.size()>0))
			{
				return urls.get(0);
			}
		}
		else
		{
			URLConnection conn = getConnection(url);
			if(conn!=null)
			{
				log.debug("URL: "  + url + " Headers: " + conn.getHeaderFields());
				String content_disp = conn.getHeaderField("Content-Disposition");
				log.debug(content_disp);

				if(content_disp !=null && content_disp.toUpperCase().endsWith("M3U"))
				{
					M3UParser m3u = new M3UParser();
					LinkedList<String> urls = m3u.getStreamingUrl(conn);
					if(urls.size()> 0)
					{
						return urls.getFirst();
					}
				}
				
				else if(conn.getContentType().toUpperCase().contains("AUDIO/X-SCPLS"))
				{
					PLSParser pls = new PLSParser();
					LinkedList<String> urls = pls.getStreamingUrl(conn);
					if(urls.size()> 0)
					{
						return urls.getFirst();
					}
				}
				else if(conn.getContentType().toUpperCase().contains("VIDEO/X-MS-ASF"))
				{
					ASXParser asx = new ASXParser(); 
					log.debug("ASX File: " + url);
					LinkedList<String> urls = asx.getStreamingUrl(url);
					if((urls.size()>0))
					{
						return urls.get(0);
					}
				}
				else if(conn.getContentType().toUpperCase().contains("AUDIO/MPEG"))
				{
					return url;
				}
				else
				{
					log.warn("##################Could Not Find File Type##########################");
					log.warn("URL: " + url + " Headers: " + conn.getHeaderFields());
					log.warn("####################################################################");
				}
			}
		}
		return url;
	}
	
	private URLConnection getConnection(String url)
	{
		URLConnection mUrl;
		try {
			mUrl = new URL(url).openConnection();
			return mUrl;
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

}
