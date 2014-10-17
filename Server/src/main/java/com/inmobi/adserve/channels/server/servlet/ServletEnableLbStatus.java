package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
@Path("/enablelbstatus")
public class ServletEnableLbStatus implements Servlet {
  private static final Logger LOG = LoggerFactory.getLogger(ServletEnableLbStatus.class);

  @Override
  public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
      final Channel serverChannel) throws JSONException {
    if (hrh.isRequestFromLocalHost()) {
      hrh.responseSender.sendResponse("OK", serverChannel);
      ServerStatusInfo.statusCode = 200;
      ServerStatusInfo.statusString = "OK";
      LOG.debug("asked to shut down the server");
    } else {
      hrh.responseSender.sendResponse("NOT AUTHORIZED", serverChannel);
    }
  }

  @Override
  public String getName() {
    return "enablelbstatus";
  }

}
