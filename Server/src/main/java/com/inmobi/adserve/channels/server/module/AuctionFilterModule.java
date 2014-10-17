package com.inmobi.adserve.channels.server.module;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AuctionFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionAdvertiserDomainFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionBidFloorFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCreativeAttributeFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCreativeIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCreativeValidatorFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCurrencyFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionIUrlFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionImpressionIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionLogCreative;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionNoAdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionSeatIdFilter;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;

public class AuctionFilterModule extends AbstractModule {

  private final Reflections reflections;
  private final static Comparator<ChannelSegmentFilter> FILTER_COMPARATOR = new Comparator<ChannelSegmentFilter>() {
    @Override
    public int compare(final ChannelSegmentFilter o1, final ChannelSegmentFilter o2) {
      return o1.getOrder().getValue() - o2.getOrder().getValue();
    }
  };

  public AuctionFilterModule() {
    reflections = new Reflections("com.inmobi.adserve.channels.server.auction.auctionfilter.impl");
  }

  @Override
  protected void configure() {

  }

  @Singleton
  @Provides
  List<AuctionFilter> provideAuctionFilters(final Injector injector) {
    final List<AuctionFilter> auctionFilterList = Lists.newArrayList();

    final Set<Class<? extends AuctionFilter>> classes = reflections.getSubTypesOf(AuctionFilter.class);
    classes.addAll(reflections.getSubTypesOf(AbstractAuctionFilter.class));

    for (final Class<? extends AuctionFilter> class1 : classes) {
      final AuctionFilter filter = injector.getInstance(class1);
      if (filter instanceof AuctionNoAdFilter) {
        filter.setOrder(FilterOrder.FIRST);
      } else if (filter instanceof AuctionBidFloorFilter) {
        filter.setOrder(FilterOrder.SECOND);
      } else if (filter instanceof AuctionSeatIdFilter) {
        filter.setOrder(FilterOrder.THIRD);
      } else if (filter instanceof AuctionImpressionIdFilter) {
        filter.setOrder(FilterOrder.FOURTH);
      } else if (filter instanceof AuctionIdFilter) {
        filter.setOrder(FilterOrder.FIFTH);
      } else if (filter instanceof AuctionCurrencyFilter) {
        filter.setOrder(FilterOrder.SIXTH);
      } else if (filter instanceof AuctionCreativeIdFilter) {
        filter.setOrder(FilterOrder.SEVENTH);
      } else if (filter instanceof AuctionIUrlFilter) {
        filter.setOrder(FilterOrder.EIGHT);
      } else if (filter instanceof AuctionAdvertiserDomainFilter) {
        filter.setOrder(FilterOrder.NINTH);
      } else if (filter instanceof AuctionCreativeAttributeFilter) {
        filter.setOrder(FilterOrder.TENTH);
      } else if (filter instanceof AuctionCreativeValidatorFilter) {
        filter.setOrder(FilterOrder.SECOND_LAST);
      } else if (filter instanceof AuctionLogCreative) {
        filter.setOrder(FilterOrder.LAST);
      } else {
        filter.setOrder(FilterOrder.DEFAULT);
      }

      auctionFilterList.add(filter);
    }

    Collections.sort(auctionFilterList, FILTER_COMPARATOR);

    return auctionFilterList;
  }
}
