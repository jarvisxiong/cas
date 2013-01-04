package com.inmobi.adserve.channels.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

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
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.ConfigurationLoader;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class HttpRequestHandler extends IdleStateAwareChannelUpstreamHandler {

  public static final String jsonParsingError = "EJSON";
  private static final String processingError = "ESERVER";
  private static final String missingSiteId = "NOSITE";
  private static final String incompatibleSiteType = "ESITE";
  private static final String lowSdkVersion = "LSDK";
  private static final String CLOSED_CHANNEL_EXCEPTION = "java.nio.channels.ClosedChannelException";
  private static final String CONNECTION_RESET_PEER = "java.io.IOException: Connection reset by peer";
  private static int rollCount = 0;
  private static int percentRollout;
  private double bidFloor;
  public List<ChannelSegment> rtbSegments = new ArrayList<ChannelSegment>();
  public ChannelSegment rtbResponse;
  private String terminationReason = "NO";
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
  public ResponseSender responseSender;

  public String getTerminationReason() {
    return terminationReason;
  }

  public void setTerminationReason(String terminationReason) {
    this.terminationReason = terminationReason;
  }

  public static int getRollCount() {
    return rollCount;
  }

  public static void setRollCount(int rollCount) {
    HttpRequestHandler.rollCount = rollCount;
  }

  public static int getPercentRollout() {
    return percentRollout;
  }

  public static void setPercentRollout(int percentRollout) {
    HttpRequestHandler.percentRollout = percentRollout;
  }

  public void setSelectedAdIndex(int selectedAdIndex) {
    this.selectedAdIndex = selectedAdIndex;
  }

  public HttpRequestHandler() {
    responseSender = new ResponseSender(this, logger, System.currentTimeMillis(), rankList, rtbSegments, adResponse,
        false, sasParams, 0, 0, rankIndexToProcess, false);
  }

  public static void init(ConfigurationLoader config, ClientBootstrap clientBootstrap,
      ClientBootstrap rtbClientBootstrap, RepositoryHelper repositoryHelper) {
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
    if(exceptionString.equalsIgnoreCase(CLOSED_CHANNEL_EXCEPTION)
        || exceptionString.equalsIgnoreCase(CONNECTION_RESET_PEER)) {
      InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
      logger.debug("Channel is terminated " + ctx.getChannel().getId());
    }
    logger.error("Getting netty error in HttpRequestHandler: " + e.getCause());
    if(e.getChannel().isOpen()) {
      responseSender.sendNoAdResponse(e);
    }
    e.getCause().printStackTrace();
  }

  // Invoked when request timeout.
  @Override
  public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) {
    if(e.getChannel().isOpen()) {
      logger.debug("Channel is open in channelIdle handler");
      responseSender.sendNoAdResponse(e);
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

      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

      ServletFactory servletFactory = ServletHandler.servletMap.get(queryStringDecoder.getPath());
      if(servletFactory != null) {
        Servlet servlet = servletFactory.getServlet();
        servlet.handleRequest(this, queryStringDecoder, e, logger);
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
      responseSender = new ResponseSender(this, logger, totalTime, rankList, rtbSegments, adResponse, false, sasParams,
          bidFloor, bidFloor, rankIndexToProcess, false);

      if(random.nextInt(100) >= percentRollout) {
        logger.debug("Request not being served because of limited percentage rollout");
        InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
      }

      if(null == sasParams || null == sasParams.siteId) {
        logger.debug("Terminating request as site id was missing");
        terminationReason = missingSiteId;
        InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
        return;
      }

      if(!sasParams.allowBannerAds || sasParams.siteFloor > 5) {
        logger.debug("Request not being served because of banner not allowed or site floor above threshold");
        responseSender.sendNoAdResponse(e);
        return;
      }
      if(sasParams.siteType != null && !allowedSiteTypes.contains(sasParams.siteType)) {
        logger.error("Terminating request as incompatible content type");
        terminationReason = incompatibleSiteType;
        InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
        return;
      }

      if(sasParams.sdkVersion != null) {
        try {
          if((sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("i") || sasParams.sdkVersion.substring(0, 1)
              .equalsIgnoreCase("a")) && Integer.parseInt(sasParams.sdkVersion.substring(1, 2)) < 3) {
            logger.error("Terminating request as sdkVersion is less than 3");
            terminationReason = lowSdkVersion;
            InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
            responseSender.sendNoAdResponse(e);
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
          responseSender.sendNoAdResponse(e);
          return;
        }
      }

      // getting the selected third party site details
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new MatchSegments(logger)
          .matchSegments(sasParams);

      if(matchedSegments == null) {
        responseSender.sendNoAdResponse(e);
        return;
      }

      // applying channel level filters and per partner ecpm filter
      ChannelSegmentEntity[] rows = Filters.filter(matchedSegments, logger, 0.0, config, adapterConfig);

      //logger.debug("repo: " + channelAdGroupRepository.toString());
      if(rows == null || rows.length == 0) {
        responseSender.sendNoAdResponse(e);
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

      segments = AsyncRequestMaker.prepareForAsyncRequest(rows, logger, config, rtbConfig, adapterConfig,
          clientBootstrap, rtbClientBootstrap, responseSender, advertiserSet, e, repositoryHelper, jObject, sasParams);

      if(segments.size() == 0) {
        logger.debug("No succesfull configuration of adapter ");
        responseSender.sendNoAdResponse(e);
        return;
      }

      rankList = Filters.rankAdapters(segments, logger, config);
      rankList = Filters.ensureGuaranteedDelivery(rankList, adapterConfig, logger);

      rankList = AsyncRequestMaker.makeAsyncRequests(rankList, logger, responseSender, e);

      if(logger.isDebugEnabled()) {
        logger.debug("Number of tpans whose request was successfully completed " + rankList.size());
      }
      // if none of the async request succeed, we return "NO_AD"
      if(rankList.size() == 0) {
        logger.debug("No calls");
        responseSender.sendNoAdResponse(e);
        return;
      }

      // Resetting the rankIndexToProcess for already completed adapters.
      rankIndexToProcess = responseSender.getRankIndexToProcess();
      ChannelSegment segment = rankList.get(rankIndexToProcess);
      while (segment.adNetworkInterface.isRequestCompleted()) {
        if(segment.adNetworkInterface.getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
          responseSender.sendAdResponse(segment.adNetworkInterface, e);
          break;
        }
        rankIndexToProcess++;
        if(rankIndexToProcess >= rankList.size()) {
          responseSender.sendNoAdResponse(e);
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
      responseSender.sendNoAdResponse(e);
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
          logger.debug("RankList: ChannelSegment @ " + index + " is "
              + rankList.get(index).adNetworkInterface.getName());
        }
      } else {
        logger.debug("RankList is empty");
      }
    }
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
      responseSender.sendResponse("OK", e);
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
      responseSender.sendResponse("Incorrect Json", e);
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
          updates.append(configKey).append("=").append(adapterConfig.getString(configKey.replace("adapter.", "")))
              .append("\n");
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
      responseSender.sendResponse(updates.toString(), e);
    } catch (JSONException ex) {
      logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
      terminationReason = jsonParsingError;
    }
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
