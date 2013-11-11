package com.inmobi.adserve.channels.server.api;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.DebugLogger;


public interface Servlet {

    void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e, DebugLogger logger)
            throws Exception;

    String getName();

}
