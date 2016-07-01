package com.inmobi.adserve.channels.server;

import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.AdapterConfig;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;


@Singleton
public class SegmentFactory {
    private final static Logger LOG = LoggerFactory.getLogger(SegmentFactory.class);
    private final Map<String, AdapterConfig> advertiserIdConfigMap;
    private final static Long USA_COUNTRY_ID = 94L;

    @Inject
    public SegmentFactory(final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    /**
     * Call the Specific Adapter of the Ad Networks
     */
    public AdNetworkInterface getChannel(final String advertiserId, final Configuration config,
            final Bootstrap dcpClientBootstrap, final Bootstrap rtbClientBootstrap, final HttpRequestHandlerBase base,
            final Channel channel, final Set<String> advertiserSet, final SASRequestParameters sasParam) {
        final AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);
        final String adapterName = adapterConfig.getAdapterName();

        final boolean isSmartRouting = USA_COUNTRY_ID == sasParam.getCountryId() && adapterConfig.isIx();
        final String adapterHost = adapterConfig.getAdapterHost(sasParam, isSmartRouting);
        LOG.debug("adapter host : {}", adapterHost);

        if (!(CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterName))) {
            return null;
        }
        final Class<AdNetworkInterface> adNetworkInterfaceClass = adapterConfig.getAdNetworkInterfaceClass();
        if (adapterConfig.isRtb() || adapterConfig.isIx()) {
            LOG.debug("urlBase is {}", adapterHost);
            try {
                final AdNetworkInterface rtbAdNetwork = adNetworkInterfaceClass
                        .getConstructor(new Class[] {Configuration.class, Bootstrap.class, HttpRequestHandlerBase.class,
                                Channel.class, String.class, String.class})
                        .newInstance(config, rtbClientBootstrap, base, channel, adapterHost, adapterName);
                rtbAdNetwork.setName(adapterName);
                LOG.debug("Created RTB adapter instance for advertiser id : {}", advertiserId);
                return rtbAdNetwork;
            } catch (final Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }
        } else {
            try {
                final AdNetworkInterface adNetworkInterface =
                        adNetworkInterfaceClass
                                .getConstructor(new Class[] {Configuration.class, Bootstrap.class,
                                        HttpRequestHandlerBase.class, Channel.class})
                                .newInstance(config, dcpClientBootstrap, base, channel);
                adNetworkInterface.setName(adapterName);
                return adNetworkInterface;
            } catch (final Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }
        }
    }

}
