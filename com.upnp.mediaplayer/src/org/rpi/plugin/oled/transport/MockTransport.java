package org.rpi.plugin.oled.transport;

import java.nio.ByteBuffer;

/**
 * A {@link Transport} implementation that does nothing. This is useful for testing on platforms other than the Raspberry Pi.
 *
 * @author fauxpark
 */
public class MockTransport implements Transport {
	
	private static final int DC_BIT = 6;
	
	int iCount = 0;
	
	public MockTransport() {
		System.out.println("WARNING YOU ARE USING THE MOCK TRANSPORT!!!!!!!");
	}
	
	@Override
	public void reset() {}

	@Override
	public void shutdown() {}

	@Override
	public void command(int command, int... params) {}
	
	public void data(ByteBuffer data) {
		byte[] dataBytes = new byte[ 1];
		dataBytes[0] = (byte) (1 << DC_BIT);	
		int length = data.capacity();
		ByteBuffer bb = ByteBuffer.allocate(dataBytes.length + length );
		bb.put(dataBytes);
		bb.put(data);
		System.out.println("Doh You are using the MockTransport");
	}

	@Override
	public void data(byte[] data) {
		//int length = data.length;
		//byte[] dataBytes = new byte[data.length + 1];
		byte[] dataBytes = new byte[ 1];
		dataBytes[0] = (byte) (1 << DC_BIT);
		
		ByteBuffer bb = ByteBuffer.allocate(dataBytes.length + data.length );
		bb.put(dataBytes);
		bb.put(data);
;
		byte[] result = bb.array();
		if(iCount == 0) {
			System.out.println("Doh You are using the MockTransport");
		}
		
		iCount++;
		if(iCount == 1000) {
			iCount = 0;
		}

		//for(int i = 0; i < data.length; i++) {
		//	dataBytes[i + 1] = data[i];
		//}
		
		//try {
			//i2c.write(dataBytes);
		//} catch(IOException e) {
		//	e.printStackTrace();
		//}
	}

	@Override
	public void myData(byte[] data) {
		// TODO Auto-generated method stub
		
	}
}
