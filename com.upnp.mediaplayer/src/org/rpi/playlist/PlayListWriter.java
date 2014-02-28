package org.rpi.playlist;

import java.io.PrintWriter;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.rpi.config.Config;

public class PlayListWriter extends Thread {

	private boolean save = false;
	private int counter = 0;
	private CopyOnWriteArrayList<CustomTrack> tracks = null;
	private Logger log = Logger.getLogger(PlayListWriter.class);

	public PlayListWriter() {
		this.setName("PlayListWriter");
	}

	public void trigger(CopyOnWriteArrayList<CustomTrack> tracks) {
		this.tracks = tracks;
		save = true;
		counter = 0;
	}

	private String getList() {
		int i = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<TrackList>");
		for (CustomTrack t : tracks) {
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
				if (save && Config.save_local_playlist) {
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
