package com.inmobi.adserve.channels.server;

import java.util.Iterator;
import java.util.Set;

import lombok.EqualsAndHashCode;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.util.ConfigurationLoader;


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

    @SuppressWarnings("unchecked")
    public BaseAdapterConfig(final Configuration adapterConfig, final String adapterName, final String dcName) {
        this.adapterConfig = adapterConfig;
        this.adapterName = adapterName;
        this.dcName = dcName;

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

    /**
     * @return the adapterType
     */
    @Override
    public AdapterType getAdapterType() {
        boolean isRtb = adapterConfig.getBoolean("isRtb", false);

        if (isRtb) {
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

    public static void main(final String[] args) {

        ConfigurationLoader obj = ConfigurationLoader.getInstance("/opt/mkhoj/conf/cas/channel-server.properties");

        Configuration adapterConfiguration = obj.adapterConfiguration();

        Iterator<String> keyIterator = adapterConfiguration.getKeys();

        Set<String> adapterNames = Sets.newHashSet();

        while (keyIterator.hasNext()) {
            String key = keyIterator.next();

            String adapterName = key.substring(0, key.indexOf("."));
            adapterNames.add(adapterName);
        }

        for (String adapterName : adapterNames) {
            Configuration adapterConfig = adapterConfiguration.subset(adapterName);

            BaseAdapterConfig c = new BaseAdapterConfig(adapterConfig, adapterName, null);

            System.out.println("  " + c.getAdapterHost() + "  " + c.getAdapterName() + "  " + c.getAdapterType() + "  "
                    + c.getAdNetworkInterfaceClass() + "  " + c.getAdvertiserId());
        }
    }
}
