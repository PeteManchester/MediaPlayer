package org.rpi.songcast.ohu.receiver.handlers;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelSongcast;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackMetaText;
import org.rpi.songcast.ohu.receiver.messages.OHUMessageMetaText;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUMessageMetaTextHandler extends SimpleChannelInboundHandler<OHUMessageMetaText> {

	private Logger log  = Logger.getLogger(this.getClass());
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageMetaText msg) throws Exception {
		log.debug("MetaText");
		try {
			if(msg instanceof OHUMessageMetaText)
			{
				EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
				ChannelSongcast cs = new ChannelSongcast("", msg.getMetaText(), 1);
				String meta_text = msg.getMetaText();
				if (!meta_text.equalsIgnoreCase("")) {
					ev.setMetaText(msg.getMetaText());
					ev.setTitle(cs.getTitle());
					ev.setArtist(cs.getArtist());
					if (ev != null) {
						log.debug("PlayManager, UpdateTrackInfo: " + ev);
						PlayManager.getInstance().updateTrackInfo(ev);
					}
				}
				else
				{
					log.debug("meta_text was Empty");
				}
			}
		} catch (Exception e) {
			log.error("Error Releasing MetaText ByteBuf");
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Error. OHUMessageMetaTextHandler: ",cause);
		ctx.close();
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Unregistered: " + ctx.name());
		super.channelUnregistered(ctx);
	}

}
