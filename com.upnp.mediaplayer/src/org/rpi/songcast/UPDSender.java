package org.rpi.songcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.log4j.Logger;

class UPDSender implements Runnable {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private boolean run = true;

	private DatagramSocket dgramSocket = null;
	
	private byte[] queryZone = null;

	int mcastPort = 0;

	InetAddress mcastAddr = null;

	InetAddress localHost = null;
	
	String zoneID = "";
	
	private Vector mWorkQueue = new Vector();
	
	public synchronized boolean isEmpty() {
		return mWorkQueue.isEmpty();
	}
	
	public synchronized void put(Object object) {
		log.debug("Put Object in WorkQueue " + object.toString());
		try {
			mWorkQueue.addElement(object);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}
	
	
	/**
	 * Get the first object out of the queue. Return null if the queue is empty.
	 */
	public synchronized Object get() {
		Object object = peek();
		if (object != null)
			mWorkQueue.removeElementAt(0);
		return object;
	}
	
	/**
	 * Peek to see if something is available.
	 */
	public Object peek() {
		if (isEmpty())
			return null;
		return mWorkQueue.elementAt(0);
	}
	
	private void sleep(int value) {
		try {
			Thread.sleep(value);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}
	}
	
	public synchronized void clear() {
		try {
			log.info("Clearing Work Queue. Number of Items: " + mWorkQueue.size());
			mWorkQueue.clear();
			log.info("WorkQueue Cleared");
		} catch (Exception e) {
			log.debug(e.getMessage(), e);
		}
	}

	UPDSender(int port, InetAddress addr,String zoneID) {

		mcastPort = port;

		mcastAddr = addr;
		
		this.zoneID = zoneID;

		try {

			dgramSocket = new DatagramSocket();

		} catch (IOException ioe) {

			log.error("problems creating the datagram socket.",ioe);

		}

		try {
			//TODO Set own IPAddress
			localHost = InetAddress.getByName("192.168.1.72");

		} catch (UnknownHostException uhe) {
			log.error("Problems identifying local host",uhe);
		}
		
//		String header = "4f686d200100";
//		//String header = "4f687a200100";
//		//String zone = "adb3ff3c41b7ebd669a49e35d54222ae";
//		//  String zone = "b33f69011e38827aa138adc6d00cb23e";
//		//String zone = "6164623366663363343162376562643636396134396533356435343232326165";
//		String zone = "6233336636393031316533383832376161313338616463366430306362323365";
//		String lengthPacket = "0000";
//		int zl = zone.length();
//		String lengthZone = "00000000";
//		int length = header.length() + lengthPacket.length() + lengthZone.length() + zone.length();
//		length = length/2;
//		lengthPacket = DecToHex(length, 4);
//		String sZL= DecToHex(zl/2, 8);
//		//byte[] queryZone = hexStringToByteArray("6f687a200100"+ "0024" + "00000010" + "adb3ff3c41b7ebd669a49e35d54222ae");
//		//queryZone = hexStringToByteArray(header+ lengthPacket +  sZL + zone);
//		queryZone = hexStringToByteArray(header+ lengthPacket);
//		byte[] join =  hexStringToByteArray("6f686d2001000008");
//		byte[] listen = hexStringToByteArray("6f686d2001010008");
//		//DatagramPacket hi = new DatagramPacket(queryZone, queryZone.length, group, port);

	}
	
	public void run() {
		while (run) {
			if (!isEmpty()) {
				try {
					byte[] command = (byte[]) get();
					log.info("Pulled Command Subscription ");
					processEvent(command);
					log.info("Number of Commands in Queue: " + mWorkQueue.size());
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			} else {
				sleep(100);
			}
		}
	}
	
	
	private void processEvent(byte[]  bytes) {
		try
		{
		if(bytes ==null)
			return;
		log.debug("Sending Command to : " + mcastAddr.getHostAddress() + ":" + mcastPort + " bytes: " + bytes );
		DatagramPacket packet = new DatagramPacket(bytes,	bytes.length,	mcastAddr,	mcastPort);

		dgramSocket.send(packet);
		
		StringBuilder sb = new StringBuilder();
	    for (byte b : bytes) {
	        sb.append(String.format("%02X ", b));
	    }

		log.debug("sending multicast message: " + sb.toString());
		}
		catch(Exception e)
		{
			log.error("Error Sending Message: " ,e);
		}
	}

//	public void run() {
//
//		DatagramPacket packet = null;
//
//		int count = 0;
//
//		// send multicast msg once per second
//
//		while (true) {
//
//			// careate the packet to sned.
//
//			try {
//
//				// serialize the multicast message
//
//				//ByteArrayOutputStream bos = new ByteArrayOutputStream();
//
//				//ObjectOutputStream out = new ObjectOutputStream(bos);
//
//				//out.writeObject(new Integer(count++));
//
//				//out.flush();
//
//				//out.close();
//
//				// Create a datagram packet and send it
//
//				packet = new DatagramPacket(queryZone,	queryZone.length,	mcastAddr,	mcastPort);
//
//				// send the packet
//
//				dgramSocket.send(packet);
//				
//				StringBuilder sb = new StringBuilder();
//			    for (byte b : queryZone) {
//			        sb.append(String.format("%02X ", b));
//			    }
//
//				System.out.println("sending multicast message: " + sb.toString());
//
//				Thread.sleep(10000);
//
//			} catch (InterruptedException ie) {
//
//				ie.printStackTrace();
//
//			} catch (IOException ioe) {
//
//				System.out.println("error sending multicast");
//
//				ioe.printStackTrace();
//				System.exit(1);
//
//			}
//
//		}
//
//	}
	
	private  byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	  private  String convertHexToString(String hex){
		  
		  StringBuilder sb = new StringBuilder();
		  StringBuilder temp = new StringBuilder();
	 
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=2 ){
	 
		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 2));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
		      //convert the decimal to character
		      sb.append((char)decimal);
	 
		      temp.append(decimal);
		  }
		  System.out.println("Decimal : " + temp.toString());
	 
		  return sb.toString();
	  }
	  
	  private String DecToHex(int number, int length)
	  {
		  StringBuilder sb = new StringBuilder();
		  sb.append(Integer.toHexString(number));
		  while (sb.length() < length) {
		      sb.insert(0, '0'); // pad with leading zero if needed
		  }
		  String hex = sb.toString();
		  return hex;
	  }

}