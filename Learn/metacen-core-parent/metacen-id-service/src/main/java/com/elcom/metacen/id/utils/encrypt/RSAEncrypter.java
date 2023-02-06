/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elcom.metacen.id.utils.encrypt;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.apache.commons.codec.binary.Base64;
//import java.util.Base64;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

/**
 *
 * @author Admin
 */
public class RSAEncrypter {

    /**
     * Sign data with RSA Private Key
     *
     * @param data
     * @param keyPrivate
     * @return
     */
    public static String sign(String data, String keyPrivate) {
        try {
            Base64 base64 = new Base64();
            byte[] privateKeyBytes = base64.decode(keyPrivate);

            PrivateKey privateKey = KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
            Signature rsa = Signature.getInstance("SHA1withRSA");
            rsa.initSign(privateKey);
            rsa.update(data.getBytes());
            return base64.encodeAsString(rsa.sign());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean verify(String data, String sign, String keyPublic) {
        try {
            Base64 base64 = new Base64();
            byte[] publicKeyBytes = base64.decode(keyPublic);

            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(
                    new X509EncodedKeySpec(publicKeyBytes));
            Signature rsa = Signature.getInstance("SHA1withRSA");
            rsa.initVerify(publicKey);
            rsa.update(data.getBytes());
            byte[] signByte = base64.decode(sign);
            return rsa.verify(signByte);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAKAsgmXx82dlkAcN17GSKrxr/eju\n"
                + "57OxvXdEG46rRtCdgwmh4Dv3n5royrWkfIuh6WUeKHtg3koSSXwbT9GoTvs938ROIsseP7w8QD6C\n"
                + "irXRF+cS6n+TgbVe83TCXMn0jeoTR7xlhX3Nk+ntpwxggKHc5ed2yeYSJD8DtVScAdpLAgMBAAEC\n"
                + "gYEAkzQUqyhPcCkQ0srP/iu9BumgsqBNZQHmhGOdh+K09CiuMv4IHFSY5D8pfQIsTA+DdiDfR3Rh\n"
                + "FmX0DxDyrFYoLp4WzeLok488W+aoPwnM19Qndtk/7WObBACrabxQ1VHg8rVgEb5lL6jPOg83WGND\n"
                + "X/4KcVh803EdUXbwLpypuXECQQDM0RKOuQcFwcDK/fO+FUA5foCqzNAEZdpnU6d/+/dF4yZFWq7I\n"
                + "wvxJ8+yS6YAJKzmMrTXqNGSLMSiOuHTjt7e9AkEAyDN1zTmSwhUWLnNx1i7yfUrbfAV7P1YJPybp\n"
                + "M9BjabmP6hpH6JLxqh7+4fkL701v2yOJrTwR5UVHa5IJ1mXWpwJBAIMBa8xMfzhocOemPgSkVRsx\n"
                + "Vh1pudhGScdzk5ziToyuOSl9UnTpezPWSMjDf/jdZAceN3Ehp/n8LIP1ahbJyUUCQAbOWQ7lCiw2\n"
                + "804Y9qHFbtD7noKU3R/GGQJgnET6rKaHfHEE+7TYagbSju+3u8Qi6c6VYrTU+fbFaCwsBtGXHLkC\n"
                + "QQDJxDvTBPGo6TWF2QtUZikf8xGDyr7u4hOpRAzbxZL1jQ2lRy0Hm1ToqKi9PVvByYPABN3dDxCg\n"
                + "iYXjXLQNm9es";
        String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQChj0YbDuPLiLhFGsmgTwhadeg5PceK1kfL+tJn\n"
                + "Mxb/55/YCa0JXeCtMPz4vfzvuZZjtqKLiLXN9JrLfCJVIsGC+YrW1D3sGCsh+fwGB2hMQgh0GpbQ\n"
                + "o5zU7AaZ9RTzbGQgwJiBNhGzfl3uze5KDyw5Rm618DRpU8rMzTDlD1cxIQIDAQAB";

        String data = "123456789|0388245286|682396";
        String sign = sign(data, privateKey);
        boolean verify = verify(data, sign, publicKey);
        System.out.println("sign: " + sign);
        System.out.println("verify: " + verify);
    }
}
