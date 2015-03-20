package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.ConnectionLimitHandler;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.utils.JarVersionUtil;
import com.inmobi.adserve.channels.util.InspectorStats;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/stat")
public class ServletStat implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletStat.class);
    private final ConnectionLimitHandler connectionLimitHandler;

    @Inject
    public ServletStat(final ConnectionLimitHandler connectionLimitHandler) {
        this.connectionLimitHandler = connectionLimitHandler;
    }

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("Inside stat servlet");
        final JSONObject manifestJson = new JSONObject(JarVersionUtil.getManifestData());
        final JSONObject connectionJson = connectionLimitHandler.getConnectionJson();

        final JSONObject inspectorJson = InspectorStats.getStatsObj();
        inspectorJson.put("manifestData", manifestJson);
        inspectorJson.put("connectionData", connectionJson);

        hrh.responseSender.sendResponse(inspectorJson.toString(), serverChannel);
    }

    @Override
    public String getName() {
        return "stat";
    }
}
