package com.inmobi.adserve.channels.server.requesthandler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.inmobi.types.DemandSource;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.MetricsManager;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;
import com.inmobi.adtemplate.platform.CreativeType;
import com.inmobi.casthrift.ix.*;
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


public class Logging {
    private static final Logger                            LOG                     = LoggerFactory
                                                                                           .getLogger(Logging.class);

    private static AbstractMessagePublisher                dataBusPublisher;
    private static String                                  rrLogKey;
    private static String                                  sampledAdvertisementLogKey;
    private static String                                  umpAdsLogKey;
    private static boolean                                 enableFileLogging;
    private static boolean                                 enableDatabusLogging;
    public final static ConcurrentHashMap<String, String>  sampledAdvertiserLogNos = new ConcurrentHashMap<String, String>(
                                                                                           2000);
    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String>                     advertiserIdNameMap;

    public static ConcurrentHashMap<String, String> getSampledadvertiserlognos() {
        return sampledAdvertiserLogNos;
    }

    private static int totalCount;

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
    public static void rrLogging(Marker traceMarker, final ChannelSegment channelSegment, final List<ChannelSegment> rankList,
            final SASRequestParameters sasParams, String terminationReason, final long totalTime) throws JSONException,
            TException {
        InspectorStats.incrementStatCount(InspectorStrings.latency, totalTime);

        if (null != sasParams) {
            DemandSourceType dst = getDst(sasParams.getDst());
            InspectorStats.incrementStatCount(dst + "-" +InspectorStrings.latency, totalTime);
            if (null != sasParams.getAllParametersJson() && (rankList == null || rankList.isEmpty())) {
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.nomatchsegmentcount);
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.nomatchsegmentlatency, totalTime);
                InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentcount);
                InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentlatency, totalTime);
            }
            if (null != sasParams.getRFormat() && sasParams.getRFormat().equalsIgnoreCase("native")) {
                InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NATIVE_REQUESTS);
                if(rankList == null || rankList.isEmpty()){
                	InspectorStats.incrementStatCount(dst + "-" + InspectorStrings.NATIVE_REQUESTS + "-" + InspectorStrings.nomatchsegmentcount);
                }
            }
        }



        boolean isTerminated = false;
        if (null != terminationReason) {
            isTerminated = true;
        }
        else {
            terminationReason = "NO";
        }
        short adsServed = 0;
        String host;
        try {
            InetAddress addr = InetAddress.getLocalHost();
            host = addr.getHostName();
            if (host == null) {
                LOG.info("host cant be empty, abandoning rr logging");
                return;
            }
        }
        catch (UnknownHostException ex) {
            LOG.info("could not resolve host inside rr logging, so abandoning response");
            return;
        }
        InventoryType inventory = getInventoryType(sasParams);
        String timestamp = new Date().toString();
        AdIdChain adChain;
        AdMeta adMeta;
        Ad ad;
        Impression impression = null;
        boolean isServerImpression = false;
        String advertiserId = null;
        if (channelSegment != null) {
            InspectorStats.incrementStatCount(channelSegment.getAdNetworkInterface().getName(),
                    InspectorStrings.serverImpression);
            isServerImpression = true;
            advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
            adsServed = 1;
            ChannelSegmentEntity channelSegmentEntity = channelSegment.getChannelSegmentEntity();
            adChain = new AdIdChain(channelSegmentEntity.getAdId(channelSegment.getAdNetworkInterface().getCreativeType()),
                    channelSegmentEntity.getAdgroupId(), channelSegmentEntity.getCampaignId(),
                    channelSegmentEntity.getAdvertiserId(), channelSegmentEntity.getExternalSiteKey());
            ContentRating contentRating = getContentRating(sasParams);
            PricingModel pricingModel = getPricingModel(channelSegmentEntity.getPricingModel());
            adMeta = new AdMeta(contentRating, pricingModel, "BANNER"); // TODO: Check "BANNER" point
            ad = new Ad(adChain, adMeta);
            impression = new Impression(channelSegment.getAdNetworkInterface().getImpressionId(), ad);
            impression.setAdChain(createCasAdChain(channelSegment));
            double winBid = channelSegment.getAdNetworkInterface().getSecondBidPriceInUsd();
            if (winBid != -1) {
                ad.setWinBid(winBid);
            }
        }
        Short requestSlot = null;
        Short slotServed = null;
        Long handsetInternalId = null;
        Long countryId = null;
        Integer carrierId = null;
        Integer state = null;
        Integer city = null;
        if (null != sasParams) {
            handsetInternalId = sasParams.getHandsetInternalId();
            slotServed = sasParams.getSlot();
            countryId = sasParams.getCountryId();
            carrierId = sasParams.getCarrierId();
            if (null != sasParams.getRqMkSlot() && sasParams.getRqMkSlot().size() > 0) {
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

        HandsetMeta handsetMeta = new HandsetMeta();
        if (null != handsetInternalId) {
            handsetMeta.setId(handsetInternalId.intValue());
        }

        if (null != sasParams && sasParams.getOsId() != 0) {
            handsetMeta.setOsId(sasParams.getOsId());
        }

        User user = new User();
        if (null != sasParams) {
            if (null != sasParams.getAge()) {
                user.setAge(sasParams.getAge());
            }
            if (null != sasParams.getGender()) {
                user.setGender(getGender(sasParams));
            }
            user.setUids(sasParams.getTUidParams());
        }
        short adRequested = 1;
        Request request = new Request(adRequested, adsServed, sasParams == null ? null : sasParams.getSiteId(),
                sasParams == null ? null : sasParams.getTid());
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
            request.setRequestDst(getDst(sasParams.getDst()));
        }

        List<Impression> impressions = null;
        if (null != impression) {
            impressions = new ArrayList<Impression>();
            impressions.add(impression);
        }
        AdRR adRR = new AdRR(host, timestamp, request, impressions, isTerminated, terminationReason);
        adRR.setTime_stamp(new Date().getTime());
        List<Channel> channels = createChannelsLog(rankList);
        adRR.setChannels(channels);
        if (enableDatabusLogging) {
            TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            Message msg = new Message(tSerializer.serialize(adRR));
            dataBusPublisher.publish(rrLogKey, msg);
            LOG.debug(traceMarker, "ADRR is : {}", adRR);
        }
        // Logging real time stats for graphite
        if (null != sasParams) {
            DemandSourceType dst = getDst(sasParams.getDst());
            MetricsManager.updateLatency(dst.name(), totalTime);
            
            if(rankList != null && rankList.size() > 0){
            	MetricsManager.updateMatchSegmentLatency(dst.name(), totalTime);
            }
        }
    }
    
    

    // Writing creatives
    public static void creativeLogging(final List<ChannelSegment> channelSegments, final SASRequestParameters sasRequestParameters) {
        LOG.debug("inside creativeLogging");
        if (null == channelSegments || channelSegments.isEmpty()) {
            return;
        }

        for (ChannelSegment channelSegment : channelSegments) {
            AdNetworkInterface adNetworkInterface = channelSegment.getAdNetworkInterface();
            if (adNetworkInterface.isRtbPartner() && adNetworkInterface.isLogCreative()) {
                String response = adNetworkInterface.getHttpResponseContent();
                if(adNetworkInterface.getCreativeType() == ADCreativeType.NATIVE){
                	response = adNetworkInterface.getAdMarkUp();
                }
                String requestUrl = adNetworkInterface.getRequestUrl();
                ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
                String partnerName = adNetworkInterface.getName();
                String externalSiteKey = channelSegment.getChannelSegmentEntity().getExternalSiteKey();
                String advertiserId = channelSegment.getChannelSegmentEntity().getAdvertiserId();
                String adStatus = adResponse.adStatus;

                CasAdvertisementLog creativeLog = new CasAdvertisementLog(partnerName, requestUrl, response,
                        adStatus, externalSiteKey, advertiserId);
                creativeLog.setCountryId(sasRequestParameters.getCountryId().intValue());
                if(adNetworkInterface.getDst() == DemandSourceType.RTBD.getValue()) {
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
                    TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
                    msg = new Message(tSerializer.serialize(creativeLog));
                }
                catch (TException e) {
                    LOG.debug("Error while creating creative logs for databus ");
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
        List<Channel> channels = new ArrayList<Channel>();
        for (ChannelSegment channelSegment : rankList) {
            Channel channel = new Channel();
            channel.setAdStatus(getAdStatus(channelSegment.getAdNetworkInterface().getAdStatus()));
            channel.setLatency(channelSegment.getAdNetworkInterface().getLatency());
            channel.setAdChain(createCasAdChain(channelSegment));
            double bid = channelSegment.getAdNetworkInterface().getBidPriceInUsd();
            if (bid > 0) {
                channel.setBid(bid);
            }
            channels.add(channel);
            // Incrementing inspectors
            AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetwork.getResponseStruct();
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalRequests);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.latency, adResponse.latency);
            InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.connectionLatency,
                    adNetwork.getConnectionLatency());
            switch (adResponse.adStatus) {
                case "AD":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalFills);
                    break;
                case "NO_AD":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalNoFills);
                    break;
                case "TIME_OUT":
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTimeout);
                    break;
                default:
                    InspectorStats.incrementStatCount(adNetwork.getName(), InspectorStrings.totalTerminate);
                    break;
            }
        }
        return channels;
    }

    public static CasAdChain createCasAdChain(final ChannelSegment channelSegment) {
        CasAdChain casAdChain = new CasAdChain();
        casAdChain.setAdvertiserId(channelSegment.getChannelSegmentEntity().getAdvertiserId());
        casAdChain.setCampaign_inc_id(channelSegment.getChannelSegmentEntity().getCampaignIncId());
        casAdChain.setAdgroup_inc_id(channelSegment.getChannelSegmentEntity().getAdgroupIncId());
        casAdChain.setExternalSiteKey(channelSegment.getChannelSegmentEntity().getExternalSiteKey());
        casAdChain.setDst(getDst(channelSegment.getChannelSegmentEntity().getDst()));
        if (null != channelSegment.getAdNetworkInterface().getCreativeId()) {
            casAdChain.setCreativeId(channelSegment.getAdNetworkInterface().getCreativeId());
        }
        return casAdChain;
    }

    private static DemandSourceType getDst(final int dst) {
        switch (dst) {
            case 2:
                return DemandSourceType.DCP;
            case 6:
                return DemandSourceType.RTBD;
            case 8:
                return DemandSourceType.IX;
            default:
                return DemandSourceType.DCP;
        }
    }

    public static AdStatus getAdStatus(final String adStatus) {
        if ("AD".equalsIgnoreCase(adStatus)) {
            return AdStatus.AD;
        }
        else if ("NO_AD".equals(adStatus)) {
            return AdStatus.NO_AD;
        }
        else if ("TIME_OUT".equals(adStatus)) {
            return AdStatus.TIME_OUT;
        }
        return AdStatus.DROPPED;
    }

    public static void advertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        LOG.debug("came inside advertiser log");
        Logger advertiserLogger = LoggerFactory.getLogger(config.getString("advertiser"));
        if (!advertiserLogger.isDebugEnabled()) {
            return;
        }
        char sep = 0x01;
        StringBuilder log = new StringBuilder();
        LOG.debug("got logger handle for advertiser logs");
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            AdNetworkInterface adNetworkInterface = rankList.get(index).getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            String partnerName = adNetworkInterface.getName();
            log.append(partnerName);
            log.append(sep).append(adResponse.adStatus);
            String response = "";
            String requestUrl = "";
            if (adResponse.adStatus.equalsIgnoreCase("AD")) {
                response = adNetworkInterface.getHttpResponseContent();
                log.append(sep).append(response);
            }
            if (!adNetworkInterface.getRequestUrl().equals("")) {
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
     *
     * @param rankList
     * @param config
     */
    public static void sampledAdvertiserLogging(final List<ChannelSegment> rankList, final Configuration config) {
        LOG.debug("came inside sampledAdvertiser log");
        Logger sampledAdvertiserLogger = LoggerFactory.getLogger(config.getString("sampledadvertiser"));
        if (!sampledAdvertiserLogger.isDebugEnabled()) {
            return;
        }
        char sep = 0x01;
        StringBuilder log = new StringBuilder();
        LOG.debug("got logger handle for sampledAdvertiser logs");

        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            AdNetworkInterface adNetworkInterface = rankList.get(index).getAdNetworkInterface();
            ThirdPartyAdResponse adResponse = adNetworkInterface.getResponseStruct();
            String adStatus = adResponse.adStatus;
            String partnerName = adNetworkInterface.getName();
            String externalSiteKey = rankList.get(index).getChannelSegmentEntity().getExternalSiteKey();
            String advertiserId = rankList.get(index).getChannelSegmentEntity().getAdvertiserId();
            String requestUrl = adNetworkInterface.getRequestUrl();
            String response = adNetworkInterface.getHttpResponseContent();
            if(adNetworkInterface.getCreativeType() == ADCreativeType.NATIVE){
            	response = adNetworkInterface.getAdMarkUp();
            }
            
            if (!adStatus.equalsIgnoreCase("AD") || requestUrl.equals("") || response.equals("")) {
                continue;
            }

            if (enableDatabusLogging && decideToLog(partnerName, externalSiteKey)) {
                //Actual Logging to stream
                CasAdvertisementLog casAdvertisementLog = new CasAdvertisementLog(partnerName, requestUrl, response, adStatus, externalSiteKey, advertiserId);
                casAdvertisementLog.setCreativeType(adNetworkInterface.getCreativeType());
                sendToDatabus(casAdvertisementLog, sampledAdvertisementLogKey);
            }

            //File Logging
            if (index > 0 && partnerName.length() > 0 && log.length() > 0) {
                log.append("\n");
            }
            log.append(partnerName).append(sep)
                    .append(rankList.get(index).getChannelSegmentEntity().getExternalSiteKey());
            log.append(sep).append(requestUrl).append(sep).append(adStatus);
            log.append(sep).append(response).append(sep).append(advertiserId);
        }

        //Actual File Logging
        if (enableFileLogging && log.length() > 0) {
            sampledAdvertiserLogger.debug(log.toString());
            LOG.debug("done with sampledAdvertiser logging");
        }
    }

    /**
     *
     * @param casAdvertisementLog
     */
    private static void sendToDatabus(CasAdvertisementLog casAdvertisementLog, String sampledAdvertisementLogKey) {
        Message msg = null;
        try {
            TSerializer tSerializer = new TSerializer(new TBinaryProtocol.Factory());
            msg = new Message(tSerializer.serialize(casAdvertisementLog));
        }
        catch (TException e) {
            LOG.debug("Error while creating sampledAdvertiser logs for databus ");
        }
        if (null != msg) {
            dataBusPublisher.publish(sampledAdvertisementLogKey, msg);
            LOG.debug("sampledAdvertiser log pushed to stream");
        }
    }

    /**
     *
     * @param partnerName
     * @param externalSiteId
     * @return true if logging required otherwise false
     */
    private static boolean decideToLog(String partnerName, String externalSiteId) {
        long currentTime = System.currentTimeMillis();
        if (null == sampledAdvertiserLogNos.get(partnerName + externalSiteId)) {
            sampledAdvertiserLogNos.put(partnerName + externalSiteId, currentTime + "_" + 0);
        }
        Long time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + externalSiteId).split("_")[0]);
        if (currentTime - time >= 3600000) {
            sampledAdvertiserLogNos.put(partnerName + externalSiteId, currentTime + "_" + 0);
            time = Long.parseLong(sampledAdvertiserLogNos.get(partnerName + externalSiteId).split("_")[0]);
        }
        Integer count = Integer.parseInt(sampledAdvertiserLogNos.get(partnerName + externalSiteId).split("_")[1]);
        if (count >= totalCount) {
            return false;
        }
        count++;
        sampledAdvertiserLogNos.put(partnerName + externalSiteId, time + "_" + count);
        return true;
    }

    public static ContentRating getContentRating(final SASRequestParameters sasParams) {
        if (sasParams == null || null == sasParams.getSiteType()) {
            return null;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("performance")) {
            return ContentRating.PERFORMANCE;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("FAMILY_SAFE")) {
            return ContentRating.FAMILY_SAFE;
        }
        else if (sasParams.getSiteType().equalsIgnoreCase("MATURE")) {
            return ContentRating.MATURE;
        }
        return null;
    }

    public static PricingModel getPricingModel(final String pricingModel) {
        if (pricingModel == null) {
            return null;
        }
        else if (pricingModel.equalsIgnoreCase("cpc")) {
            return PricingModel.CPC;
        }
        else if (pricingModel.equalsIgnoreCase("cpm")) {
            return PricingModel.CPM;
        }
        return null;
    }

    public static InventoryType getInventoryType(final SASRequestParameters sasParams) {
        if (null != sasParams && sasParams.getSdkVersion() != null && sasParams.getSdkVersion().equalsIgnoreCase("0")) {
            return InventoryType.BROWSER;
        }
        return InventoryType.APP;
    }

    public static Gender getGender(final SASRequestParameters sasParams) {
        if (sasParams == null) {
            return null;
        }
        else if (sasParams.getGender().equalsIgnoreCase("m")) {
            return Gender.MALE;
        }
        return Gender.FEMALE;
    }
}
