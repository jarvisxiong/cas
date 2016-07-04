package com.inmobi.adserve.channels.server.module;

import static com.inmobi.adserve.channels.server.constants.FilterOrder.DEFAULT;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.FIFTH;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.FIRST;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.FOURTH;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.LAST;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.SECOND;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.SECOND_LAST;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.SEVENTH;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.SIXTH;
import static com.inmobi.adserve.channels.server.constants.FilterOrder.THIRD;

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
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionBidFloorFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCreativeIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCreativeValidatorFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionCurrencyFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionImpressionIdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionLogCreative;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionNoAdFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.AuctionSeatIdFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;

final class AuctionFilterModule extends AbstractModule {

    private final static Comparator<ChannelSegmentFilter> FILTER_COMPARATOR =
            (o1, o2) -> o1.getOrder().getValue() - o2.getOrder().getValue();
    private final Reflections reflections;

    AuctionFilterModule() {
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
                filter.setOrder(FIRST);
            } else if (filter instanceof AuctionBidFloorFilter) {
                filter.setOrder(SECOND);
            } else if (filter instanceof AuctionSeatIdFilter) {
                filter.setOrder(THIRD);
            } else if (filter instanceof AuctionImpressionIdFilter) {
                filter.setOrder(FOURTH);
            } else if (filter instanceof AuctionIdFilter) {
                filter.setOrder(FIFTH);
            } else if (filter instanceof AuctionCurrencyFilter) {
                filter.setOrder(SIXTH);
            } else if (filter instanceof AuctionCreativeIdFilter) {
                filter.setOrder(SEVENTH);
            } else if (filter instanceof AuctionCreativeValidatorFilter) {
                filter.setOrder(SECOND_LAST);
            } else if (filter instanceof AuctionLogCreative) {
                filter.setOrder(LAST);
            } else {
                filter.setOrder(DEFAULT);
            }

            auctionFilterList.add(filter);
        }

        Collections.sort(auctionFilterList, FILTER_COMPARATOR);

        return auctionFilterList;
    }
}
