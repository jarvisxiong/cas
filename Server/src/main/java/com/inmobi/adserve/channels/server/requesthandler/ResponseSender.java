package com.inmobi.adserve.channels.server.requesthandler;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.AuctionType;
import com.inmobi.adserve.adpool.Creative;
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
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;
import com.inmobi.types.PricingModel;


public class ResponseSender extends HttpRequestHandlerBase {

    private final static Logger         LOG                        = LoggerFactory.getLogger(ResponseSender.class);

    private static final String         START_TAG                  = "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
    private static final String         END_TAG                    = " ]]></Ad></Ads></AdResponse>";
    private static final String         AD_IMAI_START_TAG          = "<!DOCTYPE html>";
    private static final String         NO_AD_IMAI                 = "";
    private static final String         NO_AD_XHTML                = "<AdResponse><Ads></Ads></AdResponse>";
    private static final String         NO_AD_HTML                 = "<!-- mKhoj: No advt for this position -->";
    private static final String         NO_AD_JS_ADCODE            = "<html><head><title></title><style type=\"text/css\">"
                                                                           + " body {margin: 0; overflow: hidden; background-color: transparent}"
                                                                           + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
                                                                           + "<script type=\"text/javascript\" charset=\"utf-8\">"
                                                                           + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
    private static Set<String>          SUPPORTED_RESPONSE_FORMATS = Sets.newHashSet("html", "xhtml", "axml", "imai");

    private long                        totalTime;
    private List<ChannelSegment>        rankList;
    private ThirdPartyAdResponse        adResponse;
    private boolean                     responseSent;
    public SASRequestParameters         sasParams;
    private int                         rankIndexToProcess;
    private int                         selectedAdIndex;
    private boolean                     requestCleaned;
    public CasInternalRequestParameters casInternalRequestParameters;
    private final AuctionEngine         auctionEngine;
    private final Object                lock                       = new Object();
    private String                      terminationReason;

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public List<ChannelSegment> getRankList() {
        return this.rankList;
    }

    public void setRankList(final List<ChannelSegment> rankList) {
        this.rankList = rankList;
    }

    private void setRankIndexToProcess(final int rankIndexToProcess) {
        this.rankIndexToProcess = rankIndexToProcess;
    }

    public ThirdPartyAdResponse getAdResponse() {
        return this.adResponse;
    }

    public ChannelSegment getRtbResponse() {
        return auctionEngine.getRtbResponse();
    }

    public int getSelectedAdIndex() {
        return this.selectedAdIndex;
    }

    public long getTotalTime() {
        return this.totalTime;
    }

    @Inject
    public ResponseSender() {
        this.totalTime = System.currentTimeMillis();
        this.rankList = null;
        this.adResponse = null;
        this.responseSent = false;
        this.sasParams = null;
        this.rankIndexToProcess = 0;
        this.selectedAdIndex = 0;
        this.requestCleaned = false;
        this.auctionEngine = new AuctionEngine();
    }

