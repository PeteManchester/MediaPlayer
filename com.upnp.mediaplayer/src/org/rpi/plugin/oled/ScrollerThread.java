package org.rpi.plugin.oled;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.rpi.plugin.lcddisplay.ByteHolder;

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

	BufferedImage imgTrack = null;
	BufferedImage imgTime = null;

	private int paddingSpace = 0;
	private boolean updated = false;
	private int pauseTimer = 0;

	ByteBuffer buffScreen = null;
	Queue<ByteHolder> queue = new ConcurrentLinkedQueue<ByteHolder>();

	/***
	 * 
	 * @param ssd1306
	 */
	public ScrollerThread(SSD1306 ssd1306) {
		buffScreen = ByteBuffer.allocate(1024);
		this.ssd1306 = ssd1306;
		setTimeText("00:05", new Font("Arial", Font.PLAIN, 10), 0, 50);
		byte bit = buffScreen.get(0);
		buffScreen.put(bit |= (1 << (0 & 7)));
		byte after = buffScreen.get(0);

	}

	public void setTimeText(String text, Font font, int x, int y) {
		// text = "";
		int h = getTextHeight(font, text, 0);
		int w = getTextWidth(font, text);

		log.debug("Width: " + w + " Height: " + h);
		BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = i.createGraphics();
		FontMetrics fm = g.getFontMetrics(font);
		int maxDescent = fm.getMaxDescent();
		g.setFont(font);
		g.drawString(text, 0, h - maxDescent);
		imgTime = i;
		/*
		 * Raster rt = i.getRaster(); int rht = rt.getHeight(); int rwt =
		 * rt.getWidth();
		 * 
		 * ByteBuffer buff = ByteBuffer.allocate(((rht * rwt)/8)+1); int iCount
		 * = 0; for (int rasterY = 0; rasterY <= rht - 1; rasterY++) { for (int
		 * rasterX = 0; rasterX <= rwt - 1; rasterX++) { try { boolean isPixel =
		 * rt.getSample(rasterX, rasterY, 0) > 0; // ssd1306.setPixel(rasterX +
		 * displayWidth, rasterY + 55, // isPixel);
		 * 
		 * // int myY = (rasterY / 8) * width; int myY = (rasterY / 8) * width;
		 * byte b = buff.get(myY + rasterX);
		 * 
		 * if (isPixel) { //byte b = buff.get(myY); b |= (1 << (rasterY & 7));
		 * buff.put(myY + rasterX, b); } else { b &= ~(1 << (rasterY & 7));
		 * buff.put(myY + rasterX,b); } //buff.put(iCount, b); iCount++;
		 * System.out.println("ICount: " + iCount);
		 * 
		 * } catch (Exception e) { log.error("Error setPixel", e); } } }
		 * ByteHolder bh = new ByteHolder(buff, rwt, rht, x, y,
		 * buff.capacity()); queue.add(bh);
		 */
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

			int space = getTextWidth(font, " ");

			int screenSpaces = width / space;

			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < screenSpaces; i++) {
				sb.append(" ");
			}

			text = sb.toString() + text + sb.toString();

			int h = getTextHeight(font, text, 0);
			int w = getTextWidth(font, text);

			//int remainder = h %8;
			//if(remainder > 0) {
			//	h += 8 - remainder;
			//}
			//h += h % 8;

			// createBufferImage(x, y);
			displayWidth = x;
			displayHeight = y;

			BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
			Graphics2D g = i.createGraphics();
			FontMetrics fm = g.getFontMetrics(font);
			int maxDescent = fm.getMaxDescent();
			g.setFont(font);
			g.drawString(text, 0, h - maxDescent);
			imgTrack = i;

			/*
			Raster rt = i.getRaster();
			int rht = rt.getHeight();
			int rwt = rt.getWidth();
			int size = (rht/8) * rwt * (rwt/2);
			ByteBuffer buff = ByteBuffer.allocate(size);
			//buff.order(ByteOrder.LITTLE_ENDIAN);
			int bufferSize = rt.getDataBuffer().getSize();
			int iCount = 0;
			for (int iStep = 2; iStep <= (rwt-128); iStep = iStep + 2) {
				for (int rasterY = 0; rasterY < rht  ; rasterY++) {
					for (int rasterX = 0; rasterX < (width); rasterX++) {
						try {

							// boolean isPixel = rt.getSample(rasterX, rasterY,
							// 0) > 0;
							boolean isPixel = rt.getSample((rasterX + iStep)-1, rasterY, 0) > 0;
							// ssd1306.setPixel(rasterX + displayWidth, rasterY
							// + 55, isPixel);

							// int myY = (rasterY / 8) * width;
							int myY = (rasterY / 8) * width * iStep;
							byte b = buff.get(myY + rasterX);

							if (isPixel) {
								b |= (1 << (rasterY & 7));
							} else {
								b &= ~(1 << (rasterY & 7));
							}
							
							if(b== -64) {
								System.out.println("Whoops BAD");
							}
							
							if(myY+ rasterX == 126) {
								System.out.println("What is the value of b: " + b);
							}
							
							buff.put(myY + rasterX, b);
							//System.out.println("Putting Byte: " + myY+rasterX);
							iCount++;
							//System.out.println("ICount: " + iCount);

						} catch (Exception e) {
							log.error("Error setPixel", e);
							e.printStackTrace();
						}
					}
				}
				System.out.println("ICount FINAL: " + iCount + " iStep: " + iStep);
			}

			ByteHolder bh = new ByteHolder(buff, rwt, rht, x, y, rht/8 * rwt);
			queue.add(bh);
			*/
			
			

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
		log.debug("ScrollString: " + text);
		// get the size of a space
		int space = getTextWidth(font, " ");
		int heightOffset = 0;

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

		log.debug("Width: " + w + " Height: " + h);
		BufferedImage i = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		Graphics2D g = i.createGraphics();
		// g.drawRect(x, y, w, h);

		// Get the MaxDescent so we can work out the height to draw the font.
		FontMetrics fm = g.getFontMetrics(font);
		int maxDescent = fm.getMaxDescent();

		g.setFont(font);

		g.drawString(text, 0, h - maxDescent);
		log.debug("Setting new BufferedImage: " + text);
		this.imgTrack = i;

		// try {
		// File outputfile = new File("saved.png");
		// ImageIO.write(i, "png", outputfile);
		// } catch (IOException e1) {
		// e1.printStackTrace();
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
		int ascent = metrics.getAscent();
		int desecnt = metrics.getDescent();

		return (int) heightOffset + ascent;
	}

	@Override
	public void run() {
		ByteHolder bh = null;
		while (isRunning) {

			if (!queue.isEmpty()) {
				bh = queue.poll();
			}

			if (bh != null) {
				int bytesPerFrame = bh.getBytesPerFrame();
				while(bh.getBuffer().remaining() > bytesPerFrame) {
					try {
						byte[] bytes = new byte[bytesPerFrame];
						bh.getBuffer().get(bytes,  0, bytesPerFrame);
						buffScreen.put(bytes, bh.getPositionX(), bh.getPositionX());
						ssd1306.displayByteBuffer(buffScreen);					
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
				}
				/*
				for (int i = 0; i < bh.getBuffer().position() +(bytesPerFrame * 2); i= i+bytesPerFrame) {
					try {
						byte[] bytes = new byte[bytesPerFrame];
						
						//byte[] testBytes = new byte[bh.getBuffer().capacity()];
						//bh.getBuffer().get(testBytes);
						bh.getBuffer().get(bytes,  0, bytesPerFrame);
						buffScreen.put(bytes, bh.getPositionX(), bh.getPositionX());
						ssd1306.displayByteBuffer(buffScreen);
					} catch (Exception e) {
						//e.printStackTrace();
						System.out.println("I= " + i);
						break;
					}
				}
				*/
				System.out.println("End of FOR LOOP");
				bh.getBuffer().clear();
			} else {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

			boolean isTrue = true;

			if (isTrue) {

				if (pauseTimer > 0) {
					updated = true;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}
					pauseTimer--;
					log.debug("Pause for : " + pauseTimer);

				} else {
					if (imgTrack == null) {
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
	}

	private void loop() {
		log.debug("Start of Loop");
		Raster r = imgTrack.getRaster();
		int rh = r.getHeight();
		int rw = r.getWidth();
		updated = false;
		int stepSize = 2;
		if (rw > width) {
			int iStep = paddingSpace;
			while (!updated) {
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
					log.debug("Size: " + iStep + " Width: " + rw);
					iStep = 0;
				}

				Raster rt = imgTime.getRaster();
				int rht = rt.getHeight();
				int rwt = rt.getWidth();

				for (int rasterY = 0; rasterY <= rht - 1; rasterY++) {
					for (int rasterX = 0; rasterX <= rwt - 1; rasterX++) {
						try {
							boolean isPixel = rt.getSample(rasterX, rasterY, 0) > 0;
							ssd1306.setPixel(rasterX + displayWidth, rasterY + 55, isPixel);
						} catch (Exception e) {
							log.error("Error setPixel", e);
						}
					}
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
		imgTrack = null;
		updated = true;
	}

	public void pause(int pause) {
		log.debug("PauseTimer Set: " + pause);
		this.pauseTimer = pause;
		updated = true;
	}

	public void setTimeTime(int time) {
		// TODO Auto-generated method stub
		
	}

}
