package com.inmobi.adserve.channels.server.config;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.api.config.AdapterConfig;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfigFactory {
  AdapterConfig create(final Configuration adapterConfig, final String adapterName);
}
