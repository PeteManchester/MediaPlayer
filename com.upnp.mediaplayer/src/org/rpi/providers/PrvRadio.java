package org.rpi.providers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.openhome.net.controlpoint.ProxyError;
import org.openhome.net.core.ErrorGeneral;
import org.openhome.net.device.ActionError;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgRadio1;
import org.rpi.channel.ChannelRadio;
import org.rpi.config.Config;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventRadioPlayName;
import org.rpi.player.events.EventRadioPlayingTrackID;
import org.rpi.player.events.EventRadioStatusChanged;
import org.rpi.radio.ChannelReaderJSON;
import org.rpi.radio.parsers.ASHXParser;
import org.rpi.utils.Utils;

public class PrvRadio extends DvProviderAvOpenhomeOrgRadio1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvRadio.class);

	private byte[] array = new byte[100];

	int counter_id = 0;

	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();
	private int current_channel = -99;
	private ChannelRadio current_channel_radio = null;
	private long last_updated = 0;

	// "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='6000' nrAudioChannels='2' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'>http://cast.secureradiocast.co.uk:8004/;stream.mp3</res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>http://www.mediauk.com/logos/100/226.png</upnp:albumArtURI></item></DIDL-Lite>";
	// private String metaData =
	// "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo=''></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

	public PrvRadio(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating CustomRadio");

		enablePropertyChannelsMax();
		enablePropertyId();
		enablePropertyIdArray();
		enablePropertyMetadata();
		enablePropertyProtocolInfo();
		enablePropertyTransportState();
		enablePropertyUri();

		setPropertyChannelsMax(0);
		setPropertyId(0);
		setPropertyIdArray(array);
		setPropertyMetadata("");
		setPropertyProtocolInfo(Config.getInstance().getProtocolInfo());
		setPropertyTransportState("Stopped");
		setPropertyUri("");

		enableActionChannel();
		enableActionChannelsMax();
		enableActionId();
		enableActionIdArray();
		enableActionIdArrayChanged();
		enableActionRead();
		enableActionPause();
		enableActionPlay();
		enableActionProtocolInfo();
		enableActionReadList();
		enableActionSeekSecondAbsolute();
		enableActionSeekSecondAbsolute();
		enableActionSeekSecondRelative();
		enableActionSetChannel();
		enableActionSetId();
		enableActionStop();
		enableActionTransportState();
		PlayManager.getInstance().observeRadioEvents(this);
	}

	/***
	 * Add a list of Radio Channels
	 * 
	 * @param channels
	 */
	public void addChannels(List<ChannelRadio> channels) {
		log.debug("Start of AddRadioChannels");
		propertiesLock();
		this.channels = channels;
		array = UpdateIdArray();
		setPropertyIdArray(array);
		setPropertyChannelsMax(channels.size());
		propertiesUnlock();
		log.debug("Added Radio Channels: " + channels.size());
		last_updated = System.currentTimeMillis();
	}

	protected Channel channel(IDvInvocation paramIDvInvocation) {
		log.debug("Channel" + Utils.getLogText(paramIDvInvocation));
		ChannelRadio c = channels.get(0);
		Channel channel = new Channel(c.getUri(), c.getMetadata());
		return channel;
	};

	@Override
	protected long channelsMax(IDvInvocation paramIDvInvocation) {
		log.debug("ChannelsMax" + Utils.getLogText(paramIDvInvocation));
		return getPropertyChannelsMax();
	}

	@Override
	protected long id(IDvInvocation paramIDvInvocation) {
		log.debug("Id" + Utils.getLogText(paramIDvInvocation));
		return getPropertyId();
	}

	@Override
	protected boolean idArrayChanged(IDvInvocation paramIDvInvocation, long arg1) {
		log.debug("idArrayChanged" + Utils.getLogText(paramIDvInvocation));
		return false;
	}

	protected void pause(IDvInvocation paramIDvInvocation) {
		log.debug("Pause" + Utils.getLogText(paramIDvInvocation));
	};

	protected void play(IDvInvocation paramIDvInvocation) {
		log.debug("Play" + Utils.getLogText(paramIDvInvocation));
		if (current_channel >= 0) {
			log.debug("Play Channel Set: " + current_channel);
			getChannelById();
		} else {
			log.debug("Current Channel " + current_channel + " Not Playing..");
			if(current_channel_radio !=null) {
				if (current_channel_radio.getId() == current_channel) {
					playChannel(current_channel_radio);
				}
			}
		}
	};

	protected String protocolInfo(IDvInvocation paramIDvInvocation) {
		log.debug("protocolInfo" + Utils.getLogText(paramIDvInvocation));
		return Config.getInstance().getProtocolInfo();
	};

	@Override
	protected IdArray idArray(IDvInvocation paramIDvInvocation) {
		log.debug("GetIdArray" + Utils.getLogText(paramIDvInvocation));
		byte[] array = getPropertyIdArray();
		DvProviderAvOpenhomeOrgRadio1.IdArray idArray = new IdArray(0, array);
		return idArray;
	}

	@Override
	protected void setChannel(IDvInvocation paramIDvInvocation, String uri, String metadata) {
		log.debug("SetChannel" + Utils.getLogText(paramIDvInvocation));		
		current_channel_radio = new ChannelRadio(uri,metadata,-99,"");
		current_channel = -99;
	}

	@Override
	protected String readList(IDvInvocation paramIDvInvocation, String arg1) {
		log.debug("ReadList: " + arg1 + Utils.getLogText(paramIDvInvocation));
		getChannels();
		return getList(arg1);
	}

	private String getList(String ids) {
		int i = 0;
		HashMap<String, String> trackIds = new HashMap<String, String>();
		for (String key : ids.split(" ")) {
			trackIds.put(key, key);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("<ChannelList>");
		for (ChannelRadio t : channels) {
			if (trackIds.containsKey("" + t.getId())) {
				i++;
				sb.append(t.getFullText());
			}
		}
		sb.append("</ChannelList>");
		log.debug("ReadList Contains : " + i + "  " + sb.toString());
		return sb.toString();
	}

	@Override
	protected String read(IDvInvocation paramIDvInvocation, long id) {
		log.debug("Read: " + id + Utils.getLogText(paramIDvInvocation));
		getChannels();
		for (ChannelRadio t : channels) {
			if (id == t.getId()) {
				log.debug("Read: " + id + " Returning : " + t.getFullText());
				return t.getMetadata();
			}
		}	
		log.debug("Read: " + id + " Could Not Find Radio Channel");
		ActionError ae = new ActionError("Id not found", 800);
		throw ae;
	}

	@Override
	protected String transportState(IDvInvocation paramIDvInvocation) {
		log.debug("TransportState" + Utils.getLogText(paramIDvInvocation));
		return getPropertyTransportState();
	}

	@Override
	protected void stop(IDvInvocation paramIDvInvocation) {
		log.debug("Stop" + Utils.getLogText(paramIDvInvocation));
		PlayManager.getInstance().stop();
	}

	@Override
	protected void seekSecondAbsolute(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Seek Absolute" + Utils.getLogText(paramIDvInvocation));
		// super.seekSecondAbsolute(paramIDvInvocation, paramLong);
	}

	@Override
	protected void seekSecondRelative(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("Seek Relative" + Utils.getLogText(paramIDvInvocation));
		// super.seekSecondRelative(paramIDvInvocation, paramInt);
	}

	@Override
	protected void setId(IDvInvocation paramIDvInvocation, long id, String uri) {
		log.debug("Set ID: " + id + " URI: " + uri + Utils.getLogText(paramIDvInvocation));
		current_channel = (int) id;
		getChannelById();
	}

	/**
	 * Find the Channel by Id
	 */
	private void getChannelById() {
		if (current_channel > 0) {
			for (ChannelRadio c : channels) {
				if (c.getId() == current_channel) {
					playChannel(c);
					break;
				}
			}
		}
	}

	/**
	 * Find the Channel by Name
	 * 
	 * @param name
	 */
	private synchronized void getChannelByName(String name) {
		for (ChannelRadio c : channels) {
			if (c.getName().equalsIgnoreCase(name)) {
				playChannel(c);
				break;
			}
		}
	}

	private void playNext() {
		if (channels.size() < 1) {
			return;
		}
		if (current_channel < 0) {

			playChannel(channels.get(0));
			return;
		}
		int i = 0;
		for (ChannelRadio c : channels) {
			if (current_channel == c.getId()) {
				break;
			}
			i++;
		}
		if(channels.size()<=i+1)
		{
			return;
		}
		ChannelRadio cr = channels.get(i + 1);
		playChannel(cr);
	}

	private void playPrevious() {
		if (channels.size() < 1) {
			return;
		}
		if (current_channel < 0) {
			playChannel(channels.get(0));
		}
		int i = 0;
		for (ChannelRadio c : channels) {
			if (current_channel == c.getId()) {
				break;
			}
			i++;
		}
		if (i - 1 < 0) {
			return;
		}

		ChannelRadio cr = channels.get(i - 1);
		playChannel(cr);

	}

	/**
	 * Play the Channel
	 * 
	 * @param c
	 */
	private void playChannel(ChannelRadio c) {
		current_channel = c.getId();
		log.debug("Play Found the Channel: " + current_channel);
		ASHXParser parser = new ASHXParser();
		if (c.getUri().toLowerCase().contains("opml.radiotime.com")) {
			log.debug("Radio URL contains 'opml.radiotime.com' Get the Correct URL: " + c.getUri());
			LinkedList<String> ashxURLs = parser.getStreamingUrl(c.getUri());
			if (ashxURLs.size() > 0) {
				c.setUri(ashxURLs.get(0));
			}
		}
		PlayManager.getInstance().playRadio(c);
	}

	/***
	 * Iterate all tracks, and create a 32 bit binary number from the track Id.
	 * Add the 32 bit binary string to a long string. Split the 32 bit binary
	 * long string 4 bytes (8bits) And add to a byte array
	 */
	private byte[] UpdateIdArray() {
		log.debug("Start of UpdateIdArray Radio");
		int size = channels.size() * 4;
		StringBuilder sb = new StringBuilder();
		byte[] bytes = new byte[size];
		for (ChannelRadio c : channels) {
			try {
				String binValue = Integer.toBinaryString(c.getId());
				// log.debug("Radio: " + c.getId());
				binValue = padLeft(binValue, 32, '0');
				// log.debug("Value " + c.getId() + " Bin Value: " + binValue);
				sb.append(binValue);
			} catch (Exception e) {
				log.error(e);
			}
		}
		// Now we have a big long string of binary, chop it up and get the
		// bytes for the byte array..
		String myBytes = sb.toString();
		int numOfBytes = myBytes.length() / 8;
		bytes = new byte[numOfBytes];
		for (int i = 0; i < numOfBytes; ++i) {
			int index = 8 * i;
			String sByte = myBytes.substring(index, index + 8);
			Integer x = Integer.parseInt(sByte, 2);
			// log.debug("Integer: " + x + " Bin: " + sByte);
			Byte sens = (byte) x.intValue();
			// log.debug("Integer: " + x + " Bin: " + sByte + " Byte: " + sens);
			bytes[i] = sens;
		}

		log.debug("End of UpdateIdArray Radio");
		return bytes;
	}

	private String padLeft(String str, int length, char padChar) {
		StringBuilder sb = new StringBuilder();

		for (int toPrepend = length - str.length(); toPrepend > 0; toPrepend--) {
			sb.append(padChar);
		}
		sb.append(str);
		return sb.toString();
	}

	private void playingTrack(int iD) {
		setPropertyId(iD);
	}

	private void setStatus(String status) {
		setPropertyTransportState(status);

	}

	@Override
	public void update(Observable o, Object arg) {
		EventBase e = (EventBase) arg;
		switch (e.getType()) {
		case EVENTRADIOSTATUSCHANGED:
			EventRadioStatusChanged ers = (EventRadioStatusChanged) e;
			setStatus(ers.getStatus());
			break;

		case EVENTRADIOPLAYINGTRACKID:
			EventRadioPlayingTrackID eri = (EventRadioPlayingTrackID) e;
			playingTrack(eri.getId());
			break;
		case EVENTRADIOPLAYNAME:
			EventRadioPlayName ern = (EventRadioPlayName) e;
			getChannelByName(ern.getName());
			break;
		case EVENTRADIOPLAYNEXT:
			playNext();
			break;
		case EVENTRADIOPLAYPREVIOUS:
			playPrevious();
			break;
		}
	}

	@Override
	public String getName() {
		return "Radio";
	}

	public void getChannels() {
		if ((System.currentTimeMillis() - last_updated) > 30000) {
			try {
				ChannelReaderJSON cr = new ChannelReaderJSON(this);
				last_updated = System.currentTimeMillis();
				Thread threadRadio = new Thread(cr, "RadioGetter");
				threadRadio.start();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

}
