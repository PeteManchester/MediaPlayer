package org.rpi.pins;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.rpi.config.Config;
import org.rpi.player.observers.ObservablePins;
import org.rpi.utils.Utils;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/***
 * A class that handles the Pin service, using both a local file and if
 * configured a web service based on Socket.io
 * 
 * @author phoyle
 *
 */

public class PinManager {

	private Logger log = Logger.getLogger(this.getClass());

	Map<Integer, PinInfo> devicePins = new ConcurrentHashMap<Integer, PinInfo>();

	Map<Integer, PinInfo> accountPins = new ConcurrentHashMap<Integer, PinInfo>();

	private String originalPins = "";

	// private String pinManagerurl =
	// "http://192.168.1.205:8081/PinServiceManager1/webapi/broadcast";
	private String pinManagerURL = Config.getInstance().getPinsServiceURL();

	private static PinManager instance = null;
	private ObservablePins obsvPinsChanged = new ObservablePins();

	private Socket socket = null;

	public static PinManager getInstance() {
		if (instance == null) {
			instance = new PinManager();
		}
		return instance;
	}

	private PinManager() {
		if (!Utils.isEmpty(pinManagerURL)) {
			log.info("PinManger URL is configured, connect to the service");
			connectToServer();
		} else {
			log.info("PinManger URL is not configured, do  NOT connect to the service");
		}
	}

