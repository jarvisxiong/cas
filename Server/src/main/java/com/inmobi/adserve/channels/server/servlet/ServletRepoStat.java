package com.inmobi.adserve.channels.server.servlet;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ServletRepoStat implements Servlet{

	@Override
  public void handleRequest(HttpRequestHandler hrh,
      QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger)
      throws Exception {
	  logger.debug("Inside repostat servlet");
	  hrh.responseSender.sendResponse(ServletHandler.repositoryHelper.getRepositoryStatsProvider().getStats(), e);
  }

	@Override
  public String getName() {
	  return "Repostat";
  }

}