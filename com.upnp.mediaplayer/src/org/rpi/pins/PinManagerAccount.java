package org.rpi.pins;

import java.io.FileWriter;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observer;

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
 * A class that handles the Pin service, using both a local file and if configured a web service based on Socket.io
 * @author phoyle
 *
 */

public class PinManagerAccount {

	private Logger log = Logger.getLogger(this.getClass());

	private String originalPins = "";

	// private String pinManagerurl =
	// "http://192.168.1.205:8081/PinServiceManager1/webapi/broadcast";
	private String pinManagerURL = Config.getInstance().getPinsServiceURL();

	private static PinManagerAccount instance = null;
	private ObservablePins obsvPinsChanged = new ObservablePins();

	private Socket socket = null;

	public static PinManagerAccount getInstance() {
		if (instance == null) {
			instance = new PinManagerAccount();
		}
		return instance;
	}

	private PinManagerAccount() {
		if (!Utils.isEmpty(pinManagerURL)) {
			log.info("PinManger URL is configured, connect to the service");
			connectToServer();
		}
	}

	private void connectToServer() {
		try {
			URL url = new URL(pinManagerURL);
			String path = url.getPath();
			String host = url.getHost();
			Options options = new Options();
			options.path = path;
			String sUrl = pinManagerURL.replace(path, "");
			socket = IO.socket(sUrl, options);
			socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
				@Override
				public void call(Object... args) {
					log.debug("Connected: " + socket.connected());
					socket.emit("foo", "hi");
				}

			}).on(Socket.EVENT_MESSAGE, new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Recieved: " + args);
					String eventType = (String) args[0];
					switch(eventType.toUpperCase()) {
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
							log.error("Could not process event",e);
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
					log.debug("Reconnect Attempt");

				}
			}).on("pins", new Emitter.Listener() {

				@Override
				public void call(Object... args) {
					log.debug("Pins updated: " + args[0].toString());
					try {
						if(args[0] instanceof JSONArray) {
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
						log.error("Could not process event",e);
					}

				}
			});
			socket.connect();
			log.debug("Status: " + socket.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
		if(!json.equalsIgnoreCase(originalPins)) {
			EventPinsChanged ev = new EventPinsChanged();
			ev.setPinInfo(json);
			obsvPinsChanged.notifyChange(ev);
			originalPins = json;
		}
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

}
