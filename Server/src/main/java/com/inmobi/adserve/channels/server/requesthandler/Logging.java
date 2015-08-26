package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserFailureThrottler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.Ad;
import com.inmobi.casthrift.AdIdChain;
import com.inmobi.casthrift.AdMeta;
import com.inmobi.casthrift.AdRR;
import com.inmobi.casthrift.AdStatus;
import com.inmobi.casthrift.AuctionInfo;
import com.inmobi.casthrift.CasAdChain;
import com.inmobi.casthrift.CasAdvertisementLog;
import com.inmobi.casthrift.Channel;
import com.inmobi.casthrift.ContentRating;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.casthrift.Gender;
import com.inmobi.casthrift.Geo;
import com.inmobi.casthrift.HandsetMeta;
import com.inmobi.casthrift.Impression;
import com.inmobi.casthrift.InventoryType;
import com.inmobi.casthrift.IxAd;
import com.inmobi.casthrift.PricingModel;
import com.inmobi.casthrift.RTBDAuctionInfo;
import com.inmobi.casthrift.Request;
import com.inmobi.casthrift.User;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;


public class Logging {
    public static final ConcurrentHashMap<String, String> SAMPLED_ADVERTISER_LOG_NOS =
            new ConcurrentHashMap<String, String>(2000);
    private static final Logger LOG = LoggerFactory.getLogger(Logging.class);
    private static final String BANNER = "BANNER";
    private static final String NO = "NO";
    private static String hostName;
    private static AbstractMessagePublisher dataBusPublisher;
    private static String rrLogKey;
    private static String sampledAdvertisementLogKey;
    private static String umpAdsLogKey;
    private static boolean enableFileLogging;
    private static boolean enableDatabusLogging;
    private static int totalCount;

    public static ConcurrentHashMap<String, String> getSampledadvertiserlognos() {
        return SAMPLED_ADVERTISER_LOG_NOS;
    }

    public static void init(final AbstractMessagePublisher dataBusPublisher, final String rrLogKey,
            final String advertisementLogKey, final String umpAdsLogKey, final Configuration config,
            final String hostNameStr) {
        Logging.dataBusPublisher = dataBusPublisher;
        Logging.rrLogKey = rrLogKey;
        Logging.sampledAdvertisementLogKey = advertisementLogKey;
        Logging.umpAdsLogKey = umpAdsLogKey;
        enableFileLogging = config.getBoolean("enableFileLogging");
        enableDatabusLogging = config.getBoolean("enableDatabusLogging");
        totalCount = config.getInt("sampledadvertisercount");
        hostName = hostNameStr;
    }

