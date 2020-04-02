package org.rpi.songcast.ohu.sender.mpd;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/***
 * 
 * @author phoyle 
 * Main controller for the MPD HTTPD listener 
 * MPD can be configured to stream PCM of HTTP. 
 * This is used for the Songcast Sender
 */
public class MPDStreamerController {

	private static MPDStreamerController instance = null;
	private Logger log = Logger.getLogger(this.getClass());
	private int iCount = 0;
	private int maxSize = 0;

	private ByteBuf queue = Unpooled.directBuffer();
	private Thread mpdThread = null;
	private MPDStreamerConnector mpdClient = null;

	/***
	 * 
	 * @return
	 */
	public static MPDStreamerController getInstance() {
		if (instance == null) {
			instance = new MPDStreamerController();

		}
		return instance;
	}

	/***
	 * 
	 */
	private MPDStreamerController() {
	}

	/***
	 * Get the next 1764 bytes of PCM
	 * 
	 * @return
	 */
	public ByteBuf getNext() {
		try {
			if (getQueue().readableBytes() > 1764) {
				ByteBuf out = Unpooled.directBuffer(1764);
				getQueue().readBytes(out, 1764);

				getQueue().discardReadBytes();
				return out;
			}
		} catch (Exception e) {
			log.error("Error getNext", e);
		} finally {

		}

		return null;
	}

	private synchronized ByteBuf getQueue() {
		return queue;
	}

	/***
	 * Add PCM to the buffer
	 * 
	 * @param b
	 */
	public void addSoundByte(ByteBuf b) {
		getQueue().writeBytes(b, 0, b.readableBytes());
		int size = getQueue().readableBytes();
		if (size > maxSize) {
			maxSize = size;
		}
		int slice = 1764000;
		if (size > slice) {
			log.debug("Buffer too big: " + size);
			try {
				
			}catch(Exception e) {
			ByteBuf out = Unpooled.directBuffer(slice);
			getQueue().readBytes(out, slice);
			getQueue().discardReadBytes();
			out.release();
			}
			
		}

		if (iCount % 1000 == 0) {
			log.debug("Count: " + iCount + " MaxBufferSize: " + maxSize);
			maxSize = 0;
		}
		iCount++;
	}

	/***
	 * Start the MPDConnector
	 */
	private void startMPDConnection() {
		log.debug("Start MPDConnection");
		if (mpdThread != null) {
			stopMPDConnection();
		}
		mpdClient = new MPDStreamerConnector();
		mpdThread = new Thread(mpdClient, "MPDStreamerConnector");
		mpdThread.start();
	}

	/***
	 * Stop the MPDConnector
	 */
	private void stopMPDConnection() {
		try {
			if (mpdClient != null) {
				log.debug("Stopping MPD Connection");
				mpdClient.stop();
			}
			mpdClient = null;
			mpdThread = null;
		} catch (Exception e) {
			log.error("Error Stopping MPDConnection", e);
		}
	}

	/***
	 * Start
	 */
	public void start() {
		if (mpdClient != null) {
			return;
		}
		stopMPDConnection();
		startMPDConnection();

	}

	/***
	 * Stop
	 */
	public void stop() {
		stopMPDConnection();
	}

}
