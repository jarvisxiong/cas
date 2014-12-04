package com.inmobi.adserve.channels.server.requesthandler;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.AuctionType;
import com.inmobi.adserve.adpool.Creative;
import com.inmobi.adserve.adpool.EncryptionKeys;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.ChannelServer;
import com.inmobi.adserve.channels.server.auction.AuctionEngine;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.casthrift.rrCasSpecificInfo.Csids;
import com.inmobi.commons.security.api.InmobiSession;
import com.inmobi.commons.security.impl.InmobiSecurityImpl;
import com.inmobi.commons.security.util.exception.InmobiSecureException;
import com.inmobi.commons.security.util.exception.InvalidMessageException;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;
import com.inmobi.types.PricingModel;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.Getter;
import static com.inmobi.casthrift.DemandSourceType.DCP;
import static com.inmobi.casthrift.DemandSourceType.IX;
import static com.inmobi.casthrift.DemandSourceType.RTBD;

public class ResponseSender extends HttpRequestHandlerBase {

    private static final int ENCRYPTED_SDK_BASE_VERSION = 430;

    private final static Logger LOG = LoggerFactory.getLogger(ResponseSender.class);

    private static final String START_TAG =
            "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
    private static final String END_TAG = " ]]></Ad></Ads></AdResponse>";
    private static final String AD_IMAI_START_TAG = "<!DOCTYPE html>";
    private static final String NO_AD_IMAI = "";
    private static final String NO_AD_XHTML = "<AdResponse><Ads></Ads></AdResponse>";
    private static final String NO_AD_HTML = "<!-- mKhoj: No advt for this position -->";
    private static final String NO_AD_JS_ADCODE = "<html><head><title></title><style type=\"text/css\">"
            + " body {margin: 0; overflow: hidden; background-color: transparent}"
            + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
            + "<script type=\"text/javascript\" charset=\"utf-8\">"
            + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
    private static Set<String> SUPPORTED_RESPONSE_FORMATS = Sets.newHashSet("html", "xhtml", "axml", "imai", "native");

    private static final int PRIVATE_AUCTION = 3;
    private static final int PREFERRED_DEAL = 4;

    public SASRequestParameters sasParams;
    public CasInternalRequestParameters casInternalRequestParameters;

    private long totalTime;
    private List<ChannelSegment> rankList;
    private ThirdPartyAdResponse adResponse;
    private boolean responseSent;
    private int rankIndexToProcess;
    private int selectedAdIndex;
    private boolean requestCleaned;
    private final AuctionEngine auctionEngine;
    private final Object lock = new Object();
    private String terminationReason;
    private final long initialTime;
    private Marker traceMarker;

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public List<ChannelSegment> getRankList() {
        return rankList;
    }

    public void setRankList(final List<ChannelSegment> rankList) {
        this.rankList = rankList;
    }

    private void setRankIndexToProcess(final int rankIndexToProcess) {
        this.rankIndexToProcess = rankIndexToProcess;
    }

    public ThirdPartyAdResponse getAdResponse() {
        return adResponse;
    }

    public ChannelSegment getRtbResponse() {
        return auctionEngine.getAuctionResponse();
    }

    public int getSelectedAdIndex() {
        return selectedAdIndex;
    }

    public long getTotalTime() {
        return totalTime;
    }
    
    public long getTimeElapsed(){
        return System.currentTimeMillis() - initialTime;
    }
    
    public String getDST(){
        if(sasParams.getDst() == 0){
            return "";
        }
        return DemandSourceType.findByValue(sasParams.getDst()).toString();
    }

    @Inject
    public ResponseSender(final Provider<Marker> traceMarkerProvider) {
        initialTime = System.currentTimeMillis();
        totalTime = 0;
        rankList = null;
        adResponse = null;
        responseSent = false;
        sasParams = null;
        rankIndexToProcess = 0;
        selectedAdIndex = 0;
        requestCleaned = false;
        auctionEngine = new AuctionEngine();
        if (null != traceMarkerProvider) {
            traceMarker = traceMarkerProvider.get();
        }
    }

