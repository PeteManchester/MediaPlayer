package org.rpi.airplay;

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

public class AudioDecrpyt extends MessageToMessageDecoder<DatagramPacket> {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private Cipher cipher = null;

	private SecretKey m_aesKey =null;
	
	private IvParameterSpec paramSpec = null;
	
	public AudioDecrpyt()
	{
		initAES();
	}
	
	/**
	 * Initiate our decryption objects
	 */
	private void initAES()
	{
		try {
			paramSpec = new IvParameterSpec(AudioSessionHolder.getInstance().getSession().getAESIV());
			m_aesKey = new SecretKeySpec(AudioSessionHolder.getInstance().getSession().getAESKEY(), "AES");
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (Exception e) {
			log.error("Error initAES",e);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) throws Exception {
		try {
			ByteBuf buffer = msg.content();
			int type = buffer.getByte(1) & ~0x80;			
			if (type == 0x60 || type == 0x56) { // audio data / resend
				int audio_size = msg.content().readableBytes();
				int off = 12;
				if (type == 0x56) {
					off += 4;
				}
				audio_size -= off;
				ByteBuf audio = Unpooled.buffer(audio_size,audio_size);
				//ByteBuf audio = ByteBufAllocator.DEFAULT.buffer(audio_size, audio_size);
				buffer.getBytes(off, audio, 0, audio_size);
				cipher.init(Cipher.DECRYPT_MODE, m_aesKey, paramSpec);
				for(int i=0; (i + 16) <= audio.capacity(); i += 16) {
					byte[] block = new byte[16];
					audio.getBytes(i, block);
					block = cipher.update(block);
					audio.setBytes(i, block);					
				}
				//Return a ChannelBuffer
				out.add(audio.retain());
			}
		}catch(Exception e)
		{
			log.error("Error Decrypt Audio",e);
		}		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error(cause);
		ctx.close();
	}

}