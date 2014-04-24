package org.rpi.airplay;

/**
 * Used to decode an ALAC byte array
 */

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;

public class AudioALACDecode extends OneToOneDecoder {

	private Logger log = Logger.getLogger(this.getClass());

	private AlacFile alac;
	private int frame_size = 0;
	private int[] outbuffer;

	public AudioALACDecode() {
		AudioSession session = AudioSessionHolder.getInstance().getSession();
		alac = session.getAlac();
		frame_size = session.getFrameSize();
		outbuffer = new int[4 * (frame_size + 3)];
	}

	@Override
	protected Object decode(ChannelHandlerContext context, Channel channel, Object msg) throws Exception {
		try {
			ChannelBuffer buffer = (ChannelBuffer) msg;
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
			// Return a byte array
			return input;
		} catch (Exception e) {
			log.error("Error Decode ALAC", e);
		}
		return null;
	}

}
