package org.rpi.airplay;

import java.util.HashMap;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;


public class DecodeDMAP {

	//6D 6C 69 74 00 00 00 B2 6D 70 65 72 00 00 00 08 1F 6E 08 46 50 27 2C 1A 61 73 61 6C 00 00 00 16 54 68 65 20 49 6E 64 69 65 20 59 65 61 72 73 20 3A 20 31 39 38 30 61 73 61 72 00 00 00 09 4D 6F 2D 44 65 74 74 65 73 61 73 63 70 00 00 00 00 61 73 67 6E 00 00 00 0B 41 6C 74 65 72 6E 61 74 69 76 65 6D 69 6E 6D 00 00 00 0A 57 68 69 74 65 20 4D 69 63 65 61 73 74 6E 00 00 00 02 00 0B 61 73 74 63 00 00 00 02 00 0C 61 73 64 6E 00 00 00 02 00 01 61 73 64 63 00 00 00 02 00 01 61 73 64 6B 00 00 00 01 00 63 61 70 73 00 00 00 01 01 61 73 74 6D 00 00 00 04 00 03 56 C8 
	private Logger log = Logger.getLogger(this.getClass());
	private HashMap<String, String> attributes = new HashMap<String, String>();
	private int counter = 0;
	private int total_size = 0;

	public DecodeDMAP(ByteBuf buf) {
		//ByteBuf buf = buffer(178);
	    //byte[] by = new byte[] { (byte) 0x6D, (byte) 0x6C, (byte) 0x69, (byte) 0x74, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xB2, (byte) 0x6D, (byte) 0x70, (byte) 0x65, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x1F, (byte) 0x6E, (byte) 0x08, (byte) 0x46, (byte) 0x50, (byte) 0x27, (byte) 0x2C, (byte) 0x1A, (byte) 0x61, (byte) 0x73, (byte) 0x61, (byte) 0x6C, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x16, (byte) 0x54, (byte) 0x68, (byte) 0x65, (byte) 0x20, (byte) 0x49, (byte) 0x6E, (byte) 0x64, (byte) 0x69, (byte) 0x65, (byte) 0x20, (byte) 0x59, (byte) 0x65, (byte) 0x61, (byte) 0x72, (byte) 0x73, (byte) 0x20, (byte) 0x3A, (byte) 0x20, (byte) 0x31, (byte) 0x39, (byte) 0x38, (byte) 0x30, (byte) 0x61, (byte) 0x73, (byte) 0x61, (byte) 0x72, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x4D, (byte) 0x6F, (byte) 0x2D, (byte) 0x44, (byte) 0x65, (byte) 0x74, (byte) 0x74, (byte) 0x65, (byte) 0x73, (byte) 0x61, (byte) 0x73, (byte) 0x63, (byte) 0x70, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x61, (byte) 0x73, (byte) 0x67, (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0B, (byte) 0x41, (byte) 0x6C, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x6E, (byte) 0x61, (byte) 0x74, (byte) 0x69, (byte) 0x76, (byte) 0x65, (byte) 0x6D, (byte) 0x69, (byte) 0x6E, (byte) 0x6D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0A, (byte) 0x57, (byte) 0x68, (byte) 0x69, (byte) 0x74, (byte) 0x65, (byte) 0x20, (byte) 0x4D, (byte) 0x69, (byte) 0x63, (byte) 0x65, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0B, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x63, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x0C, (byte) 0x61, (byte) 0x73, (byte) 0x64, (byte) 0x6E, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x61, (byte) 0x73, (byte) 0x64, (byte) 0x63, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x61, (byte) 0x73, (byte) 0x64, (byte) 0x6B, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x63, (byte) 0x61, (byte) 0x70, (byte) 0x73, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x6D, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x03, (byte) 0x56, (byte) 0xC8 };
		//buf.writeBytes(by);
		//byte[] bmeta_data = new byte[4];
		counter = 4;
		String type = getStringValue(buf, 0, 4);
		System.out.println(type);
		if (type.equalsIgnoreCase("mlit")) {
			//total_size = buf.getInt(4);
			total_size = buf.capacity();
			counter += 4;
			while (counter +4 < total_size) {
				try {
					String key = getStringValue(buf, counter, 4);
					System.out.println(key);
					counter += 4;
					int size = buf.getInt(counter);
					counter += 4;
					String value = getStringValue(buf, counter, size);
					counter += size;
					System.out.println(value);
					attributes.put(key, value);
				} catch (Exception e) {
					log.error("Error While Loop: ", e);
				}
			}
			
			String Album = getValue("asal");
			String Artist =getValue("asar");
			String title  = getValue("minm");
			log.debug("Album: " + Album + " Artist: " + Artist + " title: " + title);
		}

	}
	
	public String getValue(String key)
	{
		String res = "";
		if(attributes.containsKey(key))
		{
			res = attributes.get(key);
		}
		return res;
	}

	private String getStringValue(ByteBuf buf, int start, int length) {
		String res = "";
		try {

			byte[] bytes = new byte[length];
			buf.getBytes(start, bytes, 0, length);
			res = new String(bytes);
		} catch (Exception e) {
			log.error("Erorr:getStringValue", e);
		}
		return res;
	}

}
