package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.annotations.DcpConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.annotations.RtbConnectionLimitHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.Logging;


@Singleton
@Path("/mapsizes")
public class ServletMapsizes implements Servlet {
    private final ConnectionLimitHandler incomingConnectionLimitHandler;
    private final ConnectionLimitHandler dcpConnectionLimitHandler;
    private final ConnectionLimitHandler rtbConnectionLimitHandler;

    @Inject
    public ServletMapsizes(@IncomingConnectionLimitHandler final ConnectionLimitHandler incomingConnectionLimitHandler,
            @DcpConnectionLimitHandler final ConnectionLimitHandler dcpConnectionLimitHandler,
            @RtbConnectionLimitHandler final ConnectionLimitHandler rtbConnectionLimitHandler) {
        this.incomingConnectionLimitHandler = incomingConnectionLimitHandler;
        this.dcpConnectionLimitHandler = dcpConnectionLimitHandler;
        this.rtbConnectionLimitHandler = rtbConnectionLimitHandler;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("ResponseMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("StatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("AdStatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("SampledAdvertiserLog", Logging.getSampledadvertiserlognos().size());
        mapsizes.put("DCPActiveOutboundConnections", dcpConnectionLimitHandler.getActiveConnections());
        mapsizes.put("DCPMaxConnections", dcpConnectionLimitHandler.getMaxConnectionsLimit());
        mapsizes.put("DCPDroppedConnections", dcpConnectionLimitHandler.getDroppedConnections());
        mapsizes.put("RTBDActiveOutboundConnections", rtbConnectionLimitHandler.getActiveConnections());
        mapsizes.put("RTBDMaxConnections", rtbConnectionLimitHandler.getMaxConnectionsLimit());
        mapsizes.put("RTBDDroppedConnections", rtbConnectionLimitHandler.getDroppedConnections());
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
