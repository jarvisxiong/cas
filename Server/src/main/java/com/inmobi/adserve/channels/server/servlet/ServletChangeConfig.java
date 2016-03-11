package com.inmobi.adserve.channels.server.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

// import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.utils.CasUtils;
import com.inmobi.adserve.channels.util.InspectorStats;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 *
 * @author ritwik.kumar
 */
@Singleton
// @Path("/configChange")
public class ServletChangeConfig implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletChangeConfig.class);
    private static final String ADAPTER = "adapter.";
    private static final String SERVER = "server.";

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        final Map<String, List<String>> params = queryStringDecoder.parameters();
        JSONObject jObject = null;
        try {
            jObject = CasUtils.extractParams(params, "update"); // requestParser.extractParams(params, "update");
        } catch (final JSONException exeption) {
            LOG.debug("Encountered Json Error while creating json object inside servlet, {}", exeption);
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
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
                String replacedString = null;
                if (configKey.startsWith(ADAPTER)
                        && CasConfigUtil.getAdapterConfig().containsKey(
                                replacedString = configKey.replace(ADAPTER, StringUtils.EMPTY))) {
                    CasConfigUtil.getAdapterConfig().setProperty(replacedString, jObject.getString(configKey));
                    updates.append(configKey).append("=")
                            .append(CasConfigUtil.getAdapterConfig().getString(replacedString)).append("\n");
                } else if (configKey.startsWith(SERVER)
                        && CasConfigUtil.getServerConfig().containsKey(
                                replacedString = configKey.replace(SERVER, StringUtils.EMPTY))) {
                    CasConfigUtil.getServerConfig().setProperty(replacedString, jObject.getString(configKey));
                    updates.append(configKey).append("=")
                            .append(CasConfigUtil.getServerConfig().getString(replacedString)).append("\n");
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
