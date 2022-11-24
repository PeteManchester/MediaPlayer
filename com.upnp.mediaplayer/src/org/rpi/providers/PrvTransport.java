package org.rpi.providers;

import java.util.Observable;
import java.util.Observer;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgTransport1;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1.GetDeviceCapabilities;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1.GetMediaInfo;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1.GetPositionInfo;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1.GetTransportInfo;
import org.openhome.net.device.providers.DvProviderUpnpOrgAVTransport1.GetTransportSettings;
import org.rpi.channel.ChannelAV;
import org.rpi.channel.ChannelBase;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventPlayListStatusChanged;
import org.rpi.player.events.EventStatusChanged;
import org.rpi.player.events.EventTimeUpdate;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.Utils;



public class PrvTransport extends DvProviderAvOpenhomeOrgTransport1 implements Observer, IDisposableDevice {
	private Logger log = Logger.getLogger(this.getClass());
	private String track_uri = "";
	private String track_metadata_html = "";
	private String track_metadata = "";
	private String track_time = "00:00:00";
	private String track_duration = "00:00:00";
	private String mStatus = "STOPPED";
	private int id = 0;
	private String modes = "[\"Playlist\",\"Radio\",\"UpnpAv\",\"Receiver\"]";

	public PrvTransport(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Transport");
		enablePropertyTransportState();
		enablePropertyStreamId();
		enablePropertyCanPause();
		enablePropertyCanShuffle();
		enablePropertyCanSkipNext();
		enablePropertyCanSkipPrevious();
		enablePropertyCanRepeat();
		enablePropertyModes();
		enablePropertyCanSeek();
		enablePropertyRepeat();
		enablePropertyShuffle();


		// setPropertyLastChange(intitialEvent());
		createEvent();
		setPropertyStreamId(0);
		setPropertyCanPause(true);
		setPropertyCanRepeat(true);
		setPropertyCanShuffle(true);
		setPropertyCanSkipNext(true);
		setPropertyCanSkipPrevious(true);
		setPropertyModes(modes);
		setPropertyCanSeek(true);
		setPropertyRepeat(false);
		setPropertyShuffle(false);
		setPropertyTransportState("Stopped");
		

		enableActionSkipNext();
		enableActionPause();
		enableActionPlayAs();
		enableActionPlay();
		enableActionRepeat();
		enableActionSeekSecondAbsolute();
		enableActionSeekSecondRelative();
		enableActionModes();
		enableActionModeInfo();
		enableActionStreamInfo();
		enableActionTransportState();
		enableActionSkipPrevious();
		enableActionStop();

		//PlayManager.getInstance().observeInfoEvents(this);
		//PlayManager.getInstance().observeTimeEvents(this);
		PlayManager.getInstance().observePlayListEvents(this);
		PlayManager.getInstance().observeAVEvents(this);

	}
	
	private void createEvent() {
		setPropertyTransportState(mStatus);
		setPropertyStreamId(id);
	}
	
