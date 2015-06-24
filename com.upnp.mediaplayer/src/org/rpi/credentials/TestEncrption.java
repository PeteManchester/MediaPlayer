package org.rpi.credentials;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;


public class TestEncrption {
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException {

	    Security.addProvider(new BouncyCastleProvider());
	    
	    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
		kpg.initialize(2048);
		KeyPair keyPair = kpg.genKeyPair();
		//publicKey = keyPair.getPublic();
		//privateKey = keyPair.getPrivate();


	    Base64 base64 = new Base64();
	    String text = "this is the input text";
	    byte[] encripted;
	    System.out.println("input:\n" + text);
	    encripted = encrypt(keyPair.getPublic(), text);
	    //System.out.println("cipher:\n" + base64.encodeAsString(encripted));
	    System.out.println("decrypt:\n" + decrypt(keyPair.getPrivate(), encripted));        
	}

	private static byte[] encrypt(Key pubkey, String text) {
	    try {
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA/ECB/PKCS1Padding");
	        rsa.init(Cipher.ENCRYPT_MODE, pubkey);
	        return rsa.doFinal(text.getBytes());
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}


	private static String decrypt(Key decryptionKey, byte[] buffer) {
	    try {
	        Cipher rsa;
	        rsa = Cipher.getInstance("RSA");
	        rsa.init(Cipher.DECRYPT_MODE, decryptionKey);
	        byte[] utf8 = rsa.doFinal(buffer);
	        return new String(utf8, "UTF8");
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

//	private static KeyPair readKeyPair(File privateKey, String keyPassword) throws IOException {
//	    FileReader fileReader = new FileReader(privateKey);
//	    PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword.toCharArray()));
//	    try {
//	        return (KeyPair) r.readObject();
//	    } catch (IOException ex) {
//	        throw ex;
//	    } finally {
//	        r.close();
//	        fileReader.close();
//	    }
//	}
//
//	private static Key readPublicKey(File privateKey, String keyPassword) throws IOException {
//	    FileReader fileReader = new FileReader(privateKey);
//	    PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword.toCharArray()));
//	    try {
//	        return (RSAPublicKey) r.readObject();
//	    } catch (IOException ex) {
//	        throw ex;
//	    } finally {
//	        r.close();
//	        fileReader.close();
//	    }
//	}

}
