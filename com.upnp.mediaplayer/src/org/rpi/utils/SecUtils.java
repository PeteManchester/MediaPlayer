package org.rpi.utils;

import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
//import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

//For Bouncy Castle v1.52 or above: Convert the Key from PKCS#1 to PKCS#8
//https://stackoverflow.com/questions/15344125/load-a-rsa-private-key-in-java-algid-parse-error-not-a-sequence
//openssl pkcs8 -topk8 -inform PEM -outform PEM -in priv1.pem -out priv8.pem -nocrypt

public class SecUtils {

	private final Logger log = Logger.getLogger(SecUtils.class);

	private final String keyPem1 = "-----BEGIN RSA PRIVATE KEY-----\n" + "MIIEpQIBAAKCAQEA59dE8qLieItsH1WgjrcFRKj6eUWqi+bGLOX1HL3U3GhC/j0Qg90u3sG/1CUt\n" + "wC5vOYvfDmFI6oSFXi5ELabWJmT2dKHzBJKa3k9ok+8t9ucRqMd6DZHJ2YCCLlDRKSKv6kDqnw4U\n" + "wPdpOMXziC/AMj3Z/lUVX1G7WSHCAWKf1zNS1eLvqr+boEjXuBOitnZ/bDzPHrTOZz0Dew0uowxf\n" + "/+sG+NCK3eQJVxqcaJ/vEHKIVd2M+5qL71yJQ+87X6oV3eaYvt3zWZYD6z5vYTcrtij2VZ9Zmni/\n" + "UAaHqn9JdsBWLUEpVviYnhimNVvYFZeCXg/IdTQ+x4IRdiXNv5hEewIDAQABAoIBAQDl8Axy9XfW\n" + "BLmkzkEiqoSwF0PsmVrPzH9KsnwLGH+QZlvjWd8SWYGN7u1507HvhF5N3drJoVU3O14nDY4TFQAa\n" + "LlJ9VM35AApXaLyY1ERrN7u9ALKd2LUwYhM7Km539O4yUFYikE2nIPscEsA5ltpxOgUGCY7b7ez5\n" + "NtD6nL1ZKauw7aNXmVAvmJTcuPxWmoktF3gDJKK2wxZuNGcJE0uFQEG4Z3BrWP7yoNuSK3dii2jm\n" + "lpPHr0O/KnPQtzI3eguhe0TwUem/eYSdyzMyVx/YpwkzwtYL3sR5k0o9rKQLtvLzfAqdBxBurciz\n" + "aaA/L0HIgAmOit1GJA2saMxTVPNhAoGBAPfgv1oeZxgxmotiCcMXFEQEWflzhWYTsXrhUIuz5jFu\n" + "a39GLS99ZEErhLdrwj8rDDViRVJ5skOp9zFvlYAHs0xh92ji1E7V/ysnKBfsMrPkk5KSKPrnjndM\n" + "oPdevWnVkgJ5jxFuNgxkOLMuG9i53B4yMvDTCRiIPMQ++N2iLDaRAoGBAO9v//mU8eVkQaoANf0Z\n" + "oMjW8CN4xwWA2cSEIHkd9AfFkftuv8oyLDCG3ZAf0vrhrrtkrfa7ef+AUb69DNggq4mHQAYBp7L+\n" + "k5DKzJrKuO0r+R0YbY9pZD1+/g9dVt91d6LQNepUE/yY2PP5CNoFmjedpLHMOPFdVgqDzDFxU8hL\n" + "AoGBANDrr7xAJbqBjHVwIzQ4To9pb4BNeqDndk5Qe7fT3+/H1njGaC0/rXE0Qb7q5ySgnsCb3DvA\n" + "cJyRM9SJ7OKlGt0FMSdJD5KG0XPIpAVNwgpXXH5MDJg09KHeh0kXo+QA6viFBi21y340NonnEfdf\n" + "54PX4ZGS/Xac1UK+pLkBB+zRAoGAf0AY3H3qKS2lMEI4bzEFoHeK3G895pDaK3TFBVmD7fV0Zhov\n" + "17fegFPMwOII8MisYm9ZfT2Z0s5Ro3s5rkt+nvLAdfC/PYPKzTLalpGSwomSNYJcB9HNMlmhkGzc\n" + "1JnLYT4iyUyx6pcZBmCd8bD0iwY/FzcgNDaUmbX9+XDvRA0CgYEAkE7pIPlE71qvfJQgoA9em0gI\n" + "LAuE4Pu13aKiJnfft7hIjbK+5kyb3TysZvoyDnb3HOKvInK7vXbKuU4ISgxB2bB3HcYzQMGsz1qJ\n" + "2gG0N5hvJpzwwhbhXqFKA4zaaSrw622wDniAK5MlIE0tIAKKP4yxNGjoD2QYjhBGuhvkWKaXTyY=\n" + "-----END RSA PRIVATE KEY-----\n";
	
