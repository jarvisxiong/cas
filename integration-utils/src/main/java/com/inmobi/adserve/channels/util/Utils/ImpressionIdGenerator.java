package com.inmobi.adserve.channels.util.Utils;

import com.inmobi.phoenix.batteries.util.WilburyUUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ishanbhatnagar on 16/9/14.
 */

public class ImpressionIdGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ImpressionIdGenerator.class);
    private static final AtomicInteger counter = new AtomicInteger();
    private short hostIdCode;
    private byte dataCenterIdCode;
    private static ImpressionIdGenerator instance = null;


    public static ImpressionIdGenerator getInstance() {
        if(null == instance) {
            throw new IllegalArgumentException("Class is not initialized yet");
        }
        return instance;
    }


    public static void init(short hostIdCode, byte dataCenterIdCode) {
        if (instance == null) {
            synchronized (ImpressionIdGenerator.class) {
                if (instance == null) {
                    instance = new ImpressionIdGenerator(hostIdCode, dataCenterIdCode);
                }
            }
        }
    }


    private  ImpressionIdGenerator(short hostIdCode, byte dataCenterIdCode) {
        this.hostIdCode = hostIdCode;
        this.dataCenterIdCode = dataCenterIdCode;
    }


    public String getImpressionId(final long adId) {
        String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
        String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, hostIdCode)).toString();
        String uuidWithCyclicCounter = (WilburyUUID.setCyclicCounter(uuidMachineKey,
                (byte) (Math.abs(counter.getAndIncrement() % 128)))).toString();
        return (WilburyUUID.setDataCenterId(uuidWithCyclicCounter, dataCenterIdCode)).toString();
    }
}
