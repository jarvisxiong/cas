package com.inmobi.adserve.channels.api.config;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.util.annotations.RtbConfiguration;
import com.inmobi.adserve.channels.util.annotations.ServerConfiguration;

/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class ServerConfig implements CasConfig {

    private final Configuration serverConfiguration;
    @SuppressWarnings("unused")
    private final Configuration rtbConfiguration;
    @SuppressWarnings("rawtypes")
    private final List auctionAdvertiserDomainFilterExcludedList;
    @SuppressWarnings("rawtypes")
    private final List auctionCreativeAttributeFilterExcludedList;
    @SuppressWarnings("rawtypes")
    private final List auctionCreativeIdFilterExcludedList;
    @SuppressWarnings("rawtypes")
    private final List auctionCreativeValidatorFilterExcludedList;
    @SuppressWarnings("rawtypes")
    private final List auctionIUrlFilterExcludedList;

    @Inject
    public ServerConfig(@ServerConfiguration final Configuration serverConfiguration,
            @RtbConfiguration final Configuration rtbConfiguration) {
        this.serverConfiguration = serverConfiguration;
        this.rtbConfiguration = rtbConfiguration;
        auctionAdvertiserDomainFilterExcludedList =
                serverConfiguration.getList("auction.exclude.AuctionAdvertiserDomainFilter", Lists.newArrayList());
        auctionCreativeAttributeFilterExcludedList =
                serverConfiguration.getList("auction.exclude.AuctionCreativeAttributeFilter", Lists.newArrayList());
        auctionCreativeIdFilterExcludedList =
                serverConfiguration.getList("auction.exclude.AuctionCreativeIdFilter", Lists.newArrayList());
        auctionCreativeValidatorFilterExcludedList =
                serverConfiguration.getList("auction.exclude.AuctionCreativeValidatorFilter", Lists.newArrayList());
        auctionIUrlFilterExcludedList =
                serverConfiguration.getList("auction.exclude.AuctionIUrlFilter", Lists.newArrayList());
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

    public List<Double> getSupplyClassFloors() {
        final String[] supplyClassFloorStringArray = serverConfiguration.getStringArray("supplyClassFloors");

        final List<Double> supplyClassFloors = Lists.newArrayList();
        for (final String supplyClassFloor : supplyClassFloorStringArray) {
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

    public int getNingTimeoutInMillisForRTB() {
        return serverConfiguration.getInt("ning.timeoutMillisForRTB", 180);
    }

    public int getNingTimeoutInMillisForDCP() {
        return serverConfiguration.getInt("ning.timeoutMillisForDCP", 600);
    }

    public int getCasTimeoutHandlerTimeoutInMillisForRTB() {
        return serverConfiguration.getInt("casTimeoutHandler.timeoutMillisForRTB", 180);
    }

    public int getCasTimeoutHandlerTimeoutInMillisForDCP() {
        return serverConfiguration.getInt("casTimeoutHandler.timeoutMillisForDCP", 600);
    }

    @SuppressWarnings("rawtypes")
    public List getExcludedAdvertisers(final String filter) {
        switch (filter) {
            case "AuctionAdvertiserDomainFilter":
                return auctionAdvertiserDomainFilterExcludedList;
            case "AuctionCreativeAttributeFilter":
                return auctionCreativeAttributeFilterExcludedList;
            case "AuctionCreativeIdFilter":
                return auctionCreativeIdFilterExcludedList;
            case "AuctionCreativeValidatorFilter":
                return auctionCreativeValidatorFilterExcludedList;
            case "AuctionIUrlFilter":
                return auctionIUrlFilterExcludedList;
            default:
                return Lists.newArrayList();
        }
    }

}
