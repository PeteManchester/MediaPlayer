package org.rpi.airplay;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

public class AudioChannelRequestHandler extends SimpleChannelUpstreamHandler {

	private AudioEventQueue audioQueue = null;
	
	private Cipher cipher = null;
	private SecretKey m_aesKey =null;

	public AudioChannelRequestHandler(AudioEventQueue audioQueue) {
		this.audioQueue = audioQueue;
		initAES();
	}
	
	private void initAES()
	{
		try {
			m_aesKey = new SecretKeySpec(AudioSessionHolder.getInstance().getSession().getAESKEY(), "AES");
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
		try {

			int type = buffer.getByte(1) & ~0x80;
			int audio_size = buffer.capacity();
			// int type = packet.getData()[1] & ~0x80;
			if (type == 0x60 || type == 0x56) { // audio data / resend
				// Add 4 bits for resend packet
				int off = 12;
				if (type == 0x56) {
					off += 4;
				}
				audio_size -= off;
				ChannelBuffer audio = ChannelBuffers.buffer(audio_size);
				buffer.getBytes(off, audio, 0, audio_size);
				//log.debug("BufferSize: " + buffer.capacity() + " AudioSize: " + audio.capacity());
				cipher.init(Cipher.DECRYPT_MODE, m_aesKey, new IvParameterSpec(AudioSessionHolder.getInstance().getSession().getAESIV()));
				for(int i=0; (i + 16) <= audio.capacity(); i += 16) {
					byte[] block = new byte[16];
					audio.getBytes(i, block);
					block = cipher.update(block);
					audio.setBytes(i, block);
					
				}
				
				if (audioQueue != null) {
					audioQueue.put(audio);
				}
			}

			
		} catch (Exception ex) {
			log.error("Error ", ex);
		}
		super.messageReceived(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		if (e.getCause() != null) {
			log.error("Error AudioChannelHandler", e.getCause());
		}
		e.getChannel().close();
	}

	@Override
	public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent evt) throws Exception {
		log.debug("AudioChannel Opened");
		super.channelOpen(ctx, evt);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		log.debug("AudioChannel Connected");
		super.channelConnected(ctx, e);
	};

	@Override
	public void channelClosed(final ChannelHandlerContext ctx, final ChannelStateEvent evt) throws Exception {
		log.debug("AudioChannel Closed");
	}
}
