package org.rpi.plugin.oled;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
////import java.nio.ByteBuffer;
//import java.nio.charset.Charset;

import org.apache.log4j.Logger;

/**
 * A wrapper around the SSD1306 with some basic graphics methods.
 *
 * @author fauxpark
 */
public class Graphics {
	/**
	 * The SSD1306 OLED display.
	 */
	private SSD1306 ssd1306;

	private Logger log = Logger.getLogger(OLEDDisplayImplementation.class);

	protected Graphics2D graphics;
	protected BufferedImage img;
	protected int width = 128;
	protected int height = 64;
	private byte[] buffer;
	protected int pages = 0;

	protected ScrollerThread scrollerThread = null;

	/**
	 * Graphics constructor.
	 *
	 * @param ssd1306
	 *            The SSD1306 object to use.
	 */
	Graphics(SSD1306 ssd1306) {
		this.ssd1306 = ssd1306;
		width = ssd1306.getWidth();
		height = ssd1306.getHeight();
		this.pages = (height / 8);
		System.out.println("Page: " + pages);
		this.buffer = new byte[width * this.pages];
		scrollerThread = new ScrollerThread(ssd1306);
		Thread t = new Thread(scrollerThread);
		t.start();
		createImage();
	}

	private void createImage() {
		img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		graphics = img.createGraphics();
	}

	/**
	 * Returns Graphics object which is associated to current AWT image, if it
	 * wasn't set using setImage() with false createGraphics parameter
	 * 
	 * @return Graphics2D object
	 */
	public Graphics2D getGraphics() {
		return this.graphics;
	}

	public synchronized void displayImage() {
		Raster r = this.img.getRaster();
		for (int y = 0; y < this.height; y++) {
			for (int x = 0; x < this.width; x++) {
				// this.setPixel(x, y, );
				boolean isPixel = r.getSample(x, y, 0) > 0;
				if (isPixel) {
					log.debug("X: " + x + " Y: " + y + "IsTrue: ");
				}
				ssd1306.setPixel(x, y, isPixel);
			}
		}
		ssd1306.display();
	}

	public void drawStringFont(String text, int x, int y, java.awt.Font font) {
		log.debug("DrawString: " + text);
		int heightOffset = 2;
		// Get the expected size of the image
		int h = getTextHeight(font, text, heightOffset);
		int w = getTextWidth(font, text);
		log.debug("Width: " + w + " Height: " + h);
		// Create a buffered image
		BufferedImage i = new BufferedImage(w, h + heightOffset, BufferedImage.TYPE_BYTE_BINARY);
		// Create a grahpics object
		Graphics2D g = i.createGraphics();
		g.setFont(font);

		FontMetrics fm = g.getFontMetrics(font);
		int maxDescent = fm.getMaxDescent();

		g.drawString(text, 0, h - maxDescent);
		drawMyImage(i, w, h, x, y);
		g.dispose();
	}


	
	public void setTitle(String text, int x, int y, java.awt.Font font) {
		scrollerThread.setTitle(text, font, x, y);
	}
	


