package com.inmobi.adserve.channels.server.api;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;


public interface Servlet {

    void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder, final MessageEvent e)
            throws Exception;

    String getName();

}
