package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.thrift.TException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class HttpRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOG               = LoggerFactory.getLogger(HttpRequestHandler.class);

    public String               terminationReason = "NO";
    public JSONObject           jObject           = null;
    public AdPoolRequest        tObject;
    public ResponseSender       responseSender;

    private Provider<Marker>    traceMarkerProvider;
    private Marker              traceMarker;

    private Provider<Servlet>   servletProvider;

    private HttpRequest         httpRequest;

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(final String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public HttpRequestHandler() {
        responseSender = new ResponseSender(this);
    }

    @Inject
    HttpRequestHandler(final Provider<Marker> traceMarkerProvider, final Provider<Servlet> servletProvider) {
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
        responseSender = new ResponseSender(this);
    }

    /**
     * Invoked when an exception occurs whenever channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));

        if (cause instanceof ReadTimeoutException) {

            if (ctx.channel().isOpen()) {
                LOG.debug(traceMarker, "Channel is open in channelIdle handler");
                if (responseSender.getRankList() != null) {
                    for (ChannelSegment channelSegment : responseSender.getRankList()) {
                        if (channelSegment.getAdNetworkInterface().getAdStatus().equals("AD")) {
                            LOG.debug(traceMarker, "Got Ad from {} Top Rank was {}", channelSegment
                                    .getAdNetworkInterface().getName(), responseSender.getRankList().get(0)
                                    .getAdNetworkInterface().getName());
                            responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), ctx.channel());
                            return;
                        }
                    }
                }
                responseSender.sendNoAdResponse(ctx.channel());
            }
            // increment the totalTimeout. It means server
            // could not write the response with in 800 ms
            LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", ctx.channel());
            InspectorStats.incrementStatCount(InspectorStrings.totalTimeout);
            LOG.debug(traceMarker, "server timeout");

        }
        else {

            String exceptionString = cause.getClass().getSimpleName();
            InspectorStats.incrementStatCount(InspectorStrings.channelException, exceptionString);
            InspectorStats.incrementStatCount(InspectorStrings.channelException, InspectorStrings.count);
            if (cause instanceof ClosedChannelException || cause instanceof IOException) {
                InspectorStats.incrementStatCount(InspectorStrings.totalTerminate);
                LOG.debug(traceMarker, "Channel is terminated {}", ctx.channel());
            }
            LOG.info(traceMarker, "Getting netty error in HttpRequestHandler: {}", cause);
            if (ctx.channel().isOpen()) {
                responseSender.sendNoAdResponse(ctx.channel());
            }
        }

    }

    // Invoked when message is received over the connection
    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object msg) throws Exception {
        RequestParameterHolder requestParameterHolder = (RequestParameterHolder) msg;
        try {
            this.terminationReason = requestParameterHolder.getTerminationReason();
            this.responseSender.sasParams = requestParameterHolder.getSasParams();
            this.responseSender.casInternalRequestParameters = requestParameterHolder.getCasInternalRequestParameters();
            httpRequest = requestParameterHolder.getHttpRequest();

            traceMarker = traceMarkerProvider.get();

            Servlet servlet = servletProvider.get();

            LOG.debug(traceMarker, "Got the servlet {} , uri {}", servlet.getName(), httpRequest.getUri());

            servlet.handleRequest(this, new QueryStringDecoder(httpRequest.getUri()), ctx.channel());
            return;
        }
        catch (Exception exception) {
            terminationReason = ServletHandler.processingError;
            InspectorStats.incrementStatCount(InspectorStrings.processingError, InspectorStrings.count);
            responseSender.sendNoAdResponse(ctx.channel());
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
        finally {
            requestParameterHolder.getHttpRequest().release();
        }
    }

    public boolean isRequestFromLocalHost() {
        String host = CasUtils.getHost(httpRequest);

        if (host != null && host.startsWith("localhost")) {
            return true;
        }

        return false;
    }

    /**
     * @return the httpRequest
     */
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public void writeLogs(final ResponseSender responseSender) {
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
            Logging.rrLogging(adResponseChannelSegment, list, responseSender.sasParams, terminationReason, totalTime);
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
