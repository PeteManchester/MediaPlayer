package org.rpi.plugin.oled;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

import org.apache.log4j.Logger;

public class ScrollerThread implements Runnable {

	private Logger log = Logger.getLogger(ScrollerThread.class);

	private boolean isRunning = true;
	private String text = "";
	private Font font = null;

	protected int width = 128;
	protected int height = 64;
	protected SSD1306 ssd1306 = null;

	protected int displayWidth = 0;
	protected int displayHeight = 0;

	BufferedImage image = null;

	private int paddingSpace = 0;
	private boolean updated = false;
	private int pauseTimer = 0;
	// private String oldText = "";

	/***
	 * 
	 * @param ssd1306
	 */
	public ScrollerThread(SSD1306 ssd1306) {
		this.ssd1306 = ssd1306;
	}

	/***
	 * 
	 * @param text
	 * @param font
	 * @param x
	 * @param y
	 */
	public void setText(String text, Font font, int x, int y) {
		log.debug("Set Text: " + text);
		if (!text.equals(this.text)) {
			stop();
			log.debug("Text is Changing: " + text);
			this.text = text;
			this.font = font;
			createBufferImage(x, y);
			displayWidth = x;
			displayHeight = y;
			updated = true;
		} else {
			log.debug("Text is NOT Changing: " + text);
		}
	}

	/***
	 * Create a BufferedImage of the Text.
	 * 
	 * @param x
	 * @param y
	 */
	private void createBufferImage(int x, int y) {
		System.out.println("ScrollString: " + text);
		// get the size of a space
		int space = getTextWidth(font, " ");
		int heightOffset = 2;

		// How many spaces will fill the screen
		paddingSpace = (width / space) + 3;
		// text = String.format("%1$" + paddingSpace + "s", " ");
		StringBuilder sb = new StringBuilder(paddingSpace);
		for (int i = 0; i < paddingSpace; i++) {
			sb.append(" ");
		}
		// Pad the string on both sides
		text = sb.toString() + text + sb.toString();

		int h = getTextHeight(font, text, heightOffset);
		int w = getTextWidth(font, text);

		System.out.println("Width: " + w + " Height: " + h);
		BufferedImage i = new BufferedImage(w, h + 10, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = i.createGraphics();

		g.setFont(font);
		g.drawString(text, 0, h);
		log.debug("Setting new BufferedImage: " + text);
		this.image = i;

		/*
		 * try { File outputfile = new File("saved.png"); ImageIO.write(i,
		 * "png", outputfile); } catch (IOException e1) { // TODO Auto-generated
		 * catch block e1.printStackTrace(); }
		 */

		// if (w > width) {
		// scrollMyImage(i, x, y, paddingSpace);
		// }
	}

	public int getTextWidth(java.awt.Font font, String text) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(text, null);
		return (int) bounds.getWidth();
	}

	public int getTextHeight(java.awt.Font font, String text, int heightOffset) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		// Rectangle2D bounds = metrics.getStringBounds(text, null);
		int ascent = metrics.getAscent();
		// return (int) (bounds.getHeight()/2 )+ ascent;
		return (int) heightOffset + ascent;
	}

	@Override
	public void run() {
		while (isRunning) {

			if (pauseTimer > 0) {
				updated = true;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
				pauseTimer--;
				log.debug("Pause for : " + pauseTimer);

			} else {
				if (image == null) {
					log.debug("i is NULL");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						log.error("Error Sleep", e);
					}
				} else {	
					log.debug("Loop");
					loop();					
				}
			}
		}
	}
	
	
	private void loop() {
		log.debug("Start of Loop");
		Raster r = image.getRaster();
		int rh = r.getHeight();
		int rw = r.getWidth();
		updated = false;
		int stepSize = 2;
		if (rw > width) {
			int iStep = paddingSpace;
			while (!updated ) {
				// The raster is wider than the screen so iterate
				// around
				// it.

				for (int rasterY = 0; rasterY <= rh - 1; rasterY++) {
					for (int rasterX = 0; rasterX <= width; rasterX++) {
						try {
							boolean isPixel = r.getSample(rasterX + iStep, rasterY, 0) > 0;
							ssd1306.setPixel(rasterX + displayWidth, rasterY + displayHeight, isPixel);
						} catch (Exception e) {
							log.error("Error setPixel", e);
						}
					}
				}

				iStep = iStep + stepSize;
				if (iStep + width >= rw) {
					iStep = 0;
				}
				ssd1306.display();
			}
			log.debug("BufferImage was NULL");
			// oldText = text;
		} else {
			log.info("RASTER IS NOT WIDER THAN THE SCREEN!!!!!!!");
		}
	}

	/***
	 * Stop the Scrolling
	 */
	public void stop() {
		log.debug("Stopping Scrolling");
		image = null;
		updated = true;
	}

	public void pause(int pause) {
		log.debug("PauseTimer Set: " + pause);
		this.pauseTimer = pause;
		updated = true;
	}

}
