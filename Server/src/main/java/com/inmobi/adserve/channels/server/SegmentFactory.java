package com.inmobi.adserve.channels.server;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

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
import com.inmobi.adserve.channels.api.config.AdapterConfig;


@Singleton
public class SegmentFactory {
    private final static Logger LOG = LoggerFactory.getLogger(SegmentFactory.class);
    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    @Inject
    public SegmentFactory(final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    public AdNetworkInterface getChannel(final String advertiserId, final Configuration config,
            final Bootstrap dcpClientBootstrap, final Bootstrap rtbClientBootstrap, final HttpRequestHandlerBase base,
            final Channel channel, final Set<String> advertiserSet) {

        final AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);

        if (!(CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterConfig.getAdapterName()))) {
            return null;
        }

        final Class<AdNetworkInterface> adNetworkInterfaceClass = adapterConfig.getAdNetworkInterfaceClass();
        
        final int tmaxForAdapter = adapterConfig.getTMAX();

        if (adapterConfig.isRtb() || adapterConfig.isIx()) {
            LOG.debug("dcname is {} and urlBase is {}", ChannelServer.dataCentreName, adapterConfig.getAdapterHost());

            try {

                LOG.debug("adapterConfig.templateWinNotification() {}", adapterConfig.templateWinNotification());
                final AdNetworkInterface rtbAdNetwork =
                        adNetworkInterfaceClass.getConstructor(
                                new Class[] {Configuration.class, Bootstrap.class, HttpRequestHandlerBase.class,
                                        Channel.class, String.class, String.class, int.class, boolean.class}).newInstance(config, rtbClientBootstrap, base, channel,
                                adapterConfig.getAdapterHost(), adapterConfig.getAdapterName(), tmaxForAdapter,
                                adapterConfig.templateWinNotification());
                rtbAdNetwork.setName(adapterConfig.getAdapterName());
                LOG.debug("Created RTB adapter instance for advertiser id : {}", advertiserId);
                return rtbAdNetwork;
            } catch (final Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }

        } else {

            try {
                final AdNetworkInterface adNetworkInterface =
                        adNetworkInterfaceClass.getConstructor(
                                new Class[] {Configuration.class, Bootstrap.class, HttpRequestHandlerBase.class,
                                        Channel.class}).newInstance(config, dcpClientBootstrap, base, channel);
                adNetworkInterface.setName(adapterConfig.getAdapterName());
                return adNetworkInterface;
            } catch (final Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }
        }

    }

}
