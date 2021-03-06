package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/errorDetails")
public class ServletErrorDetails implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletErrorDetails.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("Inside errorDetails servlet");
        hrh.responseSender.sendResponse(CasConfigUtil.repositoryHelper.getRepositoryStatsProvider().getErrorDetails(),
                serverChannel);
    }

    @Override
    public String getName() {
        return "ErrorDetailstat";
    }

}
