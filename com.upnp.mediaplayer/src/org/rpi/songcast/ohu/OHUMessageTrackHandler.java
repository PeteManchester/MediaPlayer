package org.rpi.songcast.ohu;

import org.apache.log4j.Logger;
import org.rpi.channel.ChannelSongcast;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackMetaText;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class OHUMessageTrackHandler extends SimpleChannelInboundHandler<OHUMessageTrack> {

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, OHUMessageTrack msg) throws Exception {
		log.debug("Track Message");
		try {
			EventUpdateTrackMetaText ev = new EventUpdateTrackMetaText();
			ChannelSongcast cs = new ChannelSongcast("", msg.getMetaData(), 1);
			String meta_text = msg.getMetaData();
			if (!meta_text.equalsIgnoreCase("")) {
				ev.setMetaText(msg.getMetaData());
				ev.setTitle(cs.getTitle());
				ev.setArtist(cs.getArtist());
				if (ev != null) {
					PlayManager.getInstance().updateTrackInfo(ev);
				}
			}
			else
			{
				log.debug("meta_text was Empty");
			}
			PlayManager.getInstance().setStatus("Playing", "SONGCAST");
			//msg.getData().release();
		} catch (Exception e) {
			log.error("Error Handling OHUMessageTrack: ", e);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
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
