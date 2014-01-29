package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;


@Singleton
@Path("/errorDetails")
public class ServletErrorDetails implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletErrorDetails.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        LOG.debug("Inside repostat servlet");
        hrh.responseSender.sendResponse(ServletHandler.repositoryHelper.getRepositoryStatsProvider().getErrorDetails(),
            e);
    }

    @Override
    public String getName() {
        return "ErrorDetailstat";
    }

}
