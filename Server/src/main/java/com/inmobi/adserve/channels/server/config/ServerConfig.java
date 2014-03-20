package com.inmobi.adserve.channels.server.config;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.config.CasConfig;
import com.inmobi.adserve.channels.util.annotations.RtbConfiguration;
import com.inmobi.adserve.channels.util.annotations.ServerConfiguration;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ServerConfig implements CasConfig {

    private final Configuration serverConfiguration;
    private final Configuration rtbConfiguration;

    @Inject
    public ServerConfig(@ServerConfiguration final Configuration serverConfiguration,
            @RtbConfiguration final Configuration rtbConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.rtbConfiguration = rtbConfiguration;
    }

    public boolean isRtbEnabled() {
        return rtbConfiguration.getBoolean("isRtbEnabled");
    }

    public int getRtbRequestTimeoutInMillis() {
        return rtbConfiguration.getInt("RTBreadtimeoutMillis");
    }

    public double getRevenueWindow() {
        return serverConfiguration.getDouble("revenueWindow", 0.33);
    }

    public int getRtbBalanceFilterAmount() {
        return serverConfiguration.getInt("rtbBalanceFilterAmount", 50);
    }

    public int getMaxPartnerSegmentSelectionCount() {
        return serverConfiguration.getInt("partnerSegmentNo", 2);
    }

    public int getMaxSegmentSelectionCount() {
        return serverConfiguration.getInt("totalSegmentNo", -1);
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

    public int getDcpRequestTimeoutInMillis() {
        return serverConfiguration.getInt("readtimeoutMillis");
    }

    public List<Double> getSupplyClassFloors() {
        String[] supplyClassFloorStringArray = serverConfiguration.getStringArray("supplyClassFloors");

        List<Double> supplyClassFloors = Lists.newArrayList();
        for (String supplyClassFloor : supplyClassFloorStringArray) {
            supplyClassFloors.add(Double.valueOf(supplyClassFloor));
        }

        return supplyClassFloors;
    }

    public int getMaxDcpOutGoingConnections() {
        return serverConfiguration.getInt("dcpOutGoingMaxConnections", 200);
    }

    public int getMaxRtbOutGoingConnections() {
        return serverConfiguration.getInt("rtbOutGoingMaxConnections", 200);
    }

    public int getMaxIncomingConnections() {
        return serverConfiguration.getInt("incomingMaxConnections", 500);
    }

    public int getServerTimeoutInMillis() {
        return serverConfiguration.getInt("serverTimeoutMillis", 825);
    }

}
