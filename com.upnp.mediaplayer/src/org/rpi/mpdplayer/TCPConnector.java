package org.rpi.mpdplayer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;
import org.rpi.config.Config;
import org.rpi.player.IPlayer;
import org.rpi.player.events.EventBase;

public class TCPConnector extends Observable implements Observer {

	private static Logger log = Logger.getLogger(TCPConnector.class);

	final String MPD_OK = "OK";
	final String list_begin = "command_list_begin";
	final String list_end = "command_list_end";

	private String host = "";
	private int port = 6600;
	private int timeout = 3000;
	private StatusMonitor sm = null;
	private Thread th = null;

	private Socket socket = null;
	private IPlayer iPlayer = null;

	public TCPConnector(IPlayer iPlayer) {
		this.iPlayer = iPlayer;
		this.host = Config.mpd_host;
		this.port = Config.mpd_port;
		sm = new StatusMonitor(this);
		sm.addObserver(this);
		th = new Thread(sm);
		th.start();
		try {
			connect();
		} catch (Exception e) {
			log.error(e);
		}
	}


	protected synchronized String connect() throws IOException {
		this.socket = new Socket();
		String version = "";
		SocketAddress sockaddr = new InetSocketAddress(host, port);
		try {
			this.socket.connect(sockaddr, timeout);
		} catch (SocketTimeoutException ste) {
			log.error(ste);
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		String line = in.readLine();
		if (isOK(line)) {
			version = removeText(line, MPD_OK);
			log.info("MPD Version: " + version);
		}
		return line;
	}

	public synchronized HashMap<String, String> sendCommand(String command) {
		// log.debug( command);
		HashMap<String, String> res = new HashMap<String, String>();
		if (!this.socket.isConnected()) {
			try {
				connect();
			} catch (IOException e) {
				log.error(e);
				try {
					connect();
				} catch (Exception ex) {

				}
				return res;
			}
		}
		DataOutputStream dOut = null;
		try {
			dOut = new DataOutputStream(socket.getOutputStream());
			byte[] bytesToSend = command.getBytes("UTF-8");
			dOut.write(bytesToSend);
			BufferedReader dIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String line = null;
			while ((line = dIn.readLine()) != null) {
				if (isOK(line))
					break;
				if (line.startsWith("ACK")) {
					log.error("Error: " + line);
					// Raise an Error
					break;
				}
				String[] splits = line.split(":");
				if (splits.length > 1) {
					String key = splits[0].trim();
					String value = line.substring(key.length() + 1, line.length());
					res.put(key, value.trim());
				}
			}
			return res;
		} catch (Exception e) {
			log.error(e);
			try {
				socket.close();
				connect();
			} catch (Exception ex) {
				log.error(ex);
			}
		} finally {
			if (dOut != null) {
				try {
					dOut.flush();
				} catch (IOException e) {
					log.error(e);
				}
			}
		}
		return res;
	}

	/***
	 * Create a command
	 * 
	 * @param command
	 * @return
	 */
	public String createCommand(String command) {
		return createCommand(command, null);
	}

	/***
	 * Create a command
	 * 
	 * @param command
	 * @param params
	 * @return
	 */
	public String createCommand(String command, List<String> params) {
		StringBuffer sb = new StringBuffer();
		sb.append(command);
		if (params != null) {
			for (String p : params) {
				if (p.contains(" ")) {
					p = "\"" + p + "\"";
				}
				sb.append(" " + p);
			}
		}

		sb.append("\n");
		return sb.toString();
	}

	/***
	 * Create a List of Commands.
	 * 
	 * @param commands
	 * @return
	 */
	public String createCommandList(List<String> commands) {
		StringBuffer sb = new StringBuffer();
		if (commands == null) {
			return null;
		}
		if (commands.size() == 1) {
			return commands.get(0);
		}
		sb.append(list_begin);
		sb.append("\n");
		for (String s : commands) {
			sb.append(s);
		}
		sb.append(list_end);
		sb.append("\n");
		return sb.toString();
	}


	/**
	 * Remove the String from the line of Text
	 * 
	 * @param line
	 * @param remove
	 * @return
	 */
	private String removeText(String line, String remove) {
		String res = "";
		try {
			res = line.substring(remove.length(), line.length()).trim();
		} catch (Exception e) {

		}
		return res;
	}

	/**
	 * Did the Server Return OK
	 * 
	 * @param line
	 * @return
	 */
	private boolean isOK(String line) {
		if (line.startsWith(MPD_OK)) {
			return true;
		}
		return false;
	}

	public IPlayer getiPlayer() {
		return iPlayer;
	}


	public void destroy() {
		th = null;
		if (socket.isConnected()) {
			try {
				socket.close();
			} catch (Exception e) {

			}
		}

	}

	@Override
	public void update(Observable arg0, Object ev) {
		EventBase e = (EventBase) ev;
		fireEvent(e);
	}

	private void fireEvent(EventBase ev) {
		setChanged();
		notifyObservers(ev);
	}

}
