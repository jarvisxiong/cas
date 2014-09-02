package com.inmobi.adserve.channels.server.module;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.constants.FilterOrder;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AdGroupLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupMaxSegmentPerRequestFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupPartnerCountFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl.AdGroupSupplyDemandClassificationFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AbstractAdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.AdvertiserLevelFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserDetailsInvalidFilter;
import com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl.AdvertiserExcludedFilter;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;


/**
 * @author abhishek.parwal
 * 
 */
public class ChannelSegmentFilterModule extends AbstractModule {

    private final Reflections                             reflections;

    private final static Comparator<ChannelSegmentFilter> FILTER_COMPARATOR = new Comparator<ChannelSegmentFilter>() {
                                                                                @Override
                                                                                public int compare(
                                                                                        final ChannelSegmentFilter o1,
                                                                                        final ChannelSegmentFilter o2) {
                                                                                    return o1.getOrder().getValue()
                                                                                            - o2.getOrder().getValue();
                                                                                }
                                                                            };

    public ChannelSegmentFilterModule() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder()
                .filterInputsBy(
                        new FilterBuilder().includePackage(
                                "com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl")
                                .includePackage(
                                        "com.inmobi.adserve.channels.server.requesthandler.filters.advertiser.impl"))
                .setUrls(ClasspathHelper.forClassLoader()).setScanners(new SubTypesScanner());

        reflections = new Reflections(configurationBuilder);
    }

    @Override
    protected void configure() {

    }

    @Singleton
    @Provides
    List<AdvertiserLevelFilter> provideAdvertiserLevelFilters(final Injector injector) {
        List<AdvertiserLevelFilter> advertiserLevelFilterList = Lists.newArrayList();

        Set<Class<? extends AdvertiserLevelFilter>> classes = reflections.getSubTypesOf(AdvertiserLevelFilter.class);
        classes.addAll(reflections.getSubTypesOf(AbstractAdvertiserLevelFilter.class));

        for (Class<? extends AdvertiserLevelFilter> class1 : classes) {
            AdvertiserLevelFilter filter = injector.getInstance(class1);
            if (filter instanceof AdvertiserDetailsInvalidFilter) {
                filter.setOrder(FilterOrder.FIRST);
            }
            else if (filter instanceof AdvertiserExcludedFilter) {
                filter.setOrder(FilterOrder.SECOND);
            }
            else {
                filter.setOrder(FilterOrder.DEFAULT);
            }

            advertiserLevelFilterList.add(filter);
        }

        Collections.sort(advertiserLevelFilterList, FILTER_COMPARATOR);

        return advertiserLevelFilterList;
    }

    @Singleton
    @Provides
    List<AdGroupLevelFilter> provideAdGroupLevelFilters(final Injector injector) {
        List<AdGroupLevelFilter> adGroupLevelFilterList = Lists.newArrayList();

        Set<Class<? extends AdGroupLevelFilter>> classes = reflections.getSubTypesOf(AdGroupLevelFilter.class);
        classes.addAll(reflections.getSubTypesOf(AbstractAdGroupLevelFilter.class));

        for (Class<? extends AdGroupLevelFilter> class1 : classes) {
            AdGroupLevelFilter filter = injector.getInstance(class1);
            if (filter instanceof AdGroupSupplyDemandClassificationFilter) {
                filter.setOrder(FilterOrder.FIRST);
            }
            else if (filter instanceof AdGroupMaxSegmentPerRequestFilter) {
                filter.setOrder(FilterOrder.LAST);
            }
            else if (filter instanceof AdGroupPartnerCountFilter) {
                filter.setOrder(FilterOrder.SECOND_LAST);
            }
            else {
                filter.setOrder(FilterOrder.DEFAULT);
            }

            adGroupLevelFilterList.add(filter);
        }

        Collections.sort(adGroupLevelFilterList, FILTER_COMPARATOR);

        return adGroupLevelFilterList;
    }
}
