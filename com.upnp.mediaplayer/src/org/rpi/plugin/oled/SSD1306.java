package org.rpi.plugin.oled;

import java.nio.ByteBuffer;

import org.rpi.plugin.oled.transport.Transport;

/**
 * An SSD1306 OLED display.
 *
 * @author fauxpark
 */
public class SSD1306 {
	/**
	 * A helper class for drawing lines, shapes, text and images.
	 */
	private Graphics graphics;

	/**
	 * The transport to use.
	 */
	private Transport transport;

	/**
	 * The width of the display in pixels.
	 */
	private int width;

	/**
	 * The height of the display in pixels.
	 */
	private int height;

	/**
	 * The number of pages in the display.
	 */
	private int pages;

	/**
	 * The display buffer.
	 */
	private byte[] buffer;

	/**
	 * Indicates whether the display has been started up.
	 */
	private boolean initialised;

	/**
	 * The lower column start address for page addressing mode.
	 */
	private int lowerColStart;

	/**
	 * The higher column start address for page addressing mode.
	 */
	private int higherColStart;

	/**
	 * The memory addressing mode of the display.
	 */
	private int memoryMode;

	/**
	 * Indicates whether the display is currently scrolling.
	 */
	private boolean scrolling;

	/**
	 * The starting row of the display buffer.
	 */
	private int startLine;

	/**
	 * The current contrast level of the display.
	 */
	private int contrast;

	/**
	 * Indicates whether the display is horizontally flipped.
	 */
	private boolean hFlipped;

	/**
	 * Indicates whether the display is inverted.
	 */
	private boolean inverted;

	/**
	 * Indicates whether the display is on or off.
	 */
	private boolean displayOn;

	/**
	 * The starting page of the display for page addressing mode.
	 */
	private int startPage;

	/**
	 * Indicates whether the display is vertically flipped.
	 */
	private boolean vFlipped;

	/**
	 * The current display offset.
	 */
	private int offset;

	/**
	 * The hardware configuration of the display's COM pins.
	 */
	private int comPins;

	/**
	 * SSD1306 constructor.
	 *
	 * @param width
	 *            The width of the display in pixels.
	 * @param height
	 *            The height of the display in pixels.
	 * @param transport
	 *            The transport to use.
	 */
	public SSD1306(int width, int height, Transport transport) {
		this.width = width;
		this.height = height;
		pages = height / 8;
		buffer = new byte[width * pages];
		this.transport = transport;
	}

	/**
	 * Get the initialised state of the display.
	 */
	public boolean isInitialised() {
		return initialised;
	}

	/**
	 * Start the power on procedure for the display.
	 *
	 * @param externalVcc
	 *            Indicates whether the display is being driven by an external
	 *            power source.
	 */
	public void startup(boolean externalVcc) {
		reset();
		setDisplayOn(false);
		command(Command.SET_DISPLAY_CLOCK_DIV, width);
		command(Command.SET_MULTIPLEX_RATIO, height - 1);
		setOffset(0);
		setStartLine(0);
		command(Command.SET_CHARGE_PUMP, externalVcc ? Constant.CHARGE_PUMP_DISABLE : Constant.CHARGE_PUMP_ENABLE);
		setMemoryMode(Constant.MEMORY_MODE_HORIZONTAL);
		setHFlipped(false);
		setVFlipped(false);
		setCOMPinsConfiguration(height == 64 ? Constant.COM_PINS_ALTERNATING : Constant.COM_PINS_SEQUENTIAL);
		setContrast(externalVcc ? 0x9F : 0xCF);
		command(Command.SET_PRECHARGE_PERIOD, externalVcc ? 0x22 : 0xF1);
		command(Command.SET_VCOMH_DESELECT, Constant.VCOMH_DESELECT_LEVEL_00);
		command(Command.DISPLAY_ALL_ON_RESUME);
		setInverted(false);
		setDisplayOn(true);
		clear();
		display();
		initialised = true;
	}

	/**
	 * Start the power off procedure for the display.
	 */
	public void shutdown() {
		clear();
		display();
		setDisplayOn(false);
		reset();
		setInverted(false);
		setHFlipped(false);
		setVFlipped(false);
		stopScroll();
		setContrast(0);
		setOffset(0);
		transport.shutdown();
		initialised = false;
	}

	/**
	 * Reset the display.
	 */
	public void reset() {
		transport.reset();
	}

	/**
	 * Clear the buffer. <br/>
	 * NOTE: This does not clear the display, you must manually call
	 * {@link#display()}.
	 */
	public void clear() {
		buffer = new byte[width * pages];
	}

