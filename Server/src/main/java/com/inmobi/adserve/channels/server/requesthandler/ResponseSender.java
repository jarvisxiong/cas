package com.inmobi.adserve.channels.server.requesthandler;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.awt.Dimension;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import com.google.common.base.Charsets;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.ning.http.client.AsyncHttpClient;


public class ResponseSender extends HttpRequestHandlerBase {

    private static final String         startTags       = "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
    private static final String         endTags         = " ]]></Ad></Ads></AdResponse>";
    private static final String         adImaiStartTags = "<!DOCTYPE html>";
    private static final String         noAdImai        = "";
    private static final String         noAdXhtml       = "<AdResponse><Ads></Ads></AdResponse>";
    private static final String         noAdHtml        = "<!-- mKhoj: No advt for this position -->";
    private static final String         noAdJsAdcode    = "<html><head><title></title><style type=\"text/css\">"
                                                                + " body {margin: 0; overflow: hidden; background-color: transparent}"
                                                                + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
                                                                + "<script type=\"text/javascript\" charset=\"utf-8\">"
                                                                + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
    private HttpRequestHandler          hrh;
    private DebugLogger                 logger;
    private long                        totalTime;
    private List<ChannelSegment>        rankList;
    private ThirdPartyAdResponse        adResponse;
    private boolean                     responseSent;
    public SASRequestParameters         sasParams;
    private int                         rankIndexToProcess;
    private int                         selectedAdIndex;
    private boolean                     requestCleaned;
    public CasInternalRequestParameters casInternalRequestParameters;
    private AuctionEngine               auctionEngine;
    private static final String         NO_AD_HEADER    = "X-MKHOJ-NOAD";

    public List<ChannelSegment> getRankList() {
        return this.rankList;
    }

    public void setRankList(List<ChannelSegment> rankList) {
        this.rankList = rankList;
    }

    public int getRankIndexToProcess() {
        return rankIndexToProcess;
    }

    public void setRankIndexToProcess(int rankIndexToProcess) {
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

    public ResponseSender(HttpRequestHandler hrh, DebugLogger logger) {
        this.hrh = hrh;
        this.logger = logger;
        this.totalTime = System.currentTimeMillis();
        this.rankList = null;
        this.adResponse = null;
        this.responseSent = false;
        this.sasParams = null;
        this.rankIndexToProcess = 0;
        this.selectedAdIndex = 0;
        this.requestCleaned = false;
        this.auctionEngine = new AuctionEngine(logger);
    }

    @Override
    public void sendAdResponse(AdNetworkInterface selectedAdNetwork, ChannelEvent event) {
        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);
        sendAdResponse(adResponse, event);
    }

