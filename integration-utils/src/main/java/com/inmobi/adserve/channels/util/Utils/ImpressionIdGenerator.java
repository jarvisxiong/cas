package com.inmobi.adserve.channels.util.Utils;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.phoenix.batteries.util.WilburyUUID;

/**
 * Created by ishanbhatnagar on 16/9/14.
 */

public class ImpressionIdGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ImpressionIdGenerator.class);
    private static final AtomicInteger COUNTER = new AtomicInteger();
    private final short hostIdCode;
    private final byte dataCenterIdCode;
    private static ImpressionIdGenerator instance = null;


    public static ImpressionIdGenerator getInstance() {
        if (null == instance) {
            throw new IllegalArgumentException("Class is not initialized yet");
        }
        return instance;
    }


    public static void init(final short hostIdCode, final byte dataCenterIdCode) {
        if (instance == null) {
            synchronized (ImpressionIdGenerator.class) {
                if (instance == null) {
                    instance = new ImpressionIdGenerator(hostIdCode, dataCenterIdCode);
                }
            }
        }
    }


    private ImpressionIdGenerator(final short hostIdCode, final byte dataCenterIdCode) {
        this.hostIdCode = hostIdCode;
        this.dataCenterIdCode = dataCenterIdCode;
    }


    public String getImpressionId(final long adId) {
        final String uuidIntKey = WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId).toString();
        final String uuidMachineKey = WilburyUUID.setMachineId(uuidIntKey, hostIdCode).toString();
        final String uuidWithCyclicCounter =
                WilburyUUID.setCyclicCounter(uuidMachineKey, (byte) Math.abs(COUNTER.getAndIncrement() % 128))
                        .toString();
        return WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode).toString();
    }
}