	private void connectToServer() {
		try {
			URL url = new URL(pinManagerURL);
			String path = url.getPath();
			String host = url.getHost();
			Options options = new Options();
			options.path = path;
			options.transports = new String[] { "websocket" };
			String sUrl = pinManagerURL.replace(path, "");
			socket = IO.socket(sUrl, options);
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					log.debug("Connected: " + socket.connected());
					// socket.emit("foo", "hi");
				}

			}).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Recieved: " + args);
					String eventType = (String) args[0];
					switch (eventType.toUpperCase()) {
					case "PINS":
						try {
							JSONObject pins = (JSONObject) args[1];
							String json = pins.toString();
							if (!json.equalsIgnoreCase(originalPins)) {
								log.debug("Pins Config has changed, update Pins");
								originalPins = json;
								EventPinsChanged ev = new EventPinsChanged();
								ev.setPinInfo(json);
								obsvPinsChanged.notifyChange(ev);
							}
						} catch (Exception e) {
							log.error("Could not process event", e);
						}

						break;
					}
				}

			}).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("DisConnected: " + socket.connected());
				}

			}).on(Socket.EVENT_RECONNECT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Reconnect");

				}
			}).on(Socket.EVENT_RECONNECT_ATTEMPT, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Reconnect Attempt: " + args);

				}
			}).on("pins", new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Pins updated: " + args[0].toString());
					try {
						if (args[0] instanceof JSONArray) {
							JSONArray pins = (JSONArray) args[0];
							String json = pins.toString();
							if (!json.equalsIgnoreCase(originalPins)) {
								log.debug("Pins Config has changed, update Pins");
								originalPins = json;
								EventPinsChanged ev = new EventPinsChanged();
								ev.setPinInfo(json);
								obsvPinsChanged.notifyChange(ev);
							}
						}
					} catch (Exception e) {
						log.error("Could not process event", e);
					}

				}
			});
			socket.connect();
			log.debug("Status: " + socket.toString());
		} catch (Exception e) {
			log.error("Error: ", e);
		}
	}

	/***
	 * Get the configured Pins
	 * 
	 * @return
	 */
	public String getPins() {
		String res = null;
		if (!Utils.isEmpty(pinManagerURL)) {
			res = originalPins;
		}
		if (Utils.isEmpty(res)) {
			res = readFromFile("pins.json");
			originalPins = res;
		}
		return res;
	}

	public void SavePins(String json) {
		saveToFile(json);
		if (!Utils.isEmpty(pinManagerURL)) {
			saveToCloud(json);
		}
		/*
		 * if(!json.equalsIgnoreCase(originalPins)) { EventPinsChanged ev = new
		 * EventPinsChanged(); ev.setPinInfo(json);
		 * obsvPinsChanged.notifyChange(ev); originalPins = json; }
		 */
	}

	/***
	 * Read the Pins info from the file
	 * 
	 * @return
	 */
	private String readFromFile(String fileName) {
		log.debug("Got to readFromFile");
		String content = null;
		try {
			content = new String(Files.readAllBytes(Paths.get(fileName)));
		} catch (Exception e) {
			log.error("Error Reading from File: pins.json");
		}
		return content;
	}

	/***
	 * Read the Pin information from the cloud
	 * 
	 * @return
	 */
	/*
	 * private String readFromCloud() { String res = null; try {
	 * log.debug("Read From Cloud"); String url = pinManagerURL + "/read";
	 * log.debug("Read From Cloud Start of creating Client: " + url); // Client
	 * client = ClientBuilder.newClient(new ClientConfig()); WebTarget webTarget
	 * = client.target(url); log.debug("Created WebTarget"); Builder
	 * invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
	 * log.debug("Created Builder"); Response response =
	 * invocationBuilder.get(); log.debug("Called GET()"); res =
	 * response.readEntity(String.class);
	 * log.debug("Read From Cloud Result URL: " + res); // client.close(); }
	 * catch (Exception e) { log.error("Error ReadFromCloud", e); } return res;
	 * }
	 */

	private void saveToFile(String json) {
		try {
			log.debug("Save to Pins.json: " + json);
			FileWriter fw = new FileWriter("pins.json");
			fw.write(json);
			fw.close();
		} catch (Exception e) {
			log.error("SaveToFile", e);
		}

	}

	/***
	 * 
	 * @param json
	 */
	private void saveToCloud(String json) {
		try {
			/*
			 * String res = null; log.debug("Save to Cloud"); String url =
			 * pinManagerURL + "/save"; //
			 * log.debug("Save to Start to Create Client"); // Client client =
			 * ClientBuilder.newClient(new ClientConfig());
			 * log.debug("Save to Created Client"); WebTarget webTarget =
			 * client.target(url); log.debug("Save to Cloud Created Target");
			 * Builder invocationBuilder =
			 * webTarget.request(MediaType.TEXT_PLAIN);
			 * log.debug("Save to Cloud Created Builder"); Response response =
			 * invocationBuilder.post(Entity.entity(json,
			 * MediaType.TEXT_PLAIN)); log.debug("Save to Cloud After POST");
			 * res = response.readEntity(String.class);
			 * log.debug("Save to Cloud " + res); // client.close();
			 * 
			 */
			socket.emit("save", json);
		} catch (Exception e) {
			log.error("SaveToCloud", e);
		}

	}

	/*
	 * public void registerForEvent() { if(Utils.isEmpty(pinManagerURL)) { log.
	 * info("Pins option 'pins_service_url' is not conifgured, do not register"
	 * ); return; } try { client.register(new MyLogFilter()); WebTarget target =
	 * client.target(pinManagerURL); eventSource =
	 * EventSource.target(target).reconnectingEvery(1,
	 * TimeUnit.SECONDS).build(); eventSource.register(this);
	 * eventSource.open(); log.warn("Connected to PinService: " +
	 * pinManagerURL);
	 * 
	 * } catch (Exception e) { log.error("Error Registering PinService", e); } }
	 */

	public void unRegister() {
		if (Utils.isEmpty(pinManagerURL)) {
			log.info("Pins option 'pins_service_url' is not conifgured, do not unregister");
			return;
		}
		if (socket != null) {
			socket.close();
			log.warn("Closed connection to PinService");
		}
	}

	/***
	 * Observe any events.
	 * 
	 * @param o
	 */
	public synchronized void observePinEvents(Observer o) {
		obsvPinsChanged.addObserver(o);
	}

	public void putPins(Map<Integer, PinInfo> devicePins, Map<Integer, PinInfo> accountPins) {
		setDevicePins(devicePins);
		setAccountPins(accountPins);
	}

	/**
	 * @return the devicePins
	 */
	public Map<Integer, PinInfo> getDevicePins() {
		return devicePins;
	}

	/**
	 * @param devicePins
	 *            the devicePins to set
	 */
	private void setDevicePins(Map<Integer, PinInfo> devicePins) {
		this.devicePins = devicePins;
	}

	/**
	 * @return the accountPins
	 */
	public Map<Integer, PinInfo> getAccountPins() {
		return accountPins;
	}

	/**
	 * @param accountPins
	 *            the accountPins to set
	 */
	private void setAccountPins(Map<Integer, PinInfo> accountPins) {
		this.accountPins = accountPins;
	}

	/***
	 * Get the PinInfo by the Pin Id
	 * 
	 * @param id
	 * @return
	 */
	public PinInfo getPinInfoById(long id) {
		log.debug("getPinInfoById: " + id);
		PinInfo pi = null;
		for (PinInfo p : devicePins.values()) {
			if (p.getId() == id) {
				pi = p;
				log.debug("getPinInfoById: " + id + " Found in DevicePins, returning: " + pi);
				return pi;
			}
		}
		for (PinInfo p : accountPins.values()) {
			if (p.getId() == id) {
				pi = p;
				log.debug("getPinInfoById: " + id + " Found in AccountPins, returning: " + pi);
				return pi;
			}
		}
		log.debug("getPinInfoById: " + id + " Not Found ");
		return pi;
	}

	/***
	 * Get the PinInfo by the Index
	 * 
	 * @param index
	 * @return
	 */
	public PinInfo getPinInfoByIndex(int index) {
		log.debug("getPinInfoByIndex: " + index);
		int size = devicePins.size();
		int i = 0;
		if (index <= size) {
			for (PinInfo pinfo : devicePins.values()) {
				i++;
				if (i == index) {
					log.debug("getPinInfoByIndex: " + index + " Found in DevicePins, returning: " + pinfo);
					return pinfo;
				}
			}
		}
		size = accountPins.size();
		if (index <= size) {

			for (PinInfo pinfo : accountPins.values()) {
				i++;
				if (i == index) {
					log.debug("getPinInfoByIndex: " + index + " Found in AccountPins, returning: " + pinfo);
					return pinfo;
				}
			}
		}
		log.debug("getPinInfoByIndex: " + index + " Not Found: ");
		return null;
	}

	/***
	 * Decode the Pin Query String
	 * 
	 * @param query
	 * @return
	 */
	public Map<String, String> decodeQueryString(String query) {
		try {
			Map<String, String> params = new LinkedHashMap<>();
			for (String param : query.split("&")) {
				String[] keyValue = param.split("=", 2);
				String key = URLDecoder.decode(keyValue[0], "UTF-8");
				String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
				if (!key.isEmpty()) {
					params.put(key, value);
				}
			}
			return params;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // Cannot happen with UTF-8
												// encoding.
		}
	}

}
