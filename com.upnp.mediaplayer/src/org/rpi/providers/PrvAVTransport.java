package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.player.PlayManager;
import org.rpi.player.events.*;
import org.rpi.utils.Utils;

import java.util.Observable;
import java.util.Observer;

public class PrvAVTransport extends DvProviderUpnpOrgAVTransport1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvAVTransport.class);
	private String track_uri = "";
	private String track_metadata_html = "";
	private String track_metadata = "";
	private String track_time = "00:00:00";
	private String track_duration = "00:00:00";
	private String mStatus = "STOPPED";

	public PrvAVTransport(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating AvTransport");
		enablePropertyLastChange();
		// setPropertyLastChange(intitialEvent());
		createEvent();
		enableActionSetAVTransportURI();
		enableActionSetNextAVTransportURI();
		enableActionSetPlayMode();
		enableActionSetRecordQualityMode();

		enableActionGetCurrentTransportActions();
		enableActionGetDeviceCapabilities();
		enableActionGetMediaInfo();
		enableActionGetPositionInfo();
		enableActionGetTransportInfo();
		enableActionGetTransportSettings();
		enableActionSeek();
		enableActionGetCurrentTransportActions();

		enableActionNext();
		enableActionPause();
		enableActionPlay();
		enableActionPrevious();
		// enableActionRecord();

		// enableActionSetRecordQualityMode();
		enableActionStop();
		PlayManager.getInstance().observeInfoEvents(this);
		PlayManager.getInstance().observeTimeEvents(this);
		PlayManager.getInstance().observePlayListEvents(this);
		PlayManager.getInstance().observeAVEvents(this);

	}

	private void createEventState() {
		StringBuilder sb = new StringBuilder();
		sb.append("<Event xmlns = \"urn:schemas-upnp-org:metadata-1-0/AVT/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<TransportState val=\"" + mStatus.toUpperCase() + "\"/>");
		sb.append("</InstanceID>");
		sb.append("</Event>");
		setPropertyLastChange(sb.toString());
	}

	/**
	 * Create a big Event that sets all our variables
	 */
	/*
	 * private void createEvent() { StringBuilder sb = new StringBuilder();
	 * sb.append("<Event xmlns = \"urn:schemas-upnp-org:metadata-1-0/AVT/\">");
	 * sb.append("<InstanceID val=\"0\">");
	 * sb.append("	<CurrentPlayMode val=\"NORMAL\"/>");
	 * sb.append("<RecordStorageMedium val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<CurrentTrackURI	 val=\"" + track_uri + "\"/>");
	 * //sb.append("<CurrentTrackDuration val=\"" + track_duration + "\"/>");
	 * sb.append("<CurrentRecordQualityMode val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<CurrentMediaDuration val=\"" + track_time + "\"/>");
	 * sb.append("<AVTransportURI val=\"\"/>");
	 * sb.append("<TransportState val=\"" + mStatus.toUpperCase() + "\"/>");
	 * sb.append("<CurrentTrackMetaData val=\"" + track_metadata_html + "\"/>");
	 * sb.append("<NextAVTransportURI val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<PossibleRecordQualityModes val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<CurrentTrack val=\"0\" />");
	 * sb.append("<NextAVTransportURIMetaData val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<PlaybackStorageMedium val=\"NONE\"/>"); sb.append(
	 * "<CurrentTransportActions val=\"Play,Pause,Stop,Seek,Next,Previous\"/>");
	 * sb.append("<RecordMediumWriteStatus val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append
	 * ("<PossiblePlaybackStorageMedia val=\"NONE,NETWORK,HDD,CD-DA,UNKNOWN\"/>"
	 * ); sb.append("<AVTransportURIMetaData val=\"\"/>");
	 * sb.append("<NumberOfTracks val=\"0\" />");
	 * sb.append("<PossibleRecordStorageMedia val=\"NOT_IMPLEMENTED\"/>");
	 * sb.append("<TransportStatus val=\"OK\"/>");
	 * sb.append("<TransportPlaySpeed val=\"1\"/>"); sb.append("</InstanceID>");
	 * sb.append("</Event>"); setPropertyLastChange(sb.toString()); }
	 */

	private void createEvent() {
		StringBuilder sb = new StringBuilder();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("<CurrentPlayMode val=\"NORMAL\" />");
		sb.append("<RecordStorageMedium val=\"NOT_IMPLEMENTED\" />");
		// sb.append("<CurrentTrackURI	val=\"http://192.168.1.205:26125/content/c2/b16/f44100/d93277-co12.mp3\" />");
		sb.append("<CurrentTrackURI	val=\"" + track_uri + "\" />");
		sb.append("<CurrentTrackDuration val=\"" + track_duration + "\" />");
		sb.append("<CurrentRecordQualityMode val=\"NOT_IMPLEMENTED\" />");
		sb.append("<CurrentMediaDuration val=\"00:00:00\" />");
		sb.append("<AVTransportURI val=\"\" />");
		sb.append("<TransportState val=\"" + mStatus.toUpperCase() + "\" />");
		// sb.append("<CurrentTrackMetaData val=\"&lt;DIDL-Lite xmlns=&quot;urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/&quot;>&lt;item id=&quot;d93277-co12&quot; parentID=&quot;co12&quot; restricted=&quot;0&quot;>&lt;dc:title xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot;>Te Siento (Version espagnole de ''Ti Sento'')&lt;/dc:title>&lt;dc:creator xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot;>Matia Bazar&lt;/dc:creator>&lt;dc:date xmlns:dc=&quot;http://purl.org/dc/elements/1.1/&quot;>2011-01-01&lt;/dc:date>&lt;upnp:artist role=&quot;AlbumArtist&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>Matia Bazar&lt;/upnp:artist>&lt;upnp:artist role=&quot;Performer&quot; xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>Matia Bazar&lt;/upnp:artist>&lt;upnp:album xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>Fantasia - Best &amp;amp; Rarities&lt;/upnp:album>&lt;upnp:genre xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>Italian Pop&lt;/upnp:genre>&lt;upnp:originalTrackNumber xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>8&lt;/upnp:originalTrackNumber>&lt;upnp:originalDiscNumber xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>2&lt;/upnp:originalDiscNumber>&lt;res duration=&quot;0:04:06.000&quot; size=&quot;9855919&quot; bitrate=&quot;40006&quot; bitsPerSample=&quot;16&quot; sampleFrequency=&quot;44100&quot; nrAudioChannels=&quot;2&quot; protocolInfo=&quot;http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01&quot;>http://192.168.1.205:26125/content/c2/b16/f44100/d93277-co12.mp3&lt;/res>&lt;res duration=&quot;0:04:06.000&quot; size=&quot;43457948&quot; bitrate=&quot;176400&quot; bitsPerSample=&quot;16&quot; sampleFrequency=&quot;44100&quot; nrAudioChannels=&quot;2&quot; protocolInfo=&quot;http-get:*:audio/wav:DLNA.ORG_PN=WAV;DLNA.ORG_OP=01&quot;>http://192.168.1.205:26125/content/c2/b16/f44100/d93277-co12.forced.wav&lt;/res>&lt;res duration=&quot;0:04:06.000&quot; size=&quot;43457904&quot; bitrate=&quot;176400&quot; bitsPerSample=&quot;16&quot; sampleFrequency=&quot;44100&quot; nrAudioChannels=&quot;2&quot; protocolInfo=&quot;http-get:*:audio/L16:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01;DLNA.ORG_CI=1&quot;>http://192.168.1.205:26125/content/c2/b16/f44100/d93277-co12.forced.l16&lt;/res>&lt;res duration=&quot;0:04:06.000&quot; size=&quot;9855919&quot; bitrate=&quot;40006&quot; bitsPerSample=&quot;16&quot; sampleFrequency=&quot;44100&quot; nrAudioChannels=&quot;2&quot; protocolInfo=&quot;http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01&quot;>http://192.168.1.205:26125/content/c2/b16/f44100/d93277-co12.mp3&lt;/res>&lt;upnp:class xmlns:upnp=&quot;urn:schemas-upnp-org:metadata-1-0/upnp/&quot;>object.item.audioItem.musicTrack&lt;/upnp:class>&lt;/item>&lt;/DIDL-Lite>\" />");
		sb.append("<CurrentTrackMetaData val=\"" + track_metadata_html + "\" />");
		sb.append("<NextAVTransportURI val=\"NOT_IMPLEMENTED\" />");
		sb.append("<PossibleRecordQualityModes val=\"NOT_IMPLEMENTED\" />");
		sb.append("<CurrentTrack val=\"0\" />");
		sb.append("<NextAVTransportURIMetaData val=\"NOT_IMPLEMENTED\" />");
		sb.append("<PlaybackStorageMedium val=\"NONE\" />");
		sb.append("<CurrentTransportActions val=\"Play,Pause,Stop,Seek,Next,Previous\" />");
		sb.append("<RecordMediumWriteStatus val=\"NOT_IMPLEMENTED\" />");
		sb.append("<PossiblePlaybackStorageMedia val=\"NONE,NETWORK,HDD,CD-DA,UNKNOWN\" />");
		sb.append("<AVTransportURIMetaData val=\"\" />");
		sb.append("<NumberOfTracks val=\"1\" />");
		sb.append("<PossibleRecordStorageMedia val=\"NOT_IMPLEMENTED\" />");
		sb.append("<TransportStatus val=\"OK\" />");
		sb.append("<TransportPlaySpeed val=\"1\" />");
		sb.append("</InstanceID>");
		sb.append("</Event>");
		setPropertyLastChange(sb.toString());
	}

	@Override
	protected String getCurrentTransportActions(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Transport Actions" + Utils.getLogText(paramIDvInvocation));
		return "Play,Pause,Stop,Seek,Next,Previous";
		// return "";
	}

	@Override
	protected GetDeviceCapabilities getDeviceCapabilities(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetDevice Capabilities" + Utils.getLogText(paramIDvInvocation));
		String cap = "vendor-defined ,NOT_IMPLEMENTED,NONE,NETWORK,MICRO-MV,HDD,LD,DAT,DVD-AUDIO,DVD-RAM,DVD-RW,DVD+RW,DVD-R,DVD-VIDEO,DVD-ROM,MD-PICTURE,MD-AUDIO,SACD,VIDEO-CD,CD-RW,CD-R,CD-DA,CD-ROM,HI8,VIDEO8,VHSC,D-VHS,S-VHS,W-VHS,VHS,MINI-DV,DV,UNKNOWN";
		String rec = " vendor-defined ,NOT_IMPLEMENTED,2:HIGH,1:MEDIUM,0:BASIC,2:SP,1:LP,0:EP";
		// GetDeviceCapabilities dev = new
		// GetDeviceCapabilities("NONE,NETWORK,HDD,CD-DA,UNKNOWN",
		// "NOT_IMPLEMENTED", "NOT_IMPLEMENTED");
		GetDeviceCapabilities dev = new GetDeviceCapabilities(cap, cap, rec);
		return dev;

	}

	@Override
	protected GetMediaInfo getMediaInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetMediaInfo" + Utils.getLogText(paramIDvInvocation));
		// createEvent();
		GetMediaInfo info = new GetMediaInfo(0, track_duration, track_uri, track_metadata, "", "", "UNKNOWN", "UNKNOWN", "UNKNOWN");
		// GetMediaInfo info = new GetMediaInfo(0, "00:00:00", "", "", "", "",
		// "UNKNOWN", "UNKNOWN", "UNKNOWN");
		return info;
	}

	@Override
	protected GetPositionInfo getPositionInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Get Position Info" + Utils.getLogText(paramIDvInvocation));
		GetPositionInfo info = new GetPositionInfo(0, track_duration, track_metadata, track_uri, track_time, "00:00:00", 2147483647, 2147483647);
		// GetPositionInfo info = new GetPositionInfo(1, "00:00:00", "", "",
		// "00:00:00", "NOT_IMPLEMENTED", 2147483647, 2147483647);
		return info;
	}

	@Override
	protected GetTransportInfo getTransportInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetTransport Info" + Utils.getLogText(paramIDvInvocation));
		// createEvent();
		GetTransportInfo info = new GetTransportInfo(mStatus.toUpperCase(), "OK", "1");
		return info;
	}

	@Override
	protected GetTransportSettings getTransportSettings(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetTransportSettings" + Utils.getLogText(paramIDvInvocation));
		GetTransportSettings settings = new GetTransportSettings("NORMAL", "0:BASIC");
		return settings;
	}

	@Override
	protected void next(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Next: " + paramLong + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().nextTrack();
	}

	@Override
	protected void pause(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Pause" + paramLong + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().pause();
	}

	@Override
	protected void play(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("Play: " + paramString + Utils.getLogText(paramIDvInvocation));
		if (!track_uri.equalsIgnoreCase("")) {
			if (mStatus.equalsIgnoreCase("PAUSED_PLAYBACK")) {
				PlayManager.getInstance().play();
			} else {
				ChannelPlayList c = new ChannelPlayList(track_uri, track_metadata, 0);
				PlayManager.getInstance().playAV(c);
			}
		} else {
			if (mStatus.equalsIgnoreCase("PAUSED_PLAYBACK")) {
				PlayManager.getInstance().play();
			}
		}

	}

	@Override
	protected void previous(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Previous: " + paramLong + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().previousTrack();
	}

	@Override
	protected void record(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Record");
	}

	@Override
	protected void seek(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("Seek: " + paramString2 + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().seekAbsolute(getSeconds(paramString2));
	}

	private long getSeconds(String t) {
		// String timestampStr = "14:35:06";
		int duration = 0;
		try {
			String[] tokens = t.split(":");
			int hours = Integer.parseInt(tokens[0]);
			int minutes = Integer.parseInt(tokens[1]);
			int seconds = Integer.parseInt(tokens[2]);
			duration = 3600 * hours + 60 * minutes + seconds;
		} catch (Exception e) {

		}
		return (long) duration;
	}

	@Override
	protected void setAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String url, String meta_data) {
		log.debug("SetAVTransport: " + paramLong + " URL: " + url + " MetaData: " + meta_data + Utils.getLogText(paramIDvInvocation));
		track_uri = url;
		track_metadata = meta_data;
	}

	@Override
	protected void setNextAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("SetNexAVTransport: " + paramLong + " " + paramString1 + " " + paramString2 + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected void setPlayMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetPlayMode: " + paramLong + " " + paramString + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected void setRecordQualityMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetRecordQuality: " + paramLong + " " + paramString + Utils.getLogText(paramIDvInvocation));
	}

	@Override
	protected void stop(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Stop: " + paramLong + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().stop();
	}

	@Override
	public void update(Observable arg0, Object ev) {
		EventBase e = (EventBase) ev;
		switch (e.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged ec = (EventTrackChanged) e;
			ChannelBase track = ec.getTrack();
			String m_uri = "";
			String m_metadata = "";
			if (track != null) {
				m_uri = track.getUri();
				m_metadata = track.getMetadata();
				if (!(m_uri.equalsIgnoreCase(track_uri)) || (!m_metadata.equalsIgnoreCase(track_metadata))) {
					track_uri = m_uri;
					track_metadata = m_metadata;
					track_metadata_html = stringToHTMLString(m_metadata);
					createEvent();
				}
			} else {
				// m_uri = "";
				// m_metadata = "";
			}

			break;
		case EVENTTIMEUPDATED:
			EventTimeUpdate etime = (EventTimeUpdate) e;
			track_time = ConvertTime(etime.getTime());
			// createEvent();
			break;
		case EVENTUPDATETRACKINFO:
			EventUpdateTrackInfo eti = (EventUpdateTrackInfo) e;
			track_duration = ConvertTime(eti.getTrackInfo().getDuration());
			createEvent();
			break;
		// case EVENTPLAYLISTSTATUSCHANGED:
		// EventPlayListStatusChanged eps = (EventPlayListStatusChanged) e;
		// String status = eps.getStatus();
		// if (status != null) {
		// if (status.equalsIgnoreCase("PAUSED")) {
		// status = "PAUSED_PLAYBACK";
		// }
		// if (!mStatus.equalsIgnoreCase(status)) {
		// createEvent();
		// }
		// mStatus = status;
		// }
		case EVENTSTATUSCHANGED:
			EventStatusChanged esc = (EventStatusChanged) e;
			String statuss = esc.getStatus();
			log.debug("Status: " + statuss);
			if (statuss != null) {
				if (statuss.equalsIgnoreCase("PAUSED")) {
					statuss = "PAUSED_PLAYBACK";
				}
				if (statuss.equalsIgnoreCase("BUFFERING")) {
					statuss = "TRANSITIONING";
				}
				if (!mStatus.equalsIgnoreCase(statuss)) {
					mStatus = statuss;
					createEvent();
				}
			}
		default:
		}
	}

	/***
	 * Convert seconds to Hours:Seconds
	 * 
	 * @param lTime
	 * @return
	 */
	private String ConvertTime(long lTime) {
		int seconds = (int) lTime;
		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		seconds = seconds % 60;

		return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}

	private String twoDigitString(int number) {

		if (number < 0) {
			number = 0;
		}
		if (number == 0) {
			return "00";
		}

		if (number / 10 == 0) {
			return "0" + number;
		}

		return String.valueOf(number);
	}

	public static String stringToHTMLString(String string) {
		StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				if (c == '"')
					sb.append("&quot;");
				else if (c == '&')
					sb.append("&amp;");
				else if (c == '<')
					sb.append("&lt;");
				else if (c == '>')
					sb.append("&gt;");
				else if (c == '\n')
					// Handle Newline
					sb.append("&lt;br/&gt;");
				else {
					int ci = 0xffff & c;
					if (ci < 160)
						// nothing special only 7 Bit
						sb.append(c);
					else {
						// Not 7 Bit use the unicode system
						sb.append("&#");
						sb.append(new Integer(ci).toString());
						sb.append(';');
					}
				}
			}
		}
		return sb.toString();
	}

    @Override
    public String getName() {
        return "AVTransport";
    }

}
