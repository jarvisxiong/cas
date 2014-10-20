package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;

import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.ResponseSender;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

/**
 * @author abhishek.parwal
 * 
 */
public class CasExceptionHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CasExceptionHandler.class);
    private final Marker traceMarker;
    private final ResponseSender responseSender;

    @Inject
    public CasExceptionHandler(@Nullable final Marker traceMarker, final ResponseSender responseSender) {
        this.traceMarker = traceMarker;
        this.responseSender = responseSender;
    }

    /**
     * Invoked when an exception occurs whenever: 1) channel throws closedchannelexception increment the totalterminate
     * means channel is closed by party who requested for the ad. 2) When timeoutexception occurs, among the partners
     * who gave us the ad, we run the auction from here and return it.
     */
    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
        MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));

        if (cause instanceof ReadTimeoutException) {
            // increment the totalTimeout. It means server could not write the response with in the timeout we specified
            LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", ctx.channel());
            InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TIMEOUT);
            LOG.debug(traceMarker, "server timeout");

            // This list contains rtb or ix channel segments
            final List<ChannelSegment> unfilteredChannelSegmentList =
                    responseSender.getAuctionEngine().getUnfilteredChannelSegmentList();

            // This contains dcp channel segments
            List<ChannelSegment> segmentList = responseSender.getRankList();

            // The request is for either dcp or rtb or ix, hence only one will be valid.
            if (unfilteredChannelSegmentList != null && unfilteredChannelSegmentList.size() > 0) {
                segmentList = unfilteredChannelSegmentList;
            }

            if (segmentList != null && segmentList.size() > 0) {
                // We need to send one response from this point, so take the best from here and return it.
                for (final ChannelSegment channelSegment : segmentList) {
                    channelSegment.getAdNetworkInterface().processResponse();
                }
                return;
            }

            responseSender.sendNoAdResponse(ctx.channel());
        } else {
            final String exceptionString = cause.getClass().getSimpleName();
            InspectorStats.incrementStatCount(InspectorStrings.CHANNEL_EXCEPTION, exceptionString);
            InspectorStats.incrementStatCount(InspectorStrings.CHANNEL_EXCEPTION, InspectorStrings.COUNT);
            if (cause instanceof ClosedChannelException || cause instanceof IOException) {
                InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TERMINATE);
                LOG.debug(traceMarker, "Channel is terminated {}", ctx.channel());
            }
            LOG.info(traceMarker, "Getting netty error in HttpRequestHandler: {}", cause);
            responseSender.sendNoAdResponse(ctx.channel());
        }

    }
}
