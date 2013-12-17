package com.inmobi.adserve.channels.server.servlet;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.util.InspectorStats;


public class ServletStat implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletStat.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        LOG.debug("Inside stat servlet");
        hrh.responseSender.sendResponse(
            InspectorStats.getStats(BootstrapCreation.getMaxConnections(), BootstrapCreation.getDroppedConnections()),
            e);
    }

    @Override
    public String getName() {
        return "stat";
    }
}
