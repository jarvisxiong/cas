package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.apache.commons.configuration.ConfigurationConverter;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
@Path("/getServerConfig")
public class ServletGetServerConfig implements Servlet {

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        hrh.responseSender.sendResponse(
            new JSONObject(ConfigurationConverter.getMap(ServletHandler.getServerConfig())).toString(), e);
    }

    @Override
    public String getName() {
        return "getServerConfig";
    }
}
