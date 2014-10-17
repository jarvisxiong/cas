package com.inmobi.adserve.channels.server.auction.auctionfilter;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.casthrift.DemandSourceType;

public abstract class AbstractAuctionFilter implements AuctionFilter {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractAuctionFilter.class);

  protected final Provider<Marker> traceMarkerProvider;
  private final String inspectorString;
  private FilterOrder order;
  private final ServerConfig serverConfiguration;
  protected Boolean isApplicableRTBD; // Whether the filter is applicable to RTBD
  protected Boolean isApplicableIX; // Whether the filter is applicable to IX


  protected AbstractAuctionFilter(final Provider<Marker> traceMarkerProvider, final String inspectorString,
      final ServerConfig serverConfiguration) {
    this.traceMarkerProvider = traceMarkerProvider;
    this.inspectorString = inspectorString;
    this.serverConfiguration = serverConfiguration;
  }

  @Override
  public void filter(final List<ChannelSegment> channelSegments,
      final CasInternalRequestParameters casInternalRequestParameters) {
    final Marker traceMarker = null;// = traceMarkerProvider.get();

    for (final Iterator<ChannelSegment> iterator = channelSegments.listIterator(); iterator.hasNext();) {
      final ChannelSegment channelSegment = iterator.next();

      boolean result = false;

      // Check whether the auction filter is applicable to the particular channel entity and also whether it
      // is applicable to the particular demand source type
      if (isApplicable(channelSegment.getChannelEntity().getAccountId())) {
        result = failedInFilter(channelSegment, casInternalRequestParameters);
      }

      if (result) {
        iterator.remove();
        LOG.debug(traceMarker, "Failed in auction filter {}  , advertiser {}", this.getClass().getSimpleName(),
            channelSegment.getAdNetworkInterface().getName());
        incrementStats(channelSegment);
      } else {
        LOG.debug(traceMarker, "Passed in auction filter {} ,  advertiser {}", this.getClass().getSimpleName(),
            channelSegment.getAdNetworkInterface().getName());
      }
    }
  }

  /**
   * @param channelSegment
   * @return
   */
  protected abstract boolean failedInFilter(final ChannelSegment channelSegment,
      final CasInternalRequestParameters casInternalRequestParameters);

  /**
   * @param channelSegment
   */
  protected void incrementStats(final ChannelSegment channelSegment) {
    if (StringUtils.isNotEmpty(inspectorString)) {
      channelSegment.incrementInspectorStats(inspectorString);
    }
  }

  @Override
  final public void setOrder(final FilterOrder order) {
    this.order = order;
  }

  @Override
  public FilterOrder getOrder() {
    return order;
  }

  @Override
  public boolean isApplicable(final String advertiserId) {
    return !serverConfiguration.getExcludedAdvertisers(this.getClass().getSimpleName()).contains(advertiserId);
  }

  @Override
  public boolean isApplicable(final DemandSourceType dst) {
    switch (dst) {
      case RTBD:
        return isApplicableRTBD;
      case IX:
        return isApplicableIX;
      default:
        return true;
    }
  }

  /*
   * Auction Filters                         isApplicableIX    isApplicableRTBD
   *
   * AuctionBidFloorFilter.java              YES               YES
   * AuctionNoAdFilter.java                  YES               YES
   * AuctionCreativeIdFilter.java            YES               YES
   * AuctionIdFilter.java                    YES               YES
   * AuctionIXImpressionIdFilter.java        YES               NO
   * AuctionSeatIdFilter.java                NO                YES
   * AuctionCreativeAttributeFilter.java     NO                YES
   * AuctionCreativeValidatorFilter.java     NO                YES
   * AuctionCurrencyFilter.java              NO                YES
   * AuctionImpressionIdFilter.java          NO                YES
   * AuctionIUrlFilter.java                  NO                YES
   * AuctionLogCreative.java                 NO                YES
   * AuctionAdvertiserDomainFilter.java      NO                YES
   */
}
