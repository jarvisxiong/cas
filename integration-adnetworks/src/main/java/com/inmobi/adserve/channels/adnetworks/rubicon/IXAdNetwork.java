package com.inmobi.adserve.channels.adnetworks.rubicon;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;

import org.apache.commons.configuration.Configuration;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

public class IXAdNetwork extends RtbAdNetwork {

	public IXAdNetwork(Configuration config, Bootstrap clientBootstrap, HttpRequestHandlerBase baseRequestHandler, Channel serverChannel, String urlBase,
			String advertiserName, int tmax, RepositoryHelper repositoryHelper, boolean templateWinNotification) {
		super(config, clientBootstrap, baseRequestHandler, serverChannel, urlBase, advertiserName, tmax, repositoryHelper, templateWinNotification);
	}

}
