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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
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
import org.apache.commons.lang.StringUtils;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SlotSizeMapping;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.repository.ChannelAdGroupRepository;
import com.inmobi.adserve.channels.repository.ChannelFeedbackRepository;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.ChannelSegmentFeedbackRepository;
import com.inmobi.adserve.channels.server.ClickUrlMaker.TrackingUrls;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.phoenix.batteries.util.WilburyUUID;

public class HttpRequestHandler extends HttpRequestHandlerBase {

  public class ChannelSegment {
    public ChannelSegmentEntity channelSegmentEntity;
    public AdNetworkInterface adNetworkInterface;
    public ChannelEntity channelEntity;
    public ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity;
    public int lowerPriorityRange;
    public int higherPriorityRange;

    public ChannelSegment(ChannelSegmentEntity channelSegmentEntity, AdNetworkInterface adNetworkInterface, ChannelEntity channelEntity,
        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity) {
      this.channelSegmentEntity = channelSegmentEntity;
      this.adNetworkInterface = adNetworkInterface;
      this.channelEntity = channelEntity;
      this.channelSegmentFeedbackEntity = channelSegmentFeedbackEntity;
    }
  }

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
      + " body {margin: 0; overflow: hidden; background-color: transparent}" + " </style></head><body class=\"nofill\"><!-- NO FILL -->"
      + "<script type=\"text/javascript\" charset=\"utf-8\">" + "parent.postMessage('{\"topic\":\"nfr\",\"container\" : \"%s\"}', '*');</script></body></html>";
  private static int rollCount = 0;
  private static int percentRollout;
  private long totalTime;
  private List<ChannelSegment> rankList = null;
  private static ChannelAdGroupRepository channelAdGroupRepository;
  private static Configuration config;
  private static Configuration rtbConfig;
  private static Configuration adapterConfig;
  private static Configuration loggerConfig;
  private static Configuration log4jConfig;
  private static Configuration databaseConfig;
  private static ClientBootstrap clientBootstrap;
  private static ClientBootstrap rtbClientBootstrap;
  private static ChannelRepository channelRepository;
  private static ChannelFeedbackRepository channelFeedbackRepository;
  private static ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository;
  private SASRequestParameters sasParams = new SASRequestParameters();
  private JSONObject jObject = null;
  private static InspectorStats inspectorStat;
  private static ChannelSegmentCache cache;
  private static Random random = new Random();
  private final int adIndex[] = new int[1];
  private static List<String> allowedSiteTypes;
  private int rankIndexToProcess = 0;
  private int selectedAdIndex = 0;
  static DebugLogger logger = new DebugLogger();
  public ThirdPartyAdResponse adResponse = null;
  private static final String CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
  private static final String CONNECTION_RESET_PEER = "java.io.IOException: Connection reset by peer";

