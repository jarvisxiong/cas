package com.inmobi.adserve.channels.server;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.*;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.thrift.TException;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


public class HttpRequestHandler extends IdleStateAwareChannelUpstreamHandler {

    private static final Logger LOG               = LoggerFactory.getLogger(HttpRequestHandler.class);

    public String               terminationReason = "NO";
    public JSONObject           jObject           = null;
    public AdPoolRequest        tObject;
    public ResponseSender       responseSender;
    private ChannelGroup        allChannels;

    private Provider<Marker>    traceMarkerProvider;
    private Marker              traceMarker;

    private Provider<Servlet>   servletProvider;


    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    HttpRequestHandler() {
        responseSender = new ResponseSender(this);
    }

    @Inject
    HttpRequestHandler(final ChannelGroup allChannels, final Provider<Marker> traceMarkerProvider,
            final Provider<Servlet> servletProvider) {
        this.allChannels = allChannels;
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
        responseSender = new ResponseSender(this);
    }

    @Override
    public void channelOpen(final ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
        allChannels.add(e.getChannel());
    }

    /**
     * Invoked when an exception occurs whenever channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final ExceptionEvent e) throws Exception {
        MDC.put("requestId", e.getChannel().getId().toString());
        String exceptionString = e.getClass().getSimpleName();
        InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
        InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
        if (exceptionString.equalsIgnoreCase(ServletHandler.CLOSED_CHANNEL_EXCEPTION)
                || exceptionString.equalsIgnoreCase(ServletHandler.CONNECTION_RESET_PEER)) {
            InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
            LOG.debug(traceMarker, "Channel is terminated {}", ctx.getChannel().getId());
        }
        LOG.info(traceMarker, "Getting netty error in HttpRequestHandler: {}", e);
        if (e.getChannel().isOpen()) {
            responseSender.sendNoAdResponse(e);
        }
        e.getCause().printStackTrace();
        e.getChannel().close();
    }

    // Invoked when request timeout.
    @Override
    public void channelIdle(final ChannelHandlerContext ctx, final IdleStateEvent e) {
        MDC.put("requestId", e.getChannel().getId().toString());
        if (e.getChannel().isOpen()) {
            LOG.debug(traceMarker, "Channel is open in channelIdle handler");
            if (responseSender.getRankList() != null) {
                for (ChannelSegment channelSegment : responseSender.getRankList()) {
                    if (channelSegment.getAdNetworkInterface().getAdStatus().equals("AD")) {
                        LOG.debug(traceMarker, "Got Ad from {} Top Rank was {}", channelSegment.getAdNetworkInterface()
                                .getName(), responseSender.getRankList().get(0).getAdNetworkInterface().getName());
                        responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), e);
                        return;
                    }
                }
            }
            responseSender.sendNoAdResponse(e);
        }

        LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", e.getChannel().getId());
        if (e.getState().toString().equalsIgnoreCase("ALL_IDLE")
                || e.getState().toString().equalsIgnoreCase("WRITE_IDLE")) {
            InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
            LOG.debug(traceMarker, "server timeout");
        }
    }

    // Invoked when message is received over the connection
    @Override
    public void messageReceived(final ChannelHandlerContext ctx, final MessageEvent e) throws Exception {
        try {
            RequestParameterHolder requestParameterHolder = (RequestParameterHolder) e.getMessage();
            this.terminationReason = requestParameterHolder.getTerminationReason();
            this.responseSender.sasParams = requestParameterHolder.getSasParams();
            this.responseSender.casInternalRequestParameters = requestParameterHolder.getCasInternalRequestParameters();
            traceMarker = traceMarkerProvider.get();

            Servlet servlet = servletProvider.get();
            String servletName = servlet.getName();

            LOG.debug(traceMarker, "Got the servlet {}", servletName);

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(requestParameterHolder.getUri());

            servlet.handleRequest(this, queryStringDecoder, e);
        }
        catch (Exception exception) {
            terminationReason = ServletHandler.processingError;
            InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
            responseSender.sendNoAdResponse(e);
            String exceptionClass = exception.getClass().getSimpleName();
            InspectorStats.incrementStatCount(exceptionClass, InspectorStrings.count);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            exception.printStackTrace(pw);
            LOG.info(traceMarker, "stack trace is {}", sw);
            if (LOG.isDebugEnabled()) {
                sendMail(exception.getMessage(), sw.toString());
            }
        }
    }

    public void writeLogs(ResponseSender responseSender) {
        List<ChannelSegment> list = new ArrayList<ChannelSegment>();
        if (null != responseSender.getRankList()) {
            list.addAll(responseSender.getRankList());
        }
        if (null != responseSender.getAuctionEngine().getRtbSegments()) {
            list.addAll(responseSender.getAuctionEngine().getRtbSegments());
        }
        long totalTime = responseSender.getTotalTime();
        if (totalTime > 2000) {
            totalTime = 0;
        }
        try {
            ChannelSegment adResponseChannelSegment = null;
            if (null != responseSender.getRtbResponse()) {
                adResponseChannelSegment = responseSender.getRtbResponse();
            }
            else if (null != responseSender.getAdResponse()) {
                adResponseChannelSegment = responseSender.getRankList().get(responseSender.getSelectedAdIndex());
            }
            Logging.rrLogging(adResponseChannelSegment, list, responseSender.sasParams, terminationReason,
                    totalTime);
            Logging.advertiserLogging(list, ServletHandler.getLoggerConfig());
            Logging.sampledAdvertiserLogging(list, ServletHandler.getLoggerConfig());
        }
        catch (JSONException exception) {
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        catch (TException exception) {
            LOG.debug(ChannelServer.getMyStackTrace(exception));
        }
        LOG.debug("done with logging");
    }

    // send Mail if channel server crashes
    public static void sendMail(final String errorMessage, final String stackTrace) {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", ServletHandler.getServerConfig().getString("smtpServer"));
        Session session = Session.getDefaultInstance(properties);
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(ServletHandler.getServerConfig().getString("sender")));
            List<String> recipients = ServletHandler.getServerConfig().getList("recipients");
            javax.mail.internet.InternetAddress[] addressTo = new javax.mail.internet.InternetAddress[recipients.size()];

            for (int index = 0; index < recipients.size(); index++) {
                addressTo[index] = new javax.mail.internet.InternetAddress(recipients.get(index));
            }

            message.setRecipients(Message.RecipientType.TO, addressTo);
            InetAddress addr = InetAddress.getLocalHost();
            message.setSubject("Channel Ad Server Crashed on Host " + addr.getHostName());
            message.setText(errorMessage + stackTrace);
            Transport.send(message);
        }
        catch (MessagingException mex) {
            // logger.info("Error while sending mail");
            mex.printStackTrace();
        }
        catch (UnknownHostException ex) {
            // logger.debug("could not resolve host inside send mail");
            ex.printStackTrace();
        }
    }

}
