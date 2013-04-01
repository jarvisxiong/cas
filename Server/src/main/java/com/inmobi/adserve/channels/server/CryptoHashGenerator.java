package com.inmobi.adserve.channels.server;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.inmobi.adserve.channels.util.DebugLogger;

public class CryptoHashGenerator {
  private ThreadSafeMac mac;
  private DebugLogger logger;

  private static class ThreadSafeMac extends ThreadLocal<Mac> {
    String secretKey;

    public ThreadSafeMac(String secretKey) {
      this.secretKey = secretKey;
    }

    public Mac initialValue() {
      Mac mac;
      SecretKeySpec sk;
      try {
        sk = new SecretKeySpec(secretKey.getBytes(), "HmacMD5");
        mac = Mac.getInstance("HmacMD5");
        mac.init(sk);
        return mac;
      } catch (NoSuchAlgorithmException e) {
        return null;
      } catch (InvalidKeyException e) {
        return null;
      }
    }
  }

  public CryptoHashGenerator(String secretKey, DebugLogger logger) {
    this.logger = logger;
    mac = new ThreadSafeMac(secretKey);
  }

  public String generateHash(String url) {
    CRC32 crc = new CRC32();
    crc.update(mac.get().doFinal(url.getBytes()));
    String hash = Long.toHexString(crc.getValue());
    logger.debug("CryptoHash Generated is", hash);
    return hash;
  }
}
