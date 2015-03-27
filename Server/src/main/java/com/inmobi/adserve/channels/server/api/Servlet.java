package com.inmobi.adserve.channels.server.api;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;

import com.inmobi.adserve.channels.server.HttpRequestHandler;


public interface Servlet {

    /**
     * 
     * @param hrh
     * @param queryStringDecoder
     * @param severChannel
     * @throws Exception
     */
    void handleRequest(final HttpRequestHandler hrh, final QueryStringDecoder queryStringDecoder,
            final Channel severChannel) throws Exception;

    /**
     * 
     * @return
     */
    String getName();

}
