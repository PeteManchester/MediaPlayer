package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport2;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventPlayListPlayingTrackID;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.playlist.CustomTrack;

public class PrvAVTransport extends DvProviderUpnpOrgAVTransport1 implements Observer {

	private Logger log = Logger.getLogger(PrvAVTransport.class);
	private String track_uri ="";
	private String track_metadata_html ="";
	private String track_metadata = "";
	private String track_time ="00:00:00";
	private String track_duration="00:00:00";
	private String mStatus="STOPPED";
	
	


	public PrvAVTransport(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating AvTransport");
		enablePropertyLastChange();
		createEvent();
		//setPropertyLastChange(intitialEvent());
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
		PlayManager.getInstance().observInfoEvents(this);
		PlayManager.getInstance().observTimeEvents(this);
		PlayManager.getInstance().observPlayListEvents(this);
		
	}
	

	
	private void createEvent()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<Event xmlns=\"urn:schemas-upnp-org:metadata-1-0/AVT/\">");
		sb.append("<InstanceID val=\"0\">");
		sb.append("	<CurrentPlayMode val=\"NORMAL\" />");
		sb.append("<RecordStorageMedium val=\"NOT_IMPLEMENTED\" />");
			sb.append("<CurrentTrackURI	val=\""+ track_uri + "\" />");
			sb.append("<CurrentTrackDuration val=\""+ track_duration + "\" />");
			sb.append("<CurrentRecordQualityMode val=\"NOT_IMPLEMENTED\" />");
			sb.append("<CurrentMediaDuration val=\"00:00:00\" />");
			sb.append("<AVTransportURI val=\"\" />");
			sb.append("<TransportState val=\""+ mStatus.toUpperCase() +"\" />");
			sb.append("<CurrentTrackMetaData val=\""+track_metadata_html +"\" />");
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
		log.debug("Transport Actions");
		return "Play,Pause,Stop,Seek,Next,Previous";
		//return "";
	}

	@Override
	protected GetDeviceCapabilities getDeviceCapabilities(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetDevice Capabilities");
		String cap = "vendor-defined ,NOT_IMPLEMENTED,NONE,NETWORK,MICRO-MV,HDD,LD,DAT,DVD-AUDIO,DVD-RAM,DVD-RW,DVD+RW,DVD-R,DVD-VIDEO,DVD-ROM,MD-PICTURE,MD-AUDIO,SACD,VIDEO-CD,CD-RW,CD-R,CD-DA,CD-ROM,HI8,VIDEO8,VHSC,D-VHS,S-VHS,W-VHS,VHS,MINI-DV,DV,UNKNOWN";
		String rec = " vendor-defined ,NOT_IMPLEMENTED,2:HIGH,1:MEDIUM,0:BASIC,2:SP,1:LP,0:EP";
		//GetDeviceCapabilities dev = new GetDeviceCapabilities("NONE,NETWORK,HDD,CD-DA,UNKNOWN", "NOT_IMPLEMENTED", "NOT_IMPLEMENTED");
		GetDeviceCapabilities dev = new GetDeviceCapabilities(cap, cap, rec);
		return dev;

	}

	@Override
	protected GetMediaInfo getMediaInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetMediaInfo");
		GetMediaInfo info = new GetMediaInfo(0, "0", track_duration, track_metadata, "", "", "UNKNOWN", "UNKNOWN", "UNKNOWN");
		return info;
	}

	@Override
	protected GetPositionInfo getPositionInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		//log.debug("Get Position Info");
		GetPositionInfo info = new GetPositionInfo(0, track_duration, track_metadata, track_uri, track_time, track_time, 2147483647, 2147483647);
		return info;
	}

	@Override
	protected GetTransportInfo getTransportInfo(IDvInvocation paramIDvInvocation, long paramLong) {
		//log.debug("GetTransport Info");
		GetTransportInfo info = new GetTransportInfo(mStatus.toUpperCase(), "OK", "1");
		return info;
	}

	@Override
	protected GetTransportSettings getTransportSettings(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("GetTransportSettings");
		GetTransportSettings settings = new GetTransportSettings("NORMAL", "0:BASIC");
		return settings;
	}

	@Override
	protected void next(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Next");
		PlayManager.getInstance().nextTrack();
	}

	@Override
	protected void pause(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Pause");
		PlayManager.getInstance().pause();
	}

	@Override
	protected void play(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("Play: " + paramString);
		PlayManager.getInstance().play();
	}

	@Override
	protected void previous(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Previous");
		PlayManager.getInstance().previousTrack();
	}

	@Override
	protected void record(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Record");
	}

	@Override
	protected void seek(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("Seek");
		PlayManager.getInstance().seekAbsolute(paramLong);
	}

	@Override
	protected void setAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("SetAVTransport: " + paramLong + " " + paramString1 + " " + paramString2);
	}

	@Override
	protected void setNextAVTransportURI(IDvInvocation paramIDvInvocation, long paramLong, String paramString1, String paramString2) {
		log.debug("SetNexAVTransport: " + paramLong + " " + paramString1 + " " + paramString2);
	}

	@Override
	protected void setPlayMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetPlayMode: " + paramLong + " " + paramString);
	}

	@Override
	protected void setRecordQualityMode(IDvInvocation paramIDvInvocation, long paramLong, String paramString) {
		log.debug("SetRecordQuality: " + paramLong + " " + paramString);
	}

	@Override
	protected void stop(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Stop");
		PlayManager.getInstance().stop();
	}

	@Override
	public void update(Observable arg0, Object ev) {
		EventBase e = (EventBase) ev;
		switch (e.getType()) {
		case EVENTTRACKCHANGED:
			EventTrackChanged ec = (EventTrackChanged) e;
			CustomTrack track = ec.getTrack();
			String m_uri = "";
			String m_metadata = "";
			if (track != null) {
				m_uri = track.getUri();
				m_metadata = track.getMetadata();
			} else {
				m_uri = "";
				m_metadata = "";
			}
			if(!(m_uri.equalsIgnoreCase(track_uri))||(!m_metadata.equalsIgnoreCase(track_metadata)))
			{
				track_uri = m_uri;
				track_metadata = m_metadata;
				track_metadata_html = stringToHTMLString(m_metadata);
				createEvent();
			}
			break;
		case EVENTTIMEUPDATED:
			EventTimeUpdate etime = (EventTimeUpdate) e;
			track_time  = ConvertTime(etime.getTime());
			//createEvent();
			break;
		case EVENTUPDATETRACKINFO:
			EventUpdateTrackInfo eti = (EventUpdateTrackInfo)e;
			track_duration = ConvertTime(eti.getTrackInfo().getDuration());
			createEvent();
			break;
		case EVENTPLAYLISTSTATUSCHANGED:
			EventPlayListStatusChanged eps = (EventPlayListStatusChanged)e;
			String status = eps.getStatus();
			if(status !=null)
			{
				if(status.equalsIgnoreCase("PAUSED"))
				{
					status = "PAUSED_PLAYBACK";
				}
				if(!mStatus.equalsIgnoreCase(status))
				{
					createEvent();
				}
				mStatus = status;			
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
		int seconds = (int)lTime;
		int hours = seconds / 3600;
	    int minutes = (seconds % 3600) / 60;
	    seconds = seconds % 60;

	    return twoDigitString(hours) + ":" + twoDigitString(minutes) + ":" + twoDigitString(seconds);
	}
	
	private String twoDigitString(int number) {

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

	    for (int i = 0; i < len; i++)
	        {
	        c = string.charAt(i);
	        if (c == ' ') {
	            // blank gets extra work,
	            // this solves the problem you get if you replace all
	            // blanks with &nbsp;, if you do that you loss 
	            // word breaking
	            if (lastWasBlankChar) {
	                lastWasBlankChar = false;
	                sb.append("&nbsp;");
	                }
	            else {
	                lastWasBlankChar = true;
	                sb.append(' ');
	                }
	            }
	        else {
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
	                if (ci < 160 )
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

}
