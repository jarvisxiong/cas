package com.inmobi.adserve.channels.server.config;

import org.apache.commons.configuration.Configuration;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfigFactory {
    AdapterConfig create(final Configuration adapterConfig, final String adapterName, final String dcName);
}