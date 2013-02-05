package com.inmobi.adserve.channels.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.awt.Dimension;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class ResponseSender extends HttpRequestHandlerBase {

  private static final String startTags = "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
  private static final String endTags = " ]]></Ad></Ads></AdResponse>";
  private static final String noAdXhtml = "<AdResponse><Ads></Ads></AdResponse>";
  private static final String noAdHtml = "<!-- mKhoj: No advt for this position -->";
  private static final String noAdJsAdcode = "<html><head><title></title><style type=\"text/css\">"
      + " body {margin: 0; overflow: hidden; background-color: transparent}"
      + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
      + "<script type=\"text/javascript\" charset=\"utf-8\">"
      + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
  private HttpRequestHandler hrh;
  private DebugLogger logger;
  private long totalTime;
  private List<ChannelSegment> rankList;
  public List<ChannelSegment> rtbSegments;
  private ThirdPartyAdResponse adResponse;
  private boolean responseSent;
  public SASRequestParameters sasParams;
  private boolean auctionComplete = false;
  private double secondBidPrice;
  private int rankIndexToProcess;
  private int selectedAdIndex;
  private boolean requestCleaned;
  public ChannelSegment rtbResponse;
  public CasInternalRequestParameters casInternalRequestParameters;

  public List<ChannelSegment> getRtbSegments() {
    return this.rtbSegments;
  }
  
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
    return this.rtbResponse;
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
    this.rtbSegments = new ArrayList<ChannelSegment>();
    this.adResponse = null;
    this.responseSent = false;
    this.sasParams = null;
    this.rankIndexToProcess = 0;
    this.selectedAdIndex = 0;
    this.requestCleaned = false;
    this.rtbResponse = null;
  }

  @Override
  public void sendAdResponse(AdNetworkInterface selectedAdNetwork, MessageEvent event) {
    adResponse = selectedAdNetwork.getResponseAd();
    selectedAdIndex = getRankIndex(selectedAdNetwork);
    sendAdResponse(adResponse.response, event);
    InspectorStats.incrementStatCount(selectedAdNetwork.getName(), InspectorStrings.serverImpression);
  }

  // send Ad Response
  public synchronized void sendAdResponse(String responseString, MessageEvent event) throws NullPointerException {
    // Making sure response is sent only once
    if(responseSent) {
      return;
    }
    responseSent = true;
    logger.debug("ad received so trying to send ad response");
    if(getResponseFormat().equals("xhtml")) {
      if(logger.isDebugEnabled()) {
        logger.debug("slot served is " + sasParams.slot);
      }

      if(sasParams.slot != null && SlotSizeMapping.getDimension(Long.parseLong(sasParams.slot)) != null) {
        Dimension dim = SlotSizeMapping.getDimension(Long.parseLong(sasParams.slot));
        String startElement = String.format(startTags, (int) dim.getWidth(), (int) dim.getHeight());
        responseString = startElement + responseString + endTags;
        InspectorStats.incrementStatCount(InspectorStrings.totalFills);
      } else {
        logger.error("invalid slot, so not returning response, even though we got an ad");
        responseString = noAdXhtml;
        InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);
      }
    }
    sendResponse(responseString, event);
  }

  //send response to the caller
  public void sendResponse(String responseString, ChannelEvent event) throws NullPointerException {
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
    response.setContent(ChannelBuffers.copiedBuffer(responseString, Charset.forName("UTF-8").name()));
    if(event != null) {
      logger.debug("event not null inside send Response");
      Channel channel = event.getChannel();
      if(channel != null && channel.isWritable()) {
        logger.debug("channel not null inside send Response");
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
      } else {
        logger.debug("Request Channel is null or channel is not writeable.");
      }
    }
    totalTime = System.currentTimeMillis() - totalTime;
    logger.debug("successfully sent response");
    if(null != sasParams) {
      cleanUp();
      logger.debug("successfully called cleanUp()");
    }
  }

  @Override
  public boolean isAllRtbComplete() {
    if(rtbSegments.size() == 0)
      return true;
    for (ChannelSegment channelSegment : rtbSegments) {
      if(!channelSegment.adNetworkInterface.isRequestCompleted())
        return false;
    }
    return true;
  }

  @Override
  public double getSecondBidPrice() {
    return secondBidPrice;
  }
  
  /***
   * RunRtbSecondPriceAuctionEngine returns the adnetwork selected after
   * auctioning If no of rtb segments selected after filtering is zero it
   * returns the null If no of rtb segments selected after filtering is one it
   * returns the rtb adapter for the segment BidFloor is maximum of lowestEcpm
   * and siteFloor If only 2 rtb are selected, highest bid will win and would be
   * charged the secondHighest price If only 1 rtb is selected, it will be
   * selected for sending response and will be charged the highest of
   * secondHighest price or 90% of bidFloor
   */
  @Override
  public AdNetworkInterface runRtbSecondPriceAuctionEngine() {
    auctionComplete = true;
    logger.debug("Inside RTB auction engine");
    List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
    logger.debug("No of rtb partners who sent response are", new Integer(this.rtbSegments.size()).toString());
    for (int i=0; i < this.rtbSegments.size() ; i++) {
      if (this.rtbSegments.get(0).adNetworkInterface.getAdStatus().equalsIgnoreCase("AD")) {
        rtbSegments.add(this.rtbSegments.get(i));
      }
    }
    logger.debug("No of rtb partners who sent AD response are", new Integer(rtbSegments.size()).toString());
    if(rtbSegments.size() == 0) {
      rtbResponse = null;
      logger.debug("returning from auction engine , winner is null");
      return null;
    } else if(rtbSegments.size() == 1) {
      rtbResponse = rtbSegments.get(0);
      secondBidPrice = sasParams.siteFloor > casInternalRequestParameters.highestEcpm ? sasParams.siteFloor : casInternalRequestParameters.highestEcpm + 0.01;
      rtbResponse.adNetworkInterface.setSecondBidPrice(secondBidPrice);
      logger.debug("completed auction and winner is", rtbSegments.get(0).adNetworkInterface.getName() + " and secondBidPrice is " + secondBidPrice);
      return rtbSegments.get(0).adNetworkInterface;
    }

    for (int i = 0; i < rtbSegments.size(); i++) {
      for (int j = i + 1; j < rtbSegments.size(); j++) {
        if(rtbSegments.get(i).adNetworkInterface.getBidprice() < rtbSegments.get(j).adNetworkInterface.getBidprice()) {
          ChannelSegment channelSegment = rtbSegments.get(i);
          rtbSegments.set(i, rtbSegments.get(j));
          rtbSegments.set(j, channelSegment);
        }
      }
    }
    double maxPrice = rtbSegments.get(0).adNetworkInterface.getBidprice();
    int secondHighestBidNumber = 1;
    int lowestLatency = 0;
    for (int i = 1; i < rtbSegments.size(); i++) {
      if(rtbSegments.get(i).adNetworkInterface.getBidprice() < maxPrice) {
        secondHighestBidNumber = i;
        break;
      } else if(rtbSegments.get(i).adNetworkInterface.getLatency() < rtbSegments.get(lowestLatency).adNetworkInterface
          .getLatency())
        lowestLatency = i;
    }
    if(secondHighestBidNumber != 1) {
      double secondHighestBidPrice = rtbSegments.get(secondHighestBidNumber).adNetworkInterface.getBidprice();
      double price = maxPrice * 0.9;
      if(price > secondHighestBidPrice) {
        secondBidPrice = price + 0.01;
      } else {
        secondBidPrice = secondHighestBidPrice + 0.01;
      }
    } else
      secondBidPrice = rtbSegments.get(1).adNetworkInterface.getBidprice() + 0.01;
    rtbResponse = rtbSegments.get(lowestLatency);
    rtbResponse.adNetworkInterface.setSecondBidPrice(secondBidPrice);
    logger.debug("completed auction and winner is", rtbSegments.get(lowestLatency).adNetworkInterface.getName() + " and secondBidPrice is " + secondBidPrice);
    return rtbSegments.get(lowestLatency).adNetworkInterface;
  }

  @Override
  public synchronized void sendNoAdResponse(ChannelEvent event) throws NullPointerException {
    // Making sure response is sent only once
    if(responseSent) {
      return;
    }
    responseSent = true;
    logger.debug("no ad received");
    InspectorStats.incrementStatCount(InspectorStrings.totalNoFills);

    if(getResponseFormat().equals("xhtml")) {
      sendResponse(noAdXhtml, event);
    } else if(isJsAdRequest()) {
      sendResponse(String.format(noAdJsAdcode, sasParams.rqIframe), event);
    } else {
      sendResponse(noAdHtml, event);
    }
  }

  //Return true if request contains Iframe Id and is a request from js adcode.
  public boolean isJsAdRequest() {
    if(null == sasParams) {
      return false;
    }
    String adCode = sasParams.adcode;
    String rqIframe = sasParams.rqIframe;
    if(adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS")) {
      return true;
    }
    return false;
  }

  @Override
  public Boolean isEligibleForProcess(AdNetworkInterface adNetwork) {
    if(null == rankList) {
      return false;
    }
    int index = getRankIndex(adNetwork);
    if(logger.isDebugEnabled()) {
      logger.debug("inside isEligibleForProcess for " + adNetwork.getName() + " and index is " + index);
    }

    if(index == 0 || index == rankIndexToProcess) {
      return true;
    }
    return false;
  }

  @Override
  public Boolean isLastEntry(AdNetworkInterface adNetwork) {
    int index = getRankIndex(adNetwork);
    if(logger.isDebugEnabled()) {
      logger.debug("inside isLastEntry for " + adNetwork.getName() + " and index is " + index);
    }
    if(index == rankList.size() - 1) {
      return true;
    }
    return false;
  }

  //Iterates over the complete rank list and set the new value for
  // rankIndexToProcess.
  @Override
  public void reassignRanks(AdNetworkInterface adNetworkCaller, MessageEvent event) {
    int index = getRankIndex(adNetworkCaller);
    if(logger.isDebugEnabled()) {
      logger.debug("reassignRanks called for " + adNetworkCaller.getName() + " and index is " + index);
    }

    while (index < rankList.size()) {
      ChannelSegment channel = rankList.get(index);
      AdNetworkInterface adNetwork = channel.adNetworkInterface;

      if(logger.isDebugEnabled()) {
        logger.debug("reassignRanks iterating for " + adNetwork.getName() + " and index is " + index);
      }

      if(adNetwork.isRequestCompleted()) {
        ThirdPartyAdResponse adResponse = adNetwork.getResponseAd();
        if(adResponse.responseStatus == ThirdPartyAdResponse.ResponseStatus.SUCCESS) {
          // Sends the response if request is completed for the
          // specific adapter.
          sendAdResponse(adNetwork, event);
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
    if(index == rankList.size()) {
      sendNoAdResponse(event);
    }
  }

  @Override
  public void cleanUp() {
    // Making sure cleanup is called only once
    if(requestCleaned) {
      return;
    }
    requestCleaned = true;
    logger.debug("trying to close open channels");
    if(rankList == null || rankList.size() < 1) {
      InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentcount);
      InspectorStats.incrementStatCount(InspectorStrings.nomatchsegmentlatency, totalTime);
    }
    // closing unclosed channels
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      if(logger.isDebugEnabled()) {
        logger.debug("calling clean up for channel " + rankList.get(index).adNetworkInterface.getId());
      }
      try {
        rankList.get(index).adNetworkInterface.cleanUp();
      } catch (Exception exception) {
        if(logger.isDebugEnabled()) {
          logger.debug("Error in closing channel for index: " + index + " Name: "
              + rankList.get(index).adNetworkInterface.getName() + " Exception: " + exception.getLocalizedMessage());
        }
      }
    }
    for (int index = 0; rankList != null && index < rankList.size(); index++) {
      ChannelsClientHandler.responseMap.remove(rankList.get(index).adNetworkInterface.getChannelId());
      ChannelsClientHandler.statusMap.remove(rankList.get(index).adNetworkInterface.getChannelId());
      ChannelsClientHandler.adStatusMap.remove(rankList.get(index).adNetworkInterface.getChannelId());
    }
    if(logger.isDebugEnabled()) {
      logger.debug("done with closing channels");
      logger.debug("responsemap size is :" + ChannelsClientHandler.responseMap.size());
      logger.debug("adstatus map size is :" + ChannelsClientHandler.adStatusMap.size());
      logger.debug("status map size is:" + ChannelsClientHandler.statusMap.size());
    }
    hrh.writeLogs(this, logger);
  }
  
  

  private int getRankIndex(AdNetworkInterface adNetwork) {
    int index = 0;
    for (index = 0; index < rankList.size(); index++) {
      if(rankList.get(index).adNetworkInterface.getImpressionId().equals(adNetwork.getImpressionId())) {
        break;
      }
    }
    return index;
  }

  // return the response format
  public String getResponseFormat() {
    String responseFormat = "html";
    if(sasParams == null || (responseFormat = sasParams.rFormat) == null) {
      return "html";
    }
    if(responseFormat.equalsIgnoreCase("axml")) {
      responseFormat = "xhtml";
    }
    return responseFormat;
  }

  @Override
  public boolean isAuctionComplete() {
    return auctionComplete;
  }

  @Override
  public boolean isRtbResponseNull() {
    return this.rtbResponse == null ? true : false;
  }
}