  public static void init(ConfigurationLoader config, ChannelAdGroupRepository channelAdGroupRepo, InspectorStats inspectorStat,
      ClientBootstrap clientBootstrap, ClientBootstrap rtbClientBootstrap, ChannelRepository channelRepository, ChannelSegmentCache cache,
      ChannelFeedbackRepository channelFeedbackRepository, ChannelSegmentFeedbackRepository channelSegmentFeedbackRepository) {
    HttpRequestHandler.rtbConfig = config.rtbConfiguration();
    HttpRequestHandler.loggerConfig = config.loggerConfiguration();
    HttpRequestHandler.config = config.serverConfiguration();
    HttpRequestHandler.adapterConfig = config.adapterConfiguration();
    HttpRequestHandler.log4jConfig = config.log4jConfiguration();
    HttpRequestHandler.databaseConfig = config.databaseConfiguration();
    HttpRequestHandler.channelAdGroupRepository = channelAdGroupRepo;
    HttpRequestHandler.inspectorStat = inspectorStat;
    HttpRequestHandler.clientBootstrap = clientBootstrap;
    HttpRequestHandler.rtbClientBootstrap = rtbClientBootstrap;
    HttpRequestHandler.channelRepository = channelRepository;
    HttpRequestHandler.channelFeedbackRepository = channelFeedbackRepository;
    HttpRequestHandler.channelSegmentFeedbackRepository = channelSegmentFeedbackRepository;
    allowedSiteTypes = HttpRequestHandler.config.getList("allowedSiteTypes");
    percentRollout = HttpRequestHandler.config.getInt("percentRollout", 100);
    HttpRequestHandler.cache = cache;
    inspectorStat.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(percentRollout));
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
    String exceptionClass = e.getClass().getName();
    inspectorStat.incrementStatCount(InspectorStrings.channelException, e.getCause().toString().split(":", 2)[0]);
    if(logger == null)
      logger = new DebugLogger();
    if(e.getCause().toString().equalsIgnoreCase(CLOSED_CHANNEL_EXCEPTION) || e.getCause().toString().equalsIgnoreCase(CONNECTION_RESET_PEER)) {
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
      bidFloor = rtbConfig.getDouble("bidFloor",0.0);
      logger.debug("bidFloor is " + bidFloor);
      totalTime = System.currentTimeMillis();
      HttpRequest request = (HttpRequest) e.getMessage();

      String host = getHost(request);
      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

      if(queryStringDecoder.getPath().equalsIgnoreCase("/stat")) {
        sendResponse(inspectorStat.getStats(BootstrapCreation.getMaxConnections(), BootstrapCreation.getDroppedConnections()), e);
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
        extractParams(params, "update");
        changeConfig(e, jObject);
        return;
      }

      inspectorStat.incrementStatCount(InspectorStrings.totalRequests);
      Map<String, List<String>> params = queryStringDecoder.getParameters();
      extractParams(params);
      sasParams = parseRequestParameters(jObject);

      if(random.nextInt(100) >= percentRollout) {
        logger.debug("Request not being served because of limited percentage rollout");
        InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
        sendNoAdResponse(e);
      }

      if(null == sasParams || null == sasParams.siteId) {
        logger.debug("Terminating request as site id was missing");
        terminationReason = missingSiteId;
        inspectorStat.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
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
        inspectorStat.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
        sendNoAdResponse(e);
        return;
      }

      if(sasParams.sdkVersion != null) {
        try {
          if((sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("i") || sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("a"))
              && Integer.parseInt(sasParams.sdkVersion.substring(1, 2)) < 3) {
            logger.error("Terminating request as sdkVersion is less than 3");
            terminationReason = lowSdkVersion;
            inspectorStat.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
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
        List whitelist = config.getList("whitelist");
        if(null == whitelist || !whitelist.contains(sasParams.siteId)) {
          logger.debug("site id not present in whitelist, so sending no ad response");
          sendNoAdResponse(e);
          return;
        }
      }

      // getting the selected third party site details
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = matchSegments(jObject);

      if(matchedSegments == null) {
        sendNoAdResponse(e);
        return;
      }

      // applying channel level filters and per partner ecpm filter
      ChannelSegmentEntity[] rows = convertToSegmentsArray(Filters
          .partnerSegmentCountFilter(Filters.impressionBurnFilter(matchedSegments, logger), 0.0, logger));

      // applying request level ecpm filter
      rows = Filters.segmentsPerRequestFilter(matchedSegments, rows, logger);

      logger.debug("repo: " + channelAdGroupRepository.toString());
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
        if(logger.isDebugEnabled()) {
          logger.debug("Some thing went wrong in finding adapters for end to end testing");
        }
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

      if(logger.isDebugEnabled()) {
        logger.debug("Total channels available for sending requests " + rows.length);
      }

      for (ChannelSegmentEntity row : rows) {
        boolean isRtbEnabled = false;
        isRtbEnabled = rtbConfig.getBoolean("isRtbEnabled",false);
        logger.debug("isRtbEnabled is " + isRtbEnabled);

        AdNetworkInterface network = SegmentFactory.getChannel(row.getId(), row.getChannelId(), this.adapterConfig, clientBootstrap, rtbClientBootstrap, this,
            e, advertiserSet, logger, isRtbEnabled);
        if(null == network) {
          if(logger.isDebugEnabled()) {
            logger.debug("No adapter found for adGroup: " + row.getAdgroupId());
          }
          continue;
        }
        if(logger.isDebugEnabled()) {
          logger.debug("adapter found for adGroup: " + row.getAdgroupId() + " advertiserid is " + row.getId());
        }

        if(null == channelRepository.query(row.getChannelId())) {
          logger.debug("No channel entity found for channel id: " + row.getChannelId());
          continue;
        }
        InspectorStats.initializeNetworkStats(network.getName());

        String clickUrl = null;
        String beaconUrl = null;
        sasParams.impressionId = getImpressionId(jObject, row.getIncId());
        sasParams.adIncId = row.getIncId();
        sasParams.segmentCategories = row.getTags();
        if(logger.isDebugEnabled()) {
          logger.debug("impression id is " + sasParams.impressionId);
        }

        if((network.isClickUrlRequired() || network.isBeaconUrlRequired()) && null != sasParams.impressionId) {
          ClickUrlMaker clickUrlMaker = new ClickUrlMaker(config, jObject, sasParams, logger);
          TrackingUrls trackingUrls = clickUrlMaker.getClickUrl(row.getPricingModel());
          clickUrl = trackingUrls.getClickUrl();
          beaconUrl = trackingUrls.getBeaconUrl();
          if(logger.isDebugEnabled()) {
            logger.debug("click url formed is " + clickUrl);
          }
          logger.debug("beacon url : " + beaconUrl);
        }

        if(logger.isDebugEnabled()) {
          logger.debug("Sending request to Channel of Id " + row.getId());
          logger.debug("external site key is " + row.getExternalSiteKey());
        }
        inspectorStat.incrementStatCount(network.getName(), InspectorStrings.totalInvocations);
        if(network.configureParameters(sasParams, row.getExternalSiteKey(), clickUrl, beaconUrl)) {
          inspectorStat.incrementStatCount(network.getName(), InspectorStrings.successfulConfigure);
          if(network.makeAsyncRequest()) {
            if(logger.isDebugEnabled()) {
              logger.debug("Successfully sent request to channel of  advertiser id " + row.getId() + "and channel id " + row.getChannelId());
            }
            ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = channelSegmentFeedbackRepository.query(row.getAdgroupId());
            if(null == channelSegmentFeedbackEntity)
              channelSegmentFeedbackEntity = new ChannelSegmentFeedbackEntity(row.getId(), row.getAdgroupId(), config.getDouble("default.ecpm"),
                  config.getDouble("default.fillratio"));
            if(network.isRtbPartner()) {
              rtbSegments.add(new ChannelSegment(row, network, channelRepository.query(row.getChannelId()), channelSegmentFeedbackEntity));
              logger.debug(network.getName() + " is a rtb partner so adding this network to rtb ranklist");
            }
              else
              segments.add(new ChannelSegment(row, network, channelRepository.query(row.getChannelId()), channelSegmentFeedbackEntity));
          }
        }
      }
      if(logger.isDebugEnabled()) {
        logger.debug("Number of tpans whose request was successfully completed " + segments.size());
      }
      // if none of the async request succeed, we return "NO_AD"
      if(segments.size() <= 0) {
        sendNoAdResponse(e);
        return;
      }
      rankList = Filters.rankAdapters(segments, logger);

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
      inspectorStat.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
      sendNoAdResponse(e);
      String exceptionClass = exception.getClass().getName();
      // incrementing the count of the number of exceptions thrown in the
      // server code
      inspectorStat.incrementStatCount(exceptionClass.substring(exceptionClass.lastIndexOf('.') + 1, exceptionClass.length()), InspectorStrings.count);
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      exception.printStackTrace(pw);
      logger.error("stack trace is " + sw.toString());
      if(logger.isDebugEnabled()) {
        sendMail(exception.getMessage(), sw.toString());
      }
    } finally {
      // cleanUp();
      // e.getChannel().close();
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

  private ChannelSegmentEntity[] convertToSegmentsArray(HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments) {
    ArrayList<ChannelSegmentEntity> rows = new ArrayList<ChannelSegmentEntity>();
    for (String advertiserId : matchedSegments.keySet()) {
      for (String adgroupId : matchedSegments.get(advertiserId).keySet()) {
        rows.add(matchedSegments.get(advertiserId).get(adgroupId));
        if(logger.isDebugEnabled())
          logger.debug("ChannelSegmentEntity Added to array for advertiserid : " + advertiserId + " and adgroupid " + adgroupId);
      }
    }
    return (ChannelSegmentEntity[]) rows.toArray(new ChannelSegmentEntity[0]);
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
    inspectorStat.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(percentRollout));
    logger.debug("new roll out percentage is " + percentRollout);
    sendResponse("OK", e);
  }

  public void extractParams(Map<String, List<String>> params) throws Exception {
    extractParams(params, "args");
  }

  // Extracting params.
  public void extractParams(Map<String, List<String>> params, String jsonKey) throws Exception {
    if(!params.isEmpty()) {
      for (Entry<String, List<String>> p : params.entrySet()) {
        String key = p.getKey();
        List<String> vals = p.getValue();
        for (String val : vals) {
          if(key.equalsIgnoreCase(jsonKey)) {
            try {
              jObject = new JSONObject(val);
            } catch (JSONException ex) {
              jObject = new JSONObject();
              logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
              terminationReason = jsonParsingError;
              inspectorStat.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            }
          }
        }
      }
    }
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
          sendAdResponse(adResponse.response, event);
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
      if(rankList.get(index).adNetworkInterface.getName().equals(adNetwork.getName())) {
        break;
      }
    }
    return index;
  }

  public void writeLogs() {
    List<ChannelSegment> list = new ArrayList<HttpRequestHandler.ChannelSegment>();
    if (null != rankList)
      list.addAll(rankList);
    if (null != rtbSegments)
      list.addAll(rtbSegments);
 // TODO: fix this.
    if (totalTime > 2000)
      totalTime = 0;
    try {
      if(adResponse == null) {
        Logging.channelLogline(list, null, logger, loggerConfig, inspectorStat, sasParams, totalTime, jObject);
        Logging.rrLogging(jObject, null, logger, loggerConfig, sasParams, terminationReason);
        Logging.advertiserLogging(list, logger, loggerConfig);
        Logging.sampledAdvertiserLogging(list, logger, loggerConfig);
      } else {
        Logging.channelLogline(list, adResponse.clickUrl, logger, loggerConfig, inspectorStat, sasParams, totalTime, jObject);
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
          logger.debug("Error in closing channel for index: " + index + " Name: " + rankList.get(index).adNetworkInterface.getName() + " Exception: "
              + exception.getLocalizedMessage());
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
    List headers = request.getHeaders();
    String host = null;

    for (int index = 0; index < headers.size(); index++) {
      if(((String) ((Map.Entry) (headers.get(index))).getKey()).equalsIgnoreCase("Host")) {
        host = (String) ((Map.Entry) (headers.get(index))).getValue();
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
    inspectorStat.incrementStatCount(InspectorStrings.totalNoFills);

    if(getResponseFormat().equals("xhtml")) {
      sendResponse(noAdXhtml, event);
    } else if(isJsAdRequest()) {
      sendResponse(String.format(noAdJsAdcode, stringify(jObject, "rq-iframe")), event);
    } else {
      sendResponse(noAdHtml, event);
    }
  }

  // Return true if request contains Iframe Id and is a request from js adcode.
  public boolean isJsAdRequest() {
    if(null == jObject) {
      return false;
    }
    String adCode = stringify(jObject, "adcode");
    String rqIframe = stringify(jObject, "rq-iframe");
    if(adCode != null && rqIframe != null && adCode.equalsIgnoreCase("JS")) {
      return true;
    }
    return false;
  }

  // return the response format
  public String getResponseFormat() {
    String responseFormat = "html";
    if(jObject == null || (responseFormat = stringify(jObject, "r-format")) == null) {
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
    inspectorStat.incrementStatCount("LbStatus", InspectorStrings.totalRequests);
    if(ServerStatusInfo.statusCode != 404) {
      inspectorStat.incrementStatCount("LbStatus", InspectorStrings.successfulRequests);
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
    inspectorStat.incrementStatCount(selectedAdNetwork.getName(), InspectorStrings.serverImpression);
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
        inspectorStat.incrementStatCount(InspectorStrings.totalFills);
      } else {
        logger.error("invalid slot, so not returning response, even though we got an ad");
        responseString = noAdXhtml;
        inspectorStat.incrementStatCount(InspectorStrings.totalNoFills);
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

  // parse the parameters received from nginx
  private SASRequestParameters parseRequestParameters(JSONObject jObject) {
    SASRequestParameters params = new SASRequestParameters();
    logger.debug("inside parameter parser");
    if(null == jObject) {
      logger.debug("Returning null as jObject is null.");
      return null;
    }
    params.allParametersJson = jObject.toString();
    params.remoteHostIp = stringify(jObject, "w-s-carrier");
    params.userAgent = stringify(jObject, "rq-x-inmobi-phone-useragent");
    if(null == params.userAgent) {
      params.userAgent = stringify(jObject, "rq-h-user-agent");
    }
    params.locSrc = stringify(jObject, "loc-src");
    params.latLong = stringify(jObject, "latlong");
    params.siteId = stringify(jObject, "rq-mk-siteid");
    params.source = stringify(jObject, "source");
    params.country = parseArray(jObject, "carrier", 2);
    params.area = parseArray(jObject, "carrier", 4);
    params.slot = stringify(jObject, "slot-served");
    params.sdkVersion = stringify(jObject, "sdk-version");
    params.siteType = stringify(jObject, "site-type");
    params.adcode = stringify(jObject, "adcode");
    params.platformOsId = jObject.optInt("os-id", -1);
    if(params.siteType != null) {
      params.siteType = params.siteType.toUpperCase();
    }
    params.categories = getCategory(jObject);
    params.allowBannerAds = jObject.opt("site-allowBanner") == null ? true : (Boolean) (jObject.opt("site-allowBanner"));
    params.siteFloor = jObject.opt("site-floor") == null ? 0.0 : Double.parseDouble(jObject.opt("site-floor").toString());
    if(logger.isDebugEnabled()) {
      logger.debug("country obtained is " + params.country);
      logger.debug("site floor is " + params.siteFloor);
      logger.debug("osId is " + params.platformOsId);
    }
    params = getUserParams(params, jObject);
    try {
      JSONArray siteInfo = jObject.getJSONArray("site");
      if(siteInfo != null && siteInfo.length() > 0) {
        params.siteIncId = siteInfo.getLong(0);
      }
    } catch (JSONException exception) {
      logger.debug("site object not found in request");
      params.siteIncId = 0;
    }
    if(null == params.uid || params.uid.isEmpty()) {
      params.uid = stringify(jObject, "u-id");
    }
    logger.debug("successfully parsed params");
    return params;
  }

  public String parseArray(JSONObject jObject, String param, int index) {
    try {
      JSONArray jArray = jObject.getJSONArray(param);
      return (jArray.getString(index));
    } catch (JSONException e) {
      return null;
    } catch (NullPointerException e) {
      return null;
    }
  }

  public long[] getCategory(JSONObject jObject) {
    try {
      JSONArray categories = jObject.getJSONArray("category");
      long[] category = new long[categories.length()];
      for (int index = 0; index < categories.length(); index++) {
        category[index] = categories.getLong(index);
      }
      return category;
    } catch (JSONException e) {
      logger.error("error while reading category array");
      return null;
    }
  }

  public String getImpressionId(JSONObject jObject, long adId) {
    String uuidIntKey = (WilburyUUID.setIntKey(WilburyUUID.getUUID().toString(), (int) adId)).toString();
    String uuidMachineKey = (WilburyUUID.setMachineId(uuidIntKey, ChannelServer.hostIdCode)).toString();
    return (WilburyUUID.setDataCenterId(uuidMachineKey, ChannelServer.dataCenterIdCode)).toString();
  }

  // convert the json values to string values
  public String stringify(JSONObject jObject, String field) throws NullPointerException {
    String fieldValue = "";
    try {
      fieldValue = (String) jObject.get(field);
    } catch (JSONException e) {
      return null;
    }
    logger.debug("Retrived from json " + field + " = " + fieldValue);
    return fieldValue;
  }

  // Get user specific params
  public SASRequestParameters getUserParams(SASRequestParameters parameter, JSONObject jObject) {
    logger.debug("inside parsing user params");
    try {
      JSONObject userMap = (JSONObject) jObject.get("uparams");
      parameter.age = stringify(userMap, "u-age");
      parameter.gender = stringify(userMap, "u-gender");
      parameter.uid = stringify(userMap, "u-id");
      parameter.postalCode = stringify(userMap, "u-postalcode");
      if(!StringUtils.isEmpty(parameter.postalCode))
        parameter.postalCode = parameter.postalCode.replaceAll(" ", "");
      parameter.userLocation = stringify(userMap, "u-location");
      parameter.genderOrig = stringify(userMap, "u-gender-orig");
      if(logger.isDebugEnabled()) {
        logger.debug("uid is " + parameter.uid + ",postalCode is " + parameter.postalCode + ",gender is " + parameter.gender);
        logger.debug("age is " + parameter.age + ",location is " + parameter.userLocation + ",genderorig is " + parameter.genderOrig);
      }
    } catch (JSONException exception) {
      parameter.age = null;
      parameter.gender = null;
      parameter.uid = null;
      parameter.postalCode = null;
      parameter.userLocation = null;
      parameter.genderOrig = null;
      logger.error("uparams missing in the request");
    } catch (NullPointerException exception) {
      parameter.age = null;
      parameter.gender = null;
      parameter.uid = null;
      parameter.postalCode = null;
      parameter.userLocation = null;
      parameter.genderOrig = null;
      logger.error("uparams missing in the request");
    }
    return parameter;
  }
  
  // select channel segment based on specified rules
  private HashMap<String, HashMap<String, ChannelSegmentEntity>> matchSegments(JSONObject args) {
    MatchSegments segmentMatcher = new MatchSegments(logger);
    String slotStr = stringify(args, "slot-served");
    String countryStr = parseArray(args, "carrier", 1);
    String platformStr = parseArray(args, "handset", 5);
    int osId = jObject.optInt("os-id", -1);
    String sourceStr = stringify(args, "source");
    String siteRatingStr = stringify(args, "site-type");
    Integer targetingPlatform = (sourceStr == null || sourceStr.equalsIgnoreCase("wap")) ? 2 : 1 /* app */;
    Integer siteRating = -1;
    if(null == siteRatingStr) {
      return null;
    }
    if(siteRatingStr.equalsIgnoreCase("performance")) {
      siteRating = 0;
    } else if(siteRatingStr.equalsIgnoreCase("mature")) {
      siteRating = 1;
    } else if(siteRatingStr.equalsIgnoreCase("family_safe")) {
      siteRating = 2;
    }
    if(slotStr == null || (platformStr == null && osId == -1) || Arrays.equals(sasParams.categories, null)) {
      return null;
    }
    try {
      if(logger.isDebugEnabled()) {
        logger.debug("Request# slot: " + slotStr + " country: " + countryStr + " categories: " + sasParams.categories + " platform: " + platformStr
            + " targetingPlatform: " + targetingPlatform + " siteRating: " + siteRating + " osId" + osId);
      }
      long slot = Long.parseLong(slotStr);
      long platform;
      if(osId == -1)
        platform = Long.parseLong(platformStr);
      else
        platform = -1;
      long country = -1;
      if(countryStr != null) {
        country = Long.parseLong(countryStr);
      }
      return (segmentMatcher.matchSegments(logger, slot, sasParams.categories, country, targetingPlatform, siteRating, platform, osId));
    } catch (NumberFormatException exception) {
      logger.error("Error parsing required arguments " + exception.getMessage());
      return null;
    }
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
      List recipients = config.getList("recipients");
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
      Iterator itr = jObj.keys();
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
      } else if(rtbSegments.get(i).adNetworkInterface.getLatency() < rtbSegments.get(lowestLatency).adNetworkInterface.getLatency())
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
      if(channelSegment.adNetworkInterface.isRequestCompleted());
      else
        return false;
    }
    return true;
  }
}
