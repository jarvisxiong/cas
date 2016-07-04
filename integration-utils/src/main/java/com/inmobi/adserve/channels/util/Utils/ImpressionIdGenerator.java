package com.inmobi.adserve.channels.util.Utils;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.inmobi.adserve.channels.util.demand.enums.SecondaryAdFormatConstraints;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

/**
 * Created by ishanbhatnagar on 16/9/14.
 */

public class ImpressionIdGenerator {
    private final int SIMILARITY_LIMIT = SecondaryAdFormatConstraints.values().length - 2;
    protected static final AtomicInteger COUNTER = new AtomicInteger();
    private static ImpressionIdGenerator instance = null;
    protected final short containerIdCode;
    protected final byte dataCenterIdCode;

    // Constructor for easier testing
    protected ImpressionIdGenerator(final short containerIdCode, final byte dataCenterIdCode) {
        this.containerIdCode = containerIdCode;
        this.dataCenterIdCode = dataCenterIdCode;
    }

    public static ImpressionIdGenerator getInstance() {
        if (null == instance) {
            throw new IllegalArgumentException("Class is not initialized yet");
        }
        return instance;
    }

    public static void init(final short containerIdCode, final byte dataCenterIdCode) {
        if (instance == null) {
            synchronized (ImpressionIdGenerator.class) {
                if (instance == null) {
                    instance = new ImpressionIdGenerator(containerIdCode, dataCenterIdCode);
                }
            }
        }
    }

    /**
     *
     * @param adId
     * @return
     */
    public String getImpressionId(final long adId) {
        final String uuidIntKey = WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId).toString();
        final String uuidMachineKey = WilburyUUID.setMachineId(uuidIntKey, containerIdCode).toString();
        final String uuidWithCyclicCounter = WilburyUUID
                .setCyclicCounter(uuidMachineKey, (byte) Math.abs(COUNTER.getAndIncrement() % 128)).toString();
        return WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode).toString();
    }

    /**
     *
     * @param adId
     * @return
     */
    public long getUniqueId(final long adId) {
        final String uuidIntKey = WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId).toString();
        final String uuidMachineKey = WilburyUUID.setMachineId(uuidIntKey, containerIdCode).toString();
        final String uuidWithCyclicCounter = WilburyUUID
                .setCyclicCounter(uuidMachineKey, (byte) Math.abs(COUNTER.getAndIncrement() % 128)).toString();
        return WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode).getLeastSignificantBits();
    }

    /**
     *
     * @param bid
     * @return
     */
    public String getEncryptedBid(final Double bid) {
        final long winBid = (long) (bid * Math.pow(10, 6));
        return getImpressionId(winBid);
    }

    /**
     * Takes a existing WilburyUUID String (oldImpressionId) and changes the int key to adId.
     * 
     * @param oldImpressionId
     * @param adId
     * @return New ImpressionId which differs from the oldImpressionId only in the int key.
     */
    public String resetWilburyIntKey(final String oldImpressionId, final long adId) {
        return WilburyUUID.setIntKey(oldImpressionId, (int) adId).toString();
    }

    /**
     * Checks whether two WilburyUUID strings are similar. Two WilburyUUID strings are said to be similar if all their
     * sub components (except their int key) match and their int keys differ by no more than the SIMILARITY_LIMIT.
     * 
     * @param impressionId1
     * @param impressionId2
     * @return
     */
    public boolean areImpressionIdsSimilar(final String impressionId1, final String impressionId2) {
        return WilburyUUID.setIntKey(impressionId1, 0).toString()
                .equals(WilburyUUID.setIntKey(impressionId2, 0).toString())
                && Math.abs(WilburyUUID.getIntKey(impressionId1)
                        - WilburyUUID.getIntKey(impressionId2)) <= SIMILARITY_LIMIT;
    }

    /**
     *
     * @param uuid
     * @return
     */
    public static Date extractDateFromImpId(final String uuid) {
        final UUID u = UUID.fromString(uuid);
        long lsig = u.getLeastSignificantBits();
        final long msig = u.getMostSignificantBits();
        lsig &= 0xff00ffff000000ffL;
        return new Date(new UUID(msig, lsig).timestamp());
    }

    public static void main(final String[] args) {
        final String impressionId = "d688ba4f-014f-1000-d715-3e90392b0064";

        System.out.println("UUID = " + WilburyUUID.extractUUID(impressionId));
        System.out.println("Impression Time = " + extractDateFromImpId(impressionId));
        System.out.println("Ad Version = " + WilburyUUID.getIntKey(impressionId));
        System.out.println("DataCenter Id = " + WilburyUUID.getDataCenterId(impressionId));
        System.out.println("Machine Id = " + WilburyUUID.getMachineId(impressionId));
        System.out.println("CyclicCounter = " + WilburyUUID.getCyclicCounter(impressionId));
    }

}