    @Override
    public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel serverChannel) {

        /*
         * Updating rankList for the selected DSP ChannelSegment.
         * This is being done so that all the logging happens on the selected DSP parameters, not on the RP parameters.
         * NOTE: In case of No Ad, the logging will happen on RP parameters only.
         */
        if (selectedAdNetwork instanceof IXAdNetwork) {
            getAuctionEngine().setUnfilteredChannelSegmentList(Arrays.asList(getAuctionEngine().getAuctionResponse()));
        }

        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);
        sendAdResponse(adResponse, serverChannel, selectedAdNetwork.getSelectedSlotId());
    }

    // send Ad Response

    private void sendAdResponse(final ThirdPartyAdResponse adResponse, final Channel serverChannel, final Short selectedSlotId) {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }

        LOG.debug("ad received so trying to send ad response");
        String finalResponse = adResponse.getResponse();
        if (selectedSlotId != null && SlotSizeMapping.getDimension(selectedSlotId) != null) {
            LOG.debug("slot served is {}", selectedSlotId);


            if (getResponseFormat() == ResponseFormat.XHTML) {
                final Dimension dim = SlotSizeMapping.getDimension(selectedSlotId);
                final String startElement = String.format(START_TAG, (int) dim.getWidth(), (int) dim.getHeight());
                finalResponse = startElement + finalResponse + END_TAG;
            } else if (getResponseFormat() == ResponseFormat.IMAI) {
                finalResponse = AD_IMAI_START_TAG + finalResponse;
            }
        } else {
            LOG.info("invalid slot, so not returning response, even though we got an ad");
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_NO_FILLS);
            if (getResponseFormat() == ResponseFormat.XHTML) {
                finalResponse = NO_AD_XHTML;
            }
            sendResponse(HttpResponseStatus.OK, finalResponse, adResponse.getResponseHeaders(), serverChannel);
            return;
        }

        if (sasParams.getDst() == DCP.getValue()) {
            sendResponse(HttpResponseStatus.OK, finalResponse, adResponse.getResponseHeaders(), serverChannel);
            incrementStatsForFills(sasParams.getDst());
        } else {
            final String dstName = DemandSourceType.findByValue(sasParams.getDst()).toString();
            final AdPoolResponse rtbdOrIxResponse = createThriftResponse(adResponse.getResponse());
            LOG.debug("{} response json to RE is {}", dstName, rtbdOrIxResponse);
            if (null == rtbdOrIxResponse || !SUPPORTED_RESPONSE_FORMATS.contains(sasParams.getRFormat())) {
                sendNoAdResponse(serverChannel);
            } else {
                try {
                    final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                    final byte[] serializedResponse = serializer.serialize(rtbdOrIxResponse);
                    sendResponse(HttpResponseStatus.OK, serializedResponse, adResponse.getResponseHeaders(),
                            serverChannel);
                    incrementStatsForFills(sasParams.getDst());
                } catch (final TException e) {
                    LOG.error("Error in serializing the adPool response ", e);
                    sendNoAdResponse(serverChannel);
                }
            }
        }
    }

    private void incrementStatsForFills(final int dst) {
        if (dst == DemandSourceType.DCP.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.DCP_FILLS);
        } else if (dst == DemandSourceType.RTBD.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.RULE_ENGINE_FILLS);
        } else if (dst == DemandSourceType.IX.getValue()) {
            InspectorStats.incrementStatCount(InspectorStrings.IX_FILLS);
        }

        InspectorStats.incrementStatCount(InspectorStrings.TOTAL_FILLS);
    }

    private boolean checkResponseSent() {
        if (responseSent) {
            return true;
        }

        synchronized (lock) {
            if (!responseSent) {
                responseSent = true;
                return false;
            } else {
                return true;
            }
        }
    }

    protected AdPoolResponse createThriftResponse(final String finalResponse) {
        final AdPoolResponse adPoolResponse = new AdPoolResponse();
        final AdInfo rtbdAd = new AdInfo();
        final AdIdChain adIdChain = new AdIdChain();
        final Csids csids = new Csids();

        final ChannelSegmentEntity channelSegmentEntity = getRtbResponse().getChannelSegmentEntity();
        final ADCreativeType responseCreativeType = getRtbResponse().getAdNetworkInterface().getCreativeType();

        adIdChain.setAdgroup_guid(channelSegmentEntity.getAdgroupId());
        adIdChain.setAd_guid(channelSegmentEntity.getAdId(responseCreativeType));
        adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());
        adIdChain.setCampaign_guid(channelSegmentEntity.getCampaignId());
        adIdChain.setAd(channelSegmentEntity.getIncId(responseCreativeType));
        adIdChain.setGroup(channelSegmentEntity.getAdgroupIncId());
        adIdChain.setCampaign(channelSegmentEntity.getCampaignIncId());
        adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());

        // TODO: Create method and write UT
        switch (getRtbResponse().getAdNetworkInterface().getDst()) {
            case IX: // If IX,

                // Set IX specific parameters
                if (getRtbResponse().getAdNetworkInterface() instanceof IXAdNetwork) {
                    final IXAdNetwork ixAdNetwork = (IXAdNetwork) getRtbResponse().getAdNetworkInterface();
                    final String dealId = ixAdNetwork.returnDealId();
                    final long highestBid = (long) (ixAdNetwork.returnAdjustBid() * Math.pow(10, 6));
                    final int pmpTier = ixAdNetwork.returnPmpTier();

                    // Checking whether a dealId was provided in the bid response
                    if (null != dealId) {
                        // If dealId is present, then auction type is set to PREFERRED_DEAL
                        // and dealId is set
                        if (ixAdNetwork.isExternalDeal) {
                            csids.setMatchedCsids(ixAdNetwork.returnUsedCsids());
                            TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                            try {
                                adPoolResponse.setRequestPoolSpecificInfo(serializer.serialize(csids));
                            } catch (TException exc) {
                                LOG.error("Could not send csId to UMP, thrift exception {}", exc);
                            }
                        }
                        rtbdAd.setDealId(dealId);
                        rtbdAd.setHighestBid(highestBid);
                        if (PRIVATE_AUCTION == pmpTier) {
                            // If private marketplace tier is 3 then the deal is a private auction
                            rtbdAd.setAuctionType(AuctionType.PRIVATE_AUCTION);
                        } else if (PREFERRED_DEAL == pmpTier) {
                            // If private marketplace tier is 4 then the deal is a preferred deal
                            rtbdAd.setAuctionType(AuctionType.PREFERRED_DEAL);
                        } else {
                            // When pmpTier is 0 (default value), auction is an open auction
                            // Other values are reserved for future use
                            rtbdAd.setAuctionType(AuctionType.PREFERRED_DEAL);
                        }
                    } else {
                        // otherwise auction type is set to FIRST_PRICE
                        rtbdAd.setAuctionType(AuctionType.FIRST_PRICE);
                    }
                }
                break;

            default:// For RTBD/DCP, auction type is set to SECOND_PRICE
                rtbdAd.setAuctionType(AuctionType.SECOND_PRICE);
                break;
        }

        final List<AdIdChain> adIdChains = new ArrayList<AdIdChain>();
        adIdChains.add(adIdChain);
        rtbdAd.setAdIds(adIdChains);

        rtbdAd.setPricingModel(PricingModel.CPM);
        final long bid = (long) (getRtbResponse().getAdNetworkInterface().getBidPriceInUsd() * Math.pow(10, 6));
        rtbdAd.setPrice(bid);
        rtbdAd.setBid(bid);
        final UUID uuid = UUID.fromString(getRtbResponse().getAdNetworkInterface().getImpressionId());
        rtbdAd.setImpressionId(new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
        rtbdAd.setSlotServed(getRtbResponse().getAdNetworkInterface().getSelectedSlotId());
        final Creative rtbdCreative = new Creative();
        rtbdCreative.setValue(finalResponse);
        rtbdAd.setCreative(rtbdCreative);
        adPoolResponse.setAds(Arrays.asList(rtbdAd));
        adPoolResponse
                .setMinChargedValue((long) (getRtbResponse().getAdNetworkInterface().getSecondBidPriceInUsd() * Math
                        .pow(10, 6)));
        if (!"USD".equalsIgnoreCase(getRtbResponse().getAdNetworkInterface().getCurrency())) {
            rtbdAd.setOriginalCurrencyName(getRtbResponse().getAdNetworkInterface().getCurrency());
            rtbdAd.setBidInOriginalCurrency((long) (getRtbResponse().getAdNetworkInterface().getBidPriceInLocal() * Math
                    .pow(10, 6)));
        }
        return adPoolResponse;
    }

    // send response to the caller
    private void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders,
            final Channel serverChannel) {

        final byte[] bytes = responseString.getBytes(Charsets.UTF_8);
        sendResponse(status, bytes, responseHeaders, serverChannel);
    }

    // send response to the caller
    private void sendResponse(final HttpResponseStatus status, byte[] responseBytes, final Map responseHeaders,
            final Channel serverChannel) {
        LOG.debug("Inside send Response");
        responseBytes = encryptResponseIfRequired(responseBytes);

        final FullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.wrappedBuffer(responseBytes), false);
        if (null != responseHeaders) {
            for (final Map.Entry entry : (Set<Map.Entry>) responseHeaders.entrySet()) {
                response.headers().add(entry.getKey().toString(), responseHeaders.get(entry.getValue()));
            }
        }

        response.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        // TODO: to fix keep alive, we need to fix whole flow
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, responseBytes.length);
        response.headers().add(HttpHeaders.Names.EXPIRES, "-1");
        response.headers().add(HttpHeaders.Names.PRAGMA, "no-cache");
        HttpHeaders.setKeepAlive(response, sasParams.isKeepAlive());
        System.getProperties().setProperty("http.keepAlive", String.valueOf(sasParams.isKeepAlive()));
        
        InspectorStats.updateYammerTimerStats("netty", InspectorStrings.LATENCY_FOR_MEASURING_AT_POINT_ + "sendResponse_"+getDST(), getTimeElapsed());
       
        if(serverChannel.isOpen()){
            if (sasParams.isKeepAlive()) {
                serverChannel.writeAndFlush(response);
            } else {
                serverChannel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
        }

        totalTime = System.currentTimeMillis() - initialTime;
        LOG.debug("successfully sent response");

        // dst != 0 is true only for servlets rtbdFill, backFill, ixFill
        if (null != sasParams && 0 != sasParams.getDst()) {
            cleanUp();
            LOG.debug("successfully called cleanUp()");
        }
    }

    /**
     * @param responseBytes
     * @return
     */
    private byte[] encryptResponseIfRequired(byte[] responseBytes) {
        if (sasParams.getSdkVersion() != null
                && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= ENCRYPTED_SDK_BASE_VERSION
                && sasParams.getDst() == 2) {
            LOG.debug("Encrypting the response as request is from SDK: {}", sasParams.getSdkVersion());
            final EncryptionKeys encryptionKey = sasParams.getEncryptionKey();
            final InmobiSession inmobiSession = new InmobiSecurityImpl(null).newSession(null);
            try {
                responseBytes =
                        inmobiSession.write(responseBytes, encryptionKey.getAesKey(),
                                encryptionKey.getInitializationVector());
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Encyption Details:  EncryptionKey: {}  IVBytes: {}  Response: {}", new String(
                            encryptionKey.getAesKey(), CharsetUtil.UTF_8),
                            new String(encryptionKey.getInitializationVector(), CharsetUtil.UTF_8), new String(
                                    responseBytes, CharsetUtil.UTF_8));
                }
            } catch (InmobiSecureException | InvalidMessageException e) {
                LOG.info("Exception while encrypting response from {}", e);
                throw new RuntimeException(e);
            }
        }
        return responseBytes;
    }

    // send response to the caller
    public void sendResponse(final String responseString, final Channel serverChannel) {
        sendResponse(HttpResponseStatus.OK, responseString, null, serverChannel);
    }

    @Override
    public AuctionEngine getAuctionEngine() {
        return auctionEngine;
    }

    @Override
    public void sendNoAdResponse(final Channel serverChannel) {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }
        LOG.debug("Sending No ads");
        responseSent = true;
        // dst != 0 is true only for servlets rtbdFill, backFill, ixFill
        // This check has been added to prevent totalNoFills from being updated, when any servlet other than the ones
        // mentioned above throws an exception in HttpRequestHandler.
        if (null != sasParams && 0 != sasParams.getDst()) {
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_NO_FILLS);
        }

        HttpResponseStatus httpResponseStatus;
        String defaultContent;
        switch (getResponseFormat()) {
            case IMAI:
                // status code 204 whenever format=imai
                httpResponseStatus = HttpResponseStatus.NO_CONTENT;
                defaultContent = NO_AD_IMAI;
                break;
            case NATIVE:
                // status code 200 and empty ad content( i.e. ads:[]) for format =
                // native
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = "";
                break;
            case XHTML:
                // status code 200 & empty ad content (i.e. adUnit missing) for
                // format=xml
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = NO_AD_XHTML;
                break;
            case HTML:
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = NO_AD_HTML;
                break;
            case JS_AD_CODE:
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = String.format(NO_AD_JS_ADCODE, sasParams.getRqIframe());
                break;
            default:
                httpResponseStatus = HttpResponseStatus.OK;
                defaultContent = NO_AD_HTML;
                break;
        }

        sendResponse(getResponseStatus(sasParams.getDst(), httpResponseStatus),
                getResponseBytes(sasParams.getDst(), defaultContent), new HashMap<String, String>(), serverChannel);
    }

    private HttpResponseStatus getResponseStatus(final int dstType, final HttpResponseStatus httpResponseStatus) {
        if (dstType == RTBD.getValue() || dstType == IX.getValue()) {
            return HttpResponseStatus.OK;
        }
        return httpResponseStatus;
    }

    private byte[] getResponseBytes(final int dstType, final String defaultResponse) {
        if (dstType == RTBD.getValue() || dstType == IX.getValue()) {
            final AdPoolResponse rtbdResponse = new AdPoolResponse();
            try {
                final TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                return serializer.serialize(rtbdResponse);
            } catch (final TException e) {
                LOG.error("Error in serializing the adPool response ", e);
                return "".getBytes(Charsets.UTF_8);
            }
        }
        return defaultResponse.getBytes(Charsets.UTF_8);
    }

    // Return true if request contains Iframe Id and is a request from js
    // adcode.
    private boolean isJsAdRequest() {
        if (null == sasParams) {
            return false;
        }
        final String adCode = sasParams.getAdcode();
        final String rqIframe = sasParams.getRqIframe();
        return adCode != null && rqIframe != null && "JS".equalsIgnoreCase(adCode);
    }

    @Override
    public Boolean isEligibleForProcess(final AdNetworkInterface adNetwork) {
        if (null == rankList) {
            return false;
        }
        final int index = getRankIndex(adNetwork);
        LOG.debug("inside isEligibleForProcess for {} and index is {}", adNetwork.getName(), index);
        return index == 0 || index == rankIndexToProcess;
    }

    @Override
    public Boolean isLastEntry(final AdNetworkInterface adNetwork) {
        final int index = getRankIndex(adNetwork);
        LOG.debug("inside isLastEntry for {} and index is {}", adNetwork.getName(), index);
        return index == rankList.size() - 1;
    }

    // Iterates over the complete rank list and set the new value for
    // rankIndexToProcess.
    @Override
    public void reassignRanks(final AdNetworkInterface adNetworkCaller, final Channel serverChannel) {
        int index = getRankIndex(adNetworkCaller);
        LOG.debug("reassignRanks called for {} and index is {}", adNetworkCaller.getName(), index);

        while (index < rankList.size()) {
            final ChannelSegment channelSegment = rankList.get(index);
            final AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();

            LOG.debug("reassignRanks iterating for {} and index is {}", adNetwork.getName(), index);

            if (adNetwork.isRequestCompleted()) {
                if (adNetwork.getResponseAd().getResponseStatus() == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
                    // Sends the response if request is completed for the
                    // specific adapter.
                    sendAdResponse(adNetwork, serverChannel);
                    break;
                } else {
                    // Iterates to the next adapter.
                    index++;
                }
            } else {
                // Updates the value of rankIndexToProcess which is the next
                // index to be processed.
                rankIndexToProcess = index;
                break;
            }
        }
        // Sends no ad if reached to the end of the rank list.
        if (index == rankList.size()) {
            sendNoAdResponse(serverChannel);
        }
    }

    @Override
    public void cleanUp() {
        // Making sure cleanup is called only once
        if (requestCleaned) {
            return;
        }
        requestCleaned = true;
        LOG.debug("trying to close open channels");

        // closing unclosed dcp channels
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rankList.get(index).getAdNetworkInterface());
            try {
                rankList.get(index).getAdNetworkInterface().cleanUp();
            } catch (final Exception exception) {
                LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index, rankList.get(index)
                        .getAdNetworkInterface(), exception);
            }
        }

        // Closing RTB channels
        final List<ChannelSegment> rtbList = auctionEngine.getUnfilteredChannelSegmentList();

        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            } catch (final Exception exception) {
                LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index, rtbList.get(index)
                        .getAdNetworkInterface(), exception);
            }
        }

        LOG.debug("done with closing channels");
        writeLogs();
    }

    public void writeLogs() {
        if (null == sasParams) {
            InspectorStats.incrementStatCount(InspectorStrings.NON_AD_REQUESTS);
            LOG.debug("Not logging anything, either sasParam is null or this is not an ad request");
            LOG.debug("done with logging");
            return;
        }
        final List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        if (null != getRankList()) {
            list.addAll(getRankList());
        }
        if (null != getAuctionEngine().getUnfilteredChannelSegmentList()) {
            list.addAll(getAuctionEngine().getUnfilteredChannelSegmentList());
        }
        InspectorStats.updateYammerTimerStats("netty", InspectorStrings.LATENCY_FOR_MEASURING_AT_POINT_ + "writeLogs_"+getDST(), getTimeElapsed());
        long totalTime = getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != getRtbResponse()) {
                adResponseChannelSegment = getRtbResponse();
            } else if (null != getAdResponse()) {
                adResponseChannelSegment = getRankList().get(getSelectedAdIndex());
            }
            Logging.rrLogging(traceMarker, adResponseChannelSegment, list, sasParams, terminationReason, totalTime);
            Logging.advertiserLogging(list, CasConfigUtil.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, CasConfigUtil.getLoggerConfig());
            Logging.creativeLogging(list, sasParams);
        } catch (final JSONException exception) {
            LOG.debug("Exception raised in ResponseSender {}", exception);
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        } catch (final TException exception) {
            LOG.debug("Exception raised in ResponseSender {}", exception);
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        LOG.debug("done with logging");
    }

    private int getRankIndex(final AdNetworkInterface adNetwork) {
        int index;
        for (index = 0; index < rankList.size(); index++) {
            if (rankList.get(index).getAdNetworkInterface().getImpressionId().equals(adNetwork.getImpressionId())) {
                break;
            }
        }
        return index;
    }

    // return the response format
    public ResponseFormat getResponseFormat() {
        if (sasParams == null) {
            return ResponseFormat.HTML;
        }

        final String responseFormat = sasParams.getRFormat();
        if (isJsAdRequest()) {
            return ResponseFormat.JS_AD_CODE;
        } else if (null == responseFormat) {
            return ResponseFormat.HTML;
        } else if ("axml".equalsIgnoreCase(responseFormat)) {
            return ResponseFormat.XHTML;
        } else if ("native".equalsIgnoreCase(responseFormat)) {
            return ResponseFormat.NATIVE;
        }
        return ResponseFormat.getValue(responseFormat);
    }

    @Override
    public void processDcpList(final Channel channel) {
        // There would always be rtb partner before going to dcp list
        // So will iterate over the dcp list once.
        if (getRankList().isEmpty()) {
            LOG.debug("dcp list is empty so sending NoAd");
            sendNoAdResponse(channel);
            return;
        }
        int rankIndex = rankIndexToProcess;
        if (rankList.size() <= rankIndex) {
            return;
        }
        ChannelSegment segment = getRankList().get(rankIndex);
        while (segment.getAdNetworkInterface().isRequestCompleted()) {
            if (segment.getAdNetworkInterface().getResponseAd().getResponseStatus() == ResponseStatus.SUCCESS) {
                this.sendAdResponse(segment.getAdNetworkInterface(), channel);
                break;
            }
            rankIndex++;
            if (rankIndex >= getRankList().size()) {
                sendNoAdResponse(channel);
                break;
            }
            segment = getRankList().get(rankIndex);
        }
        setRankIndexToProcess(rankIndex);
    }

    @Override
    public void processDcpPartner(final Channel channel, final AdNetworkInterface adNetworkInterface) {
        if (!isEligibleForProcess(adNetworkInterface)) {
            LOG.debug("{} is not eligible for processing", adNetworkInterface.getName());
            return;
        }
        LOG.debug("the channel is eligible for processing");
        if (adNetworkInterface.getResponseAd().getResponseStatus() == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
            sendAdResponse(adNetworkInterface, channel);
            cleanUp();
        } else if (isLastEntry(adNetworkInterface)) {
            sendNoAdResponse(channel);
            cleanUp();
        } else {
            reassignRanks(adNetworkInterface, channel);
        }
    }

    public enum ResponseFormat {
        XHTML("axml", "xhtml"), HTML("html"), IMAI("imai"), NATIVE("native"), JS_AD_CODE("jsAdCode");

        private String[] formats;

        private static final Map<String, ResponseFormat> STRING_TO_FORMAT_MAP = Maps.newHashMap();

        static {
            for (final ResponseFormat responseFormat : ResponseFormat.values()) {
                for (final String format : responseFormat.formats) {
                    STRING_TO_FORMAT_MAP.put(format, responseFormat);
                }
            }
        }

        private ResponseFormat(final String... formats) {
            this.formats = formats;
        }

        public static ResponseFormat getValue(final String format) {
            return STRING_TO_FORMAT_MAP.get(format.toLowerCase());
        }

    }

}