	/**
	 * Send the buffer to the display.
	 */
	public synchronized void display() {
		command(Command.SET_COLUMN_ADDRESS, 0, width - 1);
		command(Command.SET_PAGE_ADDRESS, 0, pages - 1);
		data(buffer);

		// Jump start scrolling again if new data is written while enabled
		if (isScrolling()) {
			noOp();
		}
	}
	
	public synchronized void displayByteBuffer(ByteBuffer buff) {
		command(Command.SET_COLUMN_ADDRESS, 0, width - 1);
		command(Command.SET_PAGE_ADDRESS, 0, pages - 1);
		data(buff);

		// Jump start scrolling again if new data is written while enabled
		if (isScrolling()) {
			noOp();
		}
	}
	
	

	/**
	 * Get the width of the display.
	 *
	 * @return The display width in pixels.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the height of the display.
	 *
	 * @return The display height in pixels.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Get the lower column start address for page addressing mode.
	 *
	 * @return The lower column start address, from 0 to 15.
	 */
	public int getLowerColStart() {
		return lowerColStart;
	}

	/**
	 * Set the lower column start address for page addressing mode.
	 *
	 * @param lowerColStart
	 *            The lower column start address, from 0 to 15. Values outside
	 *            this range will be clamped.
	 */
	public void setLowerColStart(int lowerColStart) {
		lowerColStart = clamp(0, 15, lowerColStart);
		this.lowerColStart = lowerColStart;
		command(Command.SET_LOWER_COL_START | lowerColStart);
	}

	/**
	 * Get the higher column start address for page addressing mode.
	 *
	 * @return The higher column start address, from 0 to 15.
	 */
	public int getHigherColStart() {
		return higherColStart;
	}

	/**
	 * Set the higher column start address for page addressing mode.
	 *
	 * @param higherColStart
	 *            The higher column start address, from 0 to 15. Values outside
	 *            this range will be clamped.
	 */
	public void setHigherColStart(int higherColStart) {
		higherColStart = clamp(0, 15, higherColStart);
		this.higherColStart = higherColStart;
		command(Command.SET_HIGHER_COL_START | higherColStart);
	}

	/**
	 * Get the memory addressing mode.
	 *
	 * @return The current memory mode, either
	 *         {@link Constant.MEMORY_MODE_HORIZONTAL},
	 *         {@link Constant.MEMORY_MODE_VERTICAL}, or
	 *         {@link Constant.MEMORY_MODE_PAGE}.
	 */
	public int getMemoryMode() {
		return memoryMode;
	}

	/**
	 * Set the memory addressing mode.
	 *
	 * @param memoryMode
	 *            The memory mode to set. Must be one of
	 *            {@link Constant.MEMORY_MODE_HORIZONTAL},
	 *            {@link Constant.MEMORY_MODE_VERTICAL}, or
	 *            {@link Constant.MEMORY_MODE_PAGE}.
	 */
	public void setMemoryMode(int memoryMode) {
		if (memoryMode == Constant.MEMORY_MODE_HORIZONTAL || memoryMode == Constant.MEMORY_MODE_VERTICAL || memoryMode == Constant.MEMORY_MODE_PAGE) {
			this.memoryMode = memoryMode;
			command(Command.SET_MEMORY_MODE, memoryMode);
		}
	}

	/**
	 * Get the scrolling state of the display.
	 *
	 * @return Whether the display is scrolling.
	 */
	public boolean isScrolling() {
		return scrolling;
	}

	/**
	 * Scroll the display horizontally.
	 *
	 * @param direction
	 *            The direction to scroll, where a value of true results in the
	 *            display scrolling to the left.
	 * @param start
	 *            The start page address, from 0 to 7.
	 * @param end
	 *            The end page address, from 0 to 7.
	 * @param speed
	 *            The scrolling speed (scroll step).
	 *
	 * @see Constant#SCROLL_STEP_5
	 */
	public void scrollHorizontally(boolean direction, int start, int end, int speed) {
		command(direction ? Command.LEFT_HORIZONTAL_SCROLL : Command.RIGHT_HORIZONTAL_SCROLL, Constant.DUMMY_BYTE_00, start, speed, end, Constant.DUMMY_BYTE_00, Constant.DUMMY_BYTE_FF);
	}

