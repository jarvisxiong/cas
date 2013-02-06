package com.inmobi.adserve.channels.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.adnetworks.atnt.ATNTAdNetwork;
import com.inmobi.adserve.channels.adnetworks.drawbridge.DrawBridgeAdNetwork;
import com.inmobi.adserve.channels.adnetworks.httpool.DCPHttPoolAdNetwork;
import com.inmobi.adserve.channels.adnetworks.huntmads.DCPHuntmadsAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifc.IFCAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifd.IFDAdNetwork;
import com.inmobi.adserve.channels.adnetworks.lomark.DCPLomarkAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mobilecommerce.MobileCommerceAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mullahmedia.MullahMediaNetwork;
import com.inmobi.adserve.channels.adnetworks.nexage.DCPNexageAdNetwork;
import com.inmobi.adserve.channels.adnetworks.openx.OpenxAdNetwork;
import com.inmobi.adserve.channels.adnetworks.pubmatic.DCPPubmaticAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.adnetworks.siquis.DCPSiquisAdNetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.adnetworks.verve.DCPVerveAdNetwork;
import com.inmobi.adserve.channels.adnetworks.webmoblink.WebmobLinkAdNetwork;
import com.inmobi.adserve.channels.adnetworks.xad.DCPxAdAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;

public class SegmentFactory {

  private static RepositoryHelper repositoryHelper;
  private static Set<String> rtbAdaptersNames = new HashSet<String>();
  public static RepositoryHelper getRepositoryHelper() {
    return repositoryHelper;
  }

  public static void setRepositoryHelper(RepositoryHelper repositoryHelper) {
    SegmentFactory.repositoryHelper = repositoryHelper;
  }

  public static void init(RepositoryHelper repositoryHelper, Configuration adapterConfiguration, Logger logger) {
    SegmentFactory.repositoryHelper = repositoryHelper;
    SegmentFactory.populateRTBAdapterNames(adapterConfiguration, logger);
  }

  private static void populateRTBAdapterNames(Configuration adapterConfiguration, Logger logger) {
    Iterator<String> itr = adapterConfiguration.getKeys();
    while (null != itr && itr.hasNext()) {
      String str = itr.next();
      if(str.endsWith(".advertiserId")) {
        if(adapterConfiguration.getBoolean(str.replace(".advertiserId", ".isRtb"), false)) {
          rtbAdaptersNames.add(str.replace(".advertiserId", ""));
        }
      }
    }
    logger.debug("RTB adapters in the config are" + rtbAdaptersNames.toString());
  }
    
  public static AdNetworkInterface getChannel(String advertiserId, String channelId, Configuration config, ClientBootstrap clientBootstrap,
      ClientBootstrap rtbClientBootstrap, HttpRequestHandlerBase base, MessageEvent serverEvent, Set<String> advertiserSet, DebugLogger logger,
      boolean isRtbEnabled, CasInternalRequestParameters casInternalRequestParameters) {
    if(isRtbEnabled) {
      for (String partnerName : rtbAdaptersNames) {
        if((advertiserId.equalsIgnoreCase(config.getString(partnerName + ".advertiserId")))
            && (null == advertiserSet || advertiserSet.isEmpty() || advertiserSet.contains(partnerName))
            && (config.getString(partnerName + ".status").equalsIgnoreCase("on") && config.getBoolean(partnerName
                + ".isRtb", false))) {
          String urlBase = config.getString(partnerName + ".host." + ChannelServer.dataCentreName);
          // Disabled request for a particular colo will be drpped here.
          if(urlBase != null && urlBase.equalsIgnoreCase("NA")) {
            logger.debug("RTB requests are disabled for", ChannelServer.dataCentreName.toString(),
                "colo so returning null");
            return null;
          }
          // Use default host if colo specific host is not specified in the
          // config. Return null if default is also not specified.
          if(null == urlBase && config.getString(partnerName + ".host.default", null) == null) {
            logger.debug("Default urlBase is not defined in config so returning null");
            return null;
          } else {
            urlBase = config.getString(partnerName + ".host.default", null);
          }
          RtbAdNetwork rtbAdNetwork = new RtbAdNetwork(logger, config, rtbClientBootstrap, base, serverEvent, urlBase,
              partnerName, casInternalRequestParameters);
          logger.debug("Created RTB adapter instance for advertiser id : " + advertiserId);
          return rtbAdNetwork;
        }
      }
    }

    if((advertiserId.equals(config.getString("atnt.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("atnt"))
        && (config.getString("atnt.status").equals("on"))) {
      return new ATNTAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("mobilecommerce.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("mobilecommerce"))
        && (config.getString("mobilecommerce.status").equals("on"))) {
      return new MobileCommerceAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("drawbridge.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("drawbridge"))
        && (config.getString("drawbridge.status").equals("on"))) {
      return new DrawBridgeAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("mullahmedia.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("mullahmedia"))
        && (config.getString("mullahmedia.status").equals("on"))) {
      return new MullahMediaNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("openx.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("openx"))
        && (config.getString("openx.status").equals("on"))) {
      return new OpenxAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("ifd.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("ifd"))
        && (config.getString("ifd.status").equals("on"))) {
      return new IFDAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("tapit.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("tapit"))
        && (config.getString("tapit.status").equals("on"))) {
      return new DCPTapitAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("ifc.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("ifc"))
        && (config.getString("ifc.status").equals("on"))) {
      return new IFCAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("webmoblink.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("webmoblink"))
        && (config.getString("webmoblink.status").equals("on"))) {
      return new WebmobLinkAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("siquis.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("siquis"))
        && (config.getString("siquis.status").equals("on"))) {
      return new DCPSiquisAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("huntmads.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("huntmads"))
        && (config.getString("huntmads.status").equals("on"))) {
      return new DCPHuntmadsAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("httpool.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("httpool"))
        && (config.getString("httpool.status").equals("on"))) {
      return new DCPHttPoolAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("xad.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("xad"))
        && (config.getString("xad.status").equals("on"))) {
      return new DCPxAdAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("verve.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("verve"))
        && (config.getString("verve.status").equals("on"))) {
      return new DCPVerveAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("lomark.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("lomark"))
        && (config.getString("lomark.status").equals("on"))) {
      return new DCPLomarkAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("pubmatic.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("pubmatic"))
            && (config.getString("pubmatic.status").equals("on"))) {
      return new DCPPubmaticAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    } else if((advertiserId.equals(config.getString("nexage.advertiserId"))) && (advertiserSet.isEmpty() || advertiserSet.contains("nexage"))
        && (config.getString("nexage.status").equals("on"))) {
      return new DCPNexageAdNetwork(logger, config, clientBootstrap, base, serverEvent);
    }
    return null;
  }
}
