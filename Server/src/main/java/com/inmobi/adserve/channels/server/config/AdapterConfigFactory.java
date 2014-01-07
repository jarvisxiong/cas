package com.inmobi.adserve.channels.server.config;

import org.apache.commons.configuration.Configuration;

import com.google.inject.assistedinject.Assisted;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfigFactory {
    AdapterConfig create(final Configuration adapterConfig, @Assisted("adapterName") final String adapterName);
}