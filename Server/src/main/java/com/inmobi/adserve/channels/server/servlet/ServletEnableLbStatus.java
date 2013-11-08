package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.ServletHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.util.DebugLogger;


public class ServletEnableLbStatus implements Servlet
{

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws JSONException
    {
        HttpRequest request = (HttpRequest) e.getMessage();
        String host = ServletHandler.getHost(request);
        if (host != null && host.startsWith("localhost")) {
            hrh.responseSender.sendResponse("OK", e);
            ServerStatusInfo.statusCode = 200;
            ServerStatusInfo.statusString = "OK";
            logger.debug("asked to shut down the server");
        }
        else {
            hrh.responseSender.sendResponse("NOT AUTHORIZED", e);
        }
    }

    @Override
    public String getName()
    {
        return "enaablelbstatus";
    }

}
