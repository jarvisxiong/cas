package com.inmobi.adserve.channels.server;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;

import com.inmobi.adserve.channels.adnetworks.atnt.ATNTAdNetwork;
import com.inmobi.adserve.channels.adnetworks.drawbridge.DrawBridgeAdNetwork;
import com.inmobi.adserve.channels.adnetworks.generic.GenericAdapter;
import com.inmobi.adserve.channels.adnetworks.httpool.DCPHttPoolAdNetwork;
import com.inmobi.adserve.channels.adnetworks.huntmads.DCPHuntmadsAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifc.IFCAdNetwork;
import com.inmobi.adserve.channels.adnetworks.ifd.IFDAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mobilecommerce.MobileCommerceAdNetwork;
import com.inmobi.adserve.channels.adnetworks.mullahmedia.MullahMediaNetwork;
import com.inmobi.adserve.channels.adnetworks.openx.OpenxAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.adnetworks.siquis.DCPSiquisAdNetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.adnetworks.verve.DCPVerveAdNetwork;
import com.inmobi.adserve.channels.adnetworks.webmoblink.WebmobLinkAdNetwork;
import com.inmobi.adserve.channels.adnetworks.xad.DCPxAdAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.entity.ChannelEntity;
import com.inmobi.adserve.channels.repository.ChannelRepository;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.phoenix.exception.RepositoryException;

public class SegmentFactory {

  private static RepositoryHelper repositoryHelper;

  public static RepositoryHelper getRepositoryHelper() {
    return repositoryHelper;
  }

  public static void setRepositoryHelper(RepositoryHelper repositoryHelper) {
    SegmentFactory.repositoryHelper = repositoryHelper;
  }

  public static void init(RepositoryHelper repositoryHelper) {
    SegmentFactory.repositoryHelper = repositoryHelper;
  }

  public static AdNetworkInterface getChannel(String advertiserId, String channelId, Configuration config, ClientBootstrap clientBootstrap,
      ClientBootstrap rtbClientBootstrap, HttpRequestHandlerBase base, MessageEvent serverEvent, Set<String> advertiserSet, DebugLogger logger,
      boolean isRtbEnabled) {
    ChannelEntity channelEntity = repositoryHelper.queryChannelRepository(channelId);
    // once we have values in Db use the following declaration
    // if(isRtbEnabled && channelEntity != null && channelEntity.isRtb()) {
    if(isRtbEnabled) {
      // following code will be enabled once we have entries in DB
      logger.debug("Creating RTB adapter instance for advertiser id : " + advertiserId);

      // RtbAdNetwork rtbAdNetwork = new RtbAdNetwork(logger, config,
      // rtbClientBootstrap, base, serverEvent, channelEntity.getUrlBase(),
      // channelEntity.getUrlArg(), channelEntity.getRtbMethod(),
      // channelEntity.getRtbVer(), channelEntity.getWnUrl(),
      // channelEntity.getAccountId(), channelEntity.isWnRequied(),
      // channelEntity.isWnFromClient());
      // return rtbAdNetwork;
      //

      if((advertiserId.equalsIgnoreCase(config.getString("rtbAdvertiserName.advertiserId")))
          && (null == advertiserSet || advertiserSet.isEmpty() || advertiserSet.contains("rtbAdvertiserName"))
          && (config.getString("rtbAdvertiserName.status").equalsIgnoreCase("on"))) {
        RtbAdNetwork rtbAdNetwork = new RtbAdNetwork(logger, config, rtbClientBootstrap, base, serverEvent, config.getString("rtbAdvertiserName.urlBase"),
            config.getString("rtbAdvertiserName.urlArg"), config.getString("rtbAdvertiserName.rtbMethod"), config.getString("rtbAdvertiserName.rtbVer"),
            config.getString("rtbAdvertiserName.wnUrlback"), config.getString("rtbAdvertiserName.accountId"),
            config.getBoolean("rtbAdvertiserName.isWnRequired"), config.getBoolean("rtbAdvertiserName.isWinFromClient"));
        return rtbAdNetwork;
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
    }

    // else {
    // logger.debug("going in generic adapter for advId" + advertiserId);
    // String advertiserName = "";
    // Iterator itr = config.getKeys();
    // while (itr.hasNext()) {
    // String key = itr.next().toString();
    // if(config.getString(key).equals(advertiserId) &&
    // key.endsWith(".advertiserId")) {
    // advertiserName = key.replace(".advertiserId", "");
    // break;
    // }
    // }
    // if(!advertiserName.equals("") && config.getString(advertiserName +
    // ".status").equals("on")
    // && (advertiserSet.isEmpty() || advertiserSet.contains(advertiserName))) {
    // return new GenericAdapter(logger, config, clientBootstrap, base,
    // serverEvent, advertiserName);
    // }
    // }
    // logger.debug("no genric adapter");

    return null;
  }
}
