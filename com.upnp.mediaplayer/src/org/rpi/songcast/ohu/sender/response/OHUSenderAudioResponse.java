package org.rpi.songcast.ohu.sender.response;

import java.nio.ByteOrder;

import org.apache.log4j.Logger;

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
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;

	public OHUSenderAudioResponse(int frameNumber, byte[] bytes) {
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (3 & 0xff) };
		String codecName = "PCM   ";
		int codec_length = codecName.length();
		int length = header.length() + 1 + 1 + 2 + 50 + codec_length + bytes.length  ;
		int sampleCount = bytes.length/4;
		ByteBuf test = Unpooled.buffer(length);
		//Header
		test.setBytes(0, header.getBytes(CharsetUtil.UTF_8));
		test.setBytes(4, version);
		test.setBytes(5, type);
		test.setShort(6, length);
		
		test.setBytes(8, new byte[] {(byte) (50 &0xff)});//Header Length
		test.setBytes(9, new byte[] {(byte) (2 &0xff)});
		test.setShort(10, sampleCount);
		test.setInt(12, frameNumber);
		
		test.setInt(16, 4321);
		test.setInt(20, 1);//MediaLatency
		test.setInt(24, 1);//MediaTimestamp
		test.setLong(28, 1);//Start Sample
		test.setLong(36, 4);//Total Samples
		test.setInt(44, 44100);//SampleRate
		test.setInt(48, 1411200);//BitRate
		test.setShort(52, 0);//Volume Offset
		test.setBytes(54, new byte[] {(byte) (16 &0xff)});//BitDepth
		test.setBytes(55, new byte[] {(byte) (2 &0xff)});//Channels
		test.setBytes(56, new byte[] {(byte) (0 &0xff)});//Reserved
		test.setBytes(57, new byte[] {(byte) (codec_length &0xff)});//CodecNameLength
		test.setBytes(58, codecName.getBytes(CharsetUtil.UTF_8));//CodecName
		//ByteBuf audio = Unpooled.buffer(bytes.length);
		
		test.setBytes(58 + codec_length,bytes);
		//test.order(ByteOrder.BIG_ENDIAN);

		buffer = Unpooled.copiedBuffer(test.array());
		test.release();
		//log.debug("OHU AudioResponse: " + frameNumber);
	}
	
	

	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

}
