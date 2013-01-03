package com.inmobi.adserve.channels.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

public class HttpRequestHandler extends HttpRequestHandlerBase {

  private double secondBidPrice;
  private double bidFloor;
  public List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
  public ChannelSegment rtbResponse;
  private boolean requestCleaned = false;
  private boolean responseSent = false;
  private String terminationReason = "NO";
  private static final String jsonParsingError = "EJSON";
  private static final String processingError = "ESERVER";
  private static final String missingSiteId = "NOSITE";
  private static final String incompatibleSiteType = "ESITE";
  private static final String lowSdkVersion = "LSDK";
  private static final String startTags = "<AdResponse><Ads number=\"1\"><Ad type=\"rm\" width=\"%s\" height=\"%s\"><![CDATA[";
  private static final String endTags = " ]]></Ad></Ads></AdResponse>";
  private static final String noAdXhtml = "<AdResponse><Ads></Ads></AdResponse>";
  private static final String noAdHtml = "<!-- mKhoj: No advt for this position -->";
  private static final String noAdJsAdcode = "<html><head><title></title><style type=\"text/css\">"
      + " body {margin: 0; overflow: hidden; background-color: transparent}"
      + " </style></head><body class=\"nofill\"><!-- NO FILL -->" + "<script type=\"text/javascript\" charset=\"utf-8\">"
      + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
  private static int rollCount = 0;
  private static int percentRollout;
  private long totalTime;
  private List<ChannelSegment> rankList = null;
  private static Configuration config;
  private static Configuration rtbConfig;
  private static Configuration adapterConfig;
  private static Configuration loggerConfig;
  private static Configuration log4jConfig;
  private static Configuration databaseConfig;
  private static ClientBootstrap clientBootstrap;
  private static ClientBootstrap rtbClientBootstrap;
  private static RepositoryHelper repositoryHelper;
  private SASRequestParameters sasParams = new SASRequestParameters();
  private JSONObject jObject = null;
  private static Random random = new Random();
  private static List<String> allowedSiteTypes;
  private int rankIndexToProcess = 0;
  private int selectedAdIndex = 0;
  public DebugLogger logger;
  public ThirdPartyAdResponse adResponse = null;
  private static final String CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
  private static final String CONNECTION_RESET_PEER = "java.io.IOException: Connection reset by peer";