    @Override
    public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final Channel serverChannel) {
        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);
        sendAdResponse(adResponse, serverChannel);
    }

    // send Ad Response
    private void sendAdResponse(final ThirdPartyAdResponse adResponse, final Channel serverChannel)
            throws NullPointerException {

        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }

        LOG.debug("ad received so trying to send ad response");
        String finalReponse = adResponse.response;
        if (sasParams.getSlot() != null && SlotSizeMapping.getDimension(Long.valueOf(sasParams.getSlot())) != null) {
            LOG.debug("slot served is {}", sasParams.getSlot());
            InspectorStats.incrementStatCount(InspectorStrings.totalFills);
            if (getResponseFormat().equals("xhtml")) {
                Dimension dim = SlotSizeMapping.getDimension(Long.valueOf(sasParams.getSlot()));
                String startElement = String.format(START_TAG, (int) dim.getWidth(), (int) dim.getHeight());
                finalReponse = startElement + finalReponse + END_TAG;
            }
            else if (getResponseFormat().equalsIgnoreCase("imai")) {
                finalReponse = AD_IMAI_START_TAG + finalReponse;
            }
        }
        else {
            LOG.info("invalid slot, so not returning response, even though we got an ad");
            InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);
            if (getResponseFormat().equals("xhtml")) {
                finalReponse = NO_AD_XHTML;
            }
            sendResponse(HttpResponseStatus.OK, finalReponse, adResponse.responseHeaders, serverChannel);
            return;
        }
        if (6 != sasParams.getDst()) {
            sendResponse(HttpResponseStatus.OK, finalReponse, adResponse.responseHeaders, serverChannel);
        }
        else {
            AdPoolResponse rtbdResponse = createThriftResponse(adResponse.response);
            LOG.debug("RTB response json to RE is {}", rtbdResponse);
            if (null == rtbdResponse || !SUPPORTED_RESPONSE_FORMATS.contains(sasParams.getRFormat())) {
                sendNoAdResponse(serverChannel);
            }
            else {
                try {
                    TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                    byte[] serializedResponse = serializer.serialize(rtbdResponse);
                    sendResponse(HttpResponseStatus.OK, serializedResponse, adResponse.responseHeaders, serverChannel);
                    InspectorStats.incrementStatCount(InspectorStrings.ruleEngineFills);
                    InspectorStats.incrementStatCount(InspectorStrings.totalFills);
                }
                catch (TException e) {
                    LOG.error("Error in serializing the adPool response ", e);
                    sendNoAdResponse(serverChannel);
                }
            }
        }
    }

    private boolean checkResponseSent() {
        if (responseSent) {
            return true;
        }

        synchronized (lock) {
            if (!responseSent) {
                responseSent = true;
                return false;
            }
            else {
                return true;
            }
        }
    }

    private AdPoolResponse createThriftResponse(final String finalResponse) {
        AdPoolResponse adPoolResponse = new AdPoolResponse();
        AdInfo rtbdAd = new AdInfo();
        AdIdChain adIdChain = new AdIdChain();
        ChannelSegmentEntity channelSegmentEntity = this.auctionEngine.getRtbResponse().getChannelSegmentEntity();
        adIdChain.setAdgroup_guid(channelSegmentEntity.getAdgroupId());
        adIdChain.setAd_guid(channelSegmentEntity.getAdId());
        adIdChain.setAdvertiser_guid(channelSegmentEntity.getAdvertiserId());
        adIdChain.setCampaign_guid(channelSegmentEntity.getCampaignId());
        adIdChain.setAd(channelSegmentEntity.getIncId());
        adIdChain.setGroup(channelSegmentEntity.getAdgroupIncId());
        adIdChain.setCampaign(channelSegmentEntity.getCampaignIncId());
        List<AdIdChain> adIdChains = new ArrayList<AdIdChain>();
        adIdChains.add(adIdChain);
        rtbdAd.setAdIds(adIdChains);
        rtbdAd.setAuctionType(AuctionType.SECOND_PRICE);
        rtbdAd.setPricingModel(PricingModel.CPM);
        long bid = (long) (this.auctionEngine.getRtbResponse().getAdNetworkInterface().getBidPriceInUsd() * Math.pow(
                10, 6));
        rtbdAd.setPrice(bid);
        rtbdAd.setBid(bid);
        UUID uuid = UUID.fromString(this.auctionEngine.getRtbResponse().getAdNetworkInterface().getImpressionId());
        rtbdAd.setImpressionId(new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));
        rtbdAd.setSlotServed(sasParams.getSlot());
        Creative rtbdCreative = new Creative();
        rtbdCreative.setValue(finalResponse);
        rtbdAd.setCreative(rtbdCreative);
        adPoolResponse.setAds(Arrays.asList(rtbdAd));
        adPoolResponse.setMinChargedValue((long) (this.auctionEngine.getRtbResponse().getAdNetworkInterface()
                .getSecondBidPriceInUsd() * Math.pow(10, 6)));
        if (!"USD".equalsIgnoreCase(this.auctionEngine.getRtbResponse().getAdNetworkInterface().getCurrency())) {
            rtbdAd.setOriginalCurrencyName(this.auctionEngine.getRtbResponse().getAdNetworkInterface().getCurrency());
            rtbdAd.setBidInOriginalCurrency((long) (this.auctionEngine.getRtbResponse().getAdNetworkInterface()
                    .getBidPriceInLocal() * Math.pow(10, 6)));
        }
        return adPoolResponse;
    }

    // send response to the caller
    private void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders,
            final Channel serverChannel) throws NullPointerException {

        byte[] bytes = responseString.getBytes(Charsets.UTF_8);
        sendResponse(status, bytes, responseHeaders, serverChannel);

    }

    // send response to the caller
    private void sendResponse(final HttpResponseStatus status, final byte[] bytes, final Map responseHeaders,
            final Channel serverChannel) throws NullPointerException {

        LOG.debug("Inside send Response");

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status,
                Unpooled.wrappedBuffer(bytes), false);

        if (null != responseHeaders) {
            for (Map.Entry entry : (Set<Map.Entry>) responseHeaders.entrySet()) {
                response.headers().add(entry.getKey().toString(), responseHeaders.get(entry.getValue()));
            }
        }

        response.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache, no-store, must-revalidate");

        // TODO: to fix keep alive, we need to fix whole flow
        // HttpHeaders.setKeepAlive(response, serverChannel.isKeepAlive());
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);

        response.headers().add(HttpHeaders.Names.EXPIRES, "-1");
        response.headers().add(HttpHeaders.Names.PRAGMA, "no-cache");
        HttpHeaders.setKeepAlive(response, sasParams.isKeepAlive());

        if (sasParams.isKeepAlive()) {
            serverChannel.writeAndFlush(response);
        }
        else {
            serverChannel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }

        totalTime = System.currentTimeMillis() - totalTime;
        LOG.debug("successfully sent response");
        if (null != sasParams) {
            cleanUp();
            LOG.debug("successfully called cleanUp()");
        }
    }

    // send response to the caller
    public void sendResponse(final String responseString, final Channel serverChannel) throws NullPointerException {
        sendResponse(HttpResponseStatus.OK, responseString, null, serverChannel);
    }

    @Override
    public AuctionEngine getAuctionEngine() {
        return auctionEngine;
    }

    // TODO: does it need to be synchronized?
    @Override
    public void sendNoAdResponse(final Channel serverChannel) throws NullPointerException {
        // Making sure response is sent only once
        if (checkResponseSent()) {
            return;
        }

        InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);

        Map<String, String> headers = null;
        LOG.debug("Sending No ads");
        if (null != sasParams && 6 == sasParams.getDst()) {
            headers = new HashMap<String, String>();
            AdPoolResponse rtbdResponse = new AdPoolResponse();
            try {
                TSerializer serializer = new TSerializer(new TBinaryProtocol.Factory());
                byte[] serializedResponse = serializer.serialize(rtbdResponse);
                sendResponse(HttpResponseStatus.OK, serializedResponse, headers, serverChannel);
                return;
            }
            catch (TException e) {
                LOG.error("Error in serializing the adPool response ", e);
            }
        }

        if (getResponseFormat().equals("xhtml")) {
            sendResponse(HttpResponseStatus.OK, NO_AD_XHTML, headers, serverChannel);
        }
        else if (isJsAdRequest()) {
            sendResponse(HttpResponseStatus.OK, String.format(NO_AD_JS_ADCODE, sasParams.getRqIframe()), headers,
                    serverChannel);
        }
        else if (getResponseFormat().equalsIgnoreCase("imai")) {
            sendResponse(HttpResponseStatus.NO_CONTENT, NO_AD_IMAI, headers, serverChannel);
        }
        else {
            sendResponse(HttpResponseStatus.OK, NO_AD_HTML, headers, serverChannel);
        }
    }

    // Return true if request contains Iframe Id and is a request from js adcode.
    private boolean isJsAdRequest() {
        if (null == sasParams) {
            return false;
        }
        String adCode = sasParams.getAdcode();
        String rqIframe = sasParams.getRqIframe();
        return adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS");
    }

    @Override
    public Boolean isEligibleForProcess(final AdNetworkInterface adNetwork) {
        if (null == rankList) {
            return false;
        }
        int index = getRankIndex(adNetwork);
        LOG.debug("inside isEligibleForProcess for {} and index is {}", adNetwork.getName(), index);
        return index == 0 || index == rankIndexToProcess;
    }

    @Override
    public Boolean isLastEntry(final AdNetworkInterface adNetwork) {
        int index = getRankIndex(adNetwork);
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
            ChannelSegment channelSegment = rankList.get(index);
            AdNetworkInterface adNetwork = channelSegment.getAdNetworkInterface();

            LOG.debug("reassignRanks iterating for {} and index is {}", adNetwork.getName(), index);

            if (adNetwork.isRequestCompleted()) {
                if (adNetwork.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
                    // Sends the response if request is completed for the
                    // specific adapter.
                    sendAdResponse(adNetwork, serverChannel);
                    break;
                }
                else {
                    // Iterates to the next adapter.
                    index++;
                }
            }
            else {
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
        if (rankList == null || rankList.size() < 1) {
            InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentcount);
            InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentlatency, totalTime);
        }

        // closing unclosed dcp channels
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rankList.get(index).getAdNetworkInterface());
            try {
                rankList.get(index).getAdNetworkInterface().cleanUp();
            }
            catch (Exception exception) {
                LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index, rankList.get(index)
                        .getAdNetworkInterface(), exception);
            }
        }

        // Closing RTB channels
        List<ChannelSegment> rtbList = auctionEngine.getRtbSegments();

        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            }
            catch (Exception exception) {
                LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index, rtbList.get(index)
                        .getAdNetworkInterface(), exception);
            }
        }

        LOG.debug("done with closing channels");
        writeLogs();
    }

    public void writeLogs() {
        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        if (null != getRankList()) {
            list.addAll(getRankList());
        }
        if (null != getAuctionEngine().getRtbSegments()) {
            list.addAll(getAuctionEngine().getRtbSegments());
        }
        long totalTime = getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != getRtbResponse()) {
                adResponseChannelSegment = getRtbResponse();
            }
            else if (null != getAdResponse()) {
                adResponseChannelSegment = getRankList().get(getSelectedAdIndex());
            }
            Logging.rrLogging(adResponseChannelSegment, list, sasParams, terminationReason, totalTime);
            Logging.advertiserLogging(list, CasConfigUtil.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, CasConfigUtil.getLoggerConfig());
        }
        catch (JSONException exception) {
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        catch (TException exception) {
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
    public String getResponseFormat() {
        if (null != sasParams) {
            String responseFormat = sasParams.getRFormat();
            if (null == responseFormat) {
                return "html";
            }
            else if ("axml".equalsIgnoreCase(responseFormat)) {
                responseFormat = "xhtml";
            }
            return responseFormat;
        }
        return "html";
    }

    @Override
    public void processDcpList(final Channel channel) {
        // There would always be rtb partner before going to dcp list
        // So will iterate over the dcp list once.
        if (this.getRankList().isEmpty()) {
            LOG.debug("dcp list is empty so sending NoAd");
            this.sendNoAdResponse(channel);
            return;
        }
        int rankIndex = rankIndexToProcess;
        if (rankList.size() <= rankIndex) {
            return;
        }
        ChannelSegment segment = this.getRankList().get(rankIndex);
        while (segment.getAdNetworkInterface().isRequestCompleted()) {
            if (segment.getAdNetworkInterface().getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
                this.sendAdResponse(segment.getAdNetworkInterface(), channel);
                break;
            }
            rankIndex++;
            if (rankIndex >= this.getRankList().size()) {
                this.sendNoAdResponse(channel);
                break;
            }
            segment = getRankList().get(rankIndex);
        }
        this.setRankIndexToProcess(rankIndex);
    }

    @Override
    public void processDcpPartner(final Channel channel, final AdNetworkInterface adNetworkInterface) {
        if (!this.isEligibleForProcess(adNetworkInterface)) {
            LOG.debug("{} is not eligible for processing", adNetworkInterface.getName());
            return;
        }
        LOG.debug("the channel is eligible for processing");
        if (adNetworkInterface.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
            sendAdResponse(adNetworkInterface, channel);
            cleanUp();
        }
        else if (isLastEntry(adNetworkInterface)) {
            sendNoAdResponse(channel);
            cleanUp();
        }
        else {
            reassignRanks(adNetworkInterface, channel);
        }
    }

}
