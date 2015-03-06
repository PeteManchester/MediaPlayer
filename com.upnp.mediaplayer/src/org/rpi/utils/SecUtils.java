package org.rpi.utils;

import org.apache.log4j.Logger;
//import org.bouncycastle.openssl.PEMReader;

import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.PrivateKey;

public class SecUtils {

	private static final Logger LOGGER = Logger.getLogger(SecUtils.class);

	private static final String key = "-----BEGIN RSA PRIVATE KEY-----\n" + "MIIEpQIBAAKCAQEA59dE8qLieItsH1WgjrcFRKj6eUWqi+bGLOX1HL3U3GhC/j0Qg90u3sG/1CUt\n" + "wC5vOYvfDmFI6oSFXi5ELabWJmT2dKHzBJKa3k9ok+8t9ucRqMd6DZHJ2YCCLlDRKSKv6kDqnw4U\n" + "wPdpOMXziC/AMj3Z/lUVX1G7WSHCAWKf1zNS1eLvqr+boEjXuBOitnZ/bDzPHrTOZz0Dew0uowxf\n" + "/+sG+NCK3eQJVxqcaJ/vEHKIVd2M+5qL71yJQ+87X6oV3eaYvt3zWZYD6z5vYTcrtij2VZ9Zmni/\n" + "UAaHqn9JdsBWLUEpVviYnhimNVvYFZeCXg/IdTQ+x4IRdiXNv5hEewIDAQABAoIBAQDl8Axy9XfW\n" + "BLmkzkEiqoSwF0PsmVrPzH9KsnwLGH+QZlvjWd8SWYGN7u1507HvhF5N3drJoVU3O14nDY4TFQAa\n" + "LlJ9VM35AApXaLyY1ERrN7u9ALKd2LUwYhM7Km539O4yUFYikE2nIPscEsA5ltpxOgUGCY7b7ez5\n" + "NtD6nL1ZKauw7aNXmVAvmJTcuPxWmoktF3gDJKK2wxZuNGcJE0uFQEG4Z3BrWP7yoNuSK3dii2jm\n" + "lpPHr0O/KnPQtzI3eguhe0TwUem/eYSdyzMyVx/YpwkzwtYL3sR5k0o9rKQLtvLzfAqdBxBurciz\n" + "aaA/L0HIgAmOit1GJA2saMxTVPNhAoGBAPfgv1oeZxgxmotiCcMXFEQEWflzhWYTsXrhUIuz5jFu\n" + "a39GLS99ZEErhLdrwj8rDDViRVJ5skOp9zFvlYAHs0xh92ji1E7V/ysnKBfsMrPkk5KSKPrnjndM\n" + "oPdevWnVkgJ5jxFuNgxkOLMuG9i53B4yMvDTCRiIPMQ++N2iLDaRAoGBAO9v//mU8eVkQaoANf0Z\n" + "oMjW8CN4xwWA2cSEIHkd9AfFkftuv8oyLDCG3ZAf0vrhrrtkrfa7ef+AUb69DNggq4mHQAYBp7L+\n" + "k5DKzJrKuO0r+R0YbY9pZD1+/g9dVt91d6LQNepUE/yY2PP5CNoFmjedpLHMOPFdVgqDzDFxU8hL\n" + "AoGBANDrr7xAJbqBjHVwIzQ4To9pb4BNeqDndk5Qe7fT3+/H1njGaC0/rXE0Qb7q5ySgnsCb3DvA\n" + "cJyRM9SJ7OKlGt0FMSdJD5KG0XPIpAVNwgpXXH5MDJg09KHeh0kXo+QA6viFBi21y340NonnEfdf\n" + "54PX4ZGS/Xac1UK+pLkBB+zRAoGAf0AY3H3qKS2lMEI4bzEFoHeK3G895pDaK3TFBVmD7fV0Zhov\n" + "17fegFPMwOII8MisYm9ZfT2Z0s5Ro3s5rkt+nvLAdfC/PYPKzTLalpGSwomSNYJcB9HNMlmhkGzc\n" + "1JnLYT4iyUyx6pcZBmCd8bD0iwY/FzcgNDaUmbX9+XDvRA0CgYEAkE7pIPlE71qvfJQgoA9em0gI\n" + "LAuE4Pu13aKiJnfft7hIjbK+5kyb3TysZvoyDnb3HOKvInK7vXbKuU4ISgxB2bB3HcYzQMGsz1qJ\n" + "2gG0N5hvJpzwwhbhXqFKA4zaaSrw622wDniAK5MlIE0tIAKKP4yxNGjoD2QYjhBGuhvkWKaXTyY=\n" + "-----END RSA PRIVATE KEY-----\n";

	// Simple attempt to encode the password...
	public static String encrypt(String key, String value) {
		try {
			byte[] raw = key.getBytes(Charset.forName("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.encode(encrypted);
		} catch (Exception ex) {
			LOGGER.error("Error encrypt: ", ex);
		}
		return null;
	}

	public static String decrypt(String key, String encrypted) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
			byte[] original = cipher.doFinal(Base64.decode(encrypted));

			return new String(original);
		} catch (Exception ex) {
			LOGGER.error("Error decrypt: ", ex);
		}
		return null;
	}

	/**
	 * Crypts with private key
	 * 
	 * @param array
	 *            data to encrypt
	 * @return encrypted data
	 */
	public static byte[] encryptRSA(byte[] array) {
		LOGGER.info("Start of EncryptRSA");
		PEMParser pemReader = null;
		try {
			// Security.addProvider(new BouncyCastleProvider());
			LOGGER.debug("Create pemReader");
			pemReader = new PEMParser(new StringReader(key));
			LOGGER.debug("Created pemReader");
			LOGGER.debug("ReadObject");

			PEMKeyPair pObj = (PEMKeyPair) pemReader.readObject();
			//PemObject pObj = pemReader.readPemObject();
			JcaPEMKeyConverter convert = new JcaPEMKeyConverter();
			PrivateKey key = convert.getPrivateKey(pObj.getPrivateKeyInfo());

			// Encrypt
			LOGGER.debug("getInstancer");
			Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
			LOGGER.debug("Cipher");
			cipher.init(Cipher.ENCRYPT_MODE,key);
			LOGGER.info("End of EncryptRSA");
			return cipher.doFinal(array);
			

		} catch (Exception e) {
			LOGGER.error(e);
		}finally
		{
			if(pemReader !=null)
			{
				try {
					pemReader.close();
				} catch (Exception e) {

				}
			}
		}

		return null;
	}

	/**
	 * Decrypt with RSA priv key
	 * 
	 * @param array
	 * @return
	 */
	public static byte[] decryptRSA(byte[] array) {
		LOGGER.debug("Start of decryptRSA");
		PEMParser pemReader = null;
		try {

			// La clef RSA
			pemReader = new PEMParser(new StringReader(key));
			PEMKeyPair pObj = (PEMKeyPair) pemReader.readObject();

			JcaPEMKeyConverter convert = new JcaPEMKeyConverter();
			PrivateKey key = convert.getPrivateKey(pObj.getPrivateKeyInfo());
			// Encrypt
			Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			LOGGER.debug("End of decryptRSA");
			return cipher.doFinal(array);

		} catch (Exception e) {
			LOGGER.error(e);
		}
		finally
		{
			if(pemReader !=null)
			{
				try {
					pemReader.close();
				} catch (Exception e) {

				}
			}
		}

		return null;
	}

}
