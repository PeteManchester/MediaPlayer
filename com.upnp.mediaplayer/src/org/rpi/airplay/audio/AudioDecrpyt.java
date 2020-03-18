package org.rpi.airplay.audio;

/**
 * Used to decrypt the AirPlay message
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.rpi.airplay.AudioSessionHolder;
import org.rpi.config.Config;

public class AudioDecrpyt extends MessageToMessageDecoder<DatagramPacket> {

	private Logger log = Logger.getLogger(this.getClass());

	private Cipher cipher = null;

	private SecretKey m_aesKey = null;

	private IvParameterSpec paramSpec = null;

	//private int last_sequence = 0;

	private int start_count = 0;
	
	private boolean delay_start_audio = false;


	public AudioDecrpyt() {
		initAES();
		delay_start_audio = Config.getInstance().isAirPlayStartAudioDelayEnabled();
	}

	/**
	 * Initiate our decryption objects
	 */
	private void initAES() {
		try {
			paramSpec = new IvParameterSpec(AudioSessionHolder.getInstance().getSession().getAESIV());
			m_aesKey = new SecretKeySpec(AudioSessionHolder.getInstance().getSession().getAESKEY(), "AES");
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (Exception e) {
			log.error("Error initAES", e);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		try {
			ByteBuf buffer = msg.content();
			//log.debug(buffer.readableBytes());
			int type = buffer.getByte(1) & ~0x80;
			if (type == 0x60 || type == 0x56) { // audio data / resend
				int audio_size = msg.content().readableBytes();
				int sequence = buffer.getUnsignedShort(2);
				//if (sequence - last_sequence != 1) {
				//	log.debug("Missed a Frame: " + sequence + " Last Frame: " + last_sequence + "     " + ((sequence - last_sequence) - 1));
				//}
				//last_sequence = sequence;
				long time_stamp = buffer.getUnsignedInt(4);
				// log.debug(sequence + " " + time_stamp);
				int off = 12;
				if (type == 0x56) {
					off += 4;
				}
				audio_size -= off;
				//Delay the start of playing Airplay whilst the CPU recovers after decryption routines, not need for Pi2..				
				if (delay_start_audio && start_count < 233) {
					start_count++;
					//log.fatal("Pausing AirPlay");
					ByteBuf test = Unpooled.buffer(audio_size, audio_size);
					out.add(test.retain());
					return;
				}
				
				
				
				ByteBuf audio = Unpooled.buffer(audio_size, audio_size);
				// ByteBuf audio = ByteBufAllocator.DEFAULT.buffer(audio_size,
				// audio_size);
				buffer.getBytes(off, audio, 0, audio_size);
				cipher.init(Cipher.DECRYPT_MODE, m_aesKey, paramSpec);
				for (int i = 0; (i + 16) <= audio.capacity(); i += 16) {
					byte[] block = new byte[16];
					audio.getBytes(i, block);
					block = cipher.update(block);
					audio.setBytes(i, block);
				}
				
				AirPlayAudioHolder aph = new AirPlayAudioHolder(sequence, audio);
				//audio.release();				
				out.add(aph);
				
			}
		} catch (Exception e) {
			log.error("Error Decrypt Audio", e);
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