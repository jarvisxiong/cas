package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.Logging;


@Singleton
@Path("/mapsizes")
public class ServletMapsizes implements Servlet {

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        ConnectionLimitHandler incomingConnectionLimitHandler = e.getChannel().getPipeline()
                .get(ConnectionLimitHandler.class);
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("ResponseMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("StatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("AdStatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("SampledAdvertiserLog", Logging.getSampledadvertiserlognos().size());
        if (null != incomingConnectionLimitHandler) {
            mapsizes.put("IncomingMaxConnections", incomingConnectionLimitHandler.getMaxConnectionsLimit());
            mapsizes.put("IncomingDroppedConnections", incomingConnectionLimitHandler.getDroppedConnections());
            mapsizes.put("IncomingActiveConnections", incomingConnectionLimitHandler.getActiveConnections());
        }
        hrh.responseSender.sendResponse(mapsizes.toString(), e);
    }

    @Override
    public String getName() {
        return "mapsizes";
    }
}
