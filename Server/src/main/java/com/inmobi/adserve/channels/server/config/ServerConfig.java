package com.inmobi.adserve.channels.server.config;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;
import org.testng.collections.Lists;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ServerConfig implements CasConfig {

    private final Configuration serverConfiguration;

    @Inject
    public ServerConfig(@ServerConfiguration final Configuration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public double getRevenueWindow() {
        return serverConfiguration.getDouble("revenueWindow", 0.33);
    }

    public int getRtbBalanceFilterAmount() {
        return serverConfiguration.getInt("rtbBalanceFilterAmount", 50);
    }

    public int getMaxSegmentSelectionCount() {
        return serverConfiguration.getInt("partnerSegmentNo", 2);
    }

    public double getNormalizingFactor() {
        return serverConfiguration.getDouble("normalizingFactor", 0.1);
    }

    public byte getDefaultSupplyClass() {
        return serverConfiguration.getByte("defaultSupplyClass", (byte) 9);
    }

    public byte getDefaultDemandClass() {
        return serverConfiguration.getByte("defaultDemandClass", (byte) 0);
    }

    public List<Double> getSupplyClassFloors() {
        String[] supplyClassFloorStringArray = serverConfiguration.getStringArray("supplyClassFloors");

        List<Double> supplyClassFloors = Lists.newArrayList();
        for (String supplyClassFloor : supplyClassFloorStringArray) {
            supplyClassFloors.add(Double.valueOf(supplyClassFloor));
        }

        return supplyClassFloors;
    }
}
