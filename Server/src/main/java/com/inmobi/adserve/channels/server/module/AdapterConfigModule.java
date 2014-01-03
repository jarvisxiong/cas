package com.inmobi.adserve.channels.server.module;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.multibindings.MapBinder;
import com.inmobi.adserve.channels.server.annotations.AdvertiserIdNameMap;
import com.inmobi.adserve.channels.server.config.AdapterConfig;
import com.inmobi.adserve.channels.server.config.AdapterConfigFactory;
import com.inmobi.adserve.channels.server.config.BaseAdapterConfig;


/**
 * @author abhishek.parwal
 * 
 */
public class AdapterConfigModule extends AbstractModule {

    private final Configuration allAdapterConfiguration;
    private final String        dcName;

    public AdapterConfigModule(final Configuration allAdapterConfiguration, final String dcName) {
        this.allAdapterConfiguration = allAdapterConfiguration;
        this.dcName = dcName;
    }

    @Override
    protected void configure() {

        @SuppressWarnings("unchecked")
        Iterator<String> keyIterator = allAdapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        Module adapterConfigFactoryModule = new FactoryModuleBuilder().implement(AdapterConfig.class,
                BaseAdapterConfig.class).build(AdapterConfigFactory.class);
        Injector injector = Guice.createInjector(adapterConfigFactoryModule);
        AdapterConfigFactory adapterConfigFactory = injector.getInstance(AdapterConfigFactory.class);

        for (String adapterName : adapterNames) {
            Configuration adapterConfiguration = allAdapterConfiguration.subset(adapterName);
            AdapterConfig adapterConfig = adapterConfigFactory.create(adapterConfiguration, adapterName, dcName);
            adapterConfigs.add(adapterConfig);
        }

        MapBinder<String, AdapterConfig> advertiserIdConfigMapBinder = MapBinder.newMapBinder(binder(), String.class,
                AdapterConfig.class);
        MapBinder<String, String> advertiserIdToNameMapBinder = MapBinder.newMapBinder(binder(), String.class,
                String.class, AdvertiserIdNameMap.class);

        for (AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdConfigMapBinder.addBinding(adapterConfig.getAdvertiserId()).toInstance(adapterConfig);
            advertiserIdToNameMapBinder.addBinding(adapterConfig.getAdvertiserId()).toInstance(
                    adapterConfig.getAdapterName());
        }

    }

}