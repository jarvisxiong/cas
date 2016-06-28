package com.inmobi.adserve.channels.api.config;

import java.util.List;

import javax.inject.Inject;

import io.netty.util.internal.SystemPropertyUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.util.annotations.RtbConfiguration;
import com.inmobi.adserve.channels.util.annotations.ServerConfiguration;

/**
 * @author abhishek.parwal
 *
 */
@Slf4j
@Singleton
public class ServerConfig implements CasConfig {

    private static final int DEFAULT_PHOTON_NING_TIMEOUT = 10;
    private static final int DEFAULT_MAX_PHOTON_OUTGOING_CONNECTION = 200;
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
    private static final Integer NEGATIVE_ONE = -1;
    private static final int DEFAULT_EVENT_LOOP_THREADS;

    // Default value of worker thread is 2 * Number of available processor
    // source : io.netty.channel.MultithreadEventLoopGroup
    static {
        DEFAULT_EVENT_LOOP_THREADS = Math.max(1, SystemPropertyUtil.getInt(
                "io.netty.eventLoopThreads", Runtime.getRuntime().availableProcessors() * 2));
    }

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

    /**
     *
     * @return
     */
    public Integer getRoutingUH1ToUJ1Percentage() {
        return serverConfiguration.getInteger("routingFromUH1ToUJ1Percentage", NEGATIVE_ONE);
    }

    /**
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<String> getUSWestStatesCodes() {
        return serverConfiguration.getList("us.west.stateCodes", Lists.newArrayList());
    }

    public String getPhotonEndPoint() {
        return serverConfiguration.getString("photon.haproxy_endpoint");
    }

    public String getPhotonHeaderKey() {
        return serverConfiguration.getString("photon.headerKey");
    }

    public String getPhotonHeaderValue() {
        return serverConfiguration.getString("photon.headerValue");
    }

    public int getMaxPhotonOutGoingConnections() {
        try {
            return serverConfiguration.getInt("photon.outGoingMaxConnections", DEFAULT_MAX_PHOTON_OUTGOING_CONNECTION);
        } catch (final Exception e) {
            log.error("Exception while parsing Photon maximum out connection from config : {}", e.getMessage());
            return DEFAULT_MAX_PHOTON_OUTGOING_CONNECTION;
        }
    }

    public int getNingTimeoutInMillisForPhoton() {
        try {
            return serverConfiguration.getInt("photon.ning_timeout", DEFAULT_PHOTON_NING_TIMEOUT);
        } catch (final Exception e) {
            log.error("Exception while parsing photon ning timeout from config : {}", e.getMessage());
            return DEFAULT_PHOTON_NING_TIMEOUT;
        }
    }

    public int getNumOfWorkerThread() {
        try {
            final int numOfWorkerThread = DEFAULT_EVENT_LOOP_THREADS + serverConfiguration.getInt("photon.thread.count", 0);
            log.debug("Number of worker thread : {}", numOfWorkerThread);
            return numOfWorkerThread;
        } catch (final Exception e) {
            log.error("Exception while parsing worker thread count from config : {}, Default number of thread : {}",
                e.getMessage(), DEFAULT_EVENT_LOOP_THREADS);
            return DEFAULT_EVENT_LOOP_THREADS;
        }
    }

    public int getPhotonFutureTimeout() {
        final int defaultTimeout = 5;
        try {
            final int photonFutureTimeout = serverConfiguration.getInt("photon.future_timeout", defaultTimeout);
            log.debug("Photon Future timeout : {}", photonFutureTimeout);
            return photonFutureTimeout;
        } catch (final Exception e) {
            log.error("Exception while parsing photon future timeout from config : {}", e.getMessage());
            return defaultTimeout;
        }

    }
}
