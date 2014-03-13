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
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.config.AdapterConfig;


@Singleton
public class SegmentFactory {

    private final static Logger              LOG = LoggerFactory.getLogger(SegmentFactory.class);

    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    @Inject
    public SegmentFactory(final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    public AdNetworkInterface getChannel(final String advertiserId, final String channelId, final Configuration config,
            final Bootstrap dcpClientBootstrap, final Bootstrap rtbClientBootstrap, final HttpRequestHandlerBase base,
            final Channel channel, final Set<String> advertiserSet, final boolean isRtbEnabled,
            final int rtbMaxTimemout, final int dst, final RepositoryHelper repositoryHelper) {

        AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);

        if (!(CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterConfig.getAdapterName()))) {
            return null;
        }

        Class<AdNetworkInterface> adNetworkInterfaceClass = adapterConfig.getAdNetworkInterfaceClass();

        if (adapterConfig.isRtb()) {
            LOG.debug("dcname is {} and urlBase is {}", ChannelServer.dataCentreName, adapterConfig.getAdapterHost());

            try {
                AdNetworkInterface rtbAdNetwork = adNetworkInterfaceClass.getConstructor(
                        new Class[] { Configuration.class, Bootstrap.class, HttpRequestHandlerBase.class,
                                Channel.class, String.class, String.class, int.class, RepositoryHelper.class })
                        .newInstance(config, rtbClientBootstrap, base, channel, adapterConfig.getAdapterHost(),
                                adapterConfig.getAdapterName(), rtbMaxTimemout, repositoryHelper);
                rtbAdNetwork.setName(adapterConfig.getAdapterName());
                LOG.debug("Created RTB adapter instance for advertiser id : {}", advertiserId);
                return rtbAdNetwork;
            }
            catch (Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }

        }
        else {

            try {
                AdNetworkInterface adNetworkInterface = adNetworkInterfaceClass
                        .getConstructor(
                                new Class[] { Configuration.class, Bootstrap.class, HttpRequestHandlerBase.class,
                                        Channel.class }).newInstance(config, dcpClientBootstrap, base, channel);
                adNetworkInterface.setName(adapterConfig.getAdapterName());
                return adNetworkInterface;
            }
            catch (Exception e) {
                LOG.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }
        }

    }

}
