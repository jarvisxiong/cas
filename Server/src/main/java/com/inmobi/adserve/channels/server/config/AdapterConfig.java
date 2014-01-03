package com.inmobi.adserve.channels.server.config;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.server.AdapterType;


/**
 * @author abhishek.parwal
 * 
 */
public interface AdapterConfig extends CasConfig {

    String getAdvertiserId();

    String getAdapterName();

    boolean isActive();

    String getAdapterHost();

    AdapterType getAdapterType();

    Class<AdNetworkInterface> getAdNetworkInterfaceClass();

    boolean isRtb();

    int getMaxSegmentSelectionCount();

    boolean isValidHost();

}
