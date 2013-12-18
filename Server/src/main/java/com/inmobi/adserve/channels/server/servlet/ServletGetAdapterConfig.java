package com.inmobi.adserve.channels.server.servlet;

import org.apache.commons.configuration.ConfigurationConverter;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


public class ServletGetAdapterConfig implements Servlet {

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        hrh.responseSender.sendResponse(
            new JSONObject(ConfigurationConverter.getMap(ServletHandler.getAdapterConfig())).toString(), e);
    }

    @Override
    public String getName() {
        return "getAdapterConfig";
    }
}
