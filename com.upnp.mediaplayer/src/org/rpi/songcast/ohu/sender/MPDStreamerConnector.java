package org.rpi.songcast.ohu.sender;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MPDStreamerConnector implements Runnable {

	HttpURLConnection con = null;

	@Override
	public void run() {
		start();
	}

	private void start() {
		int BUFFER_SIZE = 1764;
		try {
			URL url = new URL("http://localhost:8000");
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			//int status = con.getResponseCode();
			//System.out.println(status);
			//BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			//String inputLine;
			//StringBuffer content = new StringBuffer();

			InputStream inputStream = con.getInputStream();
			//FileOutputStream outputStream = new
			//FileOutputStream("c:\\temp\\my.wav");

			int bytesRead = -1;
			int frameId = 1;
			byte[] buffer = new byte[BUFFER_SIZE];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				//byte[] test = converting(buffer);
				//outputStream.write(test, 0, bytesRead);
				MPDStreamerController.getInstance().addSoundByte(buffer, frameId);
				frameId++;
			}

			// outputStream.close();

		} catch (Exception e) {
		}

	}
	
	private  byte[] converting(byte[] value) {
	    final int length = value.length;
	    byte[] res = new byte[length];
	    for(int i = 0; i < length; i++) {
	        res[length - i - 1] = value[i];
	    }
	    return res;
	}

	public void stop() {
		if (con == null) {
			return;
		}
		con.disconnect();
	}

}