    // send Ad Response
    public synchronized void sendAdResponse(ThirdPartyAdResponse adResponse, ChannelEvent event)
            throws NullPointerException {
        // Making sure response is sent only once
        if (responseSent) {
            return;
        }
        responseSent = true;
        logger.debug("ad received so trying to send ad response");
        String finalReponse = adResponse.response;
        if (sasParams.getSlot() != null && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            logger.debug("slot served is", sasParams.getSlot());
            InspectorStats.incrementStatCount(InspectorStrings.totalFills);
            if (getResponseFormat().equals("xhtml")) {
                Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot()));
                String startElement = String.format(startTags, (int) dim.getWidth(), (int) dim.getHeight());
                finalReponse = startElement + finalReponse + endTags;
            }
            else if (getResponseFormat().equalsIgnoreCase("imai")) {
                finalReponse = adImaiStartTags + finalReponse;
            }
        }
        else {
            logger.info("invalid slot, so not returning response, even though we got an ad");
            InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);
            if (getResponseFormat().equals("xhtml")) {
                finalReponse = noAdXhtml;
            }
            sendResponse(OK, finalReponse, adResponse.responseHeaders, event);
            return;
        }
        if (6 != sasParams.getDst()) {
            sendResponse(OK, finalReponse, adResponse.responseHeaders, event);
        }
        else {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("secondHighestBid", this
                        .getAuctionEngine()
                            .getRtbResponse()
                            .getAdNetworkInterface()
                            .getSecondBidPrice());
                jsonObject.put("winnerBid", this
                        .getAuctionEngine()
                            .getRtbResponse()
                            .getAdNetworkInterface()
                            .getBidprice());
                jsonObject.put("adm", finalReponse);
                jsonObject.put("advertiserId", this.auctionEngine.getRtbResponse().getChannelEntity().getAccountId());
                jsonObject.put("adgroupId", this.auctionEngine
                        .getRtbResponse()
                            .getChannelSegmentEntity()
                            .getAdgroupId());
                jsonObject.put("adgroupIncId", this.auctionEngine
                        .getRtbResponse()
                            .getChannelSegmentEntity()
                            .getAdgroupIncId());
                jsonObject.put("adIncId", this.auctionEngine.getRtbResponse().getChannelSegmentEntity().getIncId());
                jsonObject.put("adId", this.auctionEngine.getRtbResponse().getChannelSegmentEntity().getAdId());
                jsonObject.put("rtbFloor", casInternalRequestParameters.rtbBidFloor);
                jsonObject.put("impressionId", this.auctionEngine
                        .getRtbResponse()
                            .getAdNetworkInterface()
                            .getImpressionId());
                jsonObject.put("campaignIncId", this.auctionEngine
                        .getRtbResponse()
                            .getChannelSegmentEntity()
                            .getCampaignIncId());
                jsonObject.put("campaignId", this.auctionEngine
                        .getRtbResponse()
                            .getChannelSegmentEntity()
                            .getCampaignId());
                InspectorStats.incrementStatCount(InspectorStrings.ruleEngineFills);
                sendResponse(OK, jsonObject.toString(), adResponse.responseHeaders, event);
                if (logger.isDebugEnabled()) {
                    logger.debug("RTB reponse json to RE is " + jsonObject.toString());
                }
            }
            catch (JSONException e) {
                logger.debug("Error while making json object for rule engine " + e.getMessage());
                // Sending NOAD if error making json object
                sendNoAdResponse(event);
            }
        }
    }

    // send response to the caller
    public void sendResponse(HttpResponseStatus status, String responseString, Map responseHeaders, ChannelEvent event)
            throws NullPointerException {
        if (hrh.isTraceRequest) {
            status = OK;
        }

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.addHeader("Expires", "-1");
        response.addHeader("Pragma", "no-cache");

        if (null != responseHeaders) {
            for (Object key : responseHeaders.keySet()) {
                response.addHeader(key.toString(), responseHeaders.get(key));
            }
        }
        logger.debug("trace request =", hrh.isTraceRequest);

        String finalResponse = hrh.isTraceRequest ? logger.getTrace() : responseString;
        byte[] bytes = finalResponse.getBytes(Charsets.UTF_8);

        if (hrh.isTraceRequest) {
            response.setHeader("Content-Type", "text/plain; charset=utf-8");
        }
        response.setHeader("Content-Length", bytes.length);
        response.setContent(ChannelBuffers.copiedBuffer(bytes));

        if (event != null) {
            logger.debug("event not null inside send Response");
            Channel channel = event.getChannel();
            if (channel != null && channel.isWritable()) {
                logger.debug("channel not null inside send Response");
                ChannelFuture future = channel.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
            else {
                logger.debug("Request Channel is null or channel is not writeable.");
            }
        }
        totalTime = System.currentTimeMillis() - totalTime;
        logger.debug("successfully sent response");
        if (null != sasParams) {
            cleanUp();
            logger.debug("successfully called cleanUp()");
        }
    }

    // send response to the caller
    public void sendResponse(String responseString, ChannelEvent event) throws NullPointerException {
        sendResponse(HttpResponseStatus.OK, responseString, null, event);
    }

    @Override
    public AuctionEngine getAuctionEngine() {
        return auctionEngine;
    }

    @Override
    public synchronized void sendNoAdResponse(ChannelEvent event) throws NullPointerException {
        // Making sure response is sent only once
        if (responseSent) {
            return;
        }
        responseSent = true;
        InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);

        Map<String, String> headers = null;
        if (6 == sasParams.getDst()) {
            headers = new HashMap<String, String>();
            headers.put(NO_AD_HEADER, "true");
        }

        logger.debug("Sending No ads");
        if (getResponseFormat().equals("xhtml")) {
            sendResponse(OK, noAdXhtml, headers, event);
        }
        else if (isJsAdRequest()) {
            sendResponse(OK, String.format(noAdJsAdcode, sasParams.getRqIframe()), headers, event);
        }
        else if (getResponseFormat().equalsIgnoreCase("imai")) {
            sendResponse(NO_CONTENT, noAdImai, headers, event);
        }
        else {
            sendResponse(OK, noAdHtml, headers, event);
        }
    }

    // Return true if request contains Iframe Id and is a request from js adcode.
    public boolean isJsAdRequest() {
        if (null == sasParams) {
            return false;
        }
        String adCode = sasParams.getAdcode();
        String rqIframe = sasParams.getRqIframe();
        return adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS");
    }

    @Override
    public Boolean isEligibleForProcess(AdNetworkInterface adNetwork) {
        if (null == rankList) {
            return false;
        }
        int index = getRankIndex(adNetwork);
        if (logger.isDebugEnabled()) {
            logger.debug("inside isEligibleForProcess for " + adNetwork.getName() + " and index is " + index);
        }
        return index == 0 || index == rankIndexToProcess;
    }

    @Override
    public Boolean isLastEntry(AdNetworkInterface adNetwork) {
        int index = getRankIndex(adNetwork);
        if (logger.isDebugEnabled()) {
            logger.debug("inside isLastEntry for " + adNetwork.getName() + " and index is " + index);
        }
        return index == rankList.size() - 1;
    }

    // Iterates over the complete rank list and set the new value for
    // rankIndexToProcess.
    @Override
    public void reassignRanks(AdNetworkInterface adNetworkCaller, MessageEvent event) {
        int index = getRankIndex(adNetworkCaller);
        if (logger.isDebugEnabled()) {
            logger.debug("reassignRanks called for " + adNetworkCaller.getName() + " and index is " + index);
        }

        while (index < rankList.size()) {
            ChannelSegment channel = rankList.get(index);
            AdNetworkInterface adNetwork = channel.getAdNetworkInterface();

            if (logger.isDebugEnabled()) {
                logger.debug("reassignRanks iterating for " + adNetwork.getName() + " and index is " + index);
            }

            if (adNetwork.isRequestCompleted()) {
                if (adNetwork.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
                    // Sends the response if request is completed for the
                    // specific adapter.
                    sendAdResponse(adNetwork, event);
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
            sendNoAdResponse(event);
        }
    }

    @Override
    public void cleanUp() {
        // Making sure cleanup is called only once
        if (requestCleaned) {
            return;
        }
        requestCleaned = true;
        logger.debug("trying to close open channels");
        if (rankList == null || rankList.size() < 1) {
            InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentcount);
            InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentlatency, totalTime);
        }

        // closing unclosed dcp channels
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            if (logger.isDebugEnabled()) {
                logger.debug("calling clean up for channel " + rankList.get(index).getAdNetworkInterface().getId());
            }
            try {
                rankList.get(index).getAdNetworkInterface().cleanUp();
            }
            catch (Exception exception) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error in closing channel for index: " + index + " Name: "
                            + rankList.get(index).getAdNetworkInterface().getName() + " Exception: "
                            + exception.getLocalizedMessage());
                }
            }
        }
        for (int index = 0; rankList != null && index < rankList.size(); index++) {
            if (null != rankList.get(index).getAdNetworkInterface().getChannelId()) {
                ChannelsClientHandler.responseMap.remove(rankList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.statusMap.remove(rankList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.adStatusMap.remove(rankList.get(index).getAdNetworkInterface().getChannelId());
            }
        }

        // Closing RTB channels
        List<ChannelSegment> rtbList = auctionEngine.getRtbSegments();

        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            if (logger.isDebugEnabled()) {
                logger.debug("calling clean up for channel " + rtbList.get(index).getAdNetworkInterface().getId());
            }
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            }
            catch (Exception exception) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error in closing channel for index: " + index + " Name: "
                            + rtbList.get(index).getAdNetworkInterface().getName() + " Exception: "
                            + exception.getLocalizedMessage());
                }
            }
        }
        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            if (null != rtbList.get(index).getAdNetworkInterface().getChannelId()) {
                ChannelsClientHandler.responseMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.statusMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.adStatusMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("done with closing channels");
            logger.debug("responsemap size is :" + ChannelsClientHandler.responseMap.size());
            logger.debug("adstatus map size is :" + ChannelsClientHandler.adStatusMap.size());
            logger.debug("status map size is:" + ChannelsClientHandler.statusMap.size());
        }
        hrh.writeLogs(this, logger);
    }

    private int getRankIndex(AdNetworkInterface adNetwork) {
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
    public void processDcpList(MessageEvent serverEvent) {
        // There would always be rtb partner before going to dcp list
        // So will iterate over the dcp list once.
        if (this.getRankList().isEmpty()) {
            logger.debug("dcp list is empty so sending NoAd");
            this.sendNoAdResponse(serverEvent);
            return;
        }
        int rankIndex = this.getRankIndexToProcess();
        if (rankList.size() <= rankIndex) {
            return;
        }
        ChannelSegment segment = this.getRankList().get(rankIndex);
        while (segment.getAdNetworkInterface().isRequestCompleted()) {
            if (segment.getAdNetworkInterface().getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
                this.sendAdResponse(segment.getAdNetworkInterface(), serverEvent);
                break;
            }
            rankIndex++;
            if (rankIndex >= this.getRankList().size()) {
                this.sendNoAdResponse(serverEvent);
                break;
            }
            segment = getRankList().get(rankIndex);
        }
        this.setRankIndexToProcess(rankIndex);
    }

    @Override
    public void processDcpPartner(MessageEvent serverEvent, AdNetworkInterface adNetworkInterface) {
        if (!this.isEligibleForProcess(adNetworkInterface)) {
            logger.debug(adNetworkInterface.getName(), "is not eligible for processing");
            return;
        }
        logger.debug("the channel is eligible for processing");
        if (adNetworkInterface.getResponseAd().responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
            sendAdResponse(adNetworkInterface, serverEvent);
            cleanUp();
        }
        else if (isLastEntry(adNetworkInterface)) {
            sendNoAdResponse(serverEvent);
            cleanUp();
        }
        else {
            reassignRanks(adNetworkInterface, serverEvent);
        }
    }

    @Override
    public AsyncHttpClient getAsyncClient() {
        return AsyncRequestMaker.getAsyncHttpClient();
    }

}
