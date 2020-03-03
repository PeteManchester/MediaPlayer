package org.rpi.providers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
//import org.bouncycastle.util.encoders.Base64;
import org.openhome.net.device.DvDevice;
import org.openhome.net.device.IDvInvocation;
import org.openhome.net.device.providers.DvProviderAvOpenhomeOrgCredentials1;
import org.rpi.config.Config;
import org.rpi.credentials.CredentialInfo;
import org.rpi.utils.Utils;

public class PrvCredentials extends DvProviderAvOpenhomeOrgCredentials1 implements IDisposableDevice {

	private Logger log = Logger.getLogger(PrvCredentials.class);
	//private String iPropertyIds = "tunein.com tidal.com quboz.com";
	private String iPropertyIds = "tunein.com";
	private String iPropertyPublicKey = "";
	private PublicKey publicKey = null;
	private PrivateKey privateKey = null;


	private int iPropertySequenceNumber = 0;

	byte[] encrypted = null;

	private ConcurrentHashMap<String, CredentialInfo> credentials = new ConcurrentHashMap<String, CredentialInfo>();

	public PrvCredentials(DvDevice iDevice) {
		super(iDevice);
		log.debug("Creating Credentials");
		enablePropertyIds();
		enablePropertyPublicKey();
		enablePropertySequenceNumber();

		setPropertyPublicKey("Test");
		setPropertySequenceNumber(0);
		setPropertyIds(iPropertyIds);
		iPropertyPublicKey = getPublicKey();
		setPropertyPublicKey(iPropertyPublicKey);

		enableActionGetIds();
		enableActionGetPublicKey();
		enableActionGetSequenceNumber();
		enableActionLogin();
		enableActionReLogin();
		enableActionClear();
		enableActionGet();
		enableActionSet();
		enableActionSetEnabled();
	}

	@Override
	protected void clear(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("clear" + Utils.getLogText(paramIDvInvocation) + " Service: " + paramString);
		if(credentials.containsKey(paramString))
		{
			credentials.remove(paramString);
		}
		iPropertySequenceNumber++;
		setPropertySequenceNumber(iPropertySequenceNumber);
	}

	@Override
	protected void set(IDvInvocation paramIDvInvocation, String service, String userName, byte[] password) {
		log.debug("sets" + Utils.getLogText(paramIDvInvocation) + "set Service: " + service + " UserName: " + userName + " Password Length: " + password.length );
		try {
			//Cipher cipher = Cipher.getInstance("RSA");
			//cipher.init(Cipher.DECRYPT_MODE, privateKey);
			// byte[] base64String = Base64.decode(password);
			// byte[] plainBytes = new String(base64String).getBytes("UTF-8");
			log.debug("Password Length: " + password.length);
			//byte[] decryptedBytes = cipher.doFinal(password);
			//String decryptedString = new String(decryptedBytes);
			//log.debug("And the Password is " + decryptedString);
		} catch (Exception e) {
			log.error("Erorr Decrypt", e);
		}

		//log.debug("set Service: " + service + " UserName: " + userName + " Password Length: " + password.length);
		CredentialInfo info = new CredentialInfo();
		info.setUserName(userName);
		info.setPassword(password);
		info.setEnabled(true);
		StringBuilder sb = new StringBuilder();
		sb.append("{\"partnerId\": \"");
		String partnerId = Config.getInstance().getRadioTuneInPartnerId();
		sb.append(partnerId);
		sb.append("\"}");
		info.setData(sb.toString());
		if (credentials.containsKey(service)) {
			credentials.remove(service);
		}
		//Go and log on then set the sessionId
		info.setStatus("");
		credentials.put(service, info);
		iPropertySequenceNumber++;
		setPropertySequenceNumber(iPropertySequenceNumber);
	}

	@Override
	protected void setEnabled(IDvInvocation paramIDvInvocation, String paramString, boolean paramBoolean) {
		log.debug("setEnable" + Utils.getLogText(paramIDvInvocation) + " Service: " + paramString + " Value: " + paramBoolean);
		if (credentials.containsKey(paramString)) {
			CredentialInfo info = credentials.get(paramString);
			info.setEnabled(paramBoolean);
			credentials.remove(paramString);
			credentials.put(paramString, info);
		}
	}

