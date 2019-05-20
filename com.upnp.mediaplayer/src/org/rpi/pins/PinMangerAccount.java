package org.rpi.pins;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.rpi.config.Config;
import org.rpi.player.observers.ObservablePins;
import org.rpi.utils.Utils;

public class PinMangerAccount implements EventListener {

	private Logger log = Logger.getLogger(this.getClass());

	private String originalPins = "";

	// private String pinManagerurl =
	// "http://192.168.1.205:8081/PinServiceManager1/webapi/broadcast";
	private String pinManagerURL = Config.getInstance().getPinsServiceURL();
	// private String pinManagerurl =
	// "http://localhost:8081/PinServiceManager1/webapi/broadcast";
	Client client = null;

	private static PinMangerAccount instance = null;
	private ObservablePins obsvPinsChanged = new ObservablePins();

	public static PinMangerAccount getInstance() {
		if (instance == null) {
			instance = new PinMangerAccount();
		}
		return instance;
	}

	private PinMangerAccount() {
		client = ClientBuilder.newBuilder().register(SseFeature.class).build();
		registerForEvent();
	}

	/***
	 * Get the configured Pins
	 * 
	 * @return
	 */
	public String getPins() {
		String res = null;
		if (!Utils.isEmpty(pinManagerURL)) {
			res = readFromCloud();
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
		originalPins = json;
	}

	EventSource eventSource = null;

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
	private String readFromCloud() {
		String res = null;
		try {
			log.debug("Read From Cloud");
			String url = pinManagerURL + "/read";
			log.debug("Read From Cloud Start of creating Client: " + url);
			// Client client = ClientBuilder.newClient(new ClientConfig());
			WebTarget webTarget = client.target(url);
			log.debug("Created WebTarget");
			Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
			log.debug("Created Builder");
			Response response = invocationBuilder.get();
			log.debug("Called GET()");
			res = response.readEntity(String.class);
			log.debug("Read From Cloud Result URL: " + res);
			// client.close();
		} catch (Exception e) {
			log.error("Error ReadFromCloud", e);
		}
		return res;
	}

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
			String res = null;
			log.debug("Save to Cloud");
			String url = pinManagerURL + "/save";
			// log.debug("Save to Start to Create Client");
			// Client client = ClientBuilder.newClient(new ClientConfig());
			log.debug("Save to Created Client");
			WebTarget webTarget = client.target(url);
			log.debug("Save to Cloud Created Target");
			Builder invocationBuilder = webTarget.request(MediaType.TEXT_PLAIN);
			log.debug("Save to Cloud Created Builder");
			Response response = invocationBuilder.post(Entity.entity(json, MediaType.TEXT_PLAIN));
			log.debug("Save to Cloud After POST");
			res = response.readEntity(String.class);
			log.debug("Save to Cloud " + res);
			// client.close();
		} catch (Exception e) {
			log.error("SaveToCloud", e);
		}

	}

	public void registerForEvent() {
		if(Utils.isEmpty(pinManagerURL)) {
			log.info("Pins option 'pins_service_url' is not conifgured, do not register");
			return;
		}
		try {
			client.register(new MyLogFilter());
			WebTarget target = client.target(pinManagerURL);
			eventSource = EventSource.target(target).reconnectingEvery(1, TimeUnit.SECONDS).build();
			eventSource.register(this);
			eventSource.open();
			log.warn("Connected to PinService: " + pinManagerURL);

		} catch (Exception e) {
			log.error("Error Registering PinService", e);
		}
	}

	public void unRegister() {
		if(Utils.isEmpty(pinManagerURL)) {
			log.info("Pins option 'pins_service_url' is not conifgured, do not unregister");
			return;
		}
		if (eventSource != null) {
			eventSource.close();
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

	@Override
	public void onEvent(InboundEvent inboundEvent) {
		log.debug("OnEvent: " + inboundEvent.readData(String.class));
		if (inboundEvent != null) {
			String json = inboundEvent.readData(String.class);
			if (!json.equalsIgnoreCase(originalPins)) {
				log.debug("Pins Config has changed, update Pins");
				originalPins = json;
				EventPinsChanged ev = new EventPinsChanged();
				ev.setPinInfo(json);
				obsvPinsChanged.notifyChange(ev);
			}
		}
	}

}
