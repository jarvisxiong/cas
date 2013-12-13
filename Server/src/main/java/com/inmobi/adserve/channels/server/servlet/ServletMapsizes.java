package com.inmobi.adserve.channels.server.servlet;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONObject;

import com.inmobi.adserve.channels.api.ChannelsClientHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.server.client.RtbBootstrapCreation;
import com.inmobi.adserve.channels.server.requesthandler.Logging;
import com.inmobi.adserve.channels.util.DebugLogger;


public class ServletMapsizes implements Servlet {

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception {
        JSONObject mapsizes = new JSONObject();
        mapsizes.put("ResponseMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("StatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("AdStatusMap", ChannelsClientHandler.responseMap.size());
        mapsizes.put("SampledAdvertiserLog", Logging.getSampledadvertiserlognos().size());
        mapsizes.put("DCPActiveOutboundConnections", BootstrapCreation.getActiveOutboundConnections());
        mapsizes.put("DCPMaxConnections", BootstrapCreation.getMaxConnections());
        mapsizes.put("DCPDroppedConnections", BootstrapCreation.getDroppedConnections());
        mapsizes.put("RTBDActiveOutboundConnections", RtbBootstrapCreation.getActiveOutboundConnections());
        mapsizes.put("RTBDMaxConnections", RtbBootstrapCreation.getMaxConnections());
        mapsizes.put("RTBDDroppedConnections", RtbBootstrapCreation.getDroppedConnections());
        mapsizes.put("IncomingMaxConnections", IncomingConnectionLimitHandler.getIncomingMaxConnections());
        mapsizes.put("IncomingDroppedConnections", IncomingConnectionLimitHandler.getIncomingDroppedConnections());
        mapsizes.put("IncomingActiveConnections", IncomingConnectionLimitHandler.getIncomingActiveConnections());
        hrh.responseSender.sendResponse(mapsizes.toString(), e);
    }

    @Override
    public String getName() {
        return "mapsizes";
    }
}