	/***
	 * OLD method
	 * 
	 * @param text
	 * @param x
	 * @param y
	 * @param font
	 */
	public void scrollMyText(String text, int x, int y, java.awt.Font font) {
		log.debug("ScrollString: " + text);
		// get the size of a space
		int space = getTextWidth(font, " ");
		int heightOffset = 2;

		// How many spaces will fill the screen
		int paddingSpace = (width / space) + 3;
		// text = String.format("%1$" + paddingSpace + "s", " ");
		StringBuilder sb = new StringBuilder(paddingSpace);
		for (int i = 0; i < paddingSpace; i++) {
			sb.append(" ");
		}
		// Pad the string on both sides
		text = sb.toString() + text + sb.toString();

		int h = getTextHeight(font, text, heightOffset);
		int w = getTextWidth(font, text);

		log.debug("Width: " + w + " Height: " + h);
		BufferedImage i = new BufferedImage(w, h + 10, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = i.createGraphics();

		g.setFont(font);
		g.drawString(text, 0, h);

		/*
		 * try { File outputfile = new File("saved.png"); ImageIO.write(i,
		 * "png", outputfile); } catch (IOException e1) { // TODO Auto-generated
		 * catch block e1.printStackTrace(); }
		 */

		if (w > width) {
			scrollMyImage(i, x, y, paddingSpace);
		}
		drawMyImage(i, w, h, x, y);
		g.dispose();
	}

	/***
	 * Scroll an image
	 * 
	 * @param i
	 * @param displayWidth
	 * @param displayHeight
	 * @param iPadding
	 */
	public void scrollMyImage(BufferedImage i, int displayWidth, int displayHeight, int iPadding) {
		//ByteBuffer buff = ByteBuffer.allocate(1000);
		//buff.putDouble(2);
		Raster r = i.getRaster();
		int rh = r.getHeight();
		int rw = r.getWidth();

		int stepSize = 2;

		if (rw > width) {
			int iStep = iPadding;
			while (true) {
				// The raster is wider than the screen so iterate around it.

				for (int rasterY = 0; rasterY <= rh - 1; rasterY++) {
					for (int rasterX = 0; rasterX <= width; rasterX++) {
						try {
							boolean isPixel = r.getSample(rasterX + iStep, rasterY, 0) > 0;
							ssd1306.setPixel(rasterX + displayWidth, rasterY + displayHeight, isPixel);
						} catch (Exception e) {
							// System.out.println("X= " + rasterX + iStep + " Y=
							// " + rasterY);
						}
					}
				}

				iStep = iStep + stepSize;
				if (iStep + width >= rw) {
					// System.out.println("Reset iStep");
					iStep = 0;
				}
				ssd1306.display();
			}
		}
	}

	/***
	 * Draw a BufferedImage
	 * 
	 * @param i
	 * @param textWidth
	 * @param textHeight
	 * @param displayWidth
	 * @param displayHeight
	 */
	private void drawMyImage(BufferedImage i, int textWidth, int textHeight, int displayWidth, int displayHeight) {
		Raster r = i.getRaster();
		// int rh = r.getHeight();
		// int rw = r.getWidth();
		// System.out.println("RasterWidth: " + rw + " RasterHeight: " + rh);
		for (int b = 0; b <= textHeight - 1; b++) {
			for (int a = 0; a <= textWidth - 1; a++) {
				boolean isPixel = r.getSample(a, b, 0) > 0;
				ssd1306.setPixel(a + displayWidth, b + displayHeight, isPixel);
			}
		}
		ssd1306.display();
	}

	/***
	 * OLD Method to draw a string,wipes out all other text
	 * 
	 * @param text
	 * @param x
	 * @param y
	 * @param font
	 */
	public void drawString(String text, int x, int y, java.awt.Font font) {
		// int h = getTextHeight(graphics.getFont(),text);
		// https://stackoverflow.com/questions/12018321/combine-two-graphics-objects-in-java
		createImage();
		graphics.setFont(font);
		graphics.drawString(text, x, y);
	}

	/***
	 * Get the expected width of the text
	 * 
	 * @param font
	 * @param text
	 * @return
	 */
	public int getTextWidth(java.awt.Font font, String text) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(text, null);
		return (int) bounds.getWidth();
	}

	/***
	 * Get the expected height of the text, still some work to do..
	 * 
	 * @param font
	 * @param text
	 * @param heightOffset
	 * @return
	 */
	public int getTextHeight(java.awt.Font font, String text, int heightOffset) {
		FontMetrics metrics = new FontMetrics(font) {
		};
		Rectangle2D bounds = metrics.getStringBounds(text, null);
		int ascent = metrics.getAscent();
		// return (int) (bounds.getHeight()/2 )+ ascent;
		return (int) heightOffset + ascent;
	}