	/**
	 * Scroll the display horizontally and vertically.
	 *
	 * @param direction
	 *            The direction to scroll, where a value of true results in the
	 *            display scrolling to the left.
	 * @param start
	 *            The start page address, from 0 to 7.
	 * @param end
	 *            The end page address, from 0 to 7.
	 * @param offset
	 *            The number of rows from the top to start the vertical scroll
	 *            area at.
	 * @param rows
	 *            The number of rows in the vertical scroll area.
	 * @param speed
	 *            The scrolling speed (scroll step).
	 * @param step
	 *            The number of rows to scroll vertically each frame.
	 *
	 * @see Constant#SCROLL_STEP_5
	 */
	public void scrollDiagonally(boolean direction, int start, int end, int offset, int rows, int speed, int step) {
		command(Command.SET_VERTICAL_SCROLL_AREA, offset, rows);
		command(direction ? Command.VERTICAL_AND_LEFT_HORIZONTAL_SCROLL : Command.VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL, Constant.DUMMY_BYTE_00, start, speed, end, step);
	}

	/**
	 * Stop scrolling the display.
	 */
	public void stopScroll() {
		scrolling = false;
		command(Command.DEACTIVATE_SCROLL);
	}

	/**
	 * Start scrolling the display.
	 */
	public void startScroll() {
		scrolling = true;
		command(Command.ACTIVATE_SCROLL);
	}

	/**
	 * Get the display start line.
	 *
	 * @return The row to begin displaying at.
	 */
	public int getStartLine() {
		return startLine;
	}

	/**
	 * Set the display start line.
	 *
	 * @param startLine
	 *            The row to begin displaying at.
	 */
	public void setStartLine(int startLine) {
		startLine = clamp(0, height - 1, startLine);
		this.startLine = startLine;
		command(Command.SET_START_LINE | startLine);
	}

	/**
	 * Get the display contrast.
	 *
	 * @return The current contrast level of the display.
	 */
	public int getContrast() {
		return contrast;
	}

	/**
	 * Set the display contrast.
	 *
	 * @param contrast
	 *            The contrast to set, from 0 to 255. Values outside of this
	 *            range will be clamped.
	 */
	public void setContrast(int contrast) {
		contrast = clamp(0, 255, contrast);
		this.contrast = contrast;
		command(Command.SET_CONTRAST, contrast);
	}

	/**
	 * Get the horizontal flip state of the display.
	 *
	 * @return Whether the display is horizontally flipped.
	 */
	public boolean isHFlipped() {
		return hFlipped;
	}

	/**
	 * Flip the display horizontally.
	 *
	 * @param hFlipped
	 *            Whether to flip the display or return to normal.
	 */
	public void setHFlipped(boolean hFlipped) {
		this.hFlipped = hFlipped;

		if (hFlipped) {
			command(Command.SET_SEGMENT_REMAP);
		} else {
			command(Command.SET_SEGMENT_REMAP_REVERSE);
		}

		// Horizontal flipping is not immediate
		display();
	}

	/**
	 * Get the inverted state of the display.
	 *
	 * @return Whether the display is inverted or not.
	 */
	public boolean isInverted() {
		return inverted;
	}

