package com.inmobi.adserve.channels.api.config;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import lombok.EqualsAndHashCode;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;


/**
 * @author abhishek.parwal
 * 
 */
@EqualsAndHashCode
public class AdapterConfig implements CasConfig {

    private final Configuration             adapterConfig;
    private final String                    dcName;
    private final String                    adapterName;
    private final Class<AdNetworkInterface> adapterClass;
    private final ServerConfig              serverConfig;

    @SuppressWarnings("unchecked")
    @AssistedInject
    public AdapterConfig(@Assisted final Configuration adapterConfig, @Assisted final String adapterName,
            @Nullable @Named("dcName") final String dcName, final ServerConfig serverConfig) {
        this.adapterConfig = adapterConfig;
        this.adapterName = adapterName;
        this.dcName = dcName;
        this.serverConfig = serverConfig;

        try {
            this.adapterClass = (Class<AdNetworkInterface>) Class.forName(adapterConfig.getString("class"));
        } catch (ClassNotFoundException e) {
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
     * @return true for explicit wn, false otherwise
     */
    public boolean templateWinNotification() {
        return adapterConfig.getBoolean("templateWinNotification", true);
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
        String status = adapterConfig.getString("status", "on");

        if (status.equalsIgnoreCase("on")) {
            return true;
        } else {
            return false;
        }
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
        String hostName = getAdapterHost();

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

    public boolean isIx(){
        return adapterConfig.getBoolean("isIx",false);
    }

    public int getMaxSegmentSelectionCount() {
        return adapterConfig.getInt("partnerSegmentNo", serverConfig.getMaxPartnerSegmentSelectionCount());
    }
}
