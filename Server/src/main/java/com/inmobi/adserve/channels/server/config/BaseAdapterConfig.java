package com.inmobi.adserve.channels.server.config;

import javax.annotation.Nullable;
import javax.inject.Inject;

import lombok.EqualsAndHashCode;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.server.AdapterType;


/**
 * @author abhishek.parwal
 * 
 */
@EqualsAndHashCode
public class BaseAdapterConfig implements AdapterConfig {

    private final Configuration             adapterConfig;
    private final String                    dcName;
    private final String                    adapterName;
    private final Class<AdNetworkInterface> adapterClass;
    private final ServerConfig              serverConfig;

    @SuppressWarnings("unchecked")
    @Inject
    public BaseAdapterConfig(@Assisted final Configuration adapterConfig,
            @Assisted("adapterName") final String adapterName, @Nullable @Named("dcName") final String dcName,
            final ServerConfig serverConfig) {
        this.adapterConfig = adapterConfig;
        this.adapterName = adapterName;
        this.dcName = dcName;
        this.serverConfig = serverConfig;

        try {
            this.adapterClass = (Class<AdNetworkInterface>) Class.forName(adapterConfig.getString("class"));
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the advertiserId
     */
    @Override
    public String getAdvertiserId() {
        return adapterConfig.getString("advertiserId");
    }

    /**
     * @return the adapterName
     */
    @Override
    public String getAdapterName() {
        return adapterName;
    }

    /**
     * @return the isActive
     */
    @Override
    public boolean isActive() {
        String status = adapterConfig.getString("status", "on");

        if (status.equalsIgnoreCase("on")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * @return the adapterHost
     */
    @Override
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

    @Override
    public boolean isValidHost() {
        String hostName = getAdapterHost();

        return StringUtils.isNotBlank(hostName) && !"NA".equalsIgnoreCase(hostName);
    }

    /**
     * @return the adapterType
     */
    @Override
    public AdapterType getAdapterType() {
        if (isRtb()) {
            return AdapterType.RTB;
        }
        return AdapterType.DCP;
    }

    /*
     * @return the adNetworkInterfaceClass
     */
    @Override
    public Class<AdNetworkInterface> getAdNetworkInterfaceClass() {
        return adapterClass;
    }

    @Override
    public boolean isRtb() {
        return adapterConfig.getBoolean("isRtb", false);
    }

    @Override
    public int getMaxSegmentSelectionCount() {
        return adapterConfig.getInt("partnerSegmentNo", serverConfig.getMaxPartnerSegmentSelectionCount());
    }

}
