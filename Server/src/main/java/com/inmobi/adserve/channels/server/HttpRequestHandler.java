package com.inmobi.adserve.channels.server;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

import org.apache.thrift.TException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.json.JSONException;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.ThirdPartyAdResponse.ResponseStatus;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
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
  public String terminationReason = "NO";
  
  private SASRequestParameters sasParams = new SASRequestParameters();
  private JSONObject jObject = null;
  private static Random random = new Random();
  public DebugLogger logger = null;
  public ResponseSender responseSender;

  public String getTerminationReason() {
    return terminationReason;
  }

  public void setTerminationReason(String terminationReason) {
    this.terminationReason = terminationReason;
  }


  public HttpRequestHandler() {
    logger = new DebugLogger();
    responseSender = new ResponseSender(this, logger);
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

      HttpRequest request = (HttpRequest) e.getMessage();

      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
      logger.debug(queryStringDecoder.getPath());

      ServletFactory servletFactory = ServletHandler.servletMap.get(queryStringDecoder.getPath());
      if(servletFactory != null) {
        logger.debug("Got the servlet ");
        Servlet servlet = servletFactory.getServlet();
        logger.debug(servlet.getName());
        servlet.handleRequest(this, queryStringDecoder, e, logger);
        return;
      }

      logger.debug("No servlet");

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
      responseSender.sasParams = RequestParser.parseRequestParameters(jObject, logger);

      if(random.nextInt(100) >= ServletHandler.percentRollout) {
        logger.debug("Request not being served because of limited percentage rollout");
        InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
      }

      if(null == responseSender.sasParams || null == responseSender.sasParams.siteId) {
        logger.debug("Terminating request as site id was missing");
        terminationReason = missingSiteId;
        InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
        return;
      }

      if(!responseSender.sasParams.allowBannerAds || responseSender.sasParams.siteFloor > 5) {
        logger.debug("Request not being served because of banner not allowed or site floor above threshold");
        responseSender.sendNoAdResponse(e);
        return;
      }
      if(responseSender.sasParams.siteType != null && !ServletHandler.allowedSiteTypes.contains(responseSender.sasParams.siteType)) {
        logger.error("Terminating request as incompatible content type");
        terminationReason = incompatibleSiteType;
        InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
        responseSender.sendNoAdResponse(e);
        return;
      }

      if(responseSender.sasParams.sdkVersion != null) {
        try {
          if((responseSender.sasParams.sdkVersion.substring(0, 1).equalsIgnoreCase("i") || responseSender.sasParams.sdkVersion.substring(0, 1)
              .equalsIgnoreCase("a")) && Integer.parseInt(responseSender.sasParams.sdkVersion.substring(1, 2)) < 3) {
            logger.error("Terminating request as sdkVersion is less than 3");
            terminationReason = lowSdkVersion;
            InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
            responseSender.sendNoAdResponse(e);
            return;
          } else
            logger.debug("sdk-version : " + responseSender.sasParams.sdkVersion);
        } catch (StringIndexOutOfBoundsException e2) {
          logger.debug("Invalid sdkversion " + e2.getMessage());
        } catch (NumberFormatException e3) {
          logger.debug("Invalid sdkversion " + e3.getMessage());
        }

      }

      // if sendonlytowhitelist flag is true, check if site id is present
      // in whitelist, else send no ad.
      if(ServletHandler.config.getBoolean("sendOnlyToWhitelist") == true) {
        List<String> whitelist = ServletHandler.config.getList("whitelist");
        if(null == whitelist || !whitelist.contains(responseSender.sasParams.siteId)) {
          logger.debug("site id not present in whitelist, so sending no ad response");
          responseSender.sendNoAdResponse(e);
          return;
        }
      }

      // getting the selected third party site details
      HashMap<String, HashMap<String, ChannelSegmentEntity>> matchedSegments = new MatchSegments(logger)
          .matchSegments(responseSender.sasParams);

      if(matchedSegments == null) {
        responseSender.sendNoAdResponse(e);
        return;
      }

      // applying channel level filters and per partner ecpm filter
      ChannelSegmentEntity[] rows = Filters.filter(matchedSegments, logger, 0.0, ServletHandler.config,
          ServletHandler.adapterConfig);

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

      segments = AsyncRequestMaker.prepareForAsyncRequest(rows, logger, ServletHandler.config,
          ServletHandler.rtbConfig, ServletHandler.adapterConfig, responseSender,
          advertiserSet, e, ServletHandler.repositoryHelper, jObject, responseSender.sasParams);

      if(segments.size() == 0) {
        logger.debug("No succesfull configuration of adapter ");
        responseSender.sendNoAdResponse(e);
        return;
      }

      List<ChannelSegment> tempRankList;
      tempRankList = Filters.rankAdapters(segments, logger, ServletHandler.config);
      tempRankList = Filters.ensureGuaranteedDelivery(tempRankList, ServletHandler.adapterConfig, logger);

      tempRankList = AsyncRequestMaker.makeAsyncRequests(tempRankList, logger, responseSender, e);

      responseSender.setRankList(tempRankList);

      if(logger.isDebugEnabled()) {
        logger.debug("Number of tpans whose request was successfully completed " + responseSender.getRankList().size());
      }
      // if none of the async request succeed, we return "NO_AD"
      if(responseSender.getRankList().size() == 0) {
        logger.debug("No calls");
        responseSender.sendNoAdResponse(e);
        return;
      }

      // Resetting the rankIndexToProcess for already completed adapters.
      int rankIndexToProcess = responseSender.getRankIndexToProcess();
      ChannelSegment segment = responseSender.getRankList().get(rankIndexToProcess);
      while (segment.adNetworkInterface.isRequestCompleted()) {
        if(segment.adNetworkInterface.getResponseAd().responseStatus == ResponseStatus.SUCCESS) {
          responseSender.sendAdResponse(segment.adNetworkInterface, e);
          break;
        }
        rankIndexToProcess++;
        if(rankIndexToProcess >= responseSender.getRankList().size()) {
          responseSender.sendNoAdResponse(e);
          break;
        }
        segment = responseSender.getRankList().get(rankIndexToProcess);
      }
      responseSender.setRankIndexToProcess(rankIndexToProcess);
      if(logger.isDebugEnabled()) {
        logger.debug("retunrd from send Response, ranklist size is " + responseSender.getRankList().size());
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
      if(responseSender.getRankList() != null) {
        for (int index = 0; index < responseSender.getRankList().size(); ++index) {
          logger.debug("RankList: ChannelSegment @ " + index + " is "
              + responseSender.getRankList().get(index).adNetworkInterface.getName());
        }
      } else {
        logger.debug("RankList is empty");
      }
    }
  }

  
  public void writeLogs(ResponseSender responseSender, DebugLogger logger) {
    List<ChannelSegment> list = new ArrayList<ChannelSegment>();
    if(null != responseSender.getRankList())
      list.addAll(responseSender.getRankList());
    if(null != responseSender.getRtbSegments())
      list.addAll(responseSender.getRtbSegments());
    long totalTime = responseSender.getTotalTime();
    if(totalTime > 2000)
      totalTime = 0;
    try {
      if(responseSender.getAdResponse() == null) {
        Logging.channelLogline(list, null, logger, ServletHandler.loggerConfig, responseSender.sasParams, totalTime);
        Logging.rrLogging(null, logger, ServletHandler.loggerConfig, responseSender.sasParams, terminationReason);
        Logging.advertiserLogging(list, logger, ServletHandler.loggerConfig);
        Logging.sampledAdvertiserLogging(list, logger, ServletHandler.loggerConfig);
      } else {
        Logging.channelLogline(list, responseSender.getAdResponse().clickUrl, logger, ServletHandler.loggerConfig,
            responseSender.sasParams, totalTime);
        if(responseSender.getRtbResponse() == null)
          Logging.rrLogging(responseSender.getRankList().get(responseSender.getSelectedAdIndex()), logger,
              ServletHandler.loggerConfig, responseSender.sasParams, terminationReason);
        else
          Logging.rrLogging(responseSender.getRtbResponse(), logger, ServletHandler.loggerConfig, responseSender.sasParams, terminationReason);
        Logging.advertiserLogging(list, logger, ServletHandler.loggerConfig);
        Logging.sampledAdvertiserLogging(list, logger, ServletHandler.loggerConfig);

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
  
  
  // send Mail if channel server crashes
  public static void sendMail(String errorMessage, String stackTrace) {
    // logger.error("Error in the main thread, so sending mail " +
    // errorMessage);
    Properties properties = System.getProperties();
    properties.setProperty("mail.smtp.host", ServletHandler.config.getString("smtpServer"));
    Session session = Session.getDefaultInstance(properties);
    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(ServletHandler.config.getString("sender")));
      List<String> recipients = ServletHandler.config.getList("recipients");
      javax.mail.internet.InternetAddress[] addressTo = new javax.mail.internet.InternetAddress[recipients.size()];

      for (int index = 0; index < recipients.size(); index++) {
        addressTo[index] = new javax.mail.internet.InternetAddress((String) recipients.get(index));
      }

      message.setRecipients(Message.RecipientType.TO, addressTo);
      InetAddress addr = InetAddress.getLocalHost();
      message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
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

}