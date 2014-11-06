package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.utils.JarVersionUtil;
import com.inmobi.adserve.channels.util.InspectorStats;


@Singleton
@Path("/stat")
@Slf4j
public class ServletStat implements Servlet {
    private final ConnectionLimitHandler connectionLimitHandler;

    @Inject
    public ServletStat(final ConnectionLimitHandler connectionLimitHandler) {
        this.connectionLimitHandler = connectionLimitHandler;
    }


    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        log.debug("Inside stat servlet");
        final JSONObject inspectorJson = InspectorStats.getStatsObj();
        final JSONObject manifestJson = new JSONObject(JarVersionUtil.getManifestData());
        inspectorJson.put("manifestData", manifestJson);
        final JSONObject connectionJson = connectionLimitHandler.getConnectionJson();
        inspectorJson.put("connectionData", connectionJson);

        hrh.responseSender.sendResponse(inspectorJson.toString(), serverChannel);
    }

    @Override
    public String getName() {
        return "stat";
    }
}
