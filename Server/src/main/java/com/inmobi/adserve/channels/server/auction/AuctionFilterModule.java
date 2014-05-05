package com.inmobi.adserve.channels.server.auction;

import com.google.common.collect.Lists;
import com.google.inject.*;
import com.inmobi.adserve.channels.server.constants.ChannelSegmentFilterOrder;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AuctionFilter;
import com.inmobi.adserve.channels.server.auction.auctionfilter.impl.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.List;
import java.util.Set;

public class AuctionFilterModule extends AbstractModule {

    private final Reflections reflections;

    public AuctionFilterModule() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .filterInputsBy(
                        new FilterBuilder().includePackage(
                                "com.inmobi.adserve.channels.server.auction.auctionfilter.impl")
                                .includePackage(
                                        "com.inmobi.adserve.channels.server.auction.auctionfilter.impl"))
                .setUrls(ClasspathHelper.forClassLoader()).setScanners(new SubTypesScanner());

        reflections = new Reflections(configurationBuilder);
    }
    @Override
    protected void configure() {

    }

    @Singleton
    @Provides
    List<AuctionFilter> provideAuctionFilters(final Injector injector) {
        List<AuctionFilter> auctionFilterList = Lists.newArrayList();

        Set<Class<? extends AuctionFilter>> classes = reflections.getSubTypesOf(AuctionFilter.class);
        classes.addAll(reflections.getSubTypesOf(AbstractAuctionFilter.class));

        for (Class<? extends AuctionFilter> class1 : classes) {
            AuctionFilter filter = injector.getInstance(class1);
            if (filter instanceof AuctionNoAdFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.FIRST);
            }
            else if (filter instanceof AuctionBidFloorFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.SECOND);
            }
            else if (filter instanceof AuctionSeatIdFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.THIRD);
            }
            else if (filter instanceof AuctionImpressionIdFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.FOURTH);
            }
            else if (filter instanceof AuctionIdFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.FIFTH);
            }
            else if (filter instanceof AuctionCurrencyFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.SIXTH);
            }
            else if (filter instanceof AuctionCreativeIdFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.SEVENTH);
            }
            else if (filter instanceof AuctionIUrlFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.EIGHT);
            }
            else if (filter instanceof AuctionAdvertiserDomainFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.NINTH);
            }
            else if (filter instanceof AuctionCreativeAttributeFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.TENTH);
            }
            else if (filter instanceof AuctionCreativeValidatorFilter) {
                filter.setOrder(ChannelSegmentFilterOrder.LAST);
            }
            else {
                filter.setOrder(ChannelSegmentFilterOrder.DEFAULT);
            }

            auctionFilterList.add(filter);
        }

        return auctionFilterList;
    }
}
