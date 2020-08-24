package org.rpi.plugin.oled.transport;
import java.nio.ByteBuffer;

/**
 * An interface for defining transports.
 *
 * @author fauxpark
 */
public interface Transport {
	/**
	 * Reset the display.
	 */
	void reset();

	/**
	 * Shutdown the display.
	 */
	void shutdown();

	/**
	 * Send a command to the display.
	 *
	 * @param command The command to send.
	 * @param params Any parameters the command requires.
	 */
	void command(int command, int... params);

	/**
	 * Send pixel data to the display.
	 *
	 * @param data The data to send.
	 */
	void data(byte[] data);
	
	void data(ByteBuffer data);
	
	void  myData(byte[] data);
}