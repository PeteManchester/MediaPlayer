package org.rpi.songcast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

class UDPReceiver implements Runnable {
	
	private Logger log = Logger.getLogger(this.getClass());

	int mcastPort = 0;

	InetAddress mcastAddr = null;

	InetAddress localHost = null;
	
	String zoneID = "";

	public UDPReceiver(int port, InetAddress addr, String zoneID) {

		mcastPort = port;

		mcastAddr = addr;
		this.zoneID = zoneID;

		try {
			//TODO set own IP Address
			localHost = InetAddress.getByName("192.168.1.72");

		} catch (UnknownHostException uhe) {

			log.error("Problems identifying local host",uhe);

		}

	}

	public void run() {

		MulticastSocket mSocket = null;

		try {

			log.debug("Setting up multicast receiver");

			mSocket = new MulticastSocket(mcastPort);
			log.debug("Joining Mutlicast Group: " + mcastAddr.getHostAddress() + ":" +  mcastPort);
			mSocket.joinGroup(mcastAddr);

		} catch (IOException ioe) {
			log.error("Trouble opening multicast port",ioe);

		}

		DatagramPacket packet;

		log.debug("Multicast receiver set up ");

		while (true) {

			try {

				byte[] buf = new byte[1000];

				packet = new DatagramPacket(buf, buf.length);

				log.debug("McastReceiver: waiting for packet");

				mSocket.receive(packet);
				
				log.debug("Received Packet: " + packet.getAddress().getHostAddress());

				//ByteArrayInputStream bistream =	new ByteArrayInputStream(packet.getData());

				//ObjectInputStream ois = new ObjectInputStream(bistream);

				//Integer value = (Integer) ois.readObject();
				
				byte[] data = packet.getData();
				
				//InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(data), Charset.forName("UTF-8"));
				
				StringBuilder sb = new StringBuilder();
			    for (byte b : data) {
			        sb.append(String.format("%02X ", b));
			    }
			    System.out.println(sb.toString());	
			    String text = new String(data, "UTF-8");
			    log.debug(text);				

				// ignore packets from myself, print the rest
				log.debug("Address: " + packet.getAddress().getHostAddress());
				if (!(packet.getAddress().equals(localHost))) {
					log.debug("Recevied from: " + packet.getAddress());
					//System.out.println("Received multicast packet: " +	value.intValue() + " from: " + packet.getAddress());

				}
				else
				{
					log.debug("That was a Localhost:");
				}

				//ois.close();

				//bistream.close();

			} catch (IOException ioe) {

				log.error("Trouble reading multicast message",ioe);

			} //catch (ClassNotFoundException cnfe) {

				//System.out.println("Class missing while reading mcast packet");

				//cnfe.printStackTrace();
				//System.exit(1);

			//}

		}

	}

}