	/**
	 * Sets one pixel in the current buffer
	 * 
	 * @param x
	 *            X position
	 * @param y
	 *            Y position
	 * @param white
	 *            White or black pixel
	 * @return True if the pixel was successfully set
	 */
	public boolean setPixel(int x, int y, boolean white) {
		if (x < 0 || x > this.width || y < 0 || y > this.height) {
			return false;
		}
		if (white) {
			this.buffer[x + (y / 8) * this.width] |= (1 << (y & 7));
		} else {
			this.buffer[x + (y / 8) * this.width] &= ~(1 << (y & 7));
		}
		return true;
	}

	/**
	 * Draw text onto the display.
	 *
	 * @param x
	 *            The X position to start drawing at.
	 * @param y
	 *            The Y position to start drawing at.
	 * @param font
	 *            The font to use.
	 * @param text
	 *            The text to draw.
	 * 
	 *            public void text(int x, int y, Font font, String text) { int
	 *            rows = font.getRows(); int cols = font.getColumns(); int[]
	 *            glyphs = font.getGlyphs(); byte[] bytes =
	 *            text.getBytes(Charset.forName(font.getName()));
	 *            System.out.println(text);
	 * 
	 *            for (int i = 0; i < text.length(); i++) { int p = (bytes[i] &
	 *            0xFF) * cols; StringBuilder sb = new StringBuilder(); for (int
	 *            col = 0; col < cols; col++) { int mask = glyphs[p++];
	 * 
	 *            for (int row = 0; row < rows; row++) { boolean isTrue = (mask
	 *            & 1) == 1; String s = " "; if (isTrue) { s = "X"; }
	 *            sb.append(s); ssd1306.setPixel(x, y + row, isTrue); mask >>=
	 *            1; } sb.append(System.lineSeparator()); x++; }
	 *            System.out.println(sb.toString()); System.out.println(" ");
	 *            x++; } }
	 * 
	 */
	/**
	 * Draw an image onto the display.
	 *
	 * @param image
	 *            The image to draw.
	 * @param x
	 *            The X position of the image.
	 * @param y
	 *            The Y position of the image.
	 * @param width
	 *            The width to resize the image to.
	 * @param height
	 *            The height to resize the image to.
	 */
	public void image(BufferedImage image, int x, int y, int width, int height) throws IOException {
		BufferedImage mono = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
		mono.createGraphics().drawImage(image, 0, 0, width, height, null);
		Raster r = mono.getRaster();

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				ssd1306.setPixel(x + j, y + i, r.getSample(j, i, 0) > 0);
			}
		}
		ssd1306.display();
	}

	/**
	 * Draw a line from one point to another.
	 *
	 * @param x0
	 *            The X position of the first point.
	 * @param y0
	 *            The Y position of the first point.
	 * @param x1
	 *            The X position of the second point.
	 * @param y1
	 *            The Y position of the second point.
	 */
	public void line(int x0, int y0, int x1, int y1) {
		int dx = x1 - x0;
		int dy = y1 - y0;

		if (dx == 0 && dy == 0) {
			ssd1306.setPixel(x0, y0, true);

			return;
		}

		if (dx == 0) {
			for (int y = Math.min(y0, y1); y <= Math.max(y1, y0); y++) {
				ssd1306.setPixel(x0, y, true);
			}
		} else if (dy == 0) {
			for (int x = Math.min(x0, x1); x <= Math.max(x1, x0); x++) {
				ssd1306.setPixel(x, y0, true);
			}
		} else if (Math.abs(dx) >= Math.abs(dy)) {
			if (dx < 0) {
				int ox = x0;
				int oy = y0;
				x0 = x1;
				y0 = y1;
				x1 = ox;
				y1 = oy;
				dx = x1 - x0;
				dy = y1 - y0;
			}

			double coeff = (double) dy / (double) dx;

			for (int x = 0; x <= dx; x++) {
				ssd1306.setPixel(x + x0, y0 + (int) Math.round(x * coeff), true);
			}
		} else if (Math.abs(dx) < Math.abs(dy)) {
			if (dy < 0) {
				int ox = x0;
				int oy = y0;
				x0 = x1;
				y0 = y1;
				x1 = ox;
				y1 = oy;
				dx = x1 - x0;
				dy = y1 - y0;
			}

			double coeff = (double) dx / (double) dy;

			for (int y = 0; y <= dy; y++) {
				ssd1306.setPixel(x0 + (int) Math.round(y * coeff), y + y0, true);
			}
		}
	}

	/**
	 * Draw a rectangle.
	 *
	 * @param x
	 *            The X position of the rectangle.
	 * @param y
	 *            The Y position of the rectangle.
	 * @param width
	 *            The width of the rectangle in pixels.
	 * @param height
	 *            The height of the rectangle in pixels.
	 * @param fill
	 *            Whether to draw a filled rectangle.
	 */
	public void rectangle(int x, int y, int width, int height, boolean fill) {
		if (fill) {
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					ssd1306.setPixel(x + i, y + j, true);
				}
			}
		} else if (width > 0 && height > 0) {
			line(x, y, x, y + height - 1);
			line(x, y + height - 1, x + width - 1, y + height - 1);
			line(x + width - 1, y + height - 1, x + width - 1, y);
			line(x + width - 1, y, x, y);
		}
	}

	/**
	 * Draw an arc.
	 *
	 * @param x
	 *            The X position of the center of the arc.
	 * @param y
	 *            The Y position of the center of the arc.
	 * @param radius
	 *            The radius of the arc in pixels.
	 * @param startAngle
	 *            The starting angle of the arc in degrees.
	 * @param endAngle
	 *            The ending angle of the arc in degrees.
	 */
	public void arc(int x, int y, int radius, int startAngle, int endAngle) {
		for (int i = startAngle; i <= endAngle; i++) {
			ssd1306.setPixel(x + (int) Math.round(radius * Math.sin(Math.toRadians(i))), y + (int) Math.round(radius * Math.cos(Math.toRadians(i))), true);
		}
	}

	/**
	 * Draw a circle. This is the same as calling arc() with a start and end
	 * angle of 0 and 360 respectively.
	 *
	 * @param x
	 *            The X position of the center of the circle.
	 * @param y
	 *            The Y position of the center of the circle.
	 * @param radius
	 *            The radius of the circle in pixels.
	 */
	public void circle(int x, int y, int radius) {
		arc(x, y, radius, 0, 360);
	}

	/***
	 * Set the Contrast, between 0-255
	 * 
	 * @param contrast
	 */
	public void setContrast(int contrast) {
		ssd1306.setContrast(contrast);

	}

	/***
	 * Dim the dispaly in steps
	 * 
	 * @param endValue
	 */
	public void dimContrast(int endValue) {
		for (int i = 255; i > endValue; i--) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {

			}
			ssd1306.setContrast(i);
		}
	}

	/***
	 * Bright the display in steps
	 * 
	 * @param endValue
	 */
	public void brightenContrast(int endValue) {
		for (int i = 0; i < endValue; i++) {
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {

			}
			ssd1306.setContrast(i);
		}
	}

	/***
	 * Clear the display
	 */
	public void clear() {
		ssd1306.clear();
		ssd1306.display();
	}

	/***
	 * Set the Play Time
	 * @param time
	 */
	public void setTime(String time) {
		scrollerThread.setTime(time);
		
	}
	
	public void setPauseTimer(int pauseTimer) {
		scrollerThread.setPauseTimer(pauseTimer);
	}


	public void showMessage(String text, int pause, Font font) {
        setPauseTimer(pause);
        clear();	        
        drawStringFont(text, 0, 0,new Font("Arial", Font.PLAIN, 50));		
	}
	
	public void setScroll(boolean bScroll) {
		scrollerThread.setScroll(bScroll);
	}

}
