package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.IncomingConnectionLimitHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.client.BootstrapCreation;
import com.inmobi.adserve.channels.server.client.RtbBootstrapCreation;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ServletChangeConfig implements Servlet {

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception {
        Map<String, List<String>> params = queryStringDecoder.getParameters();
        JSONObject jObject = null;
        try {
            jObject = RequestParser.extractParams(params, "update");
        }
        catch (JSONException exeption) {
            logger.debug("Encountered Json Error while creating json object inside servlet");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }
        if (jObject == null) {
            logger.debug("jobject is null so returning");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            hrh.responseSender.sendResponse("Incorrect Json", e);
            return;
        }
        logger.debug("Successfully got json for config change");
        try {
            StringBuilder updates = new StringBuilder();
            updates.append("Successfully changed Config!!!!!!!!!!!!!!!!!\n").append("The changes are\n");
            @SuppressWarnings("unchecked")
            Iterator<String> itr = jObject.keys();
            while (itr.hasNext()) {
                String configKey = itr.next().toString();
                if (configKey.startsWith("adapter")
                        && ServletHandler.getAdapterConfig().containsKey(configKey.replace("adapter.", ""))) {
                    ServletHandler.getAdapterConfig().setProperty(configKey.replace("adapter.", ""),
                        jObject.getString(configKey));
                    updates.append(configKey)
                                .append("=")
                                .append(ServletHandler.getAdapterConfig().getString(configKey.replace("adapter.", "")))
                                .append("\n");
                }
                if (configKey.startsWith("server")
                        && ServletHandler.getServerConfig().containsKey(configKey.replace("server.", ""))) {
                    ServletHandler.getServerConfig().setProperty(configKey.replace("server.", ""),
                        jObject.getString(configKey));
                    if (configKey.replace("server.", "").equals("maxconnections")) {
                        BootstrapCreation.setMaxConnectionLimit(ServletHandler.getServerConfig().getInt(
                            configKey.replace("server.", "")));
                    }
                    if (configKey.replace("server.", "").equals("incomingMaxConnections")) {
                        IncomingConnectionLimitHandler.setIncomingMaxConnections(ServletHandler.getServerConfig().getInt(
                                configKey.replace("server.", "")));
                    }
                    updates.append(configKey)
                                .append("=")
                                .append(ServletHandler.getServerConfig().getString(configKey.replace("server.", "")))
                                .append("\n");
                }
                if (configKey.startsWith("rtb")
                        && ServletHandler.getServerConfig().containsKey(configKey.replace("rtb.", ""))) {
                    ServletHandler.getServerConfig().setProperty(configKey.replace("rtb.", ""),
                            jObject.getString(configKey));
                    if (configKey.replace("rtb.", "").equals("maxconnections")) {
                        RtbBootstrapCreation.setMaxConnectionLimit(ServletHandler.getServerConfig().getInt(
                                configKey.replace("rtb.", "")));
                    }
                    updates.append(configKey)
                            .append("=")
                            .append(ServletHandler.getServerConfig().getString(configKey.replace("rtb.", "")))
                            .append("\n");
                }
                
            }
            hrh.responseSender.sendResponse(updates.toString(), e);
        }
        catch (JSONException ex) {
            logger
                    .debug("Encountered Json Error while creating json object inside HttpRequest Handler for config change");
            hrh.terminationReason = ServletHandler.jsonParsingError;
        }
    }

    @Override
    public String getName() {
        return "configchange";
    }

}
