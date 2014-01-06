package org.rpi.radio.parsers;

import org.apache.log4j.BasicConfigurator;

public class TestParsers {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicConfigurator.configure();
		PLSParser pls = new PLSParser();
		//pls.getStreamingUrl("http://radio.linnrecords.com/cast/tunein.php/linnjazz/playlist.pls");
		M3UParser m3u = new M3UParser();
		//m3u.getStreamingUrl("http://media-ice.musicradio.com/CapitalManchesterMP3.m3u");
		//ASXParser asx = new ASXParser();
		//asx.getStreamingUrl("http://bbc.co.uk/radio/listen/live/bbcmanchester.asx");
		FileParser asx = new FileParser();
		//asx.getStreamingUrl("http://192.168.1.205:26125/content/c2/b24/f96000/d92628-co38099.flac");
		//asx.getStreamingUrl("http://192.168.1.205:26125/content/c2/b16/f44100/d78050-co1674.mp3");
		//asx.getURL("http://www.bbc.co.uk/mediaselector/4/asx/b039vd9f/iplayer_intl_stream_wma_uk_concrete");
		//asx.getURL("http://bbc.co.uk/radio/listen/live/bbcmanchester.asx");	
		//asx.getURL("http://webradio.radiomonitor.com/m3u/talksport");
		//asx.getStreamingUrl("http://gmgmp3.shoutcast.streamuk.com:10022/listen.pls");
		//asx.getStreamingUrl("http://media.on.net/radio/137.m3u");
		//asx.getURL("http://streamb.wgbh.org:8004/");
		//asx.getURL("http://streaming.radionz.co.nz/concert-mbr");
		asx.getURL("http://www.voanews.com/wm/live/radiodeewa.asx");
		
	}

}
