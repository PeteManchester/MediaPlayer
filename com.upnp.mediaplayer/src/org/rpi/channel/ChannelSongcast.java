package org.rpi.channel;

public class ChannelSongcast extends ChannelBase {
	
	public ChannelSongcast(String uri, String metadata, int id) {
		super(uri, metadata, id);
	}
	
	@Override
	public String getMetaText()
	{
		String text = super.getMetaText();
		
		if(text.equalsIgnoreCase(""))
		{
			return super.getMetadata();
		}
			return text;
	}
}
