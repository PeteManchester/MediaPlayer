package org.rpi.plugin.oled;
/**
 * This class defines some useful constants, such as memory addressing modes, scrolling speeds and dummy bytes.
 *
 * @author fauxpark
 */
public class Constant {
	/**
	 * A dummy byte consisting of all zeroes.
	 */
	public static final int DUMMY_BYTE_00           = 0x00;

	/**
	 * A dummy byte consisting of all ones.
	 */
	public static final int DUMMY_BYTE_FF           = 0xFF;

	/**
	 * Horizontal memory addressing mode.
	 * In this mode, after reading/writing the display RAM, the column address pointer is incremented.
	 * When the pointer reaches the end, it is reset to the start address on the next page.
	 */
	public static final int MEMORY_MODE_HORIZONTAL  = 0x00;

	/**
	 * Vertical memory addressing mode.
	 * In this mode, after reading/writing the display RAM, the page address pointer is incremented.
	 * When the pointer reaches the end, it is reset to the start address on the next column.
	 */
	public static final int MEMORY_MODE_VERTICAL    = 0x01;

	/**
	 * Page memory addressing mode.
	 * In this mode, after reading/writing the display RAM, the column address pointer is incremented.
	 * When the pointer reaches the end, it is reset to the start address on the same page.
	 */
	public static final int MEMORY_MODE_PAGE        = 0x02;

	/**
	 * Disable the charge pump regulator.
	 */
	public static final int CHARGE_PUMP_DISABLE     = 0x10;

	/**
	 * Enable the charge pump regulator.
	 */
	public static final int CHARGE_PUMP_ENABLE      = 0x14;

	/**
	 * Sequential COM pin hardware configuration.
	 * With {@link Command.SET_COM_SCAN_INC} issued, rows 0 - 63 on the display correspond to COM0 - COM63.
	 */
	public static final int COM_PINS_SEQUENTIAL     = 0x02;

	/**
	 * Sequential COM pin hardware configuration with left/right remap.
	 * With {@link Command.SET_COM_SCAN_INC} issued, rows 0 - 31 on the display correspond to COM32 - COM63, and rows 32 - 63 correspond to COM0 - COM31.
	 */
	public static final int COM_PINS_SEQUENTIAL_LR  = 0x22;

	/**
	 * Alternating COM pin hardware configuration.
	 * With {@link Command.SET_COM_SCAN_INC} issued, row 0 on the display corresponds to COM0, row 1 to COM32, row 2 to COM2, row 3 to COM33, etc.
	 */
	public static final int COM_PINS_ALTERNATING    = 0x12;

	/**
	 * Alternating COM pin hardware configuration with left/right remap.
	 * With {@link Command.SET_COM_SCAN_INC} issued, row 0 on the display corresponds to COM32, row 1 to COM0, row 2 to COM33, row 3 to COM1, etc.
	 */
	public static final int COM_PINS_ALTERNATING_LR = 0x32;

	/**
	 * A VCOMH deselect level of ~0.65 &times; <code>V<sub>CC</sub></code>.
	 */
	public static final int VCOMH_DESELECT_LEVEL_00 = 0x00;

	/**
	 * A VCOMH deselect level of ~0.77 &times; <code>V<sub>CC</sub></code>.
	 */
	public static final int VCOMH_DESELECT_LEVEL_20 = 0x20;

	/**
	 * A VCOMH deselect level of ~0.83 &times; <code>V<sub>CC</sub></code>.
	 */
	public static final int VCOMH_DESELECT_LEVEL_30 = 0x30;

	/**
	 * Scroll by one pixel every 5 frames.
	 */
	public static final int SCROLL_STEP_5           = 0x00;

	/**
	 * Scroll by one pixel every 64 frames.
	 */
	public static final int SCROLL_STEP_64          = 0x01;

	/**
	 * Scroll by one pixel every 128 frames.
	 */
	public static final int SCROLL_STEP_128         = 0x02;

	/**
	 * Scroll by one pixel every 256 frames.
	 */
	public static final int SCROLL_STEP_256         = 0x03;

	/**
	 * Scroll by one pixel every 3 frames.
	 */
	public static final int SCROLL_STEP_3           = 0x04;

	/**
	 * Scroll by one pixel every 4 frames.
	 */
	public static final int SCROLL_STEP_4           = 0x05;

	/**
	 * Scroll by one pixel every 25 frames.
	 */
	public static final int SCROLL_STEP_25          = 0x06;

	/**
	 * Scroll by one pixel every 2 frames.
	 */
	public static final int SCROLL_STEP_2           = 0x07;
}
