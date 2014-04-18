package org.rpi.airplay;

import java.math.BigInteger;
import java.net.DatagramPacket;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Vector;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.rpi.alacdecoder.AlacDecodeUtils;
import org.rpi.alacdecoder.AlacFile;
import org.rpi.config.Config;
import org.rpi.mplayer.TrackInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventUpdateTrackInfo;
import org.rpi.songcast.core.AudioInformation;
import org.rpi.songcast.ohm.SourceTimer;

public class AudioEventQueue implements Runnable, Observer {
	private Logger log = Logger.getLogger(this.getClass());

	private Vector mWorkQueue = new Vector();
	private boolean run = true;
	private int frame_size = 4;
	public final int MAX_PACKET = 2048;
	private int lastSeqNo = 0;
	private AlacFile alac;
	private int[] outbuffer;
	private byte[] outbufferBytes;

	private SourceDataLine soundLine = null;
	private AudioFormat audioFormat = null;
	private DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);

	// private SecretKeySpec specKey;
	private Cipher cipher = null;
	private SecretKeySpec specKey = null;

	/**
	 * AES key
	 */
	private IvParameterSpec m_aesIv = null;

	private boolean bWrite = false;

	private boolean bFirstTime = true;

	private SourceTimer timer = null;
	private Thread timerThread = null;

	private AudioSession session = null;

	public AudioEventQueue(AudioSession session) {
		// this.session = session;
		sessionChanged();
		try {

			startTimer();

			if (soundLine != null) {
				close();
			}
			alac = session.getAlac();
			setAudioDevice();
			AudioSessionHolder.getInstance().addObserver(this);

			AudioInformation audioInf = new AudioInformation(44100, 48, 16, 2, "ALAC", 0, 0);
			log.debug("Creating Audio Format: " + audioInf.toString());
			audioFormat = new AudioFormat(audioInf.getSampleRate(), audioInf.getBitDepth(), audioInf.getChannels(), audioInf.isSigned(), audioInf.isBigEndian());
			info = new DataLine.Info(SourceDataLine.class, audioFormat, 16000);
			if (soundLine == null) {
				soundLine = (SourceDataLine) AudioSystem.getLine(info);
				soundLine.open(audioFormat);
				soundLine.start();
				bWrite = true;
			}
			bWrite = true;
			
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
			
		} catch (Exception e) {
			log.error("Error Opening Audio Line", e);
		}
	}

	/**
	 * Used to set the Songcast Audio Device
	 */
	private void setAudioDevice() {
		Properties props = System.getProperties();
		String name = "#" + Config.getInstance().getJavaSoundcardName();
		props.setProperty("javax.sound.sampled.SourceDataLine", name);
		log.warn("###Setting Sound Card Name: " + name);
	}

	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}

	public synchronized void put(Object object) {
		try {
			mWorkQueue.addElement(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized Object get() {
		Object object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}

	/**
	 * Peek to see if something is available.
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}

	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}

	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	@Override
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					DatagramPacket packet = (DatagramPacket) get();
					processEvent(packet);
				} catch (Exception e) {
					log.error(e);
				}
			} else {
				// log.debug("Empty Queue");
				sleep(1);
			}
		}

	}

	private void processEvent(DatagramPacket packet) {
		// 4*(this.getFrameSize()+3);
		try {
			int type = packet.getData()[1] & ~0x80;
			if (type == 0x60 || type == 0x56) { // audio data / resend
				// Add 4 bits for resend packet
				int off = 0;
				if (type == 0x56) {
					off = 4;
				}
				
				long seq =  new BigInteger(Utils.getBytes(2, 3, packet.getData())).longValue();
				long l = seq & 0xFFFFFFFFL;
				//log.debug("Seq: " + l);
				byte[] pktp = new byte[packet.getLength() - off - 12];
				for(int i=0; i<pktp.length; i++){
					pktp[i] = packet.getData()[i+12+off];
				}
				
				byte[] p = new byte[MAX_PACKET];

				// Init AES
				initAES();

				int i;
				for (i = 0; i + 16 <= pktp.length; i += 16) {
					// Decrypt
					this.decryptAES(pktp, i, 16, p, i);
				}

				// The rest of the packet is unencrypted
				for (int k = 0; k < (pktp.length % 16); k++) {
					p[i + k] = pktp[i + k];
				}
				//System.out.println("Size " + pktp.length);
				int outputsize = 0;
				try {
					outputsize = AlacDecodeUtils.decode_frame(session.getAlac(), p, outbuffer, outputsize);
					if (bWrite) {
						byte[] input = new byte[outbuffer.length*2];
						
						int j = 0;
						for(int ic=0; ic<outbuffer.length; ic++){
							input[j++] = (byte)(outbuffer[ic] >> 8);
							input[j++] = (byte)(outbuffer[ic]);
						}
						soundLine.write(input, 0, outputsize);
					}
				} catch (Exception e) {
					log.error("Error decoding",e);
				}

				assert outputsize == session.getFrameSize() * 4; // FRAME_BYTES length
				
				
				
//				// + les 12 (cfr. RFC RTP: champs a ignorer)
//				int packet_size = packet.getLength();
//				// byte[] pktp = Utils.getBytes(12 + off, packet_size,
//				// packet.getData());
//				ChannelBuffer pktp = ChannelBuffers.buffer(packet_size - (12 - off) + 1);
//
//				pktp.setBytes(0, Utils.getBytes((12 + off), packet_size, packet.getData()));
//
//				pktp = decode(pktp);
//				final byte[] alacBytes = new byte[pktp.capacity() + 3];
//				pktp.getBytes(0, alacBytes, 0, pktp.capacity());
//				int m_samplesPerFrame = session.getFrameSize();
//				final int[] pcmSamples = new int[m_samplesPerFrame * 2];
//				// log.debug("Buffer Size: " + pktp.capacity() + " OutputSize: "
//				// + pcmSamples.length);
//				try {
//					final int pcmSamplesBytes = AlacDecodeUtils.decode_frame(alac, alacBytes, pcmSamples, m_samplesPerFrame);
//					int lenBytes = convertSampleBufferToByteBuffer(pcmSamples, pcmSamplesBytes >> 1, outbufferBytes);
//					if (bWrite) {
//						soundLine.write(outbufferBytes, 0, lenBytes);
//					}
//
//				} catch (Exception e) {
//					//TODO Getting a lot of error when listening to certain streams, such as BBC radio
//					log.error("Error ALAC: Buffer Size: " + pktp.capacity() + " OutputSize: " + pcmSamples.length);
//				}
			}
		} catch (Exception e) {
			log.error("Error processEvent", e);
		}

	}

	/**
	 * Initiate the cipher
	 */
	private void initAES() {
		try {
			cipher.init(Cipher.DECRYPT_MODE, specKey, m_aesIv);
		} catch (Exception e) {
			log.error("Error initAES", e);
		}
	}

	/**
	 * New method to decode the encoded stream
	 * @param data
	 * @return
	 */
	private ChannelBuffer decode(ChannelBuffer data) {
		initAES();
		for (int i = 0; (i + 16) <= data.capacity(); i += 16) {
			byte[] block = new byte[16];
			data.getBytes(i, block);
			block = cipher.update(block);
			data.setBytes(i, block);
		}
		return data;
	}

	/**
	 * Good method Decrypt and decode the packet.
	 * 
	 * @param data
	 * @param outbuffer
	 *            the result
	 * @return
	 */
	private int alac_decode(byte[] data, int[] outbuffer) {
		byte[] packet = new byte[MAX_PACKET];
		// Init AES
		initAES();
		int i;
		for (i = 0; i + 16 <= data.length; i += 16) {
			this.decryptAES(data, i, 16, packet, i);
		}

		// The rest of the packet is unencrypted
		int k = 0;
		for (k = 0; k < (data.length % 16); k++) {
			packet[i + k] = data[i + k];
		}
		int outputsize = 0;
		try {
			outputsize = AlacDecodeUtils.decode_frame(alac, packet, outbuffer, outputsize);
			assert outputsize == frame_size * 4; // FRAME_BYTES length
		} catch (Exception e) {
			log.error("Error ALAC Decode. 'i': " + i + " k: " + k, e);
		}
		return outputsize;
	}

	/**
	 * Convert an int array to a byte array
	 * @param sampleBuffer
	 * @param len
	 * @param outbuffer
	 * @return
	 */
	private int convertSampleBufferToByteBuffer(int[] sampleBuffer, int len, byte[] outbuffer) {
		int j = 0;
		for (int i = 0; i < len; ++i) {
			int sample = sampleBuffer[i];
			outbuffer[j++] = (byte) (sample >> 8);
			outbuffer[j++] = (byte) sample;
		}
		return j;
	}

	/**
	 * Decrypt array from input offset with a length of inputlen and puts it in
	 * output at outputoffsest
	 * 
	 * @param array
	 * @param inputOffset
	 * @param inputLen
	 * @param output
	 * @param outputOffset
	 * @return
	 */
	private int decryptAES(byte[] array, int inputOffset, int inputLen, byte[] output, int outputOffset) {
		try {
			return cipher.update(array, inputOffset, inputLen, output, outputOffset);
		} catch (Exception e) {
			log.error("Error decryptAES", e);
		}

		return -1;
	}

	/**
	 * The session has changed to reset our values
	 */
	private void sessionChanged() {
		log.debug("Session Changed");
		session = AudioSessionHolder.getInstance().getSession();
		alac = session.getAlac();
		// frame_size = session.getFrameSize();
		frame_size = session.getFrameSize();
		outbuffer = new int[4 * (frame_size + 3)];
		outbufferBytes = new byte[outbuffer.length * 2];
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (Exception e) {
			log.error("Error Initiating Cipher", e);
		}
		m_aesIv = new IvParameterSpec(session.getAESIV());
		specKey = new SecretKeySpec(session.getAESKEY(), "AES");

		// initAES();
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		sessionChanged();
	}

	/**
	 * Close the SoundLine
	 */
	private void close() {
		try {
			if (soundLine != null) {
				soundLine.close();
				soundLine = null;
			}
		} catch (Exception e) {
			log.error("Error Closing Stream", e);
		}

	}

	private void startTimer() {
		timer = new SourceTimer();
		timerThread = new Thread(timer, "SongcastTimer");
		timerThread.start();
	}

	private void stopTimer() {
		if (timer != null) {
			timer.setRun(false);
			timer = null;
		}
		if (timerThread != null) {
			timerThread = null;
		}
	}
	
	public void stop() {
		stopTimer();
		bWrite = false;
		run = false;
		clear();
		close();
	}

}
