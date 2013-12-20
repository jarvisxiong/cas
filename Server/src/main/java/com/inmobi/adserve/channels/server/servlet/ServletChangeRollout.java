package com.inmobi.adserve.channels.server.servlet;

import java.util.List;

import javax.ws.rs.Path;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/changerollout")
public class ServletChangeRollout implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeRollout.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final MessageEvent e) throws Exception {
        try {
            List<String> rollout = (queryStringDecoder.getParameters().get("percentRollout"));
            ServletHandler.percentRollout = Integer.parseInt(rollout.get(0));
        }
        catch (NumberFormatException ex) {
            LOG.info("invalid attempt to change rollout percentage {}", ex);
            hrh.responseSender.sendResponse("INVALIDPERCENT", e);
        }
        InspectorStats.setWorkflowStats(InspectorStrings.percentRollout, Long.valueOf(ServletHandler.percentRollout));
        LOG.debug("new roll out percentage is {}", ServletHandler.percentRollout);
        hrh.responseSender.sendResponse("OK", e);
    }

    @Override
    public String getName() {
        return "changerollout";
    }
}
