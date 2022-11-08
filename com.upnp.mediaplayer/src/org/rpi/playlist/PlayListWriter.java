package org.rpi.playlist;

import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelPlayList;
import org.rpi.config.Config;

public class PlayListWriter extends Thread {

	private boolean save = false;
	private int counter = 0;
	private CopyOnWriteArrayList<ChannelPlayList> tracks = null;
	private Logger log = Logger.getLogger(PlayListWriter.class);

	public PlayListWriter() {
		this.setName("PlayListWriter");
	}

	public void trigger(CopyOnWriteArrayList<ChannelPlayList> tracks) {
		this.tracks = tracks;
		save = true;
		counter = 0;
	}
	
	/***
	 * Clear the tracks in the Playlist
	 */
	public void clearTracks() {
		log.info("Clearing the Playlist");
		try {
			if(tracks ==null) {
				tracks = new CopyOnWriteArrayList<ChannelPlayList>();
			}
			tracks.clear();
			save = true;
			counter=6;
		}
		catch(Exception e) {
			log.error("Error Clearing the Playlist",e);
		}		
	}

	private String getList() {
		int i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<TrackList>");
		for (ChannelPlayList t : tracks) {
			i++;
			sb.append(t.getFullText());
		}
		sb.append("</TrackList>");
		log.debug("PlayList Contains : " + i);
		return sb.toString();
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (save && Config.getInstance().isMediaplayerSaveLocalPlaylist()) {
					if (counter < 5) {
						counter++;
					} else {
						String xml = getList();
						log.debug("Saving PlayList: " );
						try {
							String s = new String(xml.getBytes(), "UTF-8");
							PrintWriter out = new PrintWriter("PlayList.xml");
							try {
								out.print(s);
							} finally {
								out.close();
							}
							save = false;
							log.debug("Saved PlayList");
						} catch (Exception e) {
							log.error("Error: SavePlayList", e);
						}
					}
				}
				Thread.sleep(1000);
			}

		} catch (Exception e) {
			log.error("Error: SavePlayList", e);
		}
	}

}
