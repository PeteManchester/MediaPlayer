package org.rpi.providers;

import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgPins1;
import org.rpi.channel.ChannelPlayList;
import org.rpi.channel.ChannelRadio;
import org.rpi.kazo.server.KazooServer;
import org.rpi.pins.PinInfo;
import org.rpi.player.PlayManager;
import org.rpi.player.events.EventPlayListUpdateList;
import org.rpi.radio.ChannelReaderJSON;
import org.rpi.utils.Utils;

public class PrvPins extends DvProviderAvOpenhomeOrgPins1 implements Observer, IDisposableDevice {

	private Logger log = Logger.getLogger(PrvPins.class);
	int iDeviceMax = 5;
	int iAccountMax = 5;

	JSONArray aModes = new JSONArray();
	Map<Integer, PinInfo> devicePins = new ConcurrentHashMap<Integer, PinInfo>();
	Map<Integer,Integer> idArray = new HashMap<Integer,Integer>();
	PinInfo dummyPinInfo = new PinInfo(-1, "", "", "", "", "", "", false);


	public PrvPins(DvDevice iDevice) {
		super(iDevice);
		for (int i = 0; i < iDeviceMax + iAccountMax; i++) {
			devicePins.put(i, dummyPinInfo);
		}
		enablePropertyAccountMax();
		enablePropertyDeviceMax();
		enablePropertyCloudConnected();
		enablePropertyIdArray();
		enablePropertyModes();

		propertiesLock();
		setPropertyAccountMax(iAccountMax);
		setPropertyDeviceMax(iDeviceMax);
		setPropertyCloudConnected(false);
		readFromFile();
		updateIdArray(false);

		aModes.put("openhome.me");
		aModes.put("tunein");
		aModes.put("transport");
		setPropertyModes(aModes.toString(2));
		propertiesUnlock();

		enableActionGetDeviceMax();
		enableActionGetModes();
		enableActionGetIdArray();
		enableActionReadList();
		enableActionInvokeId();
		enableActionInvokeIndex();
		enableActionSetDevice();
		enableActionSetAccount();
		enableActionClear();
		enableActionSwap();
		enableActionGetCloudConnected();

	}

	private void updateIdArray(boolean save) {
		//Create a Map of Pin numbers
		Map<Integer,Integer> test = new HashMap<Integer,Integer>();
		int i = 0;
		for (PinInfo pi : devicePins.values()) {
			if (pi == null || pi.getId() < 0) {
				test.put(i,0);

			} else {
				test.put(i, pi.getIdAsInt() );
			}
			i++;
		}		
		idArray = test;
		//Convert the Pin Number to an array
		JSONArray jsonArray = new JSONArray(idArray.values());
		log.debug("Update IDArray: " + jsonArray.toString());
		setPropertyIdArray(jsonArray.toString());
		if (save) {
			saveToFile();
		}
	}

	private void saveToFile() {
		try {
			JSONArray res = new JSONArray();
			for (PinInfo pi : devicePins.values()) {
				JSONObject json = pi.getJSONObject();
				res.put(json);
			}
			FileWriter fw = new FileWriter("pins.json");
			String json = res.toString(2);
			fw.write(json);
			fw.close();
		} catch (Exception e) {
			log.error("Error Saving to pins.json", e);
		}
	}

