package org.rpi.airplay.audio;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.rpi.airplay.AudioSession;
import org.rpi.airplay.AudioSessionHolder;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;
import org.rpi.java.sound.AudioInformation;
import org.rpi.java.sound.IJavaSoundPlayer;
import org.rpi.java.sound.JavaSoundPlayerLatency;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ProcessAirplayAudio implements Runnable {

	private boolean isRunning = true;
	private Logger log = Logger.getLogger(this.getClass());

	private Cipher cipher = null;

	private SecretKey m_aesKey = null;

	private IvParameterSpec paramSpec = null;

	// private int last_sequence = 0;

	private int start_count = 0;

	private boolean delay_start_audio = false;

	private Queue<ByteBuf> queue = new ConcurrentLinkedDeque<ByteBuf>();

	private AlacFile alacFile;
	private int frame_size = 0;
	private int[] outbuffer;
	private IJavaSoundPlayer audioPlayer = null;
	private Thread threadPlayer = null;

	private int last_sequence = 0;
	private int missedCount = 0;
	private int maxMissed = 0;

	public ProcessAirplayAudio() {
		initAES();
		startAudioPlayer();
		AudioSession session = AudioSessionHolder.getInstance().getSession();
		alacFile = session.getAlac();
		frame_size = session.getFrameSize();
		outbuffer = new int[4 * (frame_size + 3)];
		// this.audioQueue = audioQueue;
	}

	private void startAudioPlayer() {
		audioPlayer = new JavaSoundPlayerLatency();
		threadPlayer = new Thread(audioPlayer, "AirplaySoundPlayer");

		AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);
		audioPlayer.createSoundLine(audioInf);
		threadPlayer.start();
		TrackInfo info = new TrackInfo();
		info.setBitDepth(audioInf.getBitDepth());
		info.setCodec(audioInf.getCodec());
		info.setBitrate(audioInf.getBitRate());
		info.setSampleRate((long) audioInf.getSampleRate());
		info.setDuration(0);
		EventUpdateTrackInfo ev = new EventUpdateTrackInfo();
		ev.setTrackInfo(info);
		if (ev != null) {
			PlayManager.getInstance().updateTrackInfo(ev);
		}
	}

	public void add(ByteBuf buf) {
		queue.add(buf.retain());
	}

	@Override
	public void run() {
		while (isRunning) {
			ByteBuf buffer = queue.poll();
			if (buffer != null) {
				decrypt(buffer);
			} else {
				try {
					TimeUnit.MILLISECONDS.sleep(50);
				} catch (Exception e) {
					log.error(e);
				}
			}
		}
	}

	private void decrypt(ByteBuf buffer) {
		try {
			int type = buffer.getByte(1) & ~0x80;
			if (type == 0x60 || type == 0x56) { // audio data / resend
				int audio_size = buffer.readableBytes();
				int sequence = buffer.getUnsignedShort(2);

				if (sequence - last_sequence != 1) {
					// log.debug("Missed Sequence: " + last_sequence + "
					// Sequence: " + sequence + " " + ((sequence -
					// last_sequence)-1));
					int missed = ((sequence - last_sequence) - 1);
					missedCount += missed;
					if (missed > maxMissed) {
						maxMissed = missed;
					}
				}
				last_sequence = sequence;

				if (sequence % 1000 == 0) {
					log.debug("" + sequence + " Missed sequences: " + missedCount + " Max: " + maxMissed);
					missedCount = 0;
					maxMissed = 0;
				}

				// if (sequence - last_sequence != 1) {
				// log.debug("Missed a Frame: " + sequence + " Last Frame: " +
				// last_sequence + " " + ((sequence - last_sequence) - 1));
				// }
				// last_sequence = sequence;
				long time_stamp = buffer.getUnsignedInt(4);
				// log.debug(sequence + " " + time_stamp);
				int off = 12;
				if (type == 0x56) {
					off += 4;
				}
				audio_size -= off;
				// Delay the start of playing Airplay whilst the CPU recovers
				// after decryption routines, not need for Pi2..
				if (delay_start_audio && start_count < 233) {
					start_count++;
					// log.fatal("Pausing AirPlay");
					ByteBuf test = Unpooled.buffer(audio_size, audio_size);
					decode(test);
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
				buffer.release();
				// AirPlayAudioHolder aph = new AirPlayAudioHolder(sequence,
				// audio);
				// audio.release();
				// out.add(aph);
				decode(audio);

			}
		} catch (Exception e) {
			log.error("Error Decrypt", e);
		}
	}

	private void decode(ByteBuf buffer) {
		try {
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
			packet.setAudio(input);
			if (audioPlayer != null) {
				audioPlayer.put(packet);
			}
		} catch (Exception e) {
			log.error("Error Decode", e);
		} finally {
			buffer.release();
		}

	}

	public void stop() {
		isRunning = false;
		try {
			if(audioPlayer !=null) {
				audioPlayer.stop();
				audioPlayer =null;				
			}
			threadPlayer = null;
		}
		 catch(Exception e) {
			 log.error("Error Stopping SoundPlayer", e);
		 }
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

	/***
	 * Clear the Audio Queue
	 */
	public void clear() {
		// TODO Auto-generated method stub

	}

}
