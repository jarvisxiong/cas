package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.Ad;
import com.inmobi.casthrift.AdIdChain;
import com.inmobi.casthrift.AdMeta;
import com.inmobi.casthrift.AdRR;
import com.inmobi.casthrift.AdStatus;
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
import com.inmobi.casthrift.PricingModel;
import com.inmobi.casthrift.Request;
import com.inmobi.casthrift.User;
import com.inmobi.messaging.Message;
import com.inmobi.messaging.publisher.AbstractMessagePublisher;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Logging {

    public static final ConcurrentHashMap<String, String> SAMPLED_ADVERTISER_LOG_NOS =
            new ConcurrentHashMap<String, String>(2000);


    private static final Logger LOG = LoggerFactory.getLogger(Logging.class);
    private static AbstractMessagePublisher dataBusPublisher;
    private static String rrLogKey;
    private static String sampledAdvertisementLogKey;
    private static String umpAdsLogKey;
    private static boolean enableFileLogging;
    private static boolean enableDatabusLogging;

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String> advertiserIdNameMap;

    private static int totalCount;

    public static ConcurrentHashMap<String, String> getSampledadvertiserlognos() {
        return SAMPLED_ADVERTISER_LOG_NOS;
    }

    public static void init(final AbstractMessagePublisher dataBusPublisher, final String rrLogKey,
                            final String advertisementLogKey, final String umpAdsLogKey, final Configuration config) {
        Logging.dataBusPublisher = dataBusPublisher;
        Logging.rrLogKey = rrLogKey;
        Logging.sampledAdvertisementLogKey = advertisementLogKey;
        Logging.umpAdsLogKey = umpAdsLogKey;
        enableFileLogging = config.getBoolean("enableFileLogging");
        enableDatabusLogging = config.getBoolean("enableDatabusLogging");
        totalCount = config.getInt("sampledadvertisercount");
    }

    // Writing rrlogs
    public static void rrLogging(final Marker traceMarker, final ChannelSegment channelSegment,
                                 final List<ChannelSegment> rankList, final SASRequestParameters sasParams, String terminationReason,
                                 final long totalTime) throws JSONException, TException {
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
            if (null != sasParams.getRFormat() && "native".equalsIgnoreCase(sasParams.getRFormat())) {
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NATIVE_REQUESTS);
                if (rankList == null || rankList.isEmpty()) {
                    InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NATIVE_REQUESTS + "-"
                            + InspectorStrings.NO_MATCH_SEGMENT_COUNT);
                }
            }
        }

        boolean isTerminated = false;
        if (null != terminationReason) {
            isTerminated = true;
        } else {
            terminationReason = "NO";
        }
        short adsServed = 0;
        String host;
        try {
            final InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostName();
            if (host == null) {
                LOG.info("host cant be empty, abandoning rr logging");
                return;
            }
        } catch (final UnknownHostException ex) {
            LOG.info("could not resolve host inside rr logging so abandoning response, raised exception {}", ex);
            return;
        }
        final InventoryType inventory = getInventoryType(sasParams);
        final String timestamp = new Date().toString();
        AdIdChain adChain;
        AdMeta adMeta;
        Ad ad;
        Impression impression = null;

        Short slotServed = null;

        if (channelSegment != null) {
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.SERVER_IMPRESSION);
            adsServed = 1;
            final ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
            adChain =
                    new AdIdChain(
                            channelSegmentEntity.getAdId(channelSegment.getAdNetworkInterface().getCreativeType()),
                            channelSegmentEntity.getAdgroupId(), channelSegmentEntity.getCampaignId(),
                            channelSegmentEntity.getAdvertiserId(), channelSegmentEntity.getExternalSiteKey());
            final ContentRating contentRating = getContentRating(sasParams);
            final PricingModel pricingModel = getPricingModel(channelSegmentEntity.getPricingModel());
            adMeta = new AdMeta(contentRating, pricingModel, "BANNER"); // TODO: Check "BANNER" point
            ad = new Ad(adChain, adMeta);
            impression = new Impression(channelSegment.getAdNetworkInterface().getImpressionId(), ad);
            impression.setAdChain(createCasAdChain(channelSegment));

            // For IX, log all the package IDs which were sent to RP.
            if (channelSegment.getAdNetworkInterface() instanceof IXAdNetwork) {
                IXAdNetwork ixadNetwork = (IXAdNetwork) channelSegment.getAdNetworkInterface();
                if (null != ixadNetwork.getPackageIds()) {
                    impression.setIxPackageIds(ixadNetwork.getPackageIds());
                }
            }
            final double winBid = channelSegment.getAdNetworkInterface().getSecondBidPriceInUsd();
            if (winBid != -1) {
                ad.setWinBid(winBid);
            }
            slotServed = channelSegment.getAdNetworkInterface().getSelectedSlotId();
        }
        Short requestSlot = slotServed;
        Long handsetInternalId = null;
        Long countryId = null;
        Integer carrierId = null;
        Integer state = null;
        Integer city = null;
        if (null != sasParams) {
            handsetInternalId = sasParams.getHandsetInternalId();
            countryId = sasParams.getCountryId();
            carrierId = sasParams.getCarrierId();
            if (null == requestSlot) {
                requestSlot = sasParams.getRqMkSlot().get(0);
            }
            state = sasParams.getState();
            city = sasParams.getCity();
        }

        Geo geo = null;
        if (null != carrierId && null != countryId) {
            geo = new Geo(carrierId, countryId.shortValue());
            if (null != state) {
                geo.setRegion(state);
            }
            if (null != city) {
                geo.setCity(city);
            }
        }

        final HandsetMeta handsetMeta = new HandsetMeta();
        if (null != handsetInternalId) {
            handsetMeta.setId(handsetInternalId.intValue());
        }

        if (null != sasParams && sasParams.getOsId() != 0) {
            handsetMeta.setOsId(sasParams.getOsId());
        }

        final User user = new User();
        if (null != sasParams) {
            if (null != sasParams.getAge()) {
                user.setAge(sasParams.getAge());
            }
            if (null != sasParams.getGender()) {
                user.setGender(getGender(sasParams));
            }
            user.setUids(sasParams.getTUidParams());
        }
        final short adRequested = 1;
        final Request request =
                new Request(adRequested, adsServed, sasParams == null ? null : sasParams.getSiteId(), sasParams == null
                        ? null
                        : sasParams.getTid());
        if (slotServed != null) {
            request.setSlot_served(Integer.valueOf(slotServed).shortValue());
        }
        request.setIP(geo);
        request.setHandset(handsetMeta);
        request.setInventory(inventory);
        request.setUser(user);
        if (requestSlot != null) {
            request.setSlot_requested(requestSlot);
        }
        if (null != sasParams && null != sasParams.getSiteSegmentId()) {
            request.setSegmentId(sasParams.getSiteSegmentId());
        }

        if (null != sasParams) {
            request.setRequestDst(DemandSourceType.findByValue(sasParams.getDst()));
        }

        List<Impression> impressions = null;
        if (null != impression) {
            impressions = new ArrayList<Impression>();
            impressions.add(impression);
        }
        final AdRR adRR = new AdRR(host, timestamp, request, impressions, isTerminated, terminationReason);
        adRR.setTime_stamp(new Date().getTime());
        final List<Channel> channels = createChannelsLog(rankList);
        adRR.setChannels(channels);
        if (enableDatabusLogging) {
            final TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            final Message msg = new Message(tSerializer.serialize(adRR));
            dataBusPublisher.publish(rrLogKey, msg);
            LOG.debug(traceMarker, "ADRR is : {}", adRR);
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
        LOG.debug("inside creativeLogging");
        if (null == channelSegments || channelSegments.isEmpty()) {
            return;
        }

        for (final ChannelSegment channelSegment : channelSegments) {
            final AdNetworkInterface adNetworkInterface = channelSegment.getAdNetworkInterface();
            if (adNetworkInterface.isRtbPartner() && adNetworkInterface.isLogCreative()) {
                String response = adNetworkInterface.getHttpResponseContent();
                if (adNetworkInterface.getCreativeType() == ADCreativeType.NATIVE) {
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
                if (adNetworkInterface.getDst() == DemandSourceType.RTBD) {
                    creativeLog.setImageUrl(adNetworkInterface.getIUrl());
                    creativeLog.setCreativeAttributes(adNetworkInterface.getAttribute());
                    creativeLog.setAdvertiserDomains(adNetworkInterface.getADomain());
                }
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
            return new ArrayList<Channel>();
        }
        final List<Channel> channels = new ArrayList<Channel>();
        for (final ChannelSegment channelSegment : rankList) {
            final Channel channel = new Channel();
            channel.setAdStatus(getAdStatus(channelSegment.getAdNetworkInterface().getAdStatus()));
            channel.setLatency(channelSegment.getAdNetworkInterface().getLatency());
            channel.setAdChain(createCasAdChain(channelSegment));
            final double bid = channelSegment.getAdNetworkInterface().getBidPriceInUsd();
            if (bid > 0) {
                channel.setBid(bid);
            }
            channels.add(channel);
            // Incrementing inspectors
            final AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();
            final ThirdPartyAdResponse adResponse = adNetwork.getResponseStruct();
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_REQUESTS);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.LATENCY, adResponse.getLatency());
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.CONNECTION_LATENCY,
                    adNetwork.getConnectionLatency());
            switch (adResponse.getAdStatus()) {
                case "AD":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_FILLS);
                    break;
                case "NO_AD":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_NO_FILLS);
                    break;
                case "TIME_OUT":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_TIMEOUT);
                    InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TIMEOUT);
                    break;
                default:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.TOTAL_TERMINATE);
                    InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TERMINATE);
                    break;
            }
        }
        return channels;
    }

    public static CasAdChain createCasAdChain(final ChannelSegment channelSegment) {
        final CasAdChain casAdChain = new CasAdChain();
        casAdChain.setAdvertiserId(channelSegment.getChannelSegmentEntity().getAdvertiserId());
        casAdChain.setCampaign_inc_id(channelSegment.getChannelSegmentEntity().getCampaignIncId());
        casAdChain.setAdgroup_inc_id(channelSegment.getChannelSegmentEntity().getAdgroupIncId());
        casAdChain.setExternalSiteKey(channelSegment.getChannelSegmentEntity().getExternalSiteKey());
        casAdChain.setDst(DemandSourceType.findByValue(channelSegment.getChannelSegmentEntity().getDst()));
        if (null != channelSegment.getAdNetworkInterface().getCreativeId()) {
            casAdChain.setCreativeId(channelSegment.getAdNetworkInterface().getCreativeId());
        }
        return casAdChain;
    }

    public static AdStatus getAdStatus(final String adStatus) {
        if ("AD".equalsIgnoreCase(adStatus)) {
            return AdStatus.AD;
        } else if ("NO_AD".equals(adStatus)) {
            return AdStatus.NO_AD;
        } else if ("TIME_OUT".equals(adStatus)) {
            return AdStatus.TIME_OUT;
        }
        return AdStatus.DROPPED;
    }

    public static void advertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        LOG.debug("came inside advertiser log");
        final Logger advertiserLogger = LoggerFactory.getLogger(config.getString("advertiser"));
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
            String response = "";
            String requestUrl = "";
            if ("AD".equalsIgnoreCase(adResponse.getAdStatus())) {
                response = adNetworkInterface.getHttpResponseContent();
                log.append(sep).append(response);
            }
            if (!"".equals(adNetworkInterface.getRequestUrl())) {
                requestUrl = adNetworkInterface.getRequestUrl();
                log.append(sep).append(requestUrl);
            }
            if (index != rankList.size() - 1) {
                log.append("\n");
            }
        }
        if (enableFileLogging && log.length() > 0) {
            advertiserLogger.debug(log.toString());
            LOG.debug("done with advertiser logging");
        }
    }

    /**
     * @param rankList
     * @param config
     */
    public static void sampledAdvertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        LOG.debug("came inside sampledAdvertiser log");
        final Logger sampledAdvertiserLogger = LoggerFactory.getLogger(config.getString("sampledadvertiser"));

        final char sep = 0x01;
        final StringBuilder log = new StringBuilder();
        LOG.debug("got logger handle for sampledAdvertiser logs");

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

            if (!"AD".equalsIgnoreCase(adStatus) || "".equals(requestUrl) || "".equals(response)) {
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
    private static boolean decideToLog(final String partnerName, final String externalSiteId) {
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
            ContentType sasSiteContentType = sasParams.getSiteContentType();

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
        if (null != sasParams && sasParams.getSdkVersion() != null && "0".equalsIgnoreCase(sasParams.getSdkVersion())) {
            return InventoryType.BROWSER;
        }
        return InventoryType.APP;
    }

    public static Gender getGender(final SASRequestParameters sasParams) {
        if (sasParams == null) {
            return null;
        } else if ("m".equalsIgnoreCase(sasParams.getGender())) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }
}
