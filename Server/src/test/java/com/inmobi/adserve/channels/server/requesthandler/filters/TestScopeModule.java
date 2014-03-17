package com.inmobi.adserve.channels.server.requesthandler.filters;

import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.inmobi.adserve.channels.server.api.Servlet;


/**
 * @author abhishek.parwal
 * 
 */
public class TestScopeModule extends AbstractModule {

    @SuppressWarnings("unchecked")
    @Override
    protected void configure() {
        bind(Marker.class).toProvider((Provider<? extends Marker>) Providers.of(null));
        bind(Servlet.class).toProvider((Provider<? extends Servlet>) Providers.of(null));
    }

}
