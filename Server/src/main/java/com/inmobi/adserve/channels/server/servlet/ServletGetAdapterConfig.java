package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.apache.commons.configuration.ConfigurationConverter;
import org.json.JSONObject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
@Path("/getAdapterConfig")
public class ServletGetAdapterConfig implements Servlet {

  @Override
  public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
      final Channel serverChannel) throws Exception {
    hrh.responseSender.sendResponse(
        new JSONObject(ConfigurationConverter.getMap(CasConfigUtil.getAdapterConfig())).toString(), serverChannel);
  }

  @Override
  public String getName() {
    return "getAdapterConfig";
  }
}
