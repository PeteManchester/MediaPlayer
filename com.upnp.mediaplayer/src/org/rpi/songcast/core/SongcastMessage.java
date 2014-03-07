package org.rpi.songcast.core;

import java.io.UnsupportedEncodingException;
import java.util.Observable;

import org.rpi.player.events.EventBase;
import org.rpi.songcast.events.EventSongCastBase;

public class SongcastMessage {

	public byte[] data = null;
	

	public String convertHexToString(String hex) {

		StringBuilder sb = new StringBuilder();
		StringBuilder temp = new StringBuilder();

		// 49204c6f7665204a617661 split into two characters 49, 20, 4c...
		for (int i = 0; i < hex.length() - 1; i += 2) {

			// grab the hex in pairs
			String output = hex.substring(i, (i + 2));
			// convert hex to decimal
			int decimal = Integer.parseInt(output, 16);
			// convert the decimal to character
			sb.append((char) decimal);

			temp.append(decimal);
		}
		System.out.println("Decimal : " + temp.toString());

		return sb.toString();
	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	public String DecToHex(int number, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toHexString(number));
		while (sb.length() < length) {
			sb.insert(0, '0'); // pad with leading zero if needed
		}
		String hex = sb.toString();
		return hex;
	}

	public byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}

	public String stringToHex(String string) {
		StringBuilder buf = new StringBuilder(200);
		for (char ch : string.toCharArray()) {
			//if (buf.length() > 0)
				//buf.append(' ');
			buf.append(String.format("%02x", (int) ch));
		}
		return buf.toString();
	}
	
	/*
	 * 
	 * Get a portion of the bytes in the array.
	 */
	public byte[] getBytes(int start, int end)
	{
		int size = (end - start) + 1;
		int count = 0;
		byte[] res = new byte[size];
		for(int i = start;i<=end;i++)
		{
			res[count] = data[i];
			count++;
		}
		return res;
	}
	
	public byte[] shortToByteArray(short data) {
        return new byte[]{(byte) (data & 0xff), (byte) ((data >>> 8) & 0xff)};
    }
	
//	public int byteArrayShortToInt(byte[] b) 
//	{
//				
//	    int value = 0;
//	    for (int i = 0; i < 2; i++) {
//	        int shift = (2 - 1 - i) * 8;
//	        value += (b[i] & 0x000000FF) << shift;
//	    }
//	    return value;
//	}
	
	
	public int byteArrayToInt(byte[] b) 
	{
		return byteArrayToInt(b,4);
	}
	
	public int byteArrayToInt(byte[] b,int size) 
	{
//		if(b.length<4)
//		{
//			byte[] temp = new byte[4];
//			int size = b.length ;
//			int iCount = 4-size;
//			int iArray = 0;
//			for(int add = iCount;add < 4;add++)
//			{
//				if(iCount>add)
//				{
//				temp[add] = 0x00;
//				}
//				else
//				{
//					temp[add ] = b[ iArray];
//					iArray++;
//				}
//			}
//			b = temp;
//		}
		
	    int value = 0;
	    for (int i = 0; i < size; i++) {
	        int shift = (size - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}
	
	public String byteToHexString(byte[] bytes)
	{
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
	
	public String byteToString(byte[] bytes)
	{
		String s = "";
		try {
			s=  new String(bytes, "UTF-8");
		} catch (UnsupportedEncodingException e1) {

		}
		return s;
	}
	
//	public void fireEvent(EventSongCastBase ev) {
//		setChanged();
//		notifyObservers(ev);
//	}
	
}
