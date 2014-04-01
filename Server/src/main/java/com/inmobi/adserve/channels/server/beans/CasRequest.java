package com.inmobi.adserve.channels.server.beans;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.Builder;


/**
 * @author abhishek.parwal
 */
@Getter
@ToString
@Builder
@Accessors(fluent = true)
public class CasRequest {

    private final boolean            isKeepAlive;

    // TODO: probably remove later , when unified request
    private final QueryStringDecoder queryStringDecoder;

    // TODO: probably remove later , when unified request
    private final HttpRequest        httpRequest;

    // TODO: probably remove later , when unified response handling
    private final Channel            channel;
}