  public static void init(ConfigurationLoader config, ClientBootstrap clientBootstrap, ClientBootstrap rtbClientBootstrap,
      RepositoryHelper repositoryHelper) {
    HttpRequestHandler.rtbConfig = config.rtbConfiguration();
    HttpRequestHandler.loggerConfig = config.loggerConfiguration();
    HttpRequestHandler.config = config.serverConfiguration();
    HttpRequestHandler.adapterConfig = config.adapterConfiguration();
    HttpRequestHandler.log4jConfig = config.log4jConfiguration();
    HttpRequestHandler.databaseConfig = config.databaseConfiguration();
    HttpRequestHandler.clientBootstrap = clientBootstrap;
    HttpRequestHandler.rtbClientBootstrap = rtbClientBootstrap;
    HttpRequestHandler.repositoryHelper = repositoryHelper;
    percentRollout = HttpRequestHandler.config.getInt("percentRollout", 100);
    InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(percentRollout));
  }

  public static Configuration getRtbConfig() {
    return rtbConfig;
  }

  public static void setRtbConfig(Configuration rtbConfig) {
    HttpRequestHandler.rtbConfig = rtbConfig;
  }

  // Invoked when an exception occurs
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    // whenever channel throws closedchannelexception increment the
    // totalterminate
    // means channel is closed by party who requested for the ad
    String exceptionString = e.getClass().getSimpleName();
    InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
    InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
    if(logger == null)
      logger = new DebugLogger();
    if(exceptionString.equalsIgnoreCase(CLOSED_CHANNEL_EXCEPTION) || exceptionString.equalsIgnoreCase(CONNECTION_RESET_PEER)) {
      InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
      logger.debug("Channel is terminated " + ctx.getChannel().getId());
    }
    logger.error("Getting netty error in HttpRequestHandler: " + e.getCause());
    if(e.getChannel().isOpen()) {
      sendNoAdResponse(e);
    }
    e.getCause().printStackTrace();
  }

  // Invoked when request timeout.
  @Override
  public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
    if(e.getChannel().isOpen()) {
      logger.debug("Channel is open in channelIdle handler");
      sendNoAdResponse(e);
    }
    // Whenever channel is Wrter_idle, increment the totalTimeout. It means
    // server
    // could not write the response with in 800 ms
    logger.debug("inside channel idle event handler for Request channel ID: " + e.getChannel().getId());
    if(e.getState().toString().equalsIgnoreCase("ALL_IDLE") || e.getState().toString().equalsIgnoreCase("WRITE_IDLE")) {
      InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
      logger.debug("server timeout");
    }
  }

  // Invoked when message is received over the connection
  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    try {
      logger = new DebugLogger();
      bidFloor = rtbConfig.getDouble("bidFloor", 0.0);
      logger.debug("bidFloor is " + bidFloor);
      totalTime = System.currentTimeMillis();
      HttpRequest request = (HttpRequest) e.getMessage();

      String host = getHost(request);
      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

      if(queryStringDecoder.getPath().equalsIgnoreCase("/stat")) {
        sendResponse(InspectorStats.getStats(BootstrapCreation.getMaxConnections(), BootstrapCreation.getDroppedConnections()), e);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/mapsizes")) {
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("ResponseMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("StatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("AdStatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("SampledAdvertiserLog", Logging.sampledAdvertiserLogNos.size());
        mapsizes.put("ActiveOutboundConnections", BootstrapCreation.getActiveOutboundConnections());
        mapsizes.put("MaxConnections", BootstrapCreation.getMaxConnections());
        mapsizes.put("DroppedConnections", BootstrapCreation.getDroppedConnections());
        sendResponse(mapsizes.toString(), e);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/changerollout")) {
        changeRollout(e, queryStringDecoder);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/lbstatus")) {
        sendLbStatus(e);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/disablelbstatus")) {
        disableLbStatus(e, host);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/enablelbstatus")) {
        enableLbStatus(e, host);
        return;
      }

      if(queryStringDecoder.getPath().equalsIgnoreCase("/configChange")) {
        Map<String, List<String>> params = queryStringDecoder.getParameters();
        try {
          jObject = RequestParser.extractParams(params, "update", logger);
        } catch (JSONException exeption) {
          jObject = new JSONObject();
          logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
          terminationReason = jsonParsingError;
          InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
        }
        changeConfig(e, jObject);
        return;
      }

      InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
      Map<String, List<String>> params = queryStringDecoder.getParameters();
      try {
        jObject = RequestParser.extractParams(params, logger);
      } catch (JSONException exeption) {
        jObject = new JSONObject();
        logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
        terminationReason = jsonParsingError;
        InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
      }
      sasParams = RequestParser.parseRequestParameters(jObject, logger);

      if(random.nextInt(100) >= percentRollout) {
        logger.debug("Request not being served because of limited percentage rollout");
        InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
        sendNoAdResponse(e);
      }

      if(null == sasParams || null == sasParams.siteId) {
        logger.debug("Terminating request as site id was missing");
        terminationReason = missingSiteId;
        InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
        sendNoAdResponse(e);
        return;
      }

      if(!sasParams.allowBannerAds || sasParams.siteFloor > 5) {
        logger.debug("Request not being served because of banner not allowed or site floor above threshold");
        sendNoAdResponse(e);
        return;
      }
      if(sasParams.siteType != null && !allowedSiteTypes.contains(sasParams.siteType)) {
        logger.error("Terminating request as incompatible content type");
        terminationReason = incompatibleSiteType;
        InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
        sendNoAdResponse(e);
        return;
      }

      if(sasParams.sdkVersion != null) {
        try {
          if((sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("i") || sasParams.sdkVersion.substring(0, 1)
              .equalsIgnoreCase("a")) && Integer.parseInt(sasParams.sdkVersion.substring(1, 2)) < 3) {
            logger.error("Terminating request as sdkVersion is less than 3");
            terminationReason = lowSdkVersion;
            InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
            sendNoAdResponse(e);
            return;
          } else
            logger.debug("sdk-version : " + sasParams.sdkVersion);
        } catch (StringIndexOutOfBoundsException e2) {
          logger.debug("Invalid sdkversion " + e2.getMessage());
        } catch (NumberFormatException e3) {
          logger.debug("Invalid sdkversion " + e3.getMessage());
        }

      }

      // if sendonlytowhitelist flag is true, check if site id is present
      // in whitelist, else send no ad.
      if(config.getBoolean("sendOnlyToWhitelist") == true) {
        List<String> whitelist = config.getList("whitelist");
        if(null == whitelist || !whitelist.contains(sasParams.siteId)) {
          logger.debug("site id not present in whitelist, so sending no ad response");
          sendNoAdResponse(e);
          return;
        }
      }

      // getting the selected third party site details
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new MatchSegments(logger).matchSegments(sasParams);

      if(matchedSegments == null) {
        sendNoAdResponse(e);
        return;
      }

      // applying channel level filters and per partner ecpm filter
      ChannelSegmentEntity[] rows = Filters.filter(matchedSegments, logger, 0.0, config, adapterConfig);

      //logger.debug("repo: " + channelAdGroupRepository.toString());
      if(rows == null || rows.length == 0) {
        sendNoAdResponse(e);
        logger.debug("No Entities matching the request.");
        return;
      }

      List<ChannelSegment> segments = new ArrayList<ChannelSegment>();

      String advertisers = "";
      String[] advertiserList = null;
      try {
        JSONObject uObject = (JSONObject) jObject.get("uparams");
        if(uObject.get("u-adapter") != null) {
          advertisers = (String) uObject.get("u-adapter");
          advertiserList = advertisers.split(",");
        }
      } catch (JSONException exception) {
        logger.debug("Some thing went wrong in finding adapters for end to end testing");
      }

      Set<String> advertiserSet = new HashSet<String>();

      if(advertiserList != null) {
        for (int i = 0; i < advertiserList.length; i++) {
          advertiserSet.add(advertiserList[i]);
        }
      }

      // For each channel we configure the parameters and make the async
      // request
      // if the async request is successful we add it to segment list else
      // we drop it

      logger.debug("Total channels available for sending requests " + rows.length);

      segments = AsyncRequestMaker.prepareForAsyncRequest(rows, logger, config, rtbConfig, adapterConfig, clientBootstrap,
          rtbClientBootstrap, this, advertiserSet, e, repositoryHelper, jObject, sasParams);

      if(segments.size() == 0) {
        logger.debug("No succesfull configuration of adapter ");
        sendNoAdResponse(e);
        return;
      }

      rankList = Filters.rankAdapters(segments, logger, config);
      rankList = Filters.ensureGuaranteedDelivery(rankList, adapterConfig, logger);

      rankList = AsyncRequestMaker.makeAsyncRequests(rankList, logger, this, e);

      if(logger.isDebugEnabled()) {
        logger.debug("Number of tpans whose request was successfully completed " + rankList.size());
      }
      // if none of the async request succeed, we return "NO_AD"
      if(rankList.size() == 0) {
        logger.debug("No calls");
        sendNoAdResponse(e);
        return;
      }

      // Resetting the rankIndexToProcess for already completed adapters.
      ChannelSegment segment = rankList.get(rankIndexToProcess);
      while (segment.adNetworkInterface.isRequestCompleted()) {
        if(segment.adNetworkInterface.getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
          sendAdResponse(segment.adNetworkInterface, e);
          break;
        }
        rankIndexToProcess++;
        if(rankIndexToProcess >= rankList.size()) {
          sendNoAdResponse(e);
          break;
        }
        segment = rankList.get(rankIndexToProcess);
      }

      if(logger.isDebugEnabled()) {
        logger.debug("retunrd from send Response, ranklist size is " + rankList.size());
      }
    } catch (Exception exception) {
      terminationReason = processingError;
      InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
      sendNoAdResponse(e);
      String exceptionClass = exception.getClass().getSimpleName();
      // incrementing the count of the number of exceptions thrown in the
      // server code
      InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.count);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      logger.error("stack trace is " + sw.toString());
      if(logger.isDebugEnabled()) {
        sendMail(exception.getMessage(), sw.toString());
      }
    }
    if(logger.isDebugEnabled()) {
      if(rankList != null) {
        for (int index = 0; index < rankList.size(); ++index) {
          logger.debug("RankList: ChannelSegment @ " + index + " is " + rankList.get(index).adNetworkInterface.getName());
        }
      } else {
        logger.debug("RankList is empty");
      }
    }
  }

  // changing rollout percentage
  public void changeRollout(MessageEvent e, QueryStringDecoder queryStringDecoder) throws Exception {
    try {
      List<String> rollout = (queryStringDecoder.getParameters().get("percentRollout"));
      percentRollout = Integer.parseInt(rollout.get(0));
    } catch (NumberFormatException ex) {
      logger.error("invalid attempt to change rollout percentage " + ex);
      sendResponse("INVALIDPERCENT", e);
      return;
    }
    InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(percentRollout));
    logger.debug("new roll out percentage is " + percentRollout);
    sendResponse("OK", e);
  }

  public void disableLbStatus(MessageEvent e, String host) {
    if(host != null && host.startsWith("localhost")) {
      sendResponse("OK", e);
      ServerStatusInfo.statusCode = 404;
      ServerStatusInfo.statusString = "NOT_OK";
      logger.debug("asked to shut down the server");
    } else {
      sendResponse("NOT AUTHORIZED", e);
    }
  }

  public void enableLbStatus(MessageEvent e, String host) {
    if(host != null && host.startsWith("localhost")) {
      sendResponse("OK", e);
      ServerStatusInfo.statusCode = 200;
      ServerStatusInfo.statusString = "OK";
      logger.debug("asked to shut down the server");
    } else {
      sendResponse("NOT AUTHORIZED", e);
    }
  }

  // Returns true if request is on the top of the ranklist.
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

  // Returns true if adnetwork is a last index.
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

  // Iterates over the complete rank list and set the new value for
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

  private int getRankIndex(AdNetworkInterface adNetwork) {
    int index = 0;
    for (index = 0; index < rankList.size(); index++) {
      if(rankList.get(index).adNetworkInterface.getImpressionId().equals(adNetwork.getImpressionId())) {
        break;
      }
    }
    return index;
  }

  public void writeLogs() {
    List<ChannelSegment> list = new ArrayList<ChannelSegment>();
    if(null != rankList)
      list.addAll(rankList);
    if(null != rtbSegments)
      list.addAll(rtbSegments);
    if(totalTime > 2000)
      totalTime = 0;
    try {
      if(adResponse == null) {
        Logging.channelLogline(list, null, logger, loggerConfig, sasParams, totalTime, jObject);
        Logging.rrLogging(jObject, null, logger, loggerConfig, sasParams, terminationReason);
        Logging.advertiserLogging(list, logger, loggerConfig);
        Logging.sampledAdvertiserLogging(list, logger, loggerConfig);
      } else {
        Logging.channelLogline(list, adResponse.clickUrl, logger, loggerConfig, sasParams, totalTime, jObject);
        if(rtbResponse == null)
          Logging.rrLogging(jObject, rankList.get(selectedAdIndex), logger, loggerConfig, sasParams, terminationReason);
        else
          Logging.rrLogging(jObject, rtbResponse, logger, loggerConfig, sasParams, terminationReason);
        Logging.advertiserLogging(list, logger, loggerConfig);
        Logging.sampledAdvertiserLogging(list, logger, loggerConfig);

      }
    } catch (JSONException exception) {
      logger.error("Error while writing logs " + exception.getMessage());
      System.out.println("stack trace is ");
      exception.printStackTrace();
      return;
    } catch (TException exception) {
      logger.error("Error while writing logs " + exception.getMessage());
      System.out.println("stack trace is ");
      exception.printStackTrace();
      return;
    }
    logger.debug("done with logging");
  }

  // closing open channels and logging
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
    writeLogs();
  }

  // get host name
  public String getHost(HttpRequest request) {
    List<Map.Entry<String, String>> headers = request.getHeaders();
    String host = null;

    for (int index = 0; index < headers.size(); index++) {
      if(((String) ((Map.Entry<String, String>) (headers.get(index))).getKey()).equalsIgnoreCase("Host")) {
        host = (String) ((Map.Entry<String, String>) (headers.get(index))).getValue();
      }
    }

    if(logger.isDebugEnabled()) {
      logger.debug("host name is " + host);
    }

    if(logger.isDebugEnabled()) {
      logger.debug("Request URI: " + request.getUri());
    }
    return host;
  }

  // send No Ad Response
  @Override
  public void sendNoAdResponse(ChannelEvent event) throws NullPointerException {
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

  // Return true if request contains Iframe Id and is a request from js adcode.
  public boolean isJsAdRequest() {
    if(null == jObject) {
      return false;
    }
    String adCode = sasParams.adcode;
    String rqIframe = sasParams.rqIframe;
    if(adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS")) {
      return true;
    }
    return false;
  }

  // return the response format
  public String getResponseFormat() {
    String responseFormat = "html";
    if(jObject == null || (responseFormat = sasParams.rFormat) == null) {
      return "html";
    }
    if(responseFormat.equalsIgnoreCase("axml")) {
      responseFormat = "xhtml";
    }
    return responseFormat;
  }

  public void sendLbStatus(MessageEvent e) {

    // Initializing loggers for expected rotation format
    if(++rollCount == 20) {
      Logger rrLogger = Logger.getLogger(loggerConfig.getString("rr"));
      if(null != rrLogger) {
        rrLogger.debug("");
      }
      Logger advertiserLogger = Logger.getLogger(loggerConfig.getString("advertiser"));
      if(null != advertiserLogger) {
        advertiserLogger.debug("");
      }
      Logger channelLogger = Logger.getLogger(loggerConfig.getString("channel"));
      if(null != channelLogger) {
        channelLogger.debug("");
      }
      rollCount = 0;
    }
    logger.debug("asked for load balancer status");
    InspectorStats.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
    if(ServerStatusInfo.statusCode != 404) {
      InspectorStats.incrementStatCount("LbStatus", InspectorStrings.successfulRequests);
      sendResponse("OK", e);
      return;
    }
    HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
    response.setContent(ChannelBuffers.copiedBuffer(ServerStatusInfo.statusString, Charset.forName("UTF-8").name()));
    if(e != null) {
      Channel channel = e.getChannel();
      if(channel != null && channel.isWritable()) {
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
      }
    }
  }

  // Called only if the adNetwork have an ad.

  @Override
  public void sendAdResponse(AdNetworkInterface selectedAdNetwork, MessageEvent event) {
    adResponse = selectedAdNetwork.getResponseAd();
    selectedAdIndex = getRankIndex(selectedAdNetwork);
    sendAdResponse(adResponse.response, event);
    InspectorStats.incrementStatCount(selectedAdNetwork.getName(), InspectorStrings.serverImpression);
  }

  // send Ad Response
  public void sendAdResponse(String responseString, MessageEvent event) throws NullPointerException {
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

  // send response to the caller
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

  public String getImpressionId(JSONObject jObject, long adId) {
    String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
    String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
    return (WilburyUUID.setDataCenterId(uuidMachineKey, ChannelServer.dataCenterIdCode)).toString();
  }

  // send Mail if channel server crashes
  public static void sendMail(String errorMessage, String stackTrace) {
    // logger.error("Error in the main thread, so sending mail " +
    // errorMessage);
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", config.getString("smtpServer"));
    Session session = Session.getDefaultInstance(properties);
    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(config.getString("sender")));
      List<String> recipients = config.getList("recipients");
      javax.mail.internet.InternetAddress[] addressTo = new javax.mail.internet.InternetAddress[recipients.size()];

      for (int index = 0; index < recipients.size(); index++) {
        addressTo[index] = new javax.mail.internet.InternetAddress((String) recipients.get(index));
      }

      message.setRecipients(Message.RecipientType.TO, addressTo);
      InetAddress addr = InetAddress.getLocalHost();
      message.setSubject("Channel  Ad Server Crashed on Host " + addr.getHostName());
      message.setText(errorMessage + stackTrace);
      Transport.send(message);
    } catch (MessagingException mex) {
      // logger.info("Error while sending mail");
      mex.printStackTrace();
    } catch (UnknownHostException ex) {
      // logger.debug("could not resolve host inside send mail");
      ex.printStackTrace();
    }

  }

  public void changeConfig(MessageEvent e, JSONObject jObj) {
    if(null == jObj) {
      sendResponse("Incorrect Json", e);
      return;
    }
    logger.debug("Successfully got json for config change");
    try {
      StringBuilder updates = new StringBuilder();
      updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
      Iterator<String> itr = jObj.keys();
      while (itr.hasNext()) {
        String configKey = itr.next().toString();
        if(configKey.startsWith("adapter") && adapterConfig.containsKey(configKey.replace("adapter.", ""))) {
          adapterConfig.setProperty(configKey.replace("adapter.", ""), jObj.getString(configKey));
          updates.append(configKey).append("=").append(adapterConfig.getString(configKey.replace("adapter.", ""))).append("\n");
        }
        if(configKey.startsWith("logger")) {
          loggerConfig.setProperty(configKey.replace("logger.", ""), jObj.getString(configKey));
        }
        if(configKey.startsWith("server") && config.containsKey(configKey.replace("server.", ""))) {
          config.setProperty(configKey.replace("server.", ""), jObj.getString(configKey));
          if(configKey.replace("server.", "").equals("maxconnections")) {
            BootstrapCreation.setMaxConnectionLimit(config.getInt(configKey.replace("server.", "")));
          }
          updates.append(configKey).append("=").append(config.getString(configKey.replace("server.", ""))).append("\n");
        }
        if(configKey.startsWith("log4j")) {
          log4jConfig.setProperty(configKey.replace("log4j.", ""), jObj.getString(configKey));
        }
        if(configKey.startsWith("database")) {
          databaseConfig.setProperty(configKey.replace("database.", ""), jObj.getString(configKey));
        }
      }
      sendResponse(updates.toString(), e);
    } catch (JSONException ex) {
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
      terminationReason = jsonParsingError;
    }
  }

  @Override
  public AdNetworkInterface runRtbSecondPriceAuctionEngine() {

    if(rtbSegments.size() == 0) {
      rtbResponse = null;
      return null;
    } else if(rtbSegments.size() == 1) {
      rtbResponse = rtbSegments.get(0);
      secondBidPrice = bidFloor;
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
      if(price > secondHighestBidPrice)
        secondBidPrice = price;
      else
        secondBidPrice = secondHighestBidPrice;
    } else
      secondBidPrice = rtbSegments.get(1).adNetworkInterface.getBidprice();
    rtbResponse = rtbSegments.get(lowestLatency);
    return rtbSegments.get(lowestLatency).adNetworkInterface;
  }

  @Override
  public double getSecondBidPrice() {
    return secondBidPrice;
  }

  // Check whether all rtb request are completed or not
  public boolean isAllRtbComplete() {
    if(rtbSegments.size() == 0)
      return true;
    for (ChannelSegment channelSegment : rtbSegments) {
      if(!channelSegment.adNetworkInterface.isRequestCompleted())
        return false;
    }
    return true;
  }
}
