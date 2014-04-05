package org.rpi.airplay;

public class Utils {
	
	/**
	 * New getBytes method using SystenArrayCopy which is supposed to be quicker
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] getBytes(int start ,int end,byte[] data)
	{

		int size = (end - start) + 1;
		byte[] res = new byte[size];
		System.arraycopy(data, start, res, 0, size);
		return res;
	}

}