    // Writing Request Response Logs
    public static void rrLogging(final Marker traceMarker, final ChannelSegment channelSegment,
            final List<ChannelSegment> rankList, final SASRequestParameters sasParams,
            final CasInternalRequestParameters casInternalRequestParameters, final String terminationReason,
            final long totalTime) throws JSONException, TException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside rrLogging");
        }
        InspectorStats.incrementStatCount(InspectorStrings.LATENCY, totalTime);

        if (null != sasParams) {
            final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
            InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.LATENCY, totalTime);
            if (null != sasParams.getAllParametersJson() && (rankList == null || rankList.isEmpty())) {
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NO_MATCH_SEGMENT_COUNT);
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NO_MATCH_SEGMENT_LATENCY, totalTime);
                InspectorStats.incrementStatCount(InspectorStrings.NO_MATCH_SEGMENT_COUNT);
                InspectorStats.incrementStatCount(InspectorStrings.NO_MATCH_SEGMENT_LATENCY, totalTime);
            }
            if (null != sasParams.getRFormat() && GlobalConstant.NATIVE_STRING.equalsIgnoreCase(sasParams.getRFormat())) {
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.TOTAL_NATIVE_REQUESTS);
                if (rankList == null || rankList.isEmpty()) {
                    InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.TOTAL_NATIVE_REQUESTS + "-"
                            + InspectorStrings.NO_MATCH_SEGMENT_COUNT);
                }
            }
        }

        final AdRR adRR = getAdRR(channelSegment, rankList, sasParams, casInternalRequestParameters, terminationReason);
        if (null == adRR) {
            return;
        }

        if (enableDatabusLogging) {
            final TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            final Message msg = new Message(tSerializer.serialize(adRR));
            dataBusPublisher.publish(rrLogKey, msg);
            if (LOG.isDebugEnabled(traceMarker)) {
                LOG.debug(traceMarker, "ADRR is : {}", adRR);
            }
        }
        // Logging real time stats for graphite
        if (null != sasParams) {
            final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
            InspectorStats.updateYammerTimerStats(dst.name(), InspectorStrings.TIMER_LATENCY, totalTime);
        }
    }

    // Writing creatives
    public static void creativeLogging(final List<ChannelSegment> channelSegments,
            final SASRequestParameters sasRequestParameters) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside creativeLogging");
        }
        if (CollectionUtils.isEmpty(channelSegments) || null == sasRequestParameters) {
            return;
        }

        for (final ChannelSegment channelSegment : channelSegments) {
            final AdNetworkInterface adNetworkInterface = channelSegment.getAdNetworkInterface();

            // Log every new RTB or IX video ad creative.
            if ((adNetworkInterface.isRtbPartner() || adNetworkInterface.isIxPartner()) &&
                    adNetworkInterface.isLogCreative()) {
                String response = adNetworkInterface.getHttpResponseContent();
                if (adNetworkInterface.getCreativeType() == ADCreativeType.NATIVE) {
                    // adm is not populated for Native on IX. Use admobject instead
                    response = adNetworkInterface.getAdMarkUp();
                }

                final String requestUrl = adNetworkInterface.getRequestUrl();
                final ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
                final String partnerName = adNetworkInterface.getName();
                final String externalSiteKey = channelSegment.getChannelSegmentEntity().getExternalSiteKey();
                final String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
                final String adStatus = adResponse.getAdStatus();

                final CasAdvertisementLog creativeLog =
                        new CasAdvertisementLog(partnerName, requestUrl, response, adStatus, externalSiteKey,
                                advertiserId);

                creativeLog.setCountryId(sasRequestParameters.getCountryId().intValue());
                creativeLog.setImageUrl(adNetworkInterface.getIUrl());
                creativeLog.setCreativeAttributes(adNetworkInterface.getAttribute());
                creativeLog.setAdvertiserDomains(adNetworkInterface.getADomain());
                creativeLog.setCreativeId(adNetworkInterface.getCreativeId());
                creativeLog.setCreativeType(adNetworkInterface.getCreativeType());
                creativeLog.setTime_stamp(new Date().getTime());

                LOG.info("Creative msg is {}", creativeLog);
                Message msg = null;
                try {
                    final TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
                    msg = new Message(tSerializer.serialize(creativeLog));
                } catch (final TException e) {
                    LOG.debug("Error while creating creative logs for databus, raised exception {}", e);
                }
                if (null != msg) {
                    dataBusPublisher.publish(umpAdsLogKey, msg);
                }
            }
        }
    }

    public static List<Channel> createChannelsLog(final List<ChannelSegment> rankList) {
        if (null == rankList) {
            return new ArrayList<>();
        }
        final List<Channel> channels = new ArrayList<>();

        for (final ChannelSegment channelSegment : rankList) {
            final Channel channel = new Channel();
            final AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();
            final ThirdPartyAdResponse adResponse = adNetwork.getResponseStruct();

            channel.setAdStatus(getAdStatus(adNetwork.getAdStatus()));
            channel.setLatency(adNetwork.getLatency());
            channel.setAdChain(createCasAdChain(channelSegment));
            final double bid = adNetwork.getBidPriceInUsd();

            if (bid > 0) {
                channel.setBid(bid);
            }
            /**
             * Logging IX specific fields in ixAdInfo.
             * Populating this only if,
             *   1) we get an AD response from IX or,
             *   2) forward any package to RP.
             */
            if (adNetwork instanceof IXAdNetwork) {
                // Logging the original bid in case of agency rebate deals
                final IXAdNetwork ixAdNetwork = (IXAdNetwork) adNetwork;
                final double originalBid = ixAdNetwork.getOriginalBidPriceInUsd();
                if (originalBid > 0) {
                    channel.setBid(originalBid);
                }

                if (GlobalConstant.AD_STRING.equals(adResponse.getAdStatus())
                        || CollectionUtils.isNotEmpty(ixAdNetwork.getPackageIds())) {
                    channel.setIxAds(Arrays.asList(createIxAd(ixAdNetwork)));
                }
            }
            channels.add(channel);

            // Incrementing inspectors
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_REQUESTS);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.LATENCY, adResponse.getLatency());
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.CONNECTION_LATENCY,
                    adNetwork.getConnectionLatency());
            switch (adResponse.getAdStatus()) {
                case GlobalConstant.AD_STRING:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_FILLS);
                    break;
                case GlobalConstant.NO_AD:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_NO_FILLS);
                    break;
                case GlobalConstant.TIME_OUT:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_TIMEOUT);
                    InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TIMEOUT);
                    AdvertiserFailureThrottler.increamentRequestsThrottlerCounter(adNetwork.getId(),
                            adResponse.getStartTime());
                    break;
                default:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_TERMINATE);
                    InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TERMINATE);
                    AdvertiserFailureThrottler.increamentRequestsThrottlerCounter(adNetwork.getId(),
                            adResponse.getStartTime());
                    break;
            }
        }
        return channels;
    }

    public static CasAdChain createCasAdChain(final ChannelSegment channelSegment) {
        final CasAdChain casAdChain = new CasAdChain();
        final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();

        casAdChain.setAdvertiserId(channelSegmentEntity.getAdvertiserId());
        casAdChain.setCampaign_inc_id(channelSegmentEntity.getCampaignIncId());
        casAdChain.setAdgroup_inc_id(channelSegmentEntity.getAdgroupIncId());
        casAdChain.setExternalSiteKey(channelSegmentEntity.getExternalSiteKey());
        casAdChain.setDst(DemandSourceType.findByValue(channelSegmentEntity.getDst()));

        final String creativeId = channelSegment.getAdNetworkInterface().getCreativeId();
        if (null != creativeId) {
            casAdChain.setCreativeId(creativeId);
        }
        casAdChain
                .setAd_inc_id(channelSegmentEntity.getIncId(channelSegment.getAdNetworkInterface().getCreativeType()));
        return casAdChain;
    }

    public static IxAd createIxAd(final IXAdNetwork ixAdNetwork) {
        final IxAd ixAd = new IxAd();

        if (StringUtils.isNotEmpty(ixAdNetwork.getDspId())) {
            ixAd.setDspId(ixAdNetwork.getDspId());
        }
        if (StringUtils.isNotEmpty(ixAdNetwork.getAqid())) {
            ixAd.setAqId(ixAdNetwork.getAqid());
        }
        if (StringUtils.isNotEmpty(ixAdNetwork.getAdvId())) {
            ixAd.setAdvId(ixAdNetwork.getAdvId());
        }
        if (StringUtils.isNotEmpty(ixAdNetwork.getSeatId())) {
            ixAd.setSeatId(ixAdNetwork.getSeatId());
        }

        // Log all the package Ids which were sent to RP.
        if (CollectionUtils.isNotEmpty(ixAdNetwork.getPackageIds())) {
            ixAd.setPackageIds(ixAdNetwork.getPackageIds());

            // Log winning dealId
            if (StringUtils.isNotEmpty(ixAdNetwork.getDealId())) {
                ixAd.setWinningDealId(ixAdNetwork.getDealId());
            }
            // Log winning PackageId
            if (null != ixAdNetwork.getWinningPackageId()) {
                ixAd.setWinningPackageId(ixAdNetwork.getWinningPackageId());
            }

            // Log highest Bid
            if (null != ixAdNetwork.getAdjustbid()) {
                ixAd.setHighestBid(ixAdNetwork.getAdjustbid());
            }

            // Log agency rebate percentage
            if (null != ixAdNetwork.getAgencyRebatePercentage()) {
                ixAd.setAgencyRebatePercentage(ixAdNetwork.getAgencyRebatePercentage());
            }
        }

        return ixAd;
    }

    protected static AdRR getAdRR(final ChannelSegment channelSegment, final List<ChannelSegment> rankList,
            final SASRequestParameters sasParams, final CasInternalRequestParameters casInternalRequestParameters,
            String terminationReason) {

        AdRR adRR;

        boolean isTerminated = false;
        if (null != terminationReason) {
            isTerminated = true;
        } else {
            terminationReason = NO;
        }

        if (null == hostName) {
            LOG.info("Host cant be empty, abandoning rr logging");
            return null;
        }

        short adsServed = 0;
        List<Impression> impressions = null;
        final Impression impression = getImpressionObject(channelSegment, sasParams);
        if (null != impression) {
            adsServed = 1;
            impressions = new ArrayList<>();
            impressions.add(impression);
        }

        Short slotServed = null;
        if (channelSegment != null) {
            slotServed = channelSegment.getAdNetworkInterface().getSelectedSlotId();
        }

        Short requestSlot = slotServed;
        if (null != sasParams && null == requestSlot) {
            requestSlot = sasParams.getRqMkSlot().get(0);
        }

        final String timestamp = new Date().toString();
        final Request request = getRequestObject(sasParams, adsServed, requestSlot, slotServed, rankList);
        final List<Channel> channels = createChannelsLog(rankList);

        adRR = new AdRR(hostName, timestamp, request, impressions, isTerminated, terminationReason);
        adRR.setAuction_info(getAuctionInfo(sasParams, casInternalRequestParameters));
        adRR.setTime_stamp(new Date().getTime());
        adRR.setChannels(channels);

        return adRR;
    }

    protected static AuctionInfo getAuctionInfo(final SASRequestParameters sasParams,
                                                final CasInternalRequestParameters casParams) {
        AuctionInfo auctionInfo = null;
        if (null != sasParams && DemandSourceType.RTBD.getValue() == sasParams.getDst()) {
            final double bidGuidance = sasParams.getMarketRate();

            if (0 != bidGuidance) {
                RTBDAuctionInfo rtbdAuctionInfo = new RTBDAuctionInfo(
                        casParams.getDemandDensity()/bidGuidance,
                        casParams.getLongTermRevenue()/bidGuidance,
                        casParams.getPublisherYield());
                auctionInfo = new AuctionInfo();
                auctionInfo.setRtbd_auction_info(rtbdAuctionInfo);
            }
        }

        return auctionInfo;
    }

    protected static Impression getImpressionObject(final ChannelSegment channelSegment,
            final SASRequestParameters sasParams) {
        Impression impression = null;

        if (null != channelSegment) {
            final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
            final AdNetworkInterface adNetworkInterface = channelSegment.getAdNetworkInterface();

            if (null == channelSegmentEntity || null == adNetworkInterface) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("channelSegmentEntity or adNetworkInterface is null");
                }
                return impression;
            }

            InspectorStats.incrementStatCount(adNetworkInterface.getName(), InspectorStrings.SERVER_IMPRESSION);
            final AdIdChain adChain =
                    new AdIdChain(channelSegmentEntity.getAdId(adNetworkInterface.getCreativeType()),
                            channelSegmentEntity.getAdgroupId(), channelSegmentEntity.getCampaignId(),
                            channelSegmentEntity.getAdvertiserId(), channelSegmentEntity.getExternalSiteKey());
            final ContentRating contentRating = getContentRating(sasParams);
            final PricingModel pricingModel = getPricingModel(channelSegmentEntity.getPricingModel());
            final AdMeta adMeta = new AdMeta(contentRating, pricingModel, BANNER);
            final Ad ad = new Ad(adChain, adMeta);

            // In case of Agency Rebates, this is the net bid. The gross bid is logged in channelsLog
            final double winBid = adNetworkInterface.getSecondBidPriceInUsd();
            ad.setWinBid(winBid);

            impression = new Impression(adNetworkInterface.getImpressionId(), ad);
            impression.setAdChain(createCasAdChain(channelSegment));
        }
        return impression;
    }

    protected static Request getRequestObject(final SASRequestParameters sasParams, final short adsServed,
            final Short requestSlot, final Short slotServed, final List<ChannelSegment> rankList) {
        final short adRequested = 1;
        Request request;
        if (null != sasParams) {
            request = new Request(adRequested, adsServed, sasParams.getSiteId(), sasParams.getTid());
            // Hack for getting discounted auction bid floor
            if (CollectionUtils.isNotEmpty(rankList)) {
                final AdNetworkInterface adNetworkInterface = rankList.get(0).getAdNetworkInterface();
                if (null != adNetworkInterface.getForwardedBidFloor()) {
                    request.setAuctionBidFloor(adNetworkInterface.getForwardedBidFloor());
                }
                if (null != adNetworkInterface.getForwardedBidGuidance()) {
                    request.setBidGuidance(adNetworkInterface.getForwardedBidGuidance());
                }
            }

            // Currently only populating the siteSegmentId
            final Integer siteSegmentId = sasParams.getSiteSegmentId();
            if (null != siteSegmentId) {
                request.setSegmentId(siteSegmentId);
            }
            request.setRequestDst(DemandSourceType.findByValue(sasParams.getDst()));

        } else {
            request = new Request(adRequested, adsServed, null, null);
        }

        request.setIP(getGeoObject(sasParams));
        request.setUser(getUserObject(sasParams));
        request.setHandset(getHandsetMetaObject(sasParams));
        request.setInventory(getInventoryType(sasParams));

        if (null != slotServed) {
            request.setSlot_served(slotServed);
        }
        if (null != requestSlot) {
            request.setSlot_requested(requestSlot);
        }
        return request;
    }

    protected static Geo getGeoObject(final SASRequestParameters sasParams) {
        Geo geo = null;

        if (null != sasParams) {
            final Long countryId = sasParams.getCountryId();
            final Integer carrierId = sasParams.getCarrierId();
            final Integer state = sasParams.getState();
            final Integer city = sasParams.getCity();

            if (null != carrierId && null != countryId) {
                geo = new Geo(carrierId, countryId.shortValue());
                if (null != state) {
                    geo.setRegion(state);
                }
                if (null != city) {
                    geo.setCity(city);
                }
            }
        }
        return geo;
    }

    protected static User getUserObject(final SASRequestParameters sasParams) {
        final User user = new User();
        if (null != sasParams) {
            final Short age = sasParams.getAge();

            if (null != age) {
                user.setAge(age);
            }
            if (null != sasParams.getGender()) {
                user.setGender(getGender(sasParams));
            }
            user.setUids(sasParams.getTUidParams());
        }
        return user;
    }

    protected static HandsetMeta getHandsetMetaObject(final SASRequestParameters sasParams) {
        final HandsetMeta handsetMeta = new HandsetMeta();

        if (null != sasParams) {
            final Long handsetInternalId = sasParams.getHandsetInternalId();
            if (null != handsetInternalId) {
                handsetMeta.setId(handsetInternalId.intValue());
            }
            if (0 != sasParams.getOsId()) {
                handsetMeta.setOsId(sasParams.getOsId());
            }
        }
        return handsetMeta;
    }

    public static AdStatus getAdStatus(final String adStatus) {
        if (GlobalConstant.AD_STRING.equalsIgnoreCase(adStatus)) {
            return AdStatus.AD;
        } else if (GlobalConstant.NO_AD.equals(adStatus)) {
            return AdStatus.NO_AD;
        } else if (GlobalConstant.TIME_OUT.equals(adStatus)) {
            return AdStatus.TIME_OUT;
        }
        return AdStatus.DROPPED;
    }

    private static Logger getLogger(final String logger) {
        return LoggerFactory.getLogger(logger);
    }

    public static void advertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside advertiserLogging");
        }
        final Logger advertiserLogger = getLogger(config.getString("advertiser"));
        if (!advertiserLogger.isDebugEnabled()) {
            return;
        }
        final char sep = 0x01;
        final StringBuilder log = new StringBuilder();
        LOG.debug("got logger handle for advertiser logs");
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            final AdNetworkInterface adNetworkInterface = rankList.get(index).getAdNetworkInterface();
            final ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            final String partnerName = adNetworkInterface.getName();
            log.append(partnerName);
            log.append(sep).append(adResponse.getAdStatus());
            String response = StringUtils.EMPTY;
            String requestUrl = StringUtils.EMPTY;
            if (GlobalConstant.AD_STRING.equalsIgnoreCase(adResponse.getAdStatus())) {
                response = adNetworkInterface.getHttpResponseContent();
                log.append(sep).append(response);
            }
            if (!StringUtils.EMPTY.equals(adNetworkInterface.getRequestUrl())) {
                requestUrl = adNetworkInterface.getRequestUrl();
                log.append(sep).append(requestUrl);
            }
            if (index != rankList.size() - 1) {
                log.append("\n");
            }
        }
        if (enableFileLogging && log.length() > 0) {
            advertiserLogger.debug(log.toString());
            if (LOG.isDebugEnabled()) {
                LOG.debug("done with advertiser logging");
            }
        }
    }

    /**
     * @param rankList
     * @param config
     */
    public static void sampledAdvertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside sampledAdvertiserLogging");
        }
        final Logger sampledAdvertiserLogger = LoggerFactory.getLogger(config.getString("sampledadvertiser"));

        final char sep = 0x01;
        final StringBuilder log = new StringBuilder();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Got logger handle for sampledAdvertiser logs");
        }

        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            final AdNetworkInterface adNetworkInterface = rankList.get(index).getAdNetworkInterface();
            final ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            final String adStatus = adResponse.getAdStatus();
            final String partnerName = adNetworkInterface.getName();
            final String externalSiteKey = rankList.get(index).getChannelSegmentEntity().getExternalSiteKey();
            final String advertiserId = rankList.get(index).getChannelSegmentEntity().getAdvertiserId();
            final String requestUrl = adNetworkInterface.getRequestUrl();
            String response = adNetworkInterface.getHttpResponseContent();

            if (adNetworkInterface.getCreativeType() == ADCreativeType.NATIVE) {
                response = adNetworkInterface.getAdMarkUp();
            }

            if (!GlobalConstant.AD_STRING.equalsIgnoreCase(adStatus) || StringUtils.EMPTY.equals(requestUrl)
                    || StringUtils.EMPTY.equals(response)) {
                continue;
            }

            if (enableDatabusLogging && decideToLog(partnerName, externalSiteKey)) {
                // Actual Logging to stream
                final CasAdvertisementLog casAdvertisementLog =
                        new CasAdvertisementLog(partnerName, requestUrl, response, adStatus, externalSiteKey,
                                advertiserId);
                casAdvertisementLog.setCreativeType(adNetworkInterface.getCreativeType());
                sendToDatabus(casAdvertisementLog, sampledAdvertisementLogKey);
            }

            // File Logging
            if (index > 0 && partnerName.length() > 0 && log.length() > 0) {
                log.append("\n");
            }
            log.append(partnerName).append(sep)
                    .append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
            log.append(sep).append(requestUrl).append(sep).append(adStatus);
            log.append(sep).append(response).append(sep).append(advertiserId);
        }

        // Actual File Logging
        if (enableFileLogging && log.length() > 0) {
            sampledAdvertiserLogger.debug(log.toString());
            LOG.debug("done with sampledAdvertiser logging");
        }
    }

    /**
     * @param casAdvertisementLog
     */
    private static void sendToDatabus(final CasAdvertisementLog casAdvertisementLog,
            final String sampledAdvertisementLogKey) {
        Message msg = null;
        try {
            final TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            msg = new Message(tSerializer.serialize(casAdvertisementLog));
        } catch (final TException e) {
            LOG.debug("Error while creating sampledAdvertiser logs for databus, raised exception {}", e);
        }
        if (null != msg) {
            dataBusPublisher.publish(sampledAdvertisementLogKey, msg);
            LOG.debug("sampledAdvertiser log pushed to stream");
        }
    }

    /**
     * @param partnerName
     * @param externalSiteId
     * @return true if logging required otherwise false
     */
    protected static boolean decideToLog(final String partnerName, final String externalSiteId) {
        final long currentTime = System.currentTimeMillis();

        if (null == SAMPLED_ADVERTISER_LOG_NOS.get(partnerName + externalSiteId)) {
            SAMPLED_ADVERTISER_LOG_NOS.put(partnerName + externalSiteId, currentTime + "_" + 0);
        }
        Long time = Long.parseLong(SAMPLED_ADVERTISER_LOG_NOS.get(partnerName + externalSiteId).split("_")[0]);
        if (currentTime - time >= 3600000) {
            SAMPLED_ADVERTISER_LOG_NOS.put(partnerName + externalSiteId, currentTime + "_" + 0);
            time = Long.parseLong(SAMPLED_ADVERTISER_LOG_NOS.get(partnerName + externalSiteId).split("_")[0]);
        }
        Integer count = Integer.parseInt(SAMPLED_ADVERTISER_LOG_NOS.get(partnerName + externalSiteId).split("_")[1]);
        if (count >= totalCount) {
            return false;
        }
        count++;
        SAMPLED_ADVERTISER_LOG_NOS.put(partnerName + externalSiteId, time + "_" + count);
        return true;
    }

    public static ContentRating getContentRating(final SASRequestParameters sasParams) {
        if (sasParams == null || null == sasParams.getSiteContentType()) {
            return null;
        } else {
            final ContentType sasSiteContentType = sasParams.getSiteContentType();

            if (ContentType.PERFORMANCE == sasSiteContentType) {
                return ContentRating.PERFORMANCE;
            } else if (ContentType.FAMILY_SAFE == sasSiteContentType) {
                return ContentRating.FAMILY_SAFE;
            } else if (ContentType.MATURE == sasSiteContentType) {
                return ContentRating.MATURE;
            }
        }
        return null;
    }

    public static PricingModel getPricingModel(final String pricingModel) {
        if (pricingModel == null) {
            return null;
        } else if ("cpc".equalsIgnoreCase(pricingModel)) {
            return PricingModel.CPC;
        } else if ("cpm".equalsIgnoreCase(pricingModel)) {
            return PricingModel.CPM;
        }
        return null;
    }

    public static InventoryType getInventoryType(final SASRequestParameters sasParams) {
        if (null != sasParams && sasParams.getSdkVersion() != null
                && GlobalConstant.ZERO.equalsIgnoreCase(sasParams.getSdkVersion())) {
            return InventoryType.BROWSER;
        }
        return InventoryType.APP;
    }

    public static Gender getGender(final SASRequestParameters sasParams) {
        if (sasParams == null) {
            return null;
        } else if (GlobalConstant.GENDER_MALE.equalsIgnoreCase(sasParams.getGender())) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }
}
