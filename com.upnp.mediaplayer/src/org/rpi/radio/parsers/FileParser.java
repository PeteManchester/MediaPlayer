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
			log.debug("FLAC File: " + url);
			return url;
		}
		else if(uURL.endsWith(".MP3"))
		{
			log.debug("MP3 File: " + url);
			return url;
		}
		else if(uURL.endsWith(".WAV"))
		{
			log.debug("WAV File: " + url);
			return url;
		}
		else if (uURL.endsWith(".M4A"))
		{
			log.debug("M4A File: " + url);
			return url;
		}
		else if(uURL.endsWith(".PLS"))
		{
			log.debug("PLS File: " + url);
			PLSParser pls = new PLSParser();
			LinkedList<String> urls = pls.getStreamingUrl(url);
			if(urls.size()>0)
			{
				return urls.get(0);
			}
		}
		else if(uURL.endsWith(".M3U"))
		{
			log.debug("M3U File: " + url);
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
				log.debug("ContentDisposition:" + content_disp);
				String content_type = conn.getContentType();
				if(content_type !=null)
				{
					content_type = content_type.toUpperCase();
				}
				if(content_disp !=null && content_disp.toUpperCase().endsWith("M3U"))
				{
					log.debug("M3U File: " + url);
					M3UParser m3u = new M3UParser();
					LinkedList<String> urls = m3u.getStreamingUrl(conn);
					if(urls.size()> 0)
					{
						return urls.getFirst();
					}
				}
				
				else if(content_type != null && content_type.contains("AUDIO/X-SCPLS"))
				{
					log.debug("PLS File: " + url);
					PLSParser pls = new PLSParser();
					LinkedList<String> urls = pls.getStreamingUrl(conn);
					if(urls.size()> 0)
					{
						return urls.getFirst();
					}
				}
				else if(content_type != null && content_type.contains("VIDEO/X-MS-ASF"))
				{
					ASXParser asx = new ASXParser(); 
					log.debug("ASX File: " + url);
					LinkedList<String> urls = asx.getStreamingUrl(url);
					if((urls.size()>0))
					{
						return urls.get(0);
					}
					log.debug("ContentType was VIDEO/X-MS-ASF but could not parse .asx file, attempt to parse as .PLS File ");
					PLSParser pls = new PLSParser();
					urls = pls.getStreamingUrl(url);
					if((urls.size()>0))
					{
						return urls.get(0);
					}
				}
				else if(content_type != null && content_type.contains("AUDIO/MPEG"))
				{
					log.debug("MPEG File: " + url);
					return url;
				}
				else if (content_type != null && content_type.contains("AUDIO/X-MPEGURL"))
				{
					log.debug("M3U File: " + url);
					M3UParser m3u = new M3UParser();
					LinkedList<String> urls = m3u.getStreamingUrl(url);
					if((urls.size()>0))
					{
						return urls.get(0);
					}
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