	@Override
	protected void stop(IDvInvocation paramIDvInvocation) {
		log.debug("Stop: " + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().stop();
	}
	
	@Override
	protected void play(IDvInvocation paramIDvInvocation) {
		log.debug("Play: " + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().play();
	}
	
	@Override
	protected void pause(IDvInvocation paramIDvInvocation) {
		log.debug("Pause: " + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().pause();
	}
	
	@Override
	protected void playAs(IDvInvocation paramIDvInvocation, String paramString1, String paramString2) {
		log.debug("PlayAs: " + "String1: " + paramString1 + " String2: " + paramString2 + Utils.getLogText(paramIDvInvocation));
	}
	
   @Override
   protected String modes(IDvInvocation paramIDvInvocation) {
	   log.debug("Modes: " + Utils.getLogText(paramIDvInvocation));
	   return getPropertyModes();	   
   }
   
   @Override
   protected String transportState(IDvInvocation paramIDvInvocation) {
	   log.debug("transportState: " + Utils.getLogText(paramIDvInvocation));
	   return mStatus;
   }
   
   @Override
   protected ModeInfo modeInfo(IDvInvocation paramIDvInvocation) {
	   log.debug("modeInfo: " + Utils.getLogText(paramIDvInvocation));
	   ModeInfo info = new ModeInfo(getPropertyCanSkipPrevious(), getPropertyCanSkipNext(), getPropertyCanRepeat(), getPropertyCanShuffle());
	   return info;
   }
   
   @Override
   protected StreamInfo streamInfo(IDvInvocation paramIDvInvocation) {
	   log.debug("streamInfo: " + Utils.getLogText(paramIDvInvocation));
	   StreamInfo info = new StreamInfo(getPropertyStreamId(), getPropertyCanSeek(), getPropertyCanPause());
	   return info;
   }
   
   @Override
   protected void skipNext(IDvInvocation paramIDvInvocation) {
	   log.debug("skipNext" +  Utils.getLogText(paramIDvInvocation));
	   PlayManager.getInstance().nextTrack();
   }
	
   @Override
   protected void skipPrevious(IDvInvocation paramIDvInvocation) {
	   log.debug("skipPrevious" +  Utils.getLogText(paramIDvInvocation));
	   PlayManager.getInstance().previousTrack();
   }
   
   @Override 
   protected void setRepeat(IDvInvocation paramIDvInvocation, boolean isRepeat) {
	   log.debug("setRepeat: " + isRepeat +   Utils.getLogText(paramIDvInvocation));
	   setPropertyRepeat(isRepeat);
	   PlayManager.getInstance().setRepeatPlayList(isRepeat);
   }
   
   protected boolean repeat(IDvInvocation paramIDvInvocation) {
	   log.debug("repeat" +  Utils.getLogText(paramIDvInvocation));
	   return getPropertyRepeat();
   }
   
   @Override 
   protected void setShuffle(IDvInvocation paramIDvInvocation, boolean isShuffle) {
	   log.debug("setShuffle: " + isShuffle +  Utils.getLogText(paramIDvInvocation));
	   setPropertyShuffle(isShuffle);
	   PlayManager.getInstance().setShuffle(isShuffle);
   }
   
   protected boolean shuffle(IDvInvocation paramIDvInvocation) {
	   log.debug("shuffle" +  Utils.getLogText(paramIDvInvocation));
	   return getPropertyShuffle();
   }
   
   protected long streamId(IDvInvocation paramIDvInvocation) {
	   log.debug("streamId" +  Utils.getLogText(paramIDvInvocation));
	   return getPropertyStreamId();
   }

	

	@Override
	public void update(Observable arg0, Object ev) {
		EventBase e = (EventBase) ev;
		switch (e.getType()) {
//		case EVENTTRACKCHANGED:
//			EventTrackChanged ec = (EventTrackChanged) e;
//			ChannelBase track = ec.getTrack();
//			String m_uri = "";
//			String m_metadata = "";
//			String m_track_metadata_html = ""; 
//			
//			if (track != null) {
//				m_uri = track.getUri();
//				m_metadata = track.updateTrackChange(ec);
//				m_track_metadata_html = stringToHTMLString(m_metadata);
//				if ((!(m_uri.equalsIgnoreCase(track_uri)) || (!m_metadata.equalsIgnoreCase(track_metadata))) || (!m_track_metadata_html.equalsIgnoreCase(track_metadata_html))) {
//					track_uri = m_uri;
//					track_metadata = m_metadata;
//					track_metadata_html = stringToHTMLString(m_metadata);
//					id++;
//					createEvent();
//				}
//			} else {
//				// m_uri = "";
//				// m_metadata = "";
//			}
//			break;
//		case EVENTUPDATETRACKMETATEXT:
//		    EventUpdateTrackMetaText tmc = (EventUpdateTrackMetaText) e;
//            track_metadata = tmc.getMetaText();
//            track_metadata_html = stringToHTMLString(track_metadata);
//            createEvent();
//		    break;
//		case EVENTTIMEUPDATED:
//			EventTimeUpdate etime = (EventTimeUpdate) e;
//			track_time = ConvertTime(etime.getTime());
//			// createEvent();
//			break;
//		case EVENTUPDATETRACKINFO:
//			try {
//				EventUpdateTrackInfo eti = (EventUpdateTrackInfo) e;
//				track_duration = ConvertTime(eti.getTrackInfo().getDuration());
//				createEvent();
//			} catch (Exception ex) {
//				log.error("Error EventUpdateTrackInfo", ex);
//			}
//			break;
		 case EVENTPLAYLISTSTATUSCHANGED:
		 EventPlayListStatusChanged eps = (EventPlayListStatusChanged) e;
		 String status = eps.getStatus();
		 if (status != null) {

		 if (!mStatus.equalsIgnoreCase(status)) {
			 mStatus = status;
			 createEvent();
		 }
		 mStatus = status;
		 }
		case EVENTSTATUSCHANGED:
			EventStatusChanged esc = (EventStatusChanged) e;
			String statuss = esc.getStatus();
			log.debug("Status: " + statuss);
			if (statuss != null) {
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
