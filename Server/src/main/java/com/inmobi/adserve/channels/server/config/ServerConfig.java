package com.inmobi.adserve.channels.server.config;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.annotations.ServerConfiguration;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ServerConfig {

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

    public double getNormalizingFactor() {
        return serverConfiguration.getDouble("normalizingFactor", 0.1);
    }

    public byte getDefaultSupplyClass() {
        return serverConfiguration.getByte("defaultSupplyClass", (byte) 9);
    }

    public byte getDefaultDemandClass() {
        return serverConfiguration.getByte("defaultDemandClass", (byte) 0);
    }

    public String[] getsupplyClassFloors() {
        return serverConfiguration.getStringArray("supplyClassFloors");
    }

}
