package com.inmobi.adserve.channels.server.servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import javax.ws.rs.Path;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.utils.JarVersionUtil;
import com.inmobi.adserve.channels.util.InspectorStats;


@Singleton
@Path("/stat")
public class ServletStat implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletStat.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        LOG.debug("Inside stat servlet");
        final JSONObject inspectorJson = InspectorStats.getStatsObj();
        final JSONObject manifestJson = new JSONObject(JarVersionUtil.getManifestData());
        
        inspectorJson.put("manifestData", manifestJson);
        hrh.responseSender.sendResponse(inspectorJson.toString(), serverChannel);
    }

    @Override
    public String getName() {
        return "stat";
    }
}
