package org.rpi.plugin.oled.transport;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

/**
 * A {@link Transport} implementation that utilises I<sup>2</sup>C.
 *
 * @author fauxpark
 */
public class I2CTransport implements Transport {
	/**
	 * The Data/Command bit position.
	 */
	private static final int DC_BIT = 6;

	/**
	 * The internal GPIO instance.
	 */
	private GpioController gpio;

	/**
	 * The GPIO pin corresponding to the RST line on the display.
	 */
	private GpioPinDigitalOutput rstPin;

	/**
	 * The internal I<sup>2</sup>C device.
	 */
	private I2CDevice i2c;
	
	private byte[] dataBytes = new byte[ 1];

	/**
	 * I2CTransport constructor.
	 *
	 * @param rstPin The GPIO pin to use for the RST line.
	 * @param bus The I<sup>2</sup>C bus to use.
	 * @param address The I<sup>2</sup>C address of the display.
	 */
	public I2CTransport(Pin rstPin, int bus, int address) {
		gpio = GpioFactory.getInstance();
		dataBytes[0] = (byte) (1 << DC_BIT);	
		this.rstPin = gpio.provisionDigitalOutputPin(rstPin);

		try {
			i2c = I2CFactory.getInstance(bus).getDevice(address);
		} catch(IOException | UnsupportedBusNumberException e) {
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
		byte[] commandBytes = new byte[params.length + 2];
		commandBytes[0] = (byte) (0 << DC_BIT);
		commandBytes[1] = (byte) command;

		for(int i = 0; i < params.length; i++) {
			commandBytes[i + 2] = (byte) params[i];
		}

		try {
			i2c.write(commandBytes);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void data(byte[] data) {
		/*
		byte[] dataBytes = new byte[data.length + 1];
		dataBytes[0] = (byte) (1 << DC_BIT);

		for(int i = 0; i < data.length; i++) {
			dataBytes[i + 1] = data[i];
		}
		*/
		
		
		dataBytes[0] = (byte) (1 << DC_BIT);		
		ByteBuffer bb = ByteBuffer.allocate(dataBytes.length + data.length );
		bb.put(dataBytes);
		bb.put(data);

		byte[] result = bb.array();
		try {
			i2c.write(result);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void data(ByteBuffer data) {
		//byte[] dataBytes = new byte[ 1];
		//dataBytes[0] = (byte) (1 << DC_BIT);		
		ByteBuffer bb = ByteBuffer.allocate(dataBytes.length + data.position() );
		bb.put(dataBytes);
		bb.put(data);

		byte[] result = bb.array();
		try {
			i2c.write(result);
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
}
