package org.rpi.plugin.oled.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

/**
 * A {@link Transport} implementation that utilises SPI.
 *
 * @author fauxpark
 */
public class SPITransport implements Transport {
	/**
	 * The internal GPIO instance.
	 */
	private GpioController gpio;

	/**
	 * The GPIO pin corresponding to the RST line on the display.
	 */
	private GpioPinDigitalOutput rstPin;

	/**
	 * The GPIO pin corresponding to the D/C line on the display.
	 */
	private GpioPinDigitalOutput dcPin;

	/**
	 * The internal SPI device.
	 */
	private SpiDevice spi;

	/**
	 * SPITransport constructor.
	 *
	 * @param channel The SPI channel to use.
	 * @param rstPin The GPIO pin to use for the RST line.
	 * @param dcPin The GPIO pin to use for the D/C line.
	 */
	public SPITransport(SpiChannel channel, Pin rstPin, Pin dcPin) {
		gpio = GpioFactory.getInstance();
		this.rstPin = gpio.provisionDigitalOutputPin(rstPin);
		this.dcPin = gpio.provisionDigitalOutputPin(dcPin);

		try {
			spi = SpiFactory.getInstance(channel, 8000000);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void reset() {
		try {
			rstPin.setState(true);
			Thread.sleep(1);
			rstPin.setState(false);
			Thread.sleep(10);
			rstPin.setState(true);
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		gpio.shutdown();
	}

	@Override
	public void command(int command, int... params) {
		dcPin.setState(false);

		try {
			spi.write((byte) command);
		} catch(IOException e) {
			e.printStackTrace();
		}

		for(int param : params) {
			try {
				spi.write((byte) param);
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void data(byte[] data) {
		dcPin.setState(true);

		try {
			spi.write(data);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void data(ByteBuffer data) {
		dcPin.setState(true);

		try {
			spi.write(data);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void myData(byte[] data) {
		// TODO Auto-generated method stub
		
	}
}