	private void readFromFile() {
		try {

			String content = new String(Files.readAllBytes(Paths.get("pins.json")));
			JSONArray array = new JSONArray(content);
			int i = 0;
			for (Object o : array) {
				if (o instanceof JSONObject) {
					JSONObject jpi = (JSONObject) o;
					PinInfo pi = new PinInfo(jpi);
					devicePins.put(i, pi);
					i++;
					if (i >= iDeviceMax + iAccountMax) {
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("Error Reading from pins.json", e);
		}
		updateIdArray(false);
	}

	/***
	 * Convert the hashmap to a list of integers the represent the id array
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
		return res;
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
					log.debug("Boom!!!!! Play Radio: " + path);
					String[] splits = path.split("\\?");
					Map<String, String> paramsTuneIn = decodeQueryString(splits[1]);
					presetId = paramsTuneIn.get("id");
					String m = cr.getMetaDataForTuneInId(presetId, path, image);
					ChannelRadio c = new ChannelRadio(path, m, -99, "Test");
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
				String[] splits = path.split("\\?");
				Map<String, String> paramsTuneIn = decodeQueryString(splits[1]);
				presetId = paramsTuneIn.get("id");
				List<ChannelPlayList> channels = cr.getPodcasts(path, image);
				EventPlayListUpdateList epl = new EventPlayListUpdateList();
				PlayManager.getInstance().podcastUpdatePlayList(channels);
			}
		}
		else if(uri.startsWith("openhome.me")) {
			try {
				String test = uri.replace("openhome.me", "http:");
			URL url = new URL(test);
			String path = url.getPath();
			path = path.replace("://", "");
			String query = url.getQuery();
			Map<String, String> params = decodeQueryString(query);
			KazooServer t = new KazooServer();
			if(params.containsKey("browse") && params.containsKey("udn")) {
				String browse = params.get("browse");
				String udn = params.get("udn");
			t.getTracks(udn,path, browse);
			}
			}
			catch(Exception e) {
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
			}
		}
		return pi;
	}

	protected void invokeIndex(IDvInvocation paramIDvInvocation, long id) {
		log.debug("invokeIdnex: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
	}

	protected void setDevice(IDvInvocation paramIDvInvocation, long id, String mode, String type, String uri, String description, String var8, String artworkUri, boolean shuffle) {
		log.debug("setDevice: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		int myId = getNextIndex();
		log.debug("setDevice. MyId: " + myId);
		PinInfo pi = new PinInfo(myId, mode, type, uri, "", description, artworkUri, shuffle);
		log.debug("setDevice. PinInfo: " + pi.toString());
		devicePins.put((int) id, pi);
		updateIdArray(true);
	}
	
	//Spin through all the existing Pins numbers to find a spare pin.
	private int getNextIndex() {
		int res = 0;
		for(int i = 1; i< 10000; i++)
		{
			if(!idArray.containsValue(i))
			{
				return i;
			}
		}
		return res;
	}

	protected void setAccount(IDvInvocation paramIDvInvocation, long id, String var4, String var5, String var6, String var7, String var8, String var9, boolean var10) {
		log.debug("setAccount: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);

	}

	protected void clear(IDvInvocation paramIDvInvocation, long id) {
		log.debug("clear: " + Utils.getLogText(paramIDvInvocation) + " Id: " + id);
		if (devicePins.containsKey((int) id)) {
			devicePins.put((int) id, dummyPinInfo);
		}
		int i = 0;
		for (PinInfo pi : devicePins.values()) {
			if (pi != null || pi.getId() >= 0) {
				if (id == pi.getId()) {
					devicePins.put(i, dummyPinInfo);
				}
			}
			i++;
		}
		updateIdArray(true);
	}

	protected void swap(IDvInvocation paramIDvInvocation, long id1, long id2) {
		log.debug("setDevice: " + Utils.getLogText(paramIDvInvocation) + " Id1: " + id1 + " Id2: " + id2);
		int mId1 = (int) id1;
		int mId2 = (int) id2;
		PinInfo pinfo1 = devicePins.get(mId1);
		PinInfo pinfo2 = devicePins.get(mId2);
		devicePins.put(mId1, pinfo2);
		devicePins.put(mId2, pinfo1);
		updateIdArray(true);
	}

	protected boolean getCloudConnected(IDvInvocation paramIDvInvocation) {
		log.debug("getCloudConnected: " + Utils.getLogText(paramIDvInvocation));
		return false;
	}

	@Override
	public String getName() {
		return "Pins";
	}

	@Override
	public void update(Observable arg0, Object arg1) {

	}
	

}
