package org.rpi.utils;

import org.apache.log4j.Logger;
import org.rpi.plugin.lastfm.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;

public class SecUtils {

    private static final Logger LOGGER = Logger.getLogger(SecUtils.class);

    // Simple attempt to encode the password...
    public static  String encrypt(String key, String value) {
        try {
            byte[] raw = key.getBytes(Charset.forName("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encode(encrypted);
        } catch (Exception ex) {
            LOGGER.error("Error encrypt: " ,ex);
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
            LOGGER.error("Error decrypt: " ,ex);
        }
        return null;
    }

}
