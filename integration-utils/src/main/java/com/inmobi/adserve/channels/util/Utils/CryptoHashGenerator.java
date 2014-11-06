package com.inmobi.adserve.channels.util.Utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CryptoHashGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CryptoHashGenerator.class);

    private final ThreadSafeMac mac;

    private static class ThreadSafeMac extends ThreadLocal<Mac> {
        String secretKey;

        public ThreadSafeMac(final String secretKey) {
            this.secretKey = secretKey;
        }

        @Override
        public Mac initialValue() {
            Mac mac;
            SecretKeySpec sk;
            try {
                sk = new SecretKeySpec(secretKey.getBytes(), "HmacMD5");
                mac = Mac.getInstance("HmacMD5");
                mac.init(sk);
                return mac;
            } catch (final NoSuchAlgorithmException e) {
                LOG.debug("Exception raised in CryptoHashGenerator {}", e);
                return null;
            } catch (final InvalidKeyException e) {
                LOG.debug("Exception raised CryptoHashGenerator {}", e);
                return null;
            }
        }
    }

    public CryptoHashGenerator(final String secretKey) {
        mac = new ThreadSafeMac(secretKey);
    }

    public String generateHash(final String url) {
        final CRC32 crc = new CRC32();
        crc.update(mac.get().doFinal(url.getBytes()));
        final String hash = Long.toHexString(crc.getValue());
        LOG.debug("CryptoHash Generated is {}", hash);
        return hash;
    }
}
