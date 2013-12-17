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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.adnetworks.adelphic.DCPAdelphicAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ajillion.DCPAjillionAdnetwork;
import com.inmobi.adserve.channels.adnetworks.amobee.DCPAmobeeAdnetwork;
import com.inmobi.adserve.channels.adnetworks.appier.DCPAppierAdNetwork;
import com.inmobi.adserve.channels.adnetworks.appnexus.DCPAppNexusAdnetwork;
import com.inmobi.adserve.channels.adnetworks.atnt.ATNTAdNetwork;
import com.inmobi.adserve.channels.adnetworks.drawbridge.DrawBridgeAdNetwork;
import com.inmobi.adserve.channels.adnetworks.httpool.DCPHttPoolAdNetwork;
import com.inmobi.adserve.channels.adnetworks.huntmads.DCPHuntmadsAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifc.IFCAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifd.IFDAdNetwork;
import com.inmobi.adserve.channels.adnetworks.logan.DCPLoganAdnetwork;
import com.inmobi.adserve.channels.adnetworks.lomark.DCPLomarkAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mable.DCPMableAdnetwork;
import com.inmobi.adserve.channels.adnetworks.madnet.DCPMadNetAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mobfox.DCPMobFoxAdnetwork;
import com.inmobi.adserve.channels.adnetworks.mobilecommerce.MobileCommerceAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mopub.DCPMoPubAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mullahmedia.MoolahMediaPremiumAdnetwork;
import com.inmobi.adserve.channels.adnetworks.mullahmedia.MullahMediaNetwork;
import com.inmobi.adserve.channels.adnetworks.nexage.DCPNexageAdNetwork;
import com.inmobi.adserve.channels.adnetworks.openx.OpenxAdNetwork;
import com.inmobi.adserve.channels.adnetworks.paypal.DCPPayPalAdNetwork;
import com.inmobi.adserve.channels.adnetworks.placeiq.DCPPlaceIQAdnetwork;
import com.inmobi.adserve.channels.adnetworks.pubmatic.DCPPubmaticAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.adnetworks.siquis.DCPSiquisAdNetwork;
import com.inmobi.adserve.channels.adnetworks.smaato.DCPSmaatoAdnetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.adnetworks.verve.DCPVerveAdNetwork;
import com.inmobi.adserve.channels.adnetworks.wapstart.DCPWapStartAdNetwork;
import com.inmobi.adserve.channels.adnetworks.webmoblink.WebmobLinkAdNetwork;
import com.inmobi.adserve.channels.adnetworks.widerplanet.DCPWiderPlanetAdnetwork;
import com.inmobi.adserve.channels.adnetworks.xad.DCPxAdAdNetwork;
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

        if(adapterConfig == null){
	        return null;
        }

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
