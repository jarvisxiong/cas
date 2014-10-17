package com.inmobi.adserve.channels.api.config;

import org.apache.commons.configuration.Configuration;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfigFactory {
  AdapterConfig create(final Configuration adapterConfig, final String adapterName);
}
