package com.inmobi.adserve.channels.server;

import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.thrift.TException;
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

import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

public class HttpRequestHandler extends IdleStateAwareChannelUpstreamHandler {

  public String terminationReason = "NO";
  public JSONObject jObject = null;
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

  /**
   * Invoked when an exception occurs whenever channel throws
   * closedchannelexception increment the totalterminate means channel is closed
   * by party who requested for the ad
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {

    String exceptionString = e.getClass().getSimpleName();
    InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
    InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
    if(logger == null)
      logger = new DebugLogger();
    if(exceptionString.equalsIgnoreCase(ServletHandler.CLOSED_CHANNEL_EXCEPTION)
        || exceptionString.equalsIgnoreCase(ServletHandler.CONNECTION_RESET_PEER)) {
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

      //invalid request
      HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
      response.setContent(ChannelBuffers.copiedBuffer(ServerStatusInfo.statusString, Charset.forName("UTF-8").name()));
      if(e != null) {
        Channel channel = e.getChannel();
        if(channel != null && channel.isWritable()) {
          ChannelFuture future = channel.write(response);
          future.addListener(ChannelFutureListener.CLOSE);
        }
      }
      
    } catch (Exception exception) {
      terminationReason = ServletHandler.processingError;
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
          Logging.rrLogging(responseSender.getRtbResponse(), logger, ServletHandler.loggerConfig,
              responseSender.sasParams, terminationReason);
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
