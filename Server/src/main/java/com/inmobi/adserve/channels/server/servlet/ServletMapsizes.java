package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.util.annotations.IncomingConnectionLimitHandler;


@Singleton
@Path("/mapsizes")
public class ServletMapsizes implements Servlet {
    private final ConnectionLimitHandler incomingConnectionLimitHandler;

    @Inject
    public ServletMapsizes(@IncomingConnectionLimitHandler final ConnectionLimitHandler incomingConnectionLimitHandler) {
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("SampledAdvertiserLog", Logging.getSampledadvertiserlognos().size());

        if (null != incomingConnectionLimitHandler) {
            mapsizes.put("IncomingMaxConnections", incomingConnectionLimitHandler.getMaxConnectionsLimit());
            mapsizes.put("IncomingDroppedConnections", incomingConnectionLimitHandler.getDroppedConnections());
            mapsizes.put("IncomingActiveConnections", incomingConnectionLimitHandler.getActiveConnections());
        }
        hrh.responseSender.sendResponse(mapsizes.toString(), serverChannel);
    }

    @Override
    public String getName() {
        return "mapsizes";
    }
}
