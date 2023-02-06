package com.elcom.metacen.id.utils.encrypt;

import java.net.URLEncoder;
import javax.crypto.*;
import javax.crypto.spec.*;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;
import org.apache.commons.codec.binary.Base64;

/**
 * This program generates a AES key, retrieves its raw bytes, and then
 * instantiates a AES key from the key bytes. The instantiated key is used to
 * initialize a AES cipher for encryption and decryption.
 */
public class AES {

    public static void main(String[] args) throws Exception {
        String test = "hanm@elcom.com.vn&123456789";
        String key = "Elcom2020@123456";// key.length is 16
        System.out.println("Plain text: " + test);
        String en = encryptAESbase(test, key);
        System.out.println("encrypt AESbase: " + en);
        String de = decryptAESbase(en, key);
        System.out.println("decrypt AESbase: " + de);
        // AES new
        System.out.println("Encode = " + AES.Encrypt("http://elcom.com.vn", key));
        System.out.println("Encode = " + AES.Decrypt("Jjj+oK6iijgh5w5VRiO/nrn5oeGj2N2OBeeX2U2bVjM= ", key));
        System.out.println("URLEncoder = " + URLEncoder.encode("TRg2gmF4ZdpvASvtd0/tQA==", "UTF-8"));
        //
    }

    public static String encrypt(String plainText, String key) {
        try {
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128); // 192 and 256 bits may not be available

            byte[] keyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            byte[] encrypt = cipher.doFinal(plainText.getBytes());
            Base64 base64encoder = new Base64();
            return base64encoder.encodeAsString(encrypt);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String encryptAESbase(String plainText, String key) {
        try {
            String cipherText = "";
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128); // 192 and 256 bits may not be available

            byte[] keyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

            byte[] encrypt = cipher.doFinal(plainText.getBytes());
            cipherText = asHex(encrypt);

            return cipherText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String cipherText, String key) {
        try {
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128); // 192 and 256 bits may not be available

            byte[] keyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            Base64 base64decoder = new Base64();
            byte[] decrypt = base64decoder.decode(cipherText);

            byte ciphertextBytes[] = cipher.doFinal(decrypt);
            return bytes2String(ciphertextBytes);

        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public static String decryptAESbase(String cipherText, String key) {
        try {
            String plainText = "";
            // Get the KeyGenerator
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128); // 192 and 256 bits may not be available

            byte[] keyBytes = key.getBytes();
            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypt = cipher.doFinal(hexToBytes(cipherText));
            plainText = new String(decrypt);
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Turns array of bytes into string
     *
     * @param buf Array of bytes to convert to hex string
     * @return Generated hex string
     */
    public static String asHex(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;

        for (i = 0; i < buf.length; i++) {
            if (((int) buf[i] & 0xff) < 0x10) {
                strbuf.append("0");
            }
            strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
        }
        return strbuf.toString();
    }

    public static byte[] hexToBytes(char[] hex) {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++) {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127) {
                value -= 256;
            }
            raw[i] = (byte) value;
        }
        return raw;
    }

    public static byte[] hexToBytes(String hex) {
        return hexToBytes(hex.toCharArray());
    }

    private static String bytes2String(byte bytes[]) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            stringBuffer.append((char) bytes[i]);
        }
        return stringBuffer.toString();
    }

    public static String Decrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        Base64 decoder = new Base64();
        byte[] results = cipher.doFinal(decoder.decode(text));
        return new String(results, "UTF-8");
    }

    public static String Encrypt(String text, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] keyBytes = new byte[16];
        byte[] b = key.getBytes("UTF-8");
        int len = b.length;
        if (len > keyBytes.length) {
            len = keyBytes.length;
        }
        System.arraycopy(b, 0, keyBytes, 0, len);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(keyBytes);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        byte[] results = cipher.doFinal(text.getBytes("UTF-8"));
        Base64 encoder = new Base64();
        return encoder.encodeAsString(results);
    }
}
