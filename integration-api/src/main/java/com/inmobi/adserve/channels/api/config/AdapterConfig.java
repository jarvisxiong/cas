package com.inmobi.adserve.channels.api.config;

import java.util.Random;

import javax.annotation.Nullable;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.inject.name.Named;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.casthrift.DataCenter;

import lombok.EqualsAndHashCode;


/**
 * @author abhishek.parwal
 */
@EqualsAndHashCode
public class AdapterConfig implements CasConfig {
    private static final String UJ1 = DataCenter.UJ1.name().toLowerCase();
    private static final String UH1 = DataCenter.UH1.name().toLowerCase();
    private static final java.lang.String SANDBOX_HOST = "host.sandbox";
    private final Configuration adapterConfig;
    private final String dcName;
    private final String adapterName;
    private final Class<AdNetworkInterface> adapterClass;
    private final ServerConfig serverConfig;
    private static Integer HUNDRED_IN_PERCENTAGE = 100;
    private static final Random RANDOM = new Random();


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
     * Status == on
     *
     * @return the isActive
     */
    public boolean isActive() {
        final String status = adapterConfig.getString("status", "on");
        return "on".equalsIgnoreCase(status);
    }

    /**
     * @return the adapterHost based on Data Center
     */
    public String getAdapterHost(final String dcName) {
        String adapterHost = adapterConfig.getString("host." + dcName);
        if (StringUtils.isBlank(adapterHost)) {
            adapterHost = adapterConfig.getString("host.default");
        }
        if (StringUtils.isBlank(adapterHost)) {
            adapterHost = adapterConfig.getString("host");
        }
        return adapterHost;
    }

    /**
     * 
     * @param stateCode
     * @return
     */
    public String getDcName(final Integer stateCode) {
        return ((serverConfig.getRoutingUH1ToUJ1Percentage() >= RANDOM.nextInt(HUNDRED_IN_PERCENTAGE))
                && StringUtils.equals(dcName, UH1) && null != stateCode
                && serverConfig.getUSWestStatesCodes().contains(String.valueOf(stateCode))) ? UJ1 : dcName;
    }

    /**
     * @return the adapterHost based on Data Center and stateCode of USA
     */
    public String getAdapterHost(final SASRequestParameters sasParam, final boolean isSmartRouting) {
        return sasParam.isSandBoxRequest() ? adapterConfig.getString(SANDBOX_HOST) : (isSmartRouting ?
                getAdapterHost(getDcName(sasParam.getState())) : getAdapterHost());
    }

    public String getAdapterHost() {
        return getAdapterHost(dcName);
    }

    /**
     * Not blank and not equals to NA
     *
     * @return
     */
    public boolean isValidHost() {
        final String hostName = getAdapterHost();
        return StringUtils.isNotBlank(hostName) && !"NA".equalsIgnoreCase(hostName);
    }


    public boolean isSecureSupported() {
        return adapterConfig.getBoolean("secureSupported", false);
    }

    /**
     * @return the adNetworkInterfaceClass
     */
    public Class<AdNetworkInterface> getAdNetworkInterfaceClass() {
        return adapterClass;
    }

    public boolean isRtb() {
        return adapterConfig.getBoolean("isRtb", false);
    }

    public boolean isIx() {
        return adapterConfig.getBoolean("isIx", false);
    }

    public int getMaxSegmentSelectionCount() {
        return adapterConfig.getInt("partnerSegmentNo", serverConfig.getMaxPartnerSegmentSelectionCount());
    }
}
