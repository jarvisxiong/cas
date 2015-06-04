package com.inmobi.adserve.channels.adnetworks;

import org.slf4j.Marker;

import com.google.inject.AbstractModule;

/**
 * @author ritwik.kumar
 */
public class TestScopeModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Marker.class).toProvider(() -> null);
    }

}
