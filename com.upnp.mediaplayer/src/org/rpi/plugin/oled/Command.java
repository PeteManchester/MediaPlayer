package org.rpi.plugin.oled;
/**
 * This class defines the commands that can be sent to the SSD1306.
 * Some of them are standalone commands and others require arguments following them.
 * <br/>
 * Please refer to <a href="https://www.adafruit.com/datasheets/SSD1306.pdf">the SSD1306 datasheet</a>
 * for more information.
 *
 * @author fauxpark
 */
public class Command {
	/**
	 * Set the lower column start address for page addressing mode.
	 * OR this command with 0x00 to 0x0F (0 to 15) to set the desired value.
	 */
	public static final int SET_LOWER_COL_START                  = 0x00;

	/**
	 * Set the higher column start address for page addressing mode.
	 * OR this command with 0x00 to 0x0F (0 to 15) to set the desired value.
	 */
	public static final int SET_HIGHER_COL_START                 = 0x10;

	/**
	 * Set the memory addressing mode.
	 *
	 * @see Constant#MEMORY_MODE_HORIZONTAL
	 * @see Constant#MEMORY_MODE_VERTICAL
	 * @see Constant#MEMORY_MODE_PAGE
	 */
	public static final int SET_MEMORY_MODE                      = 0x20;

	/**
	 * Set the column start and end address of the display.
	 */
	public static final int SET_COLUMN_ADDRESS                   = 0x21;

	/**
	 * Set the page start and end address of the display.
	 */
	public static final int SET_PAGE_ADDRESS                     = 0x22;

	/**
	 * Set the display to scroll to the right.
	 */
	public static final int RIGHT_HORIZONTAL_SCROLL              = 0x26;

	/**
	 * Set the display to scroll to the left.
	 */
	public static final int LEFT_HORIZONTAL_SCROLL               = 0x27;

	/**
	 * Set the display to scroll vertically and to the right.
	 */
	public static final int VERTICAL_AND_RIGHT_HORIZONTAL_SCROLL = 0x29;

	/**
	 * Set the display to scroll vertically and to the left.
	 */
	public static final int VERTICAL_AND_LEFT_HORIZONTAL_SCROLL  = 0x2A;

	/**
	 * Turn off scrolling of the display.
	 */
	public static final int DEACTIVATE_SCROLL                    = 0x2E;

	/**
	 * Turn on scrolling of the display.
	 */
	public static final int ACTIVATE_SCROLL                      = 0x2F;

	/**
	 * Set the starting row of the display buffer.
	 * OR this command with 0x00 to 0x3F (0 to 63) to set the desired value.
	 */
	public static final int SET_START_LINE                       = 0x40;

	/**
	 * Set the contrast of the display.
	 */
	public static final int SET_CONTRAST                         = 0x81;

	/**
	 * Sets the charge pump regulator state.
	 *
	 * @see Constant#CHARGE_PUMP_DISABLE
	 * @see Constant#CHARGE_PUMP_ENABLE
	 */
	public static final int SET_CHARGE_PUMP                      = 0x8D;

	/**
	 * Map column address 0 to SEG0.
	 * This command is used for horizontally flipping the display.
	 */
	public static final int SET_SEGMENT_REMAP                    = 0xA0;

	/**
	 * Map column address 127 to SEG0.
	 * This command is used for horizontally flipping the display.
	 */
	public static final int SET_SEGMENT_REMAP_REVERSE            = 0xA1;

	/**
	 * Set the offset and number of rows in the vertical scrolling area.
	 */
	public static final int SET_VERTICAL_SCROLL_AREA             = 0xA3;

	/**
	 * Turn on the display with the buffer contents.
	 */
	public static final int DISPLAY_ALL_ON_RESUME                = 0xA4;

	/**
	 * Turn on the entire display, ignoring the buffer contents.
	 */
	public static final int DISPLAY_ALL_ON                       = 0xA5;

	/**
	 * Set the display to normal mode, where a 1 in the buffer represents a lit pixel.
	 */
	public static final int NORMAL_DISPLAY                       = 0xA6;

	/**
	 * Set the display to inverse mode, where a 1 in the buffer represents an unlit pixel.
	 */
	public static final int INVERT_DISPLAY                       = 0xA7;

	/**
	 * Set the multiplex ratio of the display.
	 */
	public static final int SET_MULTIPLEX_RATIO                  = 0xA8;

	/**
	 * Turn the display off (sleep mode).
	 */
	public static final int DISPLAY_OFF                          = 0xAE;

	/**
	 * Turn the display on.
	 */
	public static final int DISPLAY_ON                           = 0xAF;

	/**
	 * Set the page start address for page addressing mode.
	 * OR this command with 0x00 to 0x07 (0 to 7) to set the desired value.
	 */
	public static final int SET_PAGE_START_ADDR                  = 0xB0;

	/**
	 * Set the row output scan direction from COM0 to COM63.
	 * This command is used for vertically flipping the display.
	 */
	public static final int SET_COM_SCAN_INC                     = 0xC0;

	/**
	 * Set the row output scan direction from COM63 to COM0.
	 * This command is used for vertically flipping the display.
	 */
	public static final int SET_COM_SCAN_DEC                     = 0xC8;

	/**
	 * Set the display offset.
	 * Maps the display start line to the specified row.
	 */
	public static final int SET_DISPLAY_OFFSET                   = 0xD3;

	/**
	 * Set the display clock divide ratio and oscillator frequency.
	 * The divide ratio makes up the lower four bits.
	 */
	public static final int SET_DISPLAY_CLOCK_DIV                = 0xD5;

	/**
	 * Set the duration of the pre-charge period.
	 */
	public static final int SET_PRECHARGE_PERIOD                 = 0xD9;

	/**
	 * Set the hardware configuration of the display's COM pins.
	 *
	 * @see Constant.COM_PINS_SEQUENTIAL
	 * @see Constant.COM_PINS_SEQUENTIAL_LR
	 * @see Constant.COM_PINS_ALTERNATING
	 * @see Constant.COM_PINS_ALTERNATING_LR
	 */
	public static final int SET_COM_PINS                         = 0xDA;

	/**
	 * Adjust the <code>V<sub>COMH</sub></code> regulator output.
	 *
	 * @see Constant#VCOMH_DESELECT_LEVEL_00
	 * @see Constant#VCOMH_DESELECT_LEVEL_20
	 * @see Constant#VCOMH_DESELECT_LEVEL_30
	 */
	public static final int SET_VCOMH_DESELECT                   = 0xDB;

	/**
	 * No operation.
	 */
	public static final int NOOP                                 = 0xE3;
}
