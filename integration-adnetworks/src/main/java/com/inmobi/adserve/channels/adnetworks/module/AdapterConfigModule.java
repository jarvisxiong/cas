package com.inmobi.adserve.channels.adnetworks.module;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mvp.HostedAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.config.AdapterConfig;
import com.inmobi.adserve.channels.api.config.AdapterConfigFactory;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;


/**
 * @author abhishek.parwal
 * 
 */
public class AdapterConfigModule extends AbstractModule {

    private final Configuration allAdapterConfiguration;
    private final String dcName;
    private static final String DCP_NAME = "DCP";

    public AdapterConfigModule(final Configuration allAdapterConfiguration, final String dcName) {
        this.allAdapterConfiguration = allAdapterConfiguration;
        this.dcName = dcName;
    }

    @Override
    protected void configure() {

        requestStaticInjection(BaseAdNetworkImpl.class);
        requestStaticInjection(RtbAdNetwork.class);
        requestStaticInjection(IXAdNetwork.class);
        requestStaticInjection(HostedAdNetwork.class);

        install(new FactoryModuleBuilder().build(AdapterConfigFactory.class));
        bind(String.class).annotatedWith(Names.named("dcName")).toProvider(Providers.of(dcName));
    }

    @Provides
    @Singleton
    Map<String, AdapterConfig> provideAdvertiserIdConfigMap(final AdapterConfigFactory adapterConfigFactory) {

        @SuppressWarnings("unchecked")
        final Iterator<String> keyIterator = allAdapterConfiguration.getKeys();

        final Set<String> adapterNames = Sets.newHashSet();

        final Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            final String key = keyIterator.next();

            final String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (final String adapterName : adapterNames) {
            final Configuration adapterConfiguration = allAdapterConfiguration.subset(adapterName);
            final AdapterConfig adapterConfig = adapterConfigFactory.create(adapterConfiguration, adapterName);
            adapterConfigs.add(adapterConfig);
        }

        final Map<String, AdapterConfig> advertiserIdConfigMap = Maps.newHashMap();

        for (final AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdConfigMap.put(adapterConfig.getAdvertiserId(), adapterConfig);
        }
        return advertiserIdConfigMap;
    }

    @Provides
    @Singleton
    @AdvertiserIdNameMap
    Map<String, String> provideAdvertiserIdToNameMap(final AdapterConfigFactory adapterConfigFactory) {

        @SuppressWarnings("unchecked")
        final Iterator<String> keyIterator = allAdapterConfiguration.getKeys();

        final Set<String> adapterNames = Sets.newHashSet();

        final Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            final String key = keyIterator.next();

            final String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (final String adapterName : adapterNames) {
            final Configuration adapterConfiguration = allAdapterConfiguration.subset(adapterName);
            final AdapterConfig adapterConfig = adapterConfigFactory.create(adapterConfiguration, adapterName);
            adapterConfigs.add(adapterConfig);
        }

        final Map<String, String> advertiserIdToNameMap = Maps.newHashMap();

        for (final AdapterConfig adapterConfig : adapterConfigs) {
            if(adapterConfig.isIx() || adapterConfig.isRtb()){
                advertiserIdToNameMap.put(adapterConfig.getAdvertiserId(), adapterConfig.getAdapterName());    
            }else{
                advertiserIdToNameMap.put(adapterConfig.getAdvertiserId(), adapterConfig.getAdapterName() + DCP_NAME);
            }
            
        }

        return advertiserIdToNameMap;
    }

}
