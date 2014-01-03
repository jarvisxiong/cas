package com.inmobi.adserve.channels.server;

import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
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
            final ClientBootstrap clientBootstrap, final ClientBootstrap rtbClientBootstrap,
            final HttpRequestHandlerBase base, final MessageEvent serverEvent, final Set<String> advertiserSet,
            final boolean isRtbEnabled, final int rtbMaxTimemout, final int dst, final RepositoryHelper repositoryHelper) {

        AdapterConfig adapterConfig = advertiserIdConfigMap.get(advertiserId);

        boolean isRtb = adapterConfig.isRtb();

        if (!(CollectionUtils.isEmpty(advertiserSet) || advertiserSet.contains(adapterConfig.getAdapterName()))
                || ((6 == dst) && !isRtb) || (isRtb && !isRtbEnabled)) {
            return null;
        }

        Class<AdNetworkInterface> adNetworkInterfaceClass = adapterConfig.getAdNetworkInterfaceClass();

        if (adapterConfig.isRtb()) {
            LOG.debug("dcname is {} and urlBase is {}", ChannelServer.dataCentreName, adapterConfig.getAdapterHost());

            try {
                AdNetworkInterface rtbAdNetwork = adNetworkInterfaceClass.getConstructor(
                        new Class[] { Configuration.class, ClientBootstrap.class, HttpRequestHandlerBase.class,
                                MessageEvent.class, String.class, String.class, int.class, RepositoryHelper.class })
                        .newInstance(config, rtbClientBootstrap, base, serverEvent, adapterConfig.getAdapterHost(),
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
                AdNetworkInterface adNetworkInterface = adNetworkInterfaceClass.getConstructor(
                        new Class[] { Configuration.class, ClientBootstrap.class, HttpRequestHandlerBase.class,
                                MessageEvent.class }).newInstance(config, clientBootstrap, base, serverEvent);
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
