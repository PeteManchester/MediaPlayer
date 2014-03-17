package org.rpi.providers;

import org.apache.log4j.Logger;
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
import org.rpi.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class PrvRadio extends DvProviderAvOpenhomeOrgRadio1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvRadio.class);

	private byte[] array = new byte[100];

	private List<ChannelRadio> channels = new ArrayList<ChannelRadio>();
	private int current_channel = -99;

	// "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='6000' nrAudioChannels='2' protocolInfo='http-get:*:audio/mpeg:DLNA.ORG_PN=MP3;DLNA.ORG_OP=01'>http://cast.secureradiocast.co.uk:8004/;stream.mp3</res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>http://www.mediauk.com/logos/100/226.png</upnp:albumArtURI></item></DIDL-Lite>";
	//private String metaData = "<DIDL-Lite xmlns='urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/'><item id=''><dc:title xmlns:dc='http://purl.org/dc/elements/1.1/'></dc:title><upnp:class xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'>object.item.audioItem</upnp:class><res bitrate='' nrAudioChannels='' protocolInfo=''></res><upnp:albumArtURI xmlns:upnp='urn:schemas-upnp-org:metadata-1-0/upnp/'></upnp:albumArtURI></item></DIDL-Lite>";

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
		setPropertyProtocolInfo(Config.getProtocolInfo());
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
		this.channels = channels;
		propertiesLock();
		UpdateIdArray();
		setPropertyIdArray(array);
		setPropertyChannelsMax(channels.size());
		propertiesUnlock();
		log.debug("Added Channels: " + channels.size());
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
		}
		else
		{
			log.debug("Current Channel " + current_channel + " Not Playing..");
		}
	};

	protected String protocolInfo(IDvInvocation paramIDvInvocation) {
		log.debug("protocolInfo"  + Utils.getLogText(paramIDvInvocation));
		return Config.getProtocolInfo();
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
		log.debug("SetChannel"  + Utils.getLogText(paramIDvInvocation));
		ChannelRadio channel = new ChannelRadio(uri, metadata, 2,"");
		channels.add(channel);
		UpdateIdArray();
	}
	

	@Override
	protected String readList(IDvInvocation paramIDvInvocation, String arg1) {
		log.debug("ReadList: " + arg1 + Utils.getLogText(paramIDvInvocation));
		int i = 0;
		log.debug("ReadList");
		StringBuilder sb = new StringBuilder();
		sb.append("<ChannelList>");
		for (ChannelRadio c : channels) {
			i++;
			sb.append(c.getFull_text());
		}
		sb.append("</ChannelList>");
		log.debug("ReadList Contains : " + i);
		return sb.toString();
	}

	@Override
	protected String read(IDvInvocation paramIDvInvocation, long id) {
		log.debug("Read: " + id  + Utils.getLogText(paramIDvInvocation));
		ChannelRadio c = channels.get((int) id);
		return c.getMetadata();
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
		//super.seekSecondAbsolute(paramIDvInvocation, paramLong);
	}

	@Override
	protected void seekSecondRelative(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("Seek Relative" + Utils.getLogText(paramIDvInvocation));
		//super.seekSecondRelative(paramIDvInvocation, paramInt);
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
	 * @param name
	 */
	private synchronized void getChannelByName(String name) {
		for (ChannelRadio c :channels)
		{
			if(c.getName().equalsIgnoreCase(name))
			{
				playChannel(c);
				break ;
			}
		}
	}
	
	/**
	 * Play the Channel
	 * @param c
	 */
	private void playChannel(ChannelRadio c)
	{
		current_channel = c.getId();
		log.debug("Play Found the Channel: " + current_channel);
		PlayManager.getInstance().playRadio(c);
	}

	/***
	 * Iterate all tracks, and create a 32 bit binary number from the track Id.
	 * Add the 32 bit binary string to a long string Split the 32 bit binary
	 * long string 4 bytes (8bits) And add to a byte array
	 */
	private void UpdateIdArray() {
		int size = channels.size() * 4;
		StringBuilder sb = new StringBuilder();
		byte[] bytes = new byte[size];
		int intValue = 1;
		for (ChannelRadio c : channels) {
			try {
				String binValue = Integer.toBinaryString(intValue);
				binValue = padLeft(binValue, 32, '0');
				sb.append(binValue);
			} catch (Exception e) {
				log.error(e);
			}
			intValue++;
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
			Byte sens = (byte) x.intValue();
			// byte b = Byte.parseByte(sByte, 2);
			bytes[i] = sens;
		}
		array = bytes;
		setPropertyIdArray(bytes);
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
		EventBase e = (EventBase)arg;
		switch(e.getType())
		{
		case EVENTRADIOSTATUSCHANGED:
			EventRadioStatusChanged ers = (EventRadioStatusChanged)e;
			setStatus(ers.getStatus());
			break;
		
		case EVENTRADIOPLAYINGTRACKID:
			EventRadioPlayingTrackID eri = (EventRadioPlayingTrackID)e;
			playingTrack(eri.getId());
			break;
		case EVENTRADIOPLAYNAME:
			EventRadioPlayName ern = (EventRadioPlayName)e;
			getChannelByName(ern.getName());
		}
	}

    @Override
    public String getName() {
        return "Radio";
    }


}
