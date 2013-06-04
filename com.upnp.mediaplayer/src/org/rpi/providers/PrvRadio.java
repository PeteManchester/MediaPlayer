package org.rpi.providers;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgRadio1;
import org.rpi.config.Config;
import org.rpi.playlist.PlayManager;
import org.rpi.radio.CustomChannel;

public class PrvRadio extends DvProviderAvOpenhomeOrgRadio1 {

	private Logger log = Logger.getLogger(PrvRadio.class);

	private byte[] array = new byte[100];

	private List<CustomChannel> channels = new ArrayList<CustomChannel>();
	private int current_channel = -99;
	private PlayManager iPlayer = PlayManager.getInstance();

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

		setPropertyChannelsMax(100);
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

	}

	/***
	 * Add a list of Radio Channels
	 * 
	 * @param channels
	 */
	public void addChannels(List<CustomChannel> channels) {
		this.channels = channels;
		UpdateIdArray();
		propertiesLock();
		setPropertyIdArray(array);
		setPropertyChannelsMax(channels.size());
		propertiesUnlock();
		log.debug("Added Channels: " + channels.size());
	}

//	public void aChannel(int id, String url, String metadata) {
//		CustomChannel channel = new CustomChannel(url, metadata, id,"");
//		channels.add(channel);
//	}

	protected Channel channel(org.openhome.net.device.IDvInvocation arg0) {
		log.debug("Channel");
		CustomChannel c = channels.get(0);
		Channel channel = new Channel(c.getUri(), c.getMetadata());
		return channel;
	};

	@Override
	protected long channelsMax(IDvInvocation arg0) {
		log.debug("ChannelsMax");
		return getPropertyChannelsMax();
	}

	@Override
	protected long id(IDvInvocation arg0) {
		log.debug("Id");
		return getPropertyId();
	}

	@Override
	protected boolean idArrayChanged(IDvInvocation arg0, long arg1) {
		log.debug("idArrayChanged");
		return false;
	}

	protected void pause(IDvInvocation arg0) {
		log.debug("Pause");
	};

	protected void play(IDvInvocation arg0) {
		log.debug("Play");
		if (current_channel >= 0) {
			log.debug("Play Channel Set: " + current_channel);
			playChannel();
		}
		else
		{
			log.debug("Current Channel " + current_channel + " Not Playing..");
		}
	};

	protected String protocolInfo(IDvInvocation arg0) {
		return Config.getProtocolInfo();
	};

	@Override
	protected IdArray idArray(IDvInvocation paramIDvInvocation) {
		log.debug("GetIdArray");
		byte[] array = getPropertyIdArray();
		DvProviderAvOpenhomeOrgRadio1.IdArray idArray = new IdArray(0, array);
		return idArray;
	}

	@Override
	protected void setChannel(IDvInvocation arg0, String uri, String metadata) {
		log.debug("SetChannel");
		CustomChannel channel = new CustomChannel(uri, metadata, 2,"");
		channels.add(channel);
		UpdateIdArray();
	}
	
	public synchronized CustomChannel getChannel(String name) {
		for (CustomChannel c :channels)
		{
			if(c.getName().equalsIgnoreCase(name))
				return c;
		}
		return null;
	}

	@Override
	protected String readList(IDvInvocation arg0, String arg1) {
		log.debug("ReadList: " + arg1);
		int i = 0;
		log.debug("ReadList");
		StringBuilder sb = new StringBuilder();
		sb.append("<ChannelList>");
		for (CustomChannel c : channels) {
			i++;
			sb.append(c.getFull_text());
		}
		sb.append("</ChannelList>");
		log.debug("ReadList Contains : " + i);
		return sb.toString();
	}

	@Override
	protected String read(IDvInvocation paramIDvInvocation, long id) {
		log.debug("Read: " + id);
		CustomChannel c = channels.get((int) id);
		return c.getMetadata();
	}

	@Override
	protected String transportState(IDvInvocation paramIDvInvocation) {
		log.debug("TransportState");
		return getPropertyTransportState();
	}

	@Override
	protected void stop(IDvInvocation paramIDvInvocation) {
		log.debug("Stop");
		iPlayer.stop();
	}

	@Override
	protected void seekSecondAbsolute(IDvInvocation paramIDvInvocation, long paramLong) {
		log.debug("Seek Absolute");
		super.seekSecondAbsolute(paramIDvInvocation, paramLong);
	}

	@Override
	protected void seekSecondRelative(IDvInvocation paramIDvInvocation, int paramInt) {
		log.debug("Seek Relative");
		super.seekSecondRelative(paramIDvInvocation, paramInt);
	}

	@Override
	protected void setId(IDvInvocation paramIDvInvocation, long id, String uri) {
		log.debug("Set ID: " + id + " URI: " + uri);
		current_channel = (int) id;
		playChannel();
	}

	private void playChannel() {
		if (current_channel > 0) {
			for (CustomChannel c : channels) {
				if (c.getId() == current_channel) {
					log.debug("Play Found the Channel: " + current_channel);
					iPlayer.playFile(c);
				}
			}
		}
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
		for (CustomChannel c : channels) {
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

	public void playingTrack(int iD) {
		setPropertyId(iD);
	}

	public void setStatus(String status) {
		setPropertyTransportState(status);

	}



}
