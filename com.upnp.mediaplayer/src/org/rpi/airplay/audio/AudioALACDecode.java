package org.rpi.airplay.audio;

/**
 * Used to decode an ALAC byte array
 */

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import org.apache.log4j.Logger;
import org.rpi.airplay.AudioSession;
import org.rpi.airplay.AudioSessionHolder;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;
import org.rpi.java.sound.IJavaSoundPlayer;

@Deprecated
public class AudioALACDecode extends MessageToMessageDecoder<ByteBuf> {

	private Logger log = Logger.getLogger(this.getClass());
	private AlacFile alacFile;
	private int frame_size = 0;
	private int[] outbuffer;
	private IJavaSoundPlayer audioQueue = null;

	public AudioALACDecode(IJavaSoundPlayer audioQueue) {
		AudioSession session = AudioSessionHolder.getInstance().getSession();
		alacFile = session.getAlac();
		frame_size = session.getFrameSize();
		outbuffer = new int[4 * (frame_size + 3)];
		this.audioQueue = audioQueue;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		try {
			//log.debug("test");
			
			final byte[] alacBytes = new byte[buffer.capacity() + 3];
			buffer.getBytes(0, alacBytes, 0, buffer.capacity());
			// Decode ALAC to PCM 
			int outputsize = 0;
			outputsize = AlacDecodeUtils.decode_frame(alacFile, alacBytes, outbuffer, outputsize);
			// Convert int array to byte array
			byte[] input = new byte[outputsize * 2];
			int j = 0;
			for (int ic = 0; ic < outputsize; ic++) {
				input[j++] = (byte) (outbuffer[ic] >> 8);
				input[j++] = (byte) (outbuffer[ic]);
			}
			
			AirPlayPacket packet = new AirPlayPacket();
			//packet.setALAC(buffer);
			packet.setAudio(input);
			if (audioQueue != null) {
				audioQueue.put(packet);
			}
		} catch (Exception e) {
			log.error("Error Decode ALAC", e);
		}
	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("Error AudioALACDecode",cause);
		ctx.close();
	}
	
	@Override	
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception{
		log.debug("Channel Registered: " + ctx.name());
		super.channelRegistered(ctx);
	}

	@Override
	public void channelActive( ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Actvie: " + ctx.name());
		super.channelActive(ctx);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		log.debug("Channel Inactive: " + ctx.name());
		super.channelInactive(ctx);
	};

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx ) throws Exception {
		log.debug("Channel Unregistered: " + ctx.name());
		super.channelUnregistered(ctx);
	}
}