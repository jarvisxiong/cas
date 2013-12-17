package com.inmobi.adserve.channels.server.servlet;

import org.apache.commons.configuration.ConfigurationConverter;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.DebugLogger;

public class ServletGetServerConfig implements Servlet {

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger) throws Exception {
        hrh.responseSender.sendResponse(new JSONObject(ConfigurationConverter.getMap(ServletHandler.getServerConfig())).toString(), e);
    }

    @Override
    public String getName() {
        return "getServerConfig";
    }
}
