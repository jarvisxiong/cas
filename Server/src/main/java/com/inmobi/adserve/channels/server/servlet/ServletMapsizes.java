package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.CreativeCache;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import javax.ws.rs.Path;


@Singleton
@Path("/mapsizes")
public class ServletMapsizes implements Servlet {
    private final ConnectionLimitHandler incomingConnectionLimitHandler;

    @Inject
    public ServletMapsizes(final ConnectionLimitHandler incomingConnectionLimitHandler) {
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("SampledAdvertiserLog", Logging.getSampledadvertiserlognos().size());
        mapsizes.put("SampledAdvertiserMap", Logging.sampledAdvertiserLogNos);
        mapsizes.put("creativeCache", CreativeCache.creativeCache.size());
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