	private final String keyPem8 = "-----BEGIN PRIVATE KEY-----\r\n" + 
			"MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDn10TyouJ4i2wf\r\n" + 
			"VaCOtwVEqPp5RaqL5sYs5fUcvdTcaEL+PRCD3S7ewb/UJS3ALm85i98OYUjqhIVe\r\n" + 
			"LkQtptYmZPZ0ofMEkpreT2iT7y325xGox3oNkcnZgIIuUNEpIq/qQOqfDhTA92k4\r\n" + 
			"xfOIL8AyPdn+VRVfUbtZIcIBYp/XM1LV4u+qv5ugSNe4E6K2dn9sPM8etM5nPQN7\r\n" + 
			"DS6jDF//6wb40Ird5AlXGpxon+8QcohV3Yz7movvXIlD7ztfqhXd5pi+3fNZlgPr\r\n" + 
			"Pm9hNyu2KPZVn1maeL9QBoeqf0l2wFYtQSlW+JieGKY1W9gVl4JeD8h1ND7HghF2\r\n" + 
			"Jc2/mER7AgMBAAECggEBAOXwDHL1d9YEuaTOQSKqhLAXQ+yZWs/Mf0qyfAsYf5Bm\r\n" + 
			"W+NZ3xJZgY3u7XnTse+EXk3d2smhVTc7XicNjhMVABouUn1UzfkACldovJjURGs3\r\n" + 
			"u70Asp3YtTBiEzsqbnf07jJQViKQTacg+xwSwDmW2nE6BQYJjtvt7Pk20PqcvVkp\r\n" + 
			"q7Dto1eZUC+YlNy4/FaaiS0XeAMkorbDFm40ZwkTS4VAQbhncGtY/vKg25Ird2KL\r\n" + 
			"aOaWk8evQ78qc9C3Mjd6C6F7RPBR6b95hJ3LMzJXH9inCTPC1gvexHmTSj2spAu2\r\n" + 
			"8vN8Cp0HEG6tyLNpoD8vQciACY6K3UYkDaxozFNU82ECgYEA9+C/Wh5nGDGai2IJ\r\n" + 
			"wxcURARZ+XOFZhOxeuFQi7PmMW5rf0YtL31kQSuEt2vCPysMNWJFUnmyQ6n3MW+V\r\n" + 
			"gAezTGH3aOLUTtX/KycoF+wys+STkpIo+ueOd0yg9169adWSAnmPEW42DGQ4sy4b\r\n" + 
			"2LncHjIy8NMJGIg8xD743aIsNpECgYEA72//+ZTx5WRBqgA1/RmgyNbwI3jHBYDZ\r\n" + 
			"xIQgeR30B8WR+26/yjIsMIbdkB/S+uGuu2St9rt5/4BRvr0M2CCriYdABgGnsv6T\r\n" + 
			"kMrMmsq47Sv5HRhtj2lkPX7+D11W33V3otA16lQT/JjY8/kI2gWaN52kscw48V1W\r\n" + 
			"CoPMMXFTyEsCgYEA0OuvvEAluoGMdXAjNDhOj2lvgE16oOd2TlB7t9Pf78fWeMZo\r\n" + 
			"LT+tcTRBvurnJKCewJvcO8BwnJEz1Ins4qUa3QUxJ0kPkobRc8ikBU3CCldcfkwM\r\n" + 
			"mDT0od6HSRej5ADq+IUGLbXLfjQ2iecR91/ng9fhkZL9dpzVQr6kuQEH7NECgYB/\r\n" + 
			"QBjcfeopLaUwQjhvMQWgd4rcbz3mkNordMUFWYPt9XRmGi/Xt96AU8zA4gjwyKxi\r\n" + 
			"b1l9PZnSzlGjezmuS36e8sB18L89g8rNMtqWkZLCiZI1glwH0c0yWaGQbNzUmcth\r\n" + 
			"PiLJTLHqlxkGYJ3xsPSLBj8XNyA0NpSZtf35cO9EDQKBgQCQTukg+UTvWq98lCCg\r\n" + 
			"D16bSAgsC4Tg+7XdoqImd9+3uEiNsr7mTJvdPKxm+jIOdvcc4q8icru9dsq5TghK\r\n" + 
			"DEHZsHcdxjNAwazPWonaAbQ3mG8mnPDCFuFeoUoDjNppKvDrbbAOeIArkyUgTS0g\r\n" + 
			"Aoo/jLE0aOgPZBiOEEa6G+RYpg==\r\n" + 
			"-----END PRIVATE KEY-----\r\n" + 
			"";
	
