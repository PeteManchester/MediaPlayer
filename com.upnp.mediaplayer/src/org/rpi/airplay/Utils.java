package org.rpi.airplay;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import org.rpi.utils.Base64;
import org.rpi.utils.SecUtils;

public class Utils {

	/**
	 * New getBytes method using SystenArrayCopy which is supposed to be quicker
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] getBytes(int start, int end, byte[] data) {

		int size = (end - start) + 1;
		byte[] res = new byte[size];
		System.arraycopy(data, start, res, 0, size);
		return res;
	}

	// public static String getChallengeResponse(String challengeStr,
	// InetAddress address, byte[] hwAddress) {
	// try {
	// byte[] challenge = Base64.decode(challengeStr);
	// ByteArrayOutputStream out = new ByteArrayOutputStream();
	// // Challenge
	// out.write(challenge);
	// // IP-Address
	// out.write(address.getAddress());
	// // HW-Addr
	// out.write(AudioSessionHolder.getInstance().getHardWareAddress());
	// // Pad to 32 Bytes
	// int padLen = 32 - out.size();
	// for (int i = 0; i < padLen; ++i) {
	// out.write(0x00);
	// }
	//
	// String response = Base64.encode(SecUtils.encryptRSA(out.toByteArray()));
	// // String response = "Response";
	// return response.replace("=", "").replace("\r", "").replace("\n", ""); //
	// remove
	// // padding
	// // and
	// // other
	// // chars
	// } catch (Exception e) {
	// throw new RuntimeException(e);
	// }
	// }

	public static String getChallengeResponse(String challenge, InetAddress address, byte[] hwAddress) {
		{
			// BASE64 DECODE
			byte[] decoded = Base64.decode(challenge);

			// IP byte array
			// byte[] ip = socket.getLocalAddress().getAddress();
			// SocketAddress localAddress = socket.getLocalSocketAddress(); //
			// .getRemoteSocketAddress();
			// byte[] ip = ((InetSocketAddress)
			// localAddress).getAddress().getAddress();

			byte[] ip = address.getAddress();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// Challenge
			try {
				out.write(decoded);
				// IP-Address
				out.write(ip);
				// HW-Addr
				out.write(AudioSessionHolder.getInstance().getHardWareAddress());

				// Pad to 32 Bytes
				int padLen = 32 - out.size();
				for (int i = 0; i < padLen; ++i) {
					out.write(0x00);
				}

			} catch (Exception e) {
				//log.error(e);
			}

			// RSA
			byte[] crypted = SecUtils.getInstance().encryptRSA(out.toByteArray());

			// Encode64
			String ret = Base64.encode(crypted);

			// On retire les ==
			return ret = ret.replace("=", "").replace("\r", "").replace("\n", "");

			// Write
			// response.append("Apple-Response", ret);
		}

	}
}
