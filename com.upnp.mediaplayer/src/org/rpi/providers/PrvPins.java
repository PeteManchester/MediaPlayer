package org.rpi.providers;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgPins1;
import org.rpi.channel.ChannelPlayList;
import org.rpi.channel.ChannelRadio;
import org.rpi.config.Config;
import org.rpi.kazo.server.KazooServer;
import org.rpi.pins.EventPinsChanged;
import org.rpi.pins.PinInfo;
import org.rpi.pins.PinMangerAccount;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventBase;
import org.rpi.player.events.EventPlayListUpdateList;
import org.rpi.radio.ChannelReaderJSON;
import org.rpi.utils.Utils;

public class PrvPins extends DvProviderAvOpenhomeOrgPins1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvPins.class);
	int iDeviceMax = 5;
	int iAccountMax = 5;

	JSONArray aModes = new JSONArray();
	Map<Integer, PinInfo> devicePins = new ConcurrentHashMap<Integer, PinInfo>();
	Map<Integer, PinInfo> accountPins = new ConcurrentHashMap<Integer, PinInfo>();
	Map<Integer, Integer> idArray = new HashMap<Integer, Integer>();
	PinInfo dummyPinInfo = new PinInfo(-1, "", "", "", "", "", "", false);

	public PrvPins(DvDevice iDevice) {
		super(iDevice);
		PinMangerAccount.getInstance().observePinEvents(this);
		iDeviceMax = Config.getInstance().getPinsDeviceMax();
		for (int i = 0; i < iDeviceMax; i++) {
			devicePins.put(i, dummyPinInfo);
		}
		for (int i = 0; i < iAccountMax; i++) {
			accountPins.put(i, dummyPinInfo);
		}
		enablePropertyAccountMax();
		enablePropertyDeviceMax();
		enablePropertyCloudConnected();
		enablePropertyIdArray();
		enablePropertyModes();

		enableActionGetDeviceMax();
		enableActionGetAccountMax();
		enableActionGetModes();
		enableActionGetCloudConnected();
		enableActionGetIdArray();
		enableActionReadList();
		enableActionInvokeIndex();
		enableActionInvokeId();
		enableActionInvokeUri();
		enableActionSetDevice();
		enableActionSetAccount();
		enableActionClear();
		enableActionSwap();

		propertiesLock();
		setPropertyAccountMax(iAccountMax);
		setPropertyDeviceMax(iDeviceMax);
		setPropertyCloudConnected(true);
		readPins();
		updateIdArray(false);

		aModes.put("openhome.me");
		aModes.put("tunein");
		aModes.put("transport");
		setPropertyModes(aModes.toString(2));
		propertiesUnlock();

	}

	private void updateIdArray(boolean save) {
		// Create a Map of Pin numbers
		Map<Integer, Integer> test = new HashMap<Integer, Integer>();
		int i = 0;

		for (PinInfo pi : devicePins.values()) {
			if (pi == null || pi.getId() < 0) {
				test.put(i, 0);

			} else {
				test.put(i, pi.getIdAsInt());
			}
			i++;
		}

		for (PinInfo pi : accountPins.values()) {
			if (pi == null || pi.getId() < 0) {
				test.put(i, 0);

			} else {
				test.put(i, pi.getIdAsInt());
			}
			i++;
		}

		idArray = test;
		// Convert the Pin Number to an array
		JSONArray jsonArray = new JSONArray(idArray.values());
		log.debug("Update IDArray: " + jsonArray.toString());
		setPropertyIdArray(jsonArray.toString());
		if (save) {
			savePins();
		}
	}

	/***
	 * Save the Pins
	 */
	private void savePins() {
		try {
			JSONArray res = new JSONArray();
			for (PinInfo pi : devicePins.values()) {
				JSONObject json = pi.getJSONObject();
				res.put(json);
			}
			String json = res.toString();
			PinMangerAccount.getInstance().SavePins(json);
			// saveToFile(json);
			// saveToCloud(json);
		} catch (Exception e) {
			log.error("Error Saving to pins.json", e);
		}
	}

	private void readPins() {
		String content = PinMangerAccount.getInstance().getPins();
		updatePins(content, false);
	}

	private void updatePins(String json, boolean save) {
		try {
			log.debug("Got to readFromFile: " + json);
			JSONArray array = new JSONArray(json);
			int i = 0;
			for (Object o : array) {
				if (o instanceof JSONObject) {
					JSONObject jpi = (JSONObject) o;
					PinInfo pi = new PinInfo(jpi);
					devicePins.put(i, pi);
					i++;
					if (i >= iDeviceMax) {
						break;
					}
				}
			}

		} catch (Exception e) {
			log.error("Error Reading from pins.json", e);
		}
		updateIdArray(save);
	}

	/***
	 * Convert the hashmap to a list of integers the represent the id array
	 * 
	 * @param ids
	 * @return
	 */
	private JSONArray getJSONList(List<Integer> ids) {
		JSONArray res = new JSONArray();

		for (PinInfo i : devicePins.values()) {
			if (i != null) {
				JSONObject o = i.getJSONObject();
				if (ids == null) {
					res.put(o);
				} else if (ids.contains((int) i.getIdAsInt())) {
					res.put(o);
				}
			}
		}

		for (PinInfo i : accountPins.values()) {
			if (i != null) {
				JSONObject o = i.getJSONObject();
				if (ids == null) {
					res.put(o);
				} else if (ids.contains((int) i.getIdAsInt())) {
					res.put(o);
				}
			}
		}

		return res;
	}

	protected long getAccountMax(IDvInvocation paramIDvInvocation) {
		log.debug("GetAccountMax: " + Utils.getLogText(paramIDvInvocation));
		return iAccountMax;
	}

	protected long getDeviceMax(IDvInvocation paramIDvInvocation) {
		log.debug("GetDeviceMax: " + Utils.getLogText(paramIDvInvocation));
		return iDeviceMax;
	}

	protected String getModes(IDvInvocation paramIDvInvocation) {
		log.debug("GetModes: " + Utils.getLogText(paramIDvInvocation));
		return getPropertyModes();
	}

	protected String getIdArray(IDvInvocation paramIDvInvocation) {
		log.debug("GetIdArray: " + Utils.getLogText(paramIDvInvocation));
		return getPropertyIdArray();
	}

	protected String readList(IDvInvocation paramIDvInvocation, String ids) {
		log.debug("readList: " + Utils.getLogText(paramIDvInvocation) + " Ids: " + ids);
		JSONArray res = new JSONArray();
		// JSONParser parser = new JSONParser();
		try {
			// JSONArray oIds = (JSONArray) parser.parse(ids);
			JSONArray oIds = new JSONArray(ids);
			List<Integer> lids = new ArrayList<Integer>();
			for (int i = 0; i < oIds.length(); i++) {
				lids.add(oIds.getInt(i));
			}
			res = getJSONList(lids);
		} catch (Exception e) {
			log.debug("Error readList", e);
		}
		return res.toString();
	}

	protected void invokeId(IDvInvocation paramIDvInvocation, long id) {
		log.debug("invokeId: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		PinInfo pi = getPinInfo(id);
		if (pi == null) {
			return;
		}
		PlayManager.getInstance().updateShuffle(pi.isShuffle());
		String uri = pi.getUri();
		log.debug(uri);
		if (uri.startsWith("tunein://")) {
			if (uri.startsWith("tunein://stream")) {
				try {
					Map<String, String> params = decodeQueryString(uri.substring("tunein://stream".length() + 1));
					String presetId = "";
					String image = pi.getArtworkUri();

					ChannelReaderJSON cr = new ChannelReaderJSON(null);
					String path = params.get("path");
					log.debug("Play Radio: " + path);
					String[] splits = path.split("\\?");
					Map<String, String> paramsTuneIn = decodeQueryString(splits[1]);
					presetId = paramsTuneIn.get("id");
					String m = cr.getMetaDataForTuneInId(presetId, path, image);
					int rId = -99;
					try {
						rId = Integer.parseInt(presetId.replaceAll("[^0-9]+", ""));
					} catch (Exception e) {

					}
					ChannelRadio c = new ChannelRadio(path, m, rId, presetId);
					PlayManager.getInstance().playRadio(c);
				} catch (Exception e) {
					log.error(e);
				}

			} else if (uri.startsWith("tunein://podcast")) {
				Map<String, String> params = decodeQueryString(uri.substring("tunein://podcast".length() + 1));
				String presetId = "";
				String image = pi.getArtworkUri();
				ChannelReaderJSON cr = new ChannelReaderJSON(null);
				String path = params.get("path");
				log.debug("Play Podcast: " + path);
				String[] splits = path.split("\\?");
				Map<String, String> paramsTuneIn = decodeQueryString(splits[1]);
				presetId = paramsTuneIn.get("id");
				List<ChannelPlayList> channels = cr.getPodcasts(path, image);
				EventPlayListUpdateList epl = new EventPlayListUpdateList();
				PlayManager.getInstance().podcastUpdatePlayList(channels);
			}
		} else if (uri.startsWith("openhome.me")) {
			try {
				String test = uri.replace("openhome.me", "http:");
				URL url = new URL(test);
				String path = url.getPath();
				log.debug("Play Kazoo: " + path);
				path = path.replace("://", "");
				String query = url.getQuery();
				Map<String, String> params = decodeQueryString(query);
				KazooServer t = new KazooServer();
				if (params.containsKey("browse") && params.containsKey("udn")) {
					String browse = params.get("browse");
					String udn = params.get("udn");
					t.getTracks(udn, path, browse);
				}
			} catch (Exception e) {
				log.error("Error Getting URL: " + uri);
			}

		}
	}

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

	private PinInfo getPinInfo(long id) {
		PinInfo pi = null;
		for (PinInfo p : devicePins.values()) {
			if (p.getId() == id) {
				pi = p;
				return pi;
			}
		}
		for (PinInfo p : accountPins.values()) {
			if (p.getId() == id) {
				pi = p;
				return pi;
			}
		}
		return pi;
	}

	protected void invokeIndex(IDvInvocation paramIDvInvocation, long id) {
		log.debug("invokeIndex: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
	}

	@Override
	protected void invokeUri(IDvInvocation paramIDvInvocation, String mode, String type, String uri, boolean shuffle) {
		log.debug("invokeUri: " + Utils.getLogText(paramIDvInvocation) + " Id: " + uri);
	}

	protected void setDevice(IDvInvocation paramIDvInvocation, long id, String mode, String type, String uri, String description, String var8, String artworkUri, boolean shuffle) {
		log.debug("setDevice: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		int myId = getRandomIndex();// getNextIndex();
		log.debug("setDevice. MyId: " + myId);
		String myDescription = tidyDescription(description);
		PinInfo pi = new PinInfo(myId, mode, type, uri, "", myDescription, artworkUri, shuffle);
		log.debug("setDevice. PinInfo: " + pi.toString());
		devicePins.put((int) id, pi);
		updateIdArray(true);
	}

	private String tidyDescription(String description) {
		if (description.startsWith("Album : ")) {
			return description.substring("Album : ".length() - 1, description.length());
		}
		if (description.startsWith("Artist : ")) {
			return description.substring("Artist : ".length() - 1, description.length());
		}
		if (description.startsWith("Genre : ")) {
			return description.substring("Genre : ".length() - 1, description.length());
		}
		return description;
	}

	// Spin through all the existing Pins numbers to find a spare pin.
	private int getNextIndex() {
		int res = 0;
		for (int i = 1; i < 10000; i++) {
			if (!idArray.containsValue(i)) {
				return i;
			}
		}
		return res;
	}

	private int getRandomIndex() {
		int res = 0;
		res = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);
		return res;
	}

	/***
	 * Set the Account Pin
	 */
	protected void setAccount(IDvInvocation paramIDvInvocation, long id, String mode, String type, String uri, String title, String description, String artworkUri, boolean shuffle) {
		log.debug("setAccount: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		int myId = getNextIndex() + iDeviceMax;
		log.debug("setAccount. MyId: " + myId);
		String myDescription = tidyDescription(description);
		PinInfo pi = new PinInfo(myId, mode, type, uri, "", myDescription, artworkUri, shuffle);
		log.debug("setDevice. PinInfo: " + pi.toString());
		accountPins.put((int) id, pi);
		updateIdArray(true);
	}

	/***
	 * Clear a Pin
	 */
	protected void clear(IDvInvocation paramIDvInvocation, long id) {
		log.debug("clear: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		// Sort out the Device Pins
		if (devicePins.containsKey((int) id)) {
			devicePins.put((int) id, dummyPinInfo);
		}
		int i = 0;
		for (PinInfo pi : devicePins.values()) {
			if (pi != null && pi.getId() >= 0) {
				if (id == pi.getId()) {
					devicePins.put(i, dummyPinInfo);
				}
			}
			i++;
		}

		// Sort out the Account Pins
		if (accountPins.containsKey((int) id)) {
			accountPins.put((int) id, dummyPinInfo);
		}
		i = 0;
		for (PinInfo pi : accountPins.values()) {
			if (pi != null && pi.getId() >= 0) {
				if (id == pi.getId()) {
					accountPins.put(i, dummyPinInfo);
				}
			}
			i++;
		}

		updateIdArray(true);
	}

	/***
	 * Swap pins. Can only swap the same type of pin (Device or Account)
	 */
	protected void swap(IDvInvocation paramIDvInvocation, long id1, long id2) {
		log.debug("setDevice: " + Utils.getLogText(paramIDvInvocation) + " Id1: " + id1 + " Id2: " + id2);
		int mId1 = (int) id1;
		int mId2 = (int) id2;
		if (devicePins.containsKey(mId1) && devicePins.containsKey(mId2)) {
			PinInfo pinfo1 = devicePins.get(mId1);
			PinInfo pinfo2 = devicePins.get(mId2);
			devicePins.put(mId1, pinfo2);
			devicePins.put(mId2, pinfo1);
			updateIdArray(true);
		}

		if (accountPins.containsKey(mId1) && accountPins.containsKey(mId2)) {
			PinInfo pinfo1 = accountPins.get(mId1);
			PinInfo pinfo2 = accountPins.get(mId2);
			accountPins.put(mId1, pinfo2);
			accountPins.put(mId2, pinfo1);
			updateIdArray(true);
		}

	}

	protected boolean getCloudConnected(IDvInvocation paramIDvInvocation) {
		log.debug("getCloudConnected: " + Utils.getLogText(paramIDvInvocation));
		return getPropertyCloudConnected();
	}

	@Override
	public String getName() {
		return "Pins";
	}

	@Override
	public void dispose() {
		PinMangerAccount.getInstance().unRegister();
		super.dispose();
	}

	@Override
	public void update(Observable o, Object e) {
		EventBase base = (EventBase) e;
		switch (base.getType()) {
		case EVENTPINSCHANGED:
			EventPinsChanged ev = (EventPinsChanged) e;
			updatePins(ev.getPinInfo(), true);
			break;
		}
	}

}