	private static SecUtils instance = null;
	
	public static SecUtils getInstance()
	{
		if(instance ==null)
		{
			instance = new SecUtils();
		}
		return instance;
	}
	
	
	// Simple attempt to encode the password...
	public String encrypt(String key, String value) {
		try {
			byte[] raw = key.getBytes(Charset.forName("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
			byte[] encrypted = cipher.doFinal(value.getBytes());
			return Base64.encode(encrypted);
		} catch (Exception ex) {
			log.error("Error encrypt: ", ex);
		}
		return null;
	}

	public String decrypt(String key, String encrypted) {
		try {
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
			byte[] original = cipher.doFinal(Base64.decode(encrypted));

			return new String(original);
		} catch (Exception ex) {
			log.error("Error decrypt: ", ex);
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
	public byte[] encryptRSA(byte[] array) {
		log.info("Start of EncryptRSA");
		PEMParser pemReader = null;
		try {
			//Security.insertProviderAt(new BouncyCastleProvider(),1);
			 Security.addProvider(new BouncyCastleProvider());
			log.debug("Create pemReader");
			pemReader = new PEMParser(new StringReader(keyPem8.trim()));
			log.debug("Created pemReader");
			log.debug("ReadObject");
			//KeyPair pObj1 = (KeyPair) pemReader.readObject(); 
			log.debug("KeySize: " + keyPem8.length());
			PrivateKeyInfo pObj = (PrivateKeyInfo) pemReader.readObject();
			//PemObject pObj = pemReader.readPemObject();
			JcaPEMKeyConverter convert = new JcaPEMKeyConverter();
			//PrivateKey key = convert.getPrivateKey(pObj.getPrivateKeyInfo());
			PrivateKey key = convert.getPrivateKey(pObj);

			// Encrypt
			log.debug("getInstancer");
			Cipher cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding");
			log.debug("Cipher");
			cipher.init(Cipher.ENCRYPT_MODE,key);
			log.info("End of EncryptRSA");
			return cipher.doFinal(array);
			

		} catch (Exception e) {
			log.error(e);
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
	public byte[] decryptRSA(byte[] array) {
		log.debug("Start of decryptRSA");
		PEMParser pemReader = null;
		try {

			// La clef RSA
			pemReader = new PEMParser(new StringReader(keyPem8));
			PrivateKeyInfo pObj = (PrivateKeyInfo) pemReader.readObject();

			JcaPEMKeyConverter convert = new JcaPEMKeyConverter();
			//PrivateKey key = convert.getPrivateKey(pObj.getPrivateKeyInfo());
			PrivateKey key = convert.getPrivateKey(pObj);
			// Encrypt
			Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPPadding");
			cipher.init(Cipher.DECRYPT_MODE, key);
			log.debug("End of decryptRSA");
			return cipher.doFinal(array);

		} catch (Exception e) {
			log.error(e);
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
