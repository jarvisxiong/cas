package com.inmobi.adserve.channels.server;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.hadoop.thirdparty.guava.common.collect.Sets;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import com.inmobi.adserve.channels.server.requesthandler.AsyncRequestMaker;


/**
 * @author abhishek.parwal
 * 
 */
public class AdapterConfigModule extends AbstractModule {

    private final Configuration adapterConfiguration;
    private final String        dcName;

    public AdapterConfigModule(final Configuration adapterConfiguration, final String dcName) {
        this.adapterConfiguration = adapterConfiguration;
        this.dcName = dcName;
    }

    @Override
    protected void configure() {

        Iterator<String> keyIterator = adapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        Set<AdapterConfig> adapterConfigs = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (String adapterName : adapterNames) {
            Configuration adapterConfig = adapterConfiguration.subset(adapterName);

            BaseAdapterConfig baseAdapterConfig = new BaseAdapterConfig(adapterConfig, adapterName, dcName);

            adapterConfigs.add(baseAdapterConfig);

        }

        MapBinder<String, AdapterConfig> advertiserIdConfigMapBinder = MapBinder.newMapBinder(binder(), String.class,
            AdapterConfig.class);

        for (AdapterConfig adapterConfig : adapterConfigs) {
            advertiserIdConfigMapBinder.addBinding(adapterConfig.getAdvertiserId()).toInstance(adapterConfig);
        }

        requestStaticInjection(AsyncRequestMaker.class);

    }
}
