package com.inmobi.adserve.channels.server;

import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.google.inject.Inject;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;


@Singleton
public class SegmentFactory {

    private final Map<String, AdapterConfig> advertiserIdConfigMap;

    @Inject
    public SegmentFactory(final Map<String, AdapterConfig> advertiserIdConfigMap) {
        this.advertiserIdConfigMap = advertiserIdConfigMap;
    }

    public AdNetworkInterface getChannel(final String advertiserId, final String channelId, final Configuration config,
            final ClientBootstrap clientBootstrap, final ClientBootstrap rtbClientBootstrap,
            final HttpRequestHandlerBase base, final MessageEvent serverEvent, final Set<String> advertiserSet,
            final DebugLogger logger, final boolean isRtbEnabled, final int rtbMaxTimemout, final int dst,
            final RepositoryHelper repositoryHelper) {

        AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);

        if (isRtbEnabled) {

            if ((CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterConfig.getAdapterName()))
                    && adapterConfig.isActive() && adapterConfig.getAdapterType() == AdapterType.RTB) {
                logger.debug("dcname is {} and urlBase is {}", ChannelServer.dataCentreName,
                    adapterConfig.getAdapterHost());
                RtbAdNetwork rtbAdNetwork = new RtbAdNetwork(logger, config, rtbClientBootstrap, base, serverEvent,
                        adapterConfig.getAdapterHost(), adapterConfig.getAdapterName(), rtbMaxTimemout,
                        repositoryHelper);
                logger.debug("Created RTB adapter instance for advertiser id : {}", advertiserId);
                return rtbAdNetwork;
            }
        }

        if (6 == dst) {
            logger.debug("Request came from rule engine so not going through dcp adapter selection list");
            return null;
        }

        if ((CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterConfig.getAdapterName()))
                && adapterConfig.isActive()) {
            Class<AdNetworkInterface> adNetworkInterfaceClass = adapterConfig.getAdNetworkInterfaceClass();
            try {
                AdNetworkInterface adNetworkInterface = adNetworkInterfaceClass.getConstructor(
                    new Class[] { DebugLogger.class, Configuration.class, ClientBootstrap.class,
                            HttpRequestHandlerBase.class, MessageEvent.class }).newInstance(logger, config,
                    clientBootstrap, base, serverEvent);
                adNetworkInterface.setName(adapterConfig.getAdapterName());
                return adNetworkInterface;
            }
            catch (Exception e) {
                logger.error("Error instantiating adapter");
                throw new RuntimeException(e);
            }
        }

        return null;
    }

}
