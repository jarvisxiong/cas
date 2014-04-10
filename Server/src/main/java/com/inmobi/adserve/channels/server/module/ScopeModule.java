package com.inmobi.adserve.channels.server.module;

import org.slf4j.Marker;

import com.google.inject.AbstractModule;
import com.inmobi.adserve.channels.server.SimpleScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.annotations.BatchScoped;


/**
 * @author abhishek.parwal
 * 
 */
public class ScopeModule extends AbstractModule {

    @Override
    protected void configure() {
        SimpleScope simpleScope = new SimpleScope();
        bindScope(BatchScoped.class, simpleScope);
        bind(SimpleScope.class).toInstance(simpleScope);
        bind(Marker.class).toProvider(SimpleScope.<Marker> seededKeyProvider()).in(simpleScope);
        bind(Servlet.class).toProvider(SimpleScope.<Servlet> seededKeyProvider()).in(simpleScope);
    }

}
