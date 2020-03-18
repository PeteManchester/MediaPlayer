package org.rpi.songcast.ohu.sender.response;

import java.net.InetSocketAddress;
import java.util.List;

import org.apache.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class OHUSenderSlave {
	
	private String header = "Ohm ";
	private Logger log = Logger.getLogger(this.getClass());
	private ByteBuf buffer = null;
	private int numSlaves = 0;
	
	public OHUSenderSlave(List<InetSocketAddress> slaves) {
		byte[] version = new byte[] { (byte) (1 & 0xff) };
		byte[] type = new byte[] { (byte) (6 & 0xff) };
		numSlaves = slaves.size();
		int length = header.length() + 1 + 1 + 2 + 4 + (numSlaves * 6) ;

		ByteBuf test = Unpooled.buffer(length);
		log.debug("Add Slave. Number of Slaves: " + numSlaves);
		test.setBytes(0, header.getBytes(CharsetUtil.UTF_8));
		test.setBytes(4, version);
		test.setBytes(5, type);
		test.setShort(6, length);		
		//test.setBytes(8, new byte[] {(byte) (50 &0xff)});//Header Length
		
		test.setInt(8, numSlaves);
		int iCount =  0;
		for(InetSocketAddress s : slaves) {			
			byte[] addr = new byte[4];
			addr = s.getAddress().getAddress();
			test.setBytes(12 + (iCount * 6), addr);	
			log.debug("Add Slave: " + s.getAddress().getAddress() + " Port: " + s.getPort() + " iCount: " + iCount);
			//for (byte b : addr) {
			 //   System.out.println(b & 0xFF);
			//}			
			test.setShort(12 + (iCount * 6) +4 , s.getPort());
			iCount++;
		}
		
		buffer = Unpooled.copiedBuffer(test.array());
		test.release();
	}
	
	/**
	 * @return the buffer
	 */
	public ByteBuf getBuffer() {
		return buffer;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("OHUSenderSlave [numSlaves=");
		builder.append(numSlaves);
		builder.append("]");
		return builder.toString();
	}

}
