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

public class PLSParser {
	private Logger log = Logger.getLogger(PLSParser.class);

	/***
	 * 
	 * @param url
	 * @return
	 */
	public LinkedList<String> getStreamingUrl(String url) {
		LinkedList<String> murls = new LinkedList<String>();
		URLConnection conn = null;
		try {
			conn = getConnection(url);
			return getStreamingUrl(conn);
		} catch (MalformedURLException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}finally {
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
		murls.add(url);
		return murls;
	}

	public LinkedList<String> getStreamingUrl(URLConnection conn) {
		BufferedReader br = null;
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
						log.debug("Adding URL: " + murl);
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
		finally {
			murls.add(conn.getURL().toString());
			if(br !=null) {
				CloseMe.close(br);
			}
			if(conn !=null){
				
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
		
		return murls;
	}

	private String parseLine(String line) {
		if (line == null) {
			return null;
		}
		String trimmed = line.trim();
		if (trimmed.indexOf("http") >= 0) {
			String res = trimmed.substring(trimmed.indexOf("http"));
			if(res.toUpperCase().endsWith("MSWMEXT=.ASF"))
			{
				log.debug("URL ends with MSWExt=.asf " + res);
				if(res.toUpperCase().startsWith("HTTP://"))
				{
					log.debug("URL ends with MSWExt=.asf " + res + " and starts wtih 'http://' " + res);
					res = "mmsh://" +  res.substring(7);
					log.debug("URL 'http://' with 'mmsh://' " + res);
				}
			}
			return res;
		}
		return "";
	}

	private URLConnection getConnection(String url) throws MalformedURLException, IOException {
		URLConnection mUrl = new URL(url).openConnection();
		return mUrl;
	}

}
