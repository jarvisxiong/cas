package com.inmobi.adserve.channels.server.config;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.config.CasConfig;
import com.inmobi.adserve.channels.api.config.ServerConfig;


/**
 * @author abhishek.parwal
 * 
 */
@EqualsAndHashCode
public class AdapterConfig implements CasConfig {

    private final Configuration adapterConfig;
    private final String dcName;
    private final String adapterName;
    private final Class<AdNetworkInterface> adapterClass;
    private final ServerConfig serverConfig;

    @SuppressWarnings("unchecked")
    @AssistedInject
    public AdapterConfig(@Assisted final Configuration adapterConfig, @Assisted final String adapterName,
            @Nullable @Named("dcName") final String dcName, final ServerConfig serverConfig) {
        this.adapterConfig = adapterConfig;
        this.adapterName = adapterName;
        this.dcName = dcName;
        this.serverConfig = serverConfig;

        try {
            adapterClass = (Class<AdNetworkInterface>) Class.forName(adapterConfig.getString("class"));
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the advertiserId
     */
    public String getAdvertiserId() {
        return adapterConfig.getString("advertiserId");
    }

    /**
     * @return the adapterName
     */
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * @return the isActive
     */
    public boolean isActive() {
        final String status = adapterConfig.getString("status", "on");

        return "on".equalsIgnoreCase(status);
    }

    /**
     * @return the adapterHost
     */
    public String getAdapterHost() {

        String adapterHost = adapterConfig.getString("host." + dcName);

        if (StringUtils.isBlank(adapterHost)) {
            adapterHost = adapterConfig.getString("host.default");
        }
        if (StringUtils.isBlank(adapterHost)) {
            adapterHost = adapterConfig.getString("host");
        }

        return adapterHost;
    }

    public boolean isValidHost() {
        final String hostName = getAdapterHost();

        return StringUtils.isNotBlank(hostName) && !"NA".equalsIgnoreCase(hostName);
    }

    /*
     * @return the adNetworkInterfaceClass
     */
    public Class<AdNetworkInterface> getAdNetworkInterfaceClass() {
        return adapterClass;
    }

    public boolean isRtb() {
        return adapterConfig.getBoolean("isRtb", false);
    }

    public int getMaxSegmentSelectionCount() {
        return adapterConfig.getInt("partnerSegmentNo", serverConfig.getMaxPartnerSegmentSelectionCount());
    }
}
