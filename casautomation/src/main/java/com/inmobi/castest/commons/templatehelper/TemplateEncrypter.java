package com.inmobi.castest.commons.templatehelper;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import spark.Spark;

import com.inmobi.commons.security.api.InmobiSession;
import com.inmobi.commons.security.impl.InmobiSecurityImpl;
import com.inmobi.commons.security.util.exception.InmobiSecureException;
import com.inmobi.commons.security.util.exception.InvalidMessageException;

public class TemplateEncrypter {

    public static String getEncryptedResponse(String vastXML) throws InvalidMessageException,
            UnsupportedEncodingException {
        byte[] adResponsebyte = vastXML.getBytes(Charset.forName("UTF-8"));
        byte[] encryptedByte = encryptResponse(adResponsebyte);
        String adResponseStr = new String(encryptedByte, "UTF-8");
        System.out.println("Encrytpted Ad Response : " + adResponseStr);
        return adResponseStr;
    }

    public static void getEncryptedResponseHosted(String vastXML) throws InvalidMessageException,
            UnsupportedEncodingException, InterruptedException {
        byte[] adResponsebyte = vastXML.getBytes(Charset.forName("UTF-8"));
        byte[] encryptedByte = encryptResponse(adResponsebyte);
        String adResponseStr = new String(encryptedByte, "UTF-8");
        System.out.println("Encrytpted Ad Response : " + adResponseStr);

        Spark.post("/eres", (req, res) -> {
            res.body(adResponseStr);
            res.type("text/html");
            res.status(200);
            return res.toString();
        });
        System.out.println("Your encrypted response is now hosted for use !");
        Thread.sleep(120000); // setting it for 2 minutes

    }

    public static byte[] encryptResponse(byte[] byteResponse) throws InvalidMessageException {

        InmobiSession inmobiSession = new InmobiSecurityImpl(null).newSession(null);

        // key to encrypt
        String key = "abcdefghijklmnop";
        byte[] byteKey = key.getBytes(Charset.forName("UTF-8"));
        try {
            byteResponse = inmobiSession.write(byteResponse, byteKey, byteKey);
        } catch (InmobiSecureException e) {
            System.out.println("Exception while encrypting response from {}" + e);
            throw new RuntimeException(e);
        }
        return byteResponse;
    }
}
