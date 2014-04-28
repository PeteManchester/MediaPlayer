package org.rpi.airplay;

/**
 * Used to decode an ALAC byte array
 */

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import org.apache.log4j.Logger;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;

public class AudioALACDecode extends MessageToMessageDecoder<ByteBuf> {

	private Logger log = Logger.getLogger(this.getClass());
	private AlacFile alac;
	private int frame_size = 0;
	private int[] outbuffer;
	private AudioEventQueue audioQueue = null;

	public AudioALACDecode(AudioEventQueue audioQueue) {
		AudioSession session = AudioSessionHolder.getInstance().getSession();
		alac = session.getAlac();
		frame_size = session.getFrameSize();
		outbuffer = new int[4 * (frame_size + 3)];
		this.audioQueue = audioQueue;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
		try {
			final byte[] alacBytes = new byte[buffer.capacity() + 3];
			buffer.getBytes(0, alacBytes, 0, buffer.capacity());
			/* Decode ALAC to PCM */
			int outputsize = 0;
			outputsize = AlacDecodeUtils.decode_frame(alac, alacBytes, outbuffer, outputsize);
			// Convert int array to byte array
			byte[] input = new byte[outputsize * 2];
			int j = 0;
			for (int ic = 0; ic < outputsize; ic++) {
				input[j++] = (byte) (outbuffer[ic] >> 8);
				input[j++] = (byte) (outbuffer[ic]);
			}
			
			if (audioQueue != null) {
				audioQueue.put(input);
			}
			buffer.release();
			// Return a byte array
			//out.add(buffer);
		} catch (Exception e) {
			log.error("Error Decode ALAC", e);
		}
	
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}
}