	/**
	 * Invert the display. When inverted, an "on" bit in the buffer results in
	 * an unlit pixel.
	 *
	 * @param inverted
	 *            Whether to invert the display or return to normal.
	 */
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
		command(inverted ? Command.INVERT_DISPLAY : Command.NORMAL_DISPLAY);
	}

	/**
	 * Get the display state.
	 *
	 * @return True if the display is on.
	 */
	public boolean isDisplayOn() {
		return displayOn;
	}

	/**
	 * Turn the display on or off.
	 *
	 * @param displayOn
	 *            Whether to turn the display on.
	 */
	public void setDisplayOn(boolean displayOn) {
		this.displayOn = displayOn;

		if (displayOn) {
			command(Command.DISPLAY_ON);
		} else {
			command(Command.DISPLAY_OFF);
		}
	}

	/**
	 * Get the starting page for page addressing mode.
	 *
	 * @return The page to begin displaying at, from 0 to 7.
	 */
	public int getStartPage() {
		return startPage;
	}

	/**
	 * Set the starting page for page addressing mode.
	 *
	 * @param startPage
	 *            The page to begin displaying at, from 0 to 7. Values outside
	 *            this range will be clamped.
	 */
	public void setStartPage(int startPage) {
		startPage = clamp(0, 7, startPage);
		this.startPage = startPage;
		command(Command.SET_PAGE_START_ADDR | startPage);
	}

	/**
	 * Get the vertical flip state of the display.
	 *
	 * @return Whether the display is vertically flipped.
	 */
	public boolean isVFlipped() {
		return vFlipped;
	}

	/**
	 * Flip the display vertically.
	 *
	 * @param vFlipped
	 *            Whether to flip the display or return to normal.
	 */
	public void setVFlipped(boolean vFlipped) {
		this.vFlipped = vFlipped;

		if (vFlipped) {
			command(Command.SET_COM_SCAN_INC);
		} else {
			command(Command.SET_COM_SCAN_DEC);
		}
	}

	/**
	 * Get the display offset.
	 *
	 * @return The number of rows the display is offset by.
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Set the display offset.
	 *
	 * @param offset
	 *            The number of rows to offset the display by. Values outside of
	 *            this range will be clamped.
	 */
	public void setOffset(int offset) {
		offset = clamp(0, height - 1, offset);
		this.offset = offset;
		command(Command.SET_DISPLAY_OFFSET, offset);
	}

	/**
	 * Get hardware configuration of the display's COM pins.
	 *
	 * @return The COM pins configuration, one of
	 *         {@link Constant.COM_PINS_SEQUENTIAL},
	 *         {@link Constant.COM_PINS_SEQUENTIAL_LR},
	 *         {@link Constant.COM_PINS_ALTERNATING} or
	 *         {@link Constant.COM_PINS_ALTERNATING_LR}.
	 */
	public int getCOMPinsConfiguration() {
		return comPins;
	}

	/**
	 * Set the hardware configuration of the display's COM pins.
	 *
	 * @param comPins
	 *            The COM pins configuration. Must be one of
	 *            {@link Constant.COM_PINS_SEQUENTIAL},
	 *            {@link Constant.COM_PINS_SEQUENTIAL_LR},
	 *            {@link Constant.COM_PINS_ALTERNATING} or
	 *            {@link Constant.COM_PINS_ALTERNATING_LR}.
	 */
	public void setCOMPinsConfiguration(int comPins) {
		if (comPins == Constant.COM_PINS_SEQUENTIAL || comPins == Constant.COM_PINS_SEQUENTIAL_LR || comPins == Constant.COM_PINS_ALTERNATING || comPins == Constant.COM_PINS_ALTERNATING_LR) {
			this.comPins = comPins;
			command(Command.SET_COM_PINS, comPins);
		}
	}

	/**
	 * No operation.
	 */
	public void noOp() {
		command(Command.NOOP);
	}

	/**
	 * Get a pixel in the buffer.
	 *
	 * @param x
	 *            The X position of the pixel to set.
	 * @param y
	 *            The Y position of the pixel to set.
	 *
	 * @return False if the pixel is "off" or the given coordinates are out of
	 *         bounds, true if the pixel is "on".
	 */
	public boolean getPixel(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}

		return (buffer[x + (y / 8) * width] & (1 << (y & 7))) != 0;
	}

	/**
	 * Set a pixel in the buffer.
	 *
	 * @param x
	 *            The X position of the pixel to set.
	 * @param y
	 *            The Y position of the pixel to set.
	 * @param on
	 *            Whether to turn this pixel on or off.
	 *
	 * @return False if the given coordinates are out of bounds.
	 */
	public boolean setPixel(int x, int y, boolean on) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			return false;
		}

		//byte off =   (byte) ~(1 << (y & 7));
		
		//byte bon =   (byte) (1 << (y & 7));
		int myY = (y / 8) * width;
		
		
		
		if (on) {
			buffer[x + myY] |= (1 << (y & 7));
		} else {
			buffer[x + myY] &= ~(1 << (y & 7));
		}

		return true;
	}

	/**
	 * Get the display buffer.
	 *
	 * @return The display buffer.
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	/**
	 * Set the display buffer.
	 *
	 * @param buffer
	 *            The buffer to set.
	 */
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	/**
	 * Send a command to the display.
	 *
	 * @param command
	 *            The command to send.
	 * @param params
	 *            Any parameters the command requires.
	 */
	public void command(int command, int... params) {
		transport.command(command, params);
	}

	/**
	 * Send pixel data to the display.
	 *
	 * @param data
	 *            The data to send.
	 */
	public void data(byte[] data) {
		transport.data(data);
	}
	
	public void data(ByteBuffer data) {
		transport.data(data);
	}

	/**
	 * Get the Graphics instance, creating it if necessary.
	 *
	 * @return The Graphics instance.
	 */
	public final Graphics getGraphics() {
		if (graphics == null) {
			graphics = new Graphics(this);
		}
		return graphics;
	}
	

	/**
	 * Clamp the given value to a specified range.
	 *
	 * @param min
	 *            The minimum value.
	 * @param max
	 *            The maximum value.
	 * @param value
	 *            The value to clamp.
	 *
	 * @return The value clamped to the minimum and maximum values.
	 */
	private int clamp(int min, int max, int value) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		}

		return value;
	}
}
