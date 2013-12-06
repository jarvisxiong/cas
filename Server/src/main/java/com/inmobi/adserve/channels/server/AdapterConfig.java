package com.inmobi.adserve.channels.server;

import com.inmobi.adserve.channels.api.AdNetworkInterface;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfig {

    String getAdvertiserId();

    String getAdapterName();

    boolean isActive();

    String getAdapterHost();

    AdapterType getAdapterType();

    Class<AdNetworkInterface> getAdNetworkInterfaceClass();

}
