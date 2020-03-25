package org.rpi.radio.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.rpi.mplayer.CloseMe;

/**
 * Used to parse the TuneIn Radio URL. Returns an .m3u.
 * @author phoyle
 *
 */
public class ASHXParser {

	private Logger log = Logger.getLogger(ASHXParser.class);
	
	public LinkedList<String> getStreamingUrl(String url) {
		log.debug("Get URLs from : " + url);
		URLConnection conn = null;
		LinkedList<String> murls = new LinkedList<String>();
		try {
			conn = getConnection(url);
			return getStreamingUrl(conn);
		} catch (Exception e) {
			log.error("getStreamtingURL Exception. URL: " + url,e);
		} 
		finally {
			if(conn !=null) {
				try {
					if(conn.getInputStream() !=null) {
						CloseMe.close(conn.getInputStream());
					}
				}
				catch(Exception e) {
				}
				
				try {
					if(conn.getOutputStream() !=null) {
						CloseMe.close(conn.getOutputStream());
					}
				}
				catch(Exception e) {

				}
				
				conn = null;
			}
		}
		log.debug("Get URLs from : " + url + " Returning: " + murls.size());
		murls.add(url);
		return murls;
	}

	public LinkedList<String> getStreamingUrl(URLConnection conn) {

		final BufferedReader br;
		String murl = null;
		LinkedList<String> murls = new LinkedList<String>();
		try {
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while (true) {
				try {
					String line = br.readLine();

					if (line == null) {
						break;
					}
					murl = parseLine(line);
					if (murl != null && !murl.equals("")) {
						murls.add(murl);
					}
				} catch (IOException e) {
					log.error(e);
				}
			}
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		murls.add(conn.getURL().toString());
		return murls;
	}

	private String parseLine(String line) {
		if (line == null) {
			return null;
		}
		String trimmed = line.trim();
		if (trimmed.indexOf("http") >= 0) {
			return trimmed.substring(trimmed.indexOf("http"));
		}
		return "";
	}
	
	private URLConnection getConnection(String url) throws MalformedURLException, IOException
	{
		URLConnection mUrl = new URL(url).openConnection();
		return mUrl;
	}
}
