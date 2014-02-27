package org.rpi.songcast;

import java.io.UnsupportedEncodingException;
import java.util.Observable;

import org.rpi.player.events.EventBase;
import org.rpi.songcast.events.EventSongCastBase;

public class SongcastMessage extends Observable {

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
	
	public int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
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
	
	public void fireEvent(EventSongCastBase ev) {
		setChanged();
		notifyObservers(ev);
	}
	
}
