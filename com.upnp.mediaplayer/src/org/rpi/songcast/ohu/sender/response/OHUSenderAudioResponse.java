package org.rpi.songcast.ohu.sender.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/*
 * Join the OHM stream to start receiving audio stream.
 */

//Offset    Bytes                   Desc
//0         4                       "Ohm "
//4         1                       OhzHeader Major Version 1
//5         1                       Msg Type (1 Listen)
//6         2                       Total Bytes (Absolutely all bytes in the entire frame)
//8         1                       Msg Header Bytes (without the codec name)
//9         1                       AudioHeaderLength
//9         1                      Flags (lsb first: halt flag, lossless flag, timestamped flag all other bits 0)
//10         2                      Samples in this msg
//11         4                      Frame
//16         4                      Network timestamp
//20        4                       Media Latency
//24        4                       Media Timestamp
//28        8                       Sample Start (first sample's offset from the beginning of this track)
//36        8                       Samples Total (total samples for this track)
//44        4                       Sample Rate
//48        4                       Bit Rate
//52		2						Volume Offset
//55        1                       Bit depth of audio (16, 24)
//56        1                       Channels
//56        1                       Reserved (must be zero)
//57        1                       Codec Name Bytes
//58        n                       Codec Name
//58 + n    Msg Total Bytes - Msg Header Bytes - Code Name Bytes (Sample data in big endian, channels interleaved, packed)

public class OHUSenderAudioResponse {

	private String header = "Ohm ";
	//private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	private int audioSize = 0;
	private long audioLength = 0;
	private int bitRate = 1411200;


	public OHUSenderAudioResponse(ByteBuf bytes) {
		this(bytes, 0, 0);
	}

	public OHUSenderAudioResponse(ByteBuf bytes, int iCount, int latency) {
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (3 & 0xff) };
		// String codecName = "PCM ";
		String codecName = "PCM   ";
		int codec_length = codecName.length();
		audioSize = bytes.readableBytes();

		int length = header.length() + 1 + 1 + 2 + 50 + codec_length + audioSize;
		// log.debug("AudioSize: " + audioSize + " Full Length: " + length);
		int sampleCount = bytes.readableBytes() / 4;
		
		int flags = 0;
		
		boolean isLossLess = true;
		boolean isHalt = false;
		boolean isResent = false;
		
		if(isHalt) {
			flags |=1;
		}
		
		if(isLossLess) {
			flags |= 2;
		}
		
		if(latency > 0) {
			flags |= 4;
		}
		
		if(isResent) {
			flags |= 8;
		}
		
		double al = 0;
		if(audioSize > 0) {
			al = ((audioSize * 8)*1000)  / (bitRate) ;
		}
		audioLength =  (long)al * 1000;
		
		buffer = Unpooled.directBuffer();

		buffer.writeBytes(header.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes(version);
		buffer.writeBytes(type);
		buffer.writeShort(length);

		buffer.writeBytes(new byte[] { (byte) (50 & 0xff) });// Header Length
		buffer.writeBytes(new byte[] { (byte) (flags & 0xff) });// Flags
		buffer.writeShort(sampleCount);
		buffer.writeInt(iCount);// FrameNumber

		int now = (int)System.currentTimeMillis();

		buffer.writeInt(now);// Network TimeStamp
		buffer.writeInt(latency);// MediaLatency
		buffer.writeInt(now);// MediaTimestamp
		buffer.writeLong(0);// Start Sample
		buffer.writeLong(4);// Total Samples
		buffer.writeInt(44100);// SampleRate
		buffer.writeInt(bitRate);// BitRate
		buffer.writeShort(0);// Volume Offset
		buffer.writeBytes(new byte[] { (byte) (16 & 0xff) });// BitDepth
		buffer.writeBytes(new byte[] { (byte) (2 & 0xff) });// Channels
		buffer.writeBytes(new byte[] { (byte) (0 & 0xff) });// Reserved
		buffer.writeBytes(new byte[] { (byte) (codec_length & 0xff) });// CodecNameLength
		buffer.writeBytes(codecName.getBytes(CharsetUtil.UTF_8));// CodecName

		buffer.writeBytes(convertLEtoBE(bytes));
		
		
		// buffer.writeBytes(bytes);
	}

	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}
	
	/***
	 * Get the time in micro seconds that each audio packet lasts for
	 * @return
	 */
	public long getAudioLength() {
		return audioLength;
	}

	/***
	 * Convert a Little Endian Byte Array to a Big Endian Byte Array
	 * 
	 * @param in
	 * @return
	 */
	private ByteBuf convertLEtoBE(ByteBuf in) {
		int iSize = in.readableBytes();
		ByteBuf out = Unpooled.directBuffer(iSize);
		while (in.readableBytes() >= 8) {
			double i = in.readDoubleLE();
			out.writeDouble(i);
		}

		return out;
	}

	public void setFrameId(int frameId) {
		if (buffer != null) {
			try {
				if (buffer.capacity() > 16) {
					buffer.setInt(12, frameId);
				} else {
					//log.error("Could Not Set FrameId. BufferSize: " + buffer.capacity());
				}

			} catch (Exception e) {
				//log.error("Error setFrameId. Buffer Size: " + buffer.readableBytes(), e);
			}

		}
	}

}
