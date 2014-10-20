package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupMaxSegmentPerRequestFilter implements AdGroupLevelFilter {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractAdGroupLevelFilter.class);

	private final Provider<Marker> traceMarkerProvider;

	private final ServerConfig serverConfig;

	private FilterOrder order;

	@Inject
	AdGroupMaxSegmentPerRequestFilter(final Provider<Marker> traceMarkerProvider, final ServerConfig serverConfig) {
		this.traceMarkerProvider = traceMarkerProvider;
		this.serverConfig = serverConfig;
	}

	@Override
	public void filter(final List<ChannelSegment> channelSegments, final SASRequestParameters sasParams,
			final CasContext casContext) {

		final Marker traceMarker = traceMarkerProvider.get();

		final int maxSegmentSelectionCount = serverConfig.getMaxSegmentSelectionCount();

		if (maxSegmentSelectionCount == -1) {
			return;
		}

		int selectedSegmentCount = 0;

		for (final Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
			final ChannelSegment channelSegment = iterator.next();

			final boolean result = failedInFilter(maxSegmentSelectionCount, selectedSegmentCount);

			if (result) {
				// TODO: we can optimize if we don't need these inspector stats , then we can shorten our iteration
				iterator.remove();
				LOG.debug(traceMarker, "Failed in filter {}  , adgroup {}", this.getClass().getSimpleName(),
						channelSegment.getChannelSegmentFeedbackEntity().getId());
				incrementStats(channelSegment);
			} else {
				selectedSegmentCount++;
				LOG.debug(traceMarker, "Passed in filter {} ,  adgroup {}", this.getClass().getSimpleName(),
						channelSegment.getChannelSegmentFeedbackEntity().getId());
			}
		}
	}

	/**
	 * @param maxSegmentSelectionCount
	 * @param selectedSegmentCount
	 * @return
	 */
	private boolean failedInFilter(final int maxSegmentSelectionCount, final int selectedSegmentCount) {
		return selectedSegmentCount >= maxSegmentSelectionCount;
	}

	/**
	 * @param channelSegment
	 */
	protected void incrementStats(final ChannelSegment channelSegment) {
		channelSegment.incrementInspectorStats(InspectorStrings.DROPPED_IN_SEGMENT_PER_REQUEST_FILTER);
	}

	@Override
	final public void setOrder(final FilterOrder order) {
		this.order = order;
	}

	@Override
	public FilterOrder getOrder() {
		return order;
	}
}