	@Override
	protected Get get(IDvInvocation paramIDvInvocation, String serviceName) {
		log.debug("get" + Utils.getLogText(paramIDvInvocation) + " " + serviceName);
		CredentialInfo info = null;
		if (credentials.containsKey(serviceName)) {
			log.debug("Contained Credentials for: " + serviceName);
			info = credentials.get(serviceName);
			return new Get(info.getUserName(), info.getPassword(), info.isEnabled(), info.getStatus(), info.getData());

		} else {			
			log.debug("Did not contain Credentials for: " + serviceName + " Create empty CredentialInfo");		
		}
		info = new CredentialInfo();		
		return new Get(info.getUserName(), info.getPassword(), info.isEnabled(), info.getStatus(), info.getData());
		
	}

	@Override
	protected String login(IDvInvocation paramIDvInvocation, String paramString) {
		log.debug("login" + Utils.getLogText(paramIDvInvocation) + " " + paramString);
		iPropertySequenceNumber++;
		setPropertySequenceNumber(iPropertySequenceNumber);
		return paramString;
	}

	@Override
	protected String reLogin(IDvInvocation paramIDvInvocation, String paramString1, String paramString2) {
		log.debug("reLogin" + Utils.getLogText(paramIDvInvocation));
		iPropertySequenceNumber++;
		setPropertySequenceNumber(iPropertySequenceNumber);
		return paramString2;
	}

	@Override
	protected String getIds(IDvInvocation paramIDvInvocation) {
		log.debug("getIds" + Utils.getLogText(paramIDvInvocation));
		return iPropertyIds;
	}

	@Override
	protected String getPublicKey(IDvInvocation paramIDvInvocation) {
		log.debug("getPublicKey" + Utils.getLogText(paramIDvInvocation));
		return iPropertyPublicKey;
	}

	@Override
	protected long getSequenceNumber(IDvInvocation paramIDvInvocation) {
		log.debug("getSequenceNumber" + Utils.getLogText(paramIDvInvocation));
		return iPropertySequenceNumber;
	}

	@Override
	public String getName() {
		return "PrvCredentials";
	}

	/**
	 * Create the PublicKey
	 * 
	 * @return
	 */
	private String getPublicKey() {
		log.debug("Get Public Key");
		StringBuffer sb = new StringBuffer();
		//String lineSeparator = ((String) AccessController.doPrivileged(new GetPropertyAction("line.separator")));
		//String lineSepartor = System.lineSeparator();
		String lineSeparator = System.getProperty("line.separator");
		
		// String lineSeparator = "\r\n";
		try {
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			publicKey = kp.getPublic();
			privateKey = kp.getPrivate();

			sb.append("-----BEGIN " + "PUBLIC KEY" + "-----");
			sb.append(lineSeparator);
			byte[] paramArrayOfByte = publicKey.getEncoded();
			paramArrayOfByte = Base64.encode(paramArrayOfByte);
			byte[] buf = new byte[64];
			int i = 0;
			while (i < paramArrayOfByte.length) {
				int size = 64;
				if (paramArrayOfByte.length - (i + 64) <= 0) {
					// log.debug("Not Modulo");
					size = paramArrayOfByte.length - i;
				}
				buf = new byte[size];
				for (int j = 0; j != 64; ++j) {

					if (i + j >= paramArrayOfByte.length)
						break;
					buf[j] = paramArrayOfByte[(i + j)];
				}
				String s = new String(buf);
				sb.append(s + lineSeparator);
				i += buf.length;
			}

			sb.append("-----END " + "PUBLIC KEY" + "-----");
			log.debug("Public Key" + lineSeparator + sb.toString());

//			// Write to File
//			PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream("pete.key")));
//			PemObject pemObject = new PemObject("PUBLIC KEY", publicKey.getEncoded());
//			pemWriter.writeObject(pemObject);
//			pemWriter.close();
//			File f = new File("pete.key");
//			FileInputStream fis = new FileInputStream(f);
//			DataInputStream dis = new DataInputStream(fis);
//			byte[] keyBytes = new byte[(int) f.length()];
//			dis.readFully(keyBytes);
//			dis.close();
//
//			String temp = new String(keyBytes);
//			String publicKeyPEM = temp.replace("-----BEGIN PUBLIC KEY-----\r\n", "");
//			publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
//
//			BASE64Decoder b64 = new BASE64Decoder();
//			byte[] decoded = b64.decodeBuffer(publicKeyPEM);
//
//			X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
//			KeyFactory kf = KeyFactory.getInstance("RSA");
//			Key myFileKey = kf.generatePublic(spec);
//
//			//
//			Cipher rsa;
//			rsa = Cipher.getInstance("RSA");
//			rsa.init(Cipher.ENCRYPT_MODE, myFileKey);
//			encrypted = rsa.doFinal("MyText".getBytes());

		} catch (Exception e) {
			log.error("Erorr Key", e);
		}

		// return test;
		return sb.toString().trim();
	}

}
