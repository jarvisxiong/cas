package com.inmobi.adserve.channels.util.Utils;

import java.util.concurrent.atomic.AtomicInteger;

import com.inmobi.phoenix.batteries.util.WilburyUUID;

/**
 * Created by ishanbhatnagar on 16/9/14.
 */

public class ImpressionIdGenerator {
    protected static final AtomicInteger COUNTER = new AtomicInteger();
    private static ImpressionIdGenerator instance = null;
    protected final short hostIdCode;
    protected final byte dataCenterIdCode;

    protected ImpressionIdGenerator(final short hostIdCode, final byte dataCenterIdCode) {
        this.hostIdCode = hostIdCode;
        this.dataCenterIdCode = dataCenterIdCode;
    }

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

    public String getImpressionId(final long adId) {
        final String uuidIntKey = WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId).toString();
        final String uuidMachineKey = WilburyUUID.setMachineId(uuidIntKey, hostIdCode).toString();
        final String uuidWithCyclicCounter =
                WilburyUUID.setCyclicCounter(uuidMachineKey, (byte) Math.abs(COUNTER.getAndIncrement() % 128))
                        .toString();
        return WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode).toString();
    }

    public long getUniqueId(final long adId) {
        final String uuidIntKey = WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId).toString();
        final String uuidMachineKey = WilburyUUID.setMachineId(uuidIntKey, hostIdCode).toString();
        final String uuidWithCyclicCounter =
                WilburyUUID.setCyclicCounter(uuidMachineKey, (byte) Math.abs(COUNTER.getAndIncrement() % 128))
                        .toString();
        return WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode).getLeastSignificantBits();
    }

    /**
     * Takes a existing WilburyUUID String (oldImpressionId) and changes the int key to adId.
     * @param oldImpressionId
     * @param adId
     * @return New ImpressionId which differs from the oldImpressionId only in the int key.
     */
    public String resetWilburyIntKey(final String oldImpressionId, final long adId) {
        return WilburyUUID.setIntKey(oldImpressionId, (int) adId).toString();
    }

}
