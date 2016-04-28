package com.inmobi.adserve.channels.server.servlet;

import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.Path;

import org.apache.commons.configuration.ConfigurationConverter;

import com.google.gson.Gson;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.api.Servlet;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;


@Singleton
@Path("/getServerConfig")
public class ServletGetServerConfig implements Servlet {
    private static final Gson GSON = new Gson();

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel serverChannel) throws Exception {
        final Map<?, ?> adapterConfigMap = new TreeMap(ConfigurationConverter.getMap(CasConfigUtil.getServerConfig()));
        hrh.responseSender.sendResponse(GSON.toJson(adapterConfigMap), serverChannel);
    }

    @Override
    public String getName() {
        return "getServerConfig";
    }
}
