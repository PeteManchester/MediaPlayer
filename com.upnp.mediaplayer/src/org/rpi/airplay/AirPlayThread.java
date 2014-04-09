package org.rpi.airplay;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.rpi.utils.SecUtils;
import org.rpi.utils.Utils;

/**
 * LaunchThread class which starts services
 * 
 * @author bencall
 * 
 */
public class AirPlayThread extends Thread {
	private Logger log = Logger.getLogger(this.getClass());
	private List<BonjourEmitter> emitter = new ArrayList<BonjourEmitter>();
	//private ServerSocket servSock = null;
	private String name;
	private String password;
	private boolean stopThread = false;
	private byte[] hwAddr = null;
	
	private static ChannelGroup s_allChannels = new DefaultChannelGroup();
	
	/**
	 * Global executor service. Used e.g. to initialize the various netty channel factories 
	 */
	public static final ExecutorService ExecutorService = Executors.newCachedThreadPool();

	/**
	 * Channel execution handler. Spreads channel message handling over multiple threads
	 */
	public static final ExecutionHandler ChannelExecutionHandler = new ExecutionHandler(
		new OrderedMemoryAwareThreadPoolExecutor(4, 0, 0)
	);
	
	/**
	 * Channel handle that registers the channel to be closed on shutdown
	 */
	public static final ChannelHandler CloseChannelOnShutdownHandler = new SimpleChannelUpstreamHandler() {
		@Override
		public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
			s_allChannels.add(e.getChannel());
			super.channelOpen(ctx, e);
		}
	};

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public AirPlayThread(String name) {
		super();
		this.name = name;
	}

	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public AirPlayThread(String name, String pass) {
		super();
		this.name = name;
		this.password = pass;
	}
	
	public byte[] getHardwareAddress() {
	    if (hwAddr == null || hwAddr.length == 0) {
	      // MAC couldn't be determined
	      try {
	        InetAddress local = InetAddress.getLocalHost();
	        NetworkInterface ni = NetworkInterface.getByInetAddress(local);
	        if (ni != null) {
	          hwAddr = ni.getHardwareAddress();
	          return hwAddr;
	        }
	      } catch (Exception e) {
	        log.error("Error Getting HardwareAddress:",e);
	      }
	      log.debug("Could not get HardwareAddress, create a Randon one");
	      Random rand = new Random();
	      byte[] mac = new byte[8];
	      rand.nextBytes(mac);
	      mac[0] = 0x00;
	      hwAddr = mac;
	    }
	    return hwAddr;
	  }

	private String getStringHardwareAdress(byte[] hwAddr) {
		StringBuilder sb = new StringBuilder();

		for (byte b : hwAddr)
			sb.append(String.format("%02x", b));

		return sb.toString();
	}

	public void run() {
		log.debug("Starting AirPlay Service...");
		// For the Raspi we have to do this now, because for some reason it is
		// very slow the first time it is run and if we run it when we get an
		// AirPlay connection the connection times out.
		log.debug("Create BouncyCastleProvider");
		Security.addProvider(new BouncyCastleProvider());
		log.debug("Created BouncyCastleProvider");
		log.debug("Initiate an encrypt");
		byte[] test = new byte[] { (byte) 0xe0, 0x4f, (byte) 0xd0, 0x20, (byte) 0xea, 0x3a, 0x69, 0x10, (byte) 0xa2, (byte) 0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte) 0x9d };
		SecUtils.encryptRSA(test);

		int port = 5004;
		try {
			// DNS Emitter (Bonjour)
			byte[] hwAddr = getHardwareAddress();
			log.debug("Check if Passsword is set");
			boolean bPassword = false;
			if (!Utils.isEmpty(password)) {
				bPassword = true;
			}
			AudioSessionHolder.getInstance().setHardWareAddress(hwAddr);
			
			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
			bootstrap.setOption("reuseAddress", true);
			bootstrap.setOption("child.tcpNoDelay", true);
			bootstrap.setOption("child.keepAlive", true);
			bootstrap.setPipelineFactory(new RtspServerPipelineFactory());
			s_allChannels.add(bootstrap.bind(new InetSocketAddress(port)));
			log.debug("Registering AirTunes Services");
			for (final NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (iface.isLoopback())
					continue;
				if (iface.isPointToPoint())
					continue;
				if (!iface.isUp())
					continue;

				for (final InetAddress addr : Collections.list(iface.getInetAddresses())) {
					if (!(addr instanceof Inet4Address) && !(addr instanceof Inet6Address))
						continue;

					try {
						/* Create mDNS responder for address */
						BonjourEmitter be = new BonjourEmitter(name, getStringHardwareAdress(hwAddr), port, bPassword, addr);
						emitter.add(be);
						log.debug("Registered AirTunes service '" + name + "' on " + addr);
					} catch (final Throwable e) {
						log.error("Failed to publish service on " + addr.toString() , e);
					}
				}
			}
			log.debug("Finished Registering AirTunes Services");

			while (!Thread.interrupted()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore
				}
			}


		} catch (Exception e) {
			log.error(e);

		} finally {
			try {
				closeBonjourServices();
				closeRTSPServer();
				//servSock.close();
			} catch (Exception e) {
				log.error(e);
			}
		}
	}

	private synchronized void closeBonjourServices() {
		try {
			for (BonjourEmitter be : emitter) {
				try {
					be.stop();
				} catch (Exception e) {
					log.error("Error Stopping BonjourService", e);
				}
			}

			log.info("Bonjur Service stopped.");
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private synchronized void closeRTSPServer()
	{
		log.debug("Close RTSP Server");
		final ChannelGroupFuture allChannelsClosed = s_allChannels.close();
		/* Wait for all channels to finish closing */
		allChannelsClosed.awaitUninterruptibly();
		
		/* Stop the ExecutorService */
		ExecutorService.shutdown();

		/* Release the OrderedMemoryAwareThreadPoolExecutor */
		ChannelExecutionHandler.releaseExternalResources();
	}

	/**
	 * Stop Our Thread
	 */
	public synchronized void stopThread() {
		log.debug("AirplayThread Shutdown...");
		stopThread = true;
		closeBonjourServices();
		closeRTSPServer();
	}
}
