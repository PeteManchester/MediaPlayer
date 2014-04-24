package org.rpi.airplay;

/**
 * Used to decrypt the AirPlay message
 */

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class AudioDecrpyt extends OneToOneDecoder {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private Cipher cipher = null;

	private SecretKey m_aesKey =null;
	
	private IvParameterSpec paramSpec = null;
	
	public AudioDecrpyt()
	{
		initAES();
	}

	@Override
	protected Object decode(ChannelHandlerContext context, Channel channel, Object msg) throws Exception {
		try {
			ChannelBuffer buffer = (ChannelBuffer) msg;
			int type = buffer.getByte(1) & ~0x80;
			int audio_size = buffer.capacity();
			if (type == 0x60 || type == 0x56) { // audio data / resend
				int off = 12;
				if (type == 0x56) {
					off += 4;
				}
				audio_size -= off;
				ChannelBuffer audio = ChannelBuffers.buffer(audio_size);
				buffer.getBytes(off, audio, 0, audio_size);
				cipher.init(Cipher.DECRYPT_MODE, m_aesKey, paramSpec);
				for(int i=0; (i + 16) <= audio.capacity(); i += 16) {
					byte[] block = new byte[16];
					audio.getBytes(i, block);
					block = cipher.update(block);
					audio.setBytes(i, block);
					
				}
				//Return a ChannelBuffer
				return audio;
			}
		}catch(Exception e)
		{
			log.error("Error Decrypt Audio",e);
		}
		return null;
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

}
