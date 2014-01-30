package com.inmobi.adserve.channels.server.requesthandler;

import com.google.common.base.Charsets;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.ning.http.client.AsyncHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;


public class ResponseSender extends HttpRequestHandlerBase {

    private final static Logger         LOG             = LoggerFactory.getLogger(ResponseSender.class);

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
    private final HttpRequestHandler    hrh;
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
    private static final String         NO_AD_HEADER    = "X-MKHOJ-NOAD";

    public List<ChannelSegment> getRankList() {
        return this.rankList;
    }

    public void setRankList(final List<ChannelSegment> rankList) {
        this.rankList = rankList;
    }

    public int getRankIndexToProcess() {
        return rankIndexToProcess;
    }

    public void setRankIndexToProcess(final int rankIndexToProcess) {
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

    public ResponseSender(final HttpRequestHandler hrh) {
        this.hrh = hrh;
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
    public void sendAdResponse(final AdNetworkInterface selectedAdNetwork, final ChannelEvent event) {
        adResponse = selectedAdNetwork.getResponseAd();
        selectedAdIndex = getRankIndex(selectedAdNetwork);
        sendAdResponse(adResponse, event);
    }

    // send Ad Response
    public synchronized void sendAdResponse(final ThirdPartyAdResponse adResponse, final ChannelEvent event)
            throws NullPointerException {
        // Making sure response is sent only once
        if (responseSent) {
            return;
        }
        responseSent = true;
        LOG.debug("ad received so trying to send ad response");
        String finalReponse = adResponse.response;
        if (sasParams.getSlot() != null && SlotSizeMapping.getDimension(Long.parseLong(sasParams.getSlot())) != null) {
            LOG.debug("slot served is {}", sasParams.getSlot());
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
            LOG.info("invalid slot, so not returning response, even though we got an ad");
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
                            .getSecondBidPriceInUsd());
                jsonObject.put("winnerBid", this
                        .getAuctionEngine()
                            .getRtbResponse()
                            .getAdNetworkInterface()
                            .getBidPriceInUsd());
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
                LOG.debug("RTB reponse json to RE is {}", jsonObject);
            }
            catch (JSONException e) {
                LOG.debug("Error while making json object for rule engine " + e.getMessage());
                // Sending NOAD if error making json object
                sendNoAdResponse(event);
            }
        }
    }

    // send response to the caller
    public void sendResponse(final HttpResponseStatus status, final String responseString, final Map responseHeaders,
            final ChannelEvent event) throws NullPointerException {

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.addHeader("Expires", "-1");
        response.addHeader("Pragma", "no-cache");

        if (null != responseHeaders) {
            for (Object key : responseHeaders.keySet()) {
                response.addHeader(key.toString(), responseHeaders.get(key));
            }
        }

        String finalResponse = responseString;
        byte[] bytes = finalResponse.getBytes(Charsets.UTF_8);

        response.setHeader("Content-Length", bytes.length);
        response.setContent(ChannelBuffers.copiedBuffer(bytes));

        if (event != null) {
            LOG.debug("event not null inside send Response");
            Channel channel = event.getChannel();
            if (channel != null && channel.isWritable()) {
                LOG.debug("channel not null inside send Response");
                ChannelFuture future = channel.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
            }
            else {
                LOG.debug("Request Channel is null or channel is not writeable.");
            }
        }
        totalTime = System.currentTimeMillis() - totalTime;
        LOG.debug("successfully sent response");
        if (null != sasParams) {
            cleanUp();
            LOG.debug("successfully called cleanUp()");
        }
    }

    // send response to the caller
    public void sendResponse(final String responseString, final ChannelEvent event) throws NullPointerException {
        sendResponse(HttpResponseStatus.OK, responseString, null, event);
    }

    @Override
    public AuctionEngine getAuctionEngine() {
        return auctionEngine;
    }

    @Override
    public synchronized void sendNoAdResponse(final ChannelEvent event) throws NullPointerException {
        // Making sure response is sent only once
        if (responseSent) {
            return;
        }
        responseSent = true;
        InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);

        Map<String, String> headers = null;
        if (null != sasParams && 6 == sasParams.getDst()) {
            headers = new HashMap<String, String>();
            headers.put(NO_AD_HEADER, "true");
        }

        LOG.debug("Sending No ads");
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
    public void reassignRanks(final AdNetworkInterface adNetworkCaller, final MessageEvent event) {
        int index = getRankIndex(adNetworkCaller);
        LOG.debug("reassignRanks called for {} and index is {}", adNetworkCaller.getName(), index);

        while (index < rankList.size()) {
            ChannelSegment channel = rankList.get(index);
            AdNetworkInterface adNetwork = channel.getAdNetworkInterface();

            LOG.debug("reassignRanks iterating for {} and index is {}", adNetwork.getName(), index);

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
                LOG.debug("Error in closing channel for index: {} Name: {} Exception: {}", index, rankList
                        .get(index)
                            .getAdNetworkInterface(), exception);
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
            LOG.debug("calling clean up for channel {}", rtbList.get(index).getAdNetworkInterface().getId());
            try {
                rtbList.get(index).getAdNetworkInterface().cleanUp();
            }
            catch (Exception exception) {
                LOG.debug("Error in closing channel for index: {}  Name: {} Exception: {}", index, rtbList
                        .get(index)
                            .getAdNetworkInterface(), exception);
            }
        }
        for (int index = 0; rtbList != null && index < rtbList.size(); index++) {
            if (null != rtbList.get(index).getAdNetworkInterface().getChannelId()) {
                ChannelsClientHandler.responseMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.statusMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
                ChannelsClientHandler.adStatusMap.remove(rtbList.get(index).getAdNetworkInterface().getChannelId());
            }
        }

        LOG.debug("done with closing channels");
        LOG.debug("responsemap size is : {}", ChannelsClientHandler.responseMap.size());
        LOG.debug("adstatus map size is : {}", ChannelsClientHandler.adStatusMap.size());
        LOG.debug("status map size is: {}", ChannelsClientHandler.statusMap.size());
        hrh.writeLogs(this);
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
    public void processDcpList(final MessageEvent serverEvent) {
        // There would always be rtb partner before going to dcp list
        // So will iterate over the dcp list once.
        if (this.getRankList().isEmpty()) {
            LOG.debug("dcp list is empty so sending NoAd");
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
    public void processDcpPartner(final MessageEvent serverEvent, final AdNetworkInterface adNetworkInterface) {
        if (!this.isEligibleForProcess(adNetworkInterface)) {
            LOG.debug("{} is not eligible for processing", adNetworkInterface.getName());
            return;
        }
        LOG.debug("the channel is eligible for processing");
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
