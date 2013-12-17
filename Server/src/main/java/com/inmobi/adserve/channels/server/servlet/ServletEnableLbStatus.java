package com.inmobi.adserve.channels.server.servlet;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


public class ServletEnableLbStatus implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletEnableLbStatus.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws JSONException {
        HttpRequest request = (HttpRequest) e.getMessage();
        String host = ServletHandler.getHost(request);
        if (host != null && host.startsWith("localhost")) {
            hrh.responseSender.sendResponse("OK", e);
            ServerStatusInfo.statusCode = 200;
            ServerStatusInfo.statusString = "OK";
            LOG.debug("asked to shut down the server");
        }
        else {
            hrh.responseSender.sendResponse("NOT AUTHORIZED", e);
        }
    }

    @Override
    public String getName() {
        return "enaablelbstatus";
    }

}
