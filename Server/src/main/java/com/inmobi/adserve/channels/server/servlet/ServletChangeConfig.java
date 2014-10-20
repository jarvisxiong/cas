package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Path;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Singleton
@Path("/configChange")
public class ServletChangeConfig implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeConfig.class);
    private final RequestParser requestParser;

    @Inject
    ServletChangeConfig(final RequestParser requestParser) {
        this.requestParser = requestParser;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {

        final Map<String, List<String>> params = queryStringDecoder.parameters();
        JSONObject jObject = null;
        try {
            jObject = requestParser.extractParams(params, "update");
        } catch (final JSONException exeption) {
            LOG.debug("Encountered Json Error while creating json object inside servlet, {}", exeption);
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
            return;
        }
        if (jObject == null) {
            LOG.debug("jobject is null so returning");
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            hrh.responseSender.sendResponse("Incorrect Json", serverChannel);
            return;
        }
        LOG.debug("Successfully got json for config change");
        try {
            final StringBuilder updates = new StringBuilder();
            updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
            @SuppressWarnings("unchecked")
            final Iterator<String> itr = jObject.keys();
            while (itr.hasNext()) {
                final String configKey = itr.next().toString();
                if (configKey.startsWith("adapter")
                        && CasConfigUtil.getAdapterConfig().containsKey(configKey.replace("adapter.", ""))) {
                    CasConfigUtil.getAdapterConfig().setProperty(configKey.replace("adapter.", ""),
                            jObject.getString(configKey));
                    updates.append(configKey).append("=")
                            .append(CasConfigUtil.getAdapterConfig().getString(configKey.replace("adapter.", "")))
                            .append("\n");
                } else if (configKey.startsWith("server")
                        && CasConfigUtil.getServerConfig().containsKey(configKey.replace("server.", ""))) {
                    CasConfigUtil.getServerConfig().setProperty(configKey.replace("server.", ""),
                            jObject.getString(configKey));
                    updates.append(configKey).append("=")
                            .append(CasConfigUtil.getServerConfig().getString(configKey.replace("server.", "")))
                            .append("\n");
                } else if (configKey.startsWith("resetTimers")) {
                    InspectorStats.resetTimers();
                }
            }
            hrh.responseSender.sendResponse(updates.toString(), serverChannel);
        } catch (final JSONException ex) {
            LOG.debug(
                    "Encountered Json Error while creating json object inside HttpRequest Handler for config change, exception raised {}",
                    ex);
            hrh.responseSender.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
        }
    }

    @Override
    public String getName() {
        return "configchange";
    }

}
