package com.inmobi.adserve.channels.server.module;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.inmobi.adserve.channels.server.requesthandler.filters.AdvertiserLevelFilter;


/**
 * @author abhishek.parwal
 * 
 */
public class ChannelSegmentFilterModule extends AbstractModule {

    private final Reflections reflections;

    public ChannelSegmentFilterModule() {
        this.reflections = new Reflections("com.inmobi.adserve.channels", new SubTypesScanner());
    }

    @Override
    protected void configure() {

        Multibinder<AdvertiserLevelFilter> advertiserLevelFilterBinder = Multibinder.newSetBinder(binder(),
            AdvertiserLevelFilter.class);

        Set<Class<? extends AdvertiserLevelFilter>> classes = reflections.getSubTypesOf(AdvertiserLevelFilter.class);

        for (Class<? extends AdvertiserLevelFilter> class1 : classes) {
            advertiserLevelFilterBinder.addBinding().to(class1).asEagerSingleton();
        }
    }

}
