package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

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
	 * Invoked when an exception occurs whenever channel throws
	 * closedchannelexception increment the totalterminate means channel is
	 * closed by party who requested for the ad
	 */
	@Override
	public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) throws Exception {
		MDC.put("requestId", String.format("0x%08x", ctx.channel().hashCode()));

		if (cause instanceof ReadTimeoutException) {

			LOG.debug(traceMarker, "Channel is open in channelIdle handler");
			if (responseSender.getRankList() != null) {
				for (ChannelSegment channelSegment : responseSender.getRankList()) {
					if ("AD".equals(channelSegment.getAdNetworkInterface().getAdStatus())) {
						LOG.debug(traceMarker, "Got Ad from {} Top Rank was {}", channelSegment.getAdNetworkInterface().getName(), responseSender.getRankList()
								.get(0).getAdNetworkInterface().getName());
						responseSender.sendAdResponse(channelSegment.getAdNetworkInterface(), ctx.channel());
						return;
					}
				}
			}
			responseSender.sendNoAdResponse(ctx.channel());
			// increment the totalTimeout. It means server
			// could not write the response with in 800 ms
			LOG.debug(traceMarker, "inside channel idle event handler for Request channel ID: {}", ctx.channel());
			InspectorStats.incrementStatCount(InspectorStrings.TOTAL_TIMEOUT);
			LOG.debug(traceMarker, "server timeout");

		} else {

			String exceptionString = cause.getClass().getSimpleName();
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
