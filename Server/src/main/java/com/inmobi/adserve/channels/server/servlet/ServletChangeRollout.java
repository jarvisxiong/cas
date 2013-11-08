package com.inmobi.adserve.channels.server.servlet;

import java.util.List;

import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.ServletHandler;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


public class ServletChangeRollout implements Servlet
{

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception
    {
        try {
            List<String> rollout = (queryStringDecoder.getParameters().get("percentRollout"));
            ServletHandler.percentRollout = Integer.parseInt(rollout.get(0));
        }
        catch (NumberFormatException ex) {
            logger.info("invalid attempt to change rollout percentage " + ex);
            hrh.responseSender.sendResponse("INVALIDPERCENT", e);
        }
        InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(ServletHandler.percentRollout));
        logger.debug("new roll out percentage is " + ServletHandler.percentRollout);
        hrh.responseSender.sendResponse("OK", e);
    }

    @Override
    public String getName()
    {
        return "changerollout";
    }
}
