package org.rpi.providers;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgInfo1;
import org.rpi.channel.ChannelBase;
import org.rpi.channel.ChannelPlayList;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventTrackChanged;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.utils.Utils;

import java.util.Observable;
import java.util.Observer;

public class PrvInfo extends DvProviderAvOpenhomeOrgInfo1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvInfo.class);
	
	private int meta_text_id = -99;

	public PrvInfo(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Info");

		enablePropertyTrackCount();
		enablePropertyDetailsCount();
		enablePropertyMetatextCount();
		enablePropertyUri();
		enablePropertyMetadata();
		enablePropertyDuration();
		enablePropertyBitRate();
		enablePropertyBitDepth();
		enablePropertySampleRate();
		enablePropertyLossless();
		enablePropertyCodecName();
		enablePropertyMetatext();

		setPropertyTrackCount(0);
		setPropertyDetailsCount(0);
		setPropertyMetatextCount(0);
		setPropertyUri("");
		setPropertyMetadata("");
		setPropertyDuration(0);
		setPropertyBitRate(0);
		setPropertyBitDepth(0);
		setPropertySampleRate(0);
		setPropertyLossless(false);
		setPropertyCodecName("");
		setPropertyMetatext("");

		enableActionCounters();
		enableActionTrack();
		enableActionDetails();
		enableActionMetatext();
		PlayManager.getInstance().observeInfoEvents(this);
	}

	/***
	 * Used to Set the Track
	 * 
	 * @param track
	 */
	private void setTrack(ChannelBase track) {
		try {
			if(track ==null)
				return;
			long trackCount = getPropertyTrackCount();
			trackCount++;
			propertiesLock();
			setPropertyTrackCount(trackCount);
			setPropertyUri(track.getUri());
			String metaData = track.getMetadata();
			//<DIDL-Lite xmlns="urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/"><item id="d55942-co887" parentID="co887" restricted="0"><dc:title xmlns:dc="http://purl.org/dc/elements/1.1/">Rendez-Vu</dc:title><dc:creator xmlns:dc="http://purl.org/dc/elements/1.1/">Basement Jaxx</dc:creator><dc:date xmlns:dc="http://purl.org/dc/elements/1.1/">1999-01-01</dc:date><upnp:artist role="AlbumArtist" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Basement Jaxx</upnp:artist><upnp:artist role="Composer" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Felix Buxton</upnp:artist><upnp:artist role="Composer" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Simon Ratcliffe</upnp:artist><upnp:artist role="Performer" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Basement Jaxx</upnp:artist><upnp:album xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Remedy</upnp:album><upnp:genre xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">Electronica</upnp:genre><upnp:albumArtURI dlna:profileID="JPEG_TN" xmlns:dlna="urn:schemas-dlna-org:metadata-1-0/" xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">http://192.168.1.205:26125/aa/29203/1957278084/cover.jpg?size=0</upnp:albumArtURI><upnp:originalTrackNumber xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">1</upnp:originalTrackNumber><res duration="0:05:44.000" size="5593088" bitrate="16213" bitsPerSample="16" sampleFrequency="44100" nrAudioChannels="2" protocolInfo="http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01">http://192.168.1.205:26125/content/c2/b16/f44100/d55942-co887.mp3</res><res duration="0:05:44.000" size="60850988" bitrate="176400" bitsPerSample="16" sampleFrequency="44100" nrAudioChannels="2" protocolInfo="http-get:*:audio/wav:DLNA.ORG_PN=WAV;DLNA.ORG_OP=01">http://192.168.1.205:26125/content/c2/b16/f44100/d55942-co887.forced.wav</res><res duration="0:05:44.000" size="60850944" bitrate="176400" bitsPerSample="16" sampleFrequency="44100" nrAudioChannels="2" protocolInfo="http-get:*:audio/L16:DLNA.ORG_PN=LPCM;DLNA.ORG_OP=01;DLNA.ORG_CI=1">http://192.168.1.205:26125/content/c2/b16/f44100/d55942-co887.forced.l16</res><res duration="0:05:44.000" size="5593088" bitrate="16213" bitsPerSample="16" sampleFrequency="44100" nrAudioChannels="2" protocolInfo="http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01">http://192.168.1.205:26125/content/c2/b16/f44100/d55942-co887.mp3</res><upnp:class xmlns:upnp="urn:schemas-upnp-org:metadata-1-0/upnp/">object.item.audioItem.musicTrack</upnp:class></item></DIDL-Lite>
			//String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id='d73430-co1811' parentID='co1811' restricted='0'><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'> dctitle</dc:title><dc:creator xmlns:dc='http://purl.org/dc/elements/1.1/'>dc creator</dc:creator><dc:date xmlns:dc='http://purl.org/dc/elements/1.1/'>1998-01-01</dc:date><upnp:artist role='AlbumArtist' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>AlbumArtest</upnp:artist><upnp:artist role='Composer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Ian Curtis</upnp:artist><upnp:artist role='Composer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Peter Hook</upnp:artist><upnp:artist role='Composer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Stephen Morris</upnp:artist><upnp:artist role='Composer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Bernard Sumner</upnp:artist><upnp:artist role='Performer' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Performer</upnp:artist><upnp:album xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Album</upnp:album><upnp:genre xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>Pop/Rock</upnp:genre><upnp:albumArtURI dlna:profileID='JPEG_TN' xmlns:dlna='urn:schemas-dlna-org:metadata-1-0/' xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>http://192.168.1.205:26125/aa/157341/656268630/cover.jpg?size=0</upnp:albumArtURI><upnp:originalTrackNumber xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>1</upnp:originalTrackNumber><upnp:originalDiscNumber xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>1</upnp:originalDiscNumber><upnp:originalDiscCount xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>4</upnp:originalDiscCount><res duration='0:02:52.000' size='30430100' bitrate='176400' bitsPerSample='16' sampleFrequency='44100' nrAudioChannels='2' protocolInfo='http-get:*:audio/wav:DLNA.ORG_PN=WAV;DLNA.ORG_OP=01'>http://192.168.1.205:26125/content/c2/b16/f44100/d73430-co1811.forced.wav</res><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem.musicTrack</upnp:class></item></DIDL-Lite>";
			setPropertyMetadata(metaData);
			setPropertyDetailsCount(0);	
			setPropertyMetatext(track.getMetaText());
//			if(!track.getMetaText().equalsIgnoreCase(""))
//			{
//				long meta_text_count = getPropertyMetatextCount();
//				meta_text_count++;
//				setPropertyMetatextCount(meta_text_count);
//				setPropertyMetatext(track.getMetaText());
//			}
			propertiesUnlock();
		} catch (Exception e) {
			log.error("Error: setTrack", e);
		}
	}

	public void setDetails(long duration, long bitRate, long bitDepth, long sampleRate, boolean lossLess, String codecName) {
		try {
			long detailsCount = getPropertyDetailsCount();
			detailsCount++;
			propertiesLock();
			setPropertyDetailsCount(detailsCount);
			setPropertyDuration(duration);
			setPropertyBitRate(bitRate * 1000);
			setPropertyBitDepth(bitDepth);
			setPropertySampleRate(sampleRate);
			setPropertyLossless(lossLess);
			setPropertyCodecName(codecName);
			propertiesUnlock();
		} catch (Exception e) {
			log.error("Error: setDetails", e);
		}
	}

	/***
	 * Used to Set the MetaText
	 * 
	 * @param meta_text
	 */
	public void setMetaText(String meta_text) {
		try {
			if (null != meta_text) {
				long meta_text_count = getPropertyMetatextCount();
				meta_text_count++;
				propertiesLock();
				setPropertyMetatextCount(meta_text_count);
				setPropertyMetatext(meta_text);
				propertiesUnlock();
			}
		} catch (Exception e) {
			log.error("Error SetMetaText: ", e);
		}
	}

	@Override
	protected Counters counters(IDvInvocation paramIDvInvocation) {
		log.debug("Counters" + Utils.getLogText(paramIDvInvocation));
		long trackCount = getPropertyTrackCount();
		long detailsCount = getPropertyDetailsCount();
		long metaextCount = getPropertyMetatextCount();
		Counters counters = new Counters(trackCount, detailsCount, metaextCount);
		//log.debug("Return counters: " + counters.toString());
		return counters;
	}

	@Override
	protected Track track(IDvInvocation paramIDvInvocation) {
		log.debug("track " + Utils.getLogText(paramIDvInvocation));
		String uri = getPropertyUri();
		String meta_data = getPropertyMetadata();
		Track track = new Track(uri, meta_data);
		log.debug("Return Track: " + track.toString());
		return track;
	}

	@Override
	protected Details details(IDvInvocation paramIDvInvocation) {
		log.debug("details " + Utils.getLogText(paramIDvInvocation));
		long duration = getPropertyDuration();
		long bitRate = getPropertyBitRate();
		long bitDepth = getPropertyBitDepth();
		long sampleRate = getPropertySampleRate();
		boolean lossless = getPropertyLossless();
		String codecName = getPropertyCodecName();
		Details details = new Details(duration, bitRate, bitDepth, sampleRate, lossless, codecName);
		log.debug("Return Details: " + details.toString());
		return details;
	}

	@Override
	protected String metatext(IDvInvocation paramIDvInvocation) {
		log.debug("metatext " + Utils.getLogText(paramIDvInvocation));
		String metaExt = getPropertyMetatext();
		log.debug("Return metatext: " + metaExt);
		return metaExt;
	}

	@Override
	public void update(Observable paramObservable, Object obj) {
		EventBase e = (EventBase)obj;
		switch(e.getType())
		{
		case EVENTUPDATETRACKINFO:
			EventUpdateTrackInfo euti = (EventUpdateTrackInfo)e;
			TrackInfo i = (TrackInfo) euti.getTrackInfo();
			setDetails(i.getDuration(), i.getBitrate(), i.getBitDepth(), i.getSampleRate(), false, i.getCodec());
			break;	

		case EVENTUPDATETRACKMETATEXT:
			EventUpdateTrackMetaText etm = (EventUpdateTrackMetaText)e;
			setMetaText(etm.getMetaText());
			break;
		case EVENTTRACKCHANGED:
			EventTrackChanged etc = (EventTrackChanged)e;
			setTrack(etc.getTrack());
			break;
		}
		
	}

    @Override
    public String getName() {
        return "Info";
    }
}
