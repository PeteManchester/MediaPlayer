package org.rpi.songcast.ohu.sender.response;

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
	
	public OHUSenderAudioResponse(byte[] bytes) {
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (3 & 0xff) };
		//String codecName = "PCM   ";
		String codecName = "PCM   ";
		int codec_length = codecName.length();
		int audioSize = bytes.length;
		int length = header.length() + 1 + 1 + 2 + 50 + codec_length + audioSize ;
		//log.debug("AudioSize: " + audioSize + " Full Length: " + length);
		int sampleCount = bytes.length/4;
		buffer = Unpooled.directBuffer(length);
				
		buffer.writeBytes( header.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes( version);
		buffer.writeBytes( type);
		buffer.writeShort( length);
		
		buffer.writeBytes( new byte[] {(byte) (50 &0xff)});//Header Length
		buffer.writeBytes( new byte[] {(byte) (2 &0xff)});
		buffer.writeShort( sampleCount);
		
		buffer.writeInt(0);//FrameNumber
		buffer.writeInt( 0);//Network TimeStamp
		buffer.writeInt( 1);//MediaLatency
		buffer.writeInt( 1);//MediaTimestamp
		buffer.writeLong( 1);//Start Sample
		buffer.writeLong(4);//Total Samples
		//log.debug("SampleRate: " + test.readableBytes());
		buffer.writeInt( 44100);//SampleRate
		//log.debug("BitRate: " + test.readableBytes());
		buffer.writeInt(1411200);//BitRate
		//log.debug("VolumeOffset: " + test.readableBytes());
		buffer.writeShort( 0);//Volume Offset
		//log.debug("BitDepth: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (16 &0xff)});//BitDepth
		//log.debug("Channels: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (2 &0xff)});//Channels
		//log.debug("Reserved: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (0 &0xff)});//Reserved
		//log.debug("CodecNameLength: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (codec_length &0xff)});//CodecNameLength
		//test.writeShort(codec_length);
		buffer.writeBytes( codecName.getBytes(CharsetUtil.UTF_8));//CodecName
		
		buffer.writeBytes(bytes);

	}
	

	public OHUSenderAudioResponse(ByteBuf bytes) {
		this(bytes, 0);
	}


	public OHUSenderAudioResponse(ByteBuf bytes, int iCount) {
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (3 & 0xff) };
		//String codecName = "PCM   ";
		String codecName = "PCM   ";
		int codec_length = codecName.length();
		int audioSize = bytes.readableBytes();
		int length = header.length() + 1 + 1 + 2 + 50 + codec_length + audioSize ;
		//log.debug("AudioSize: " + audioSize + " Full Length: " + length);
		int sampleCount = bytes.readableBytes()/4;
		buffer = Unpooled.directBuffer();
				
		buffer.writeBytes( header.getBytes(CharsetUtil.UTF_8));
		buffer.writeBytes( version);
		buffer.writeBytes( type);
		buffer.writeShort( length);
		
		buffer.writeBytes( new byte[] {(byte) (50 &0xff)});//Header Length
		buffer.writeBytes( new byte[] {(byte) (2 &0xff)});
		buffer.writeShort( sampleCount);
		
		buffer.writeInt(iCount);//FrameNumber
		buffer.writeInt( 0);//Network TimeStamp
		buffer.writeInt( 1);//MediaLatency
		buffer.writeInt( 1);//MediaTimestamp
		buffer.writeLong( 1);//Start Sample
		buffer.writeLong(4);//Total Samples
		//log.debug("SampleRate: " + test.readableBytes());
		buffer.writeInt( 44100);//SampleRate
		//log.debug("BitRate: " + test.readableBytes());
		buffer.writeInt(1411200);//BitRate
		//log.debug("VolumeOffset: " + test.readableBytes());
		buffer.writeShort( 0);//Volume Offset
		//log.debug("BitDepth: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (16 &0xff)});//BitDepth
		//log.debug("Channels: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (2 &0xff)});//Channels
		//log.debug("Reserved: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (0 &0xff)});//Reserved
		//log.debug("CodecNameLength: " + test.readableBytes());
		buffer.writeBytes( new byte[] {(byte) (codec_length &0xff)});//CodecNameLength
		//test.writeShort(codec_length);
		buffer.writeBytes( codecName.getBytes(CharsetUtil.UTF_8));//CodecName
		
		buffer.writeBytes(convertLEtoBE(bytes));
		//buffer.writeBytes(bytes);
	}


	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
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
		if(buffer !=null) {
			try {
				if(buffer.capacity() > 16) {
					buffer.setInt(12, frameId);		
				}else {
					log.error("Could Not Set FrameId. BufferSize: " + buffer.capacity());
				}
				
			}
			catch(Exception e) {
				log.error("Error setFrameId. Buffer Size: " + buffer.readableBytes() ,e);
			}
			
		}		
	}

}
