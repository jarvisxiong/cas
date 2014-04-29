package com.inmobi.adserve.channels.util.httpclient;

import java.net.URI;
import java.util.Map;

import lombok.Getter;

import com.google.common.collect.Maps;


/**
 * @author abhishek.parwal
 * 
 */
public class NettyRequest {
    @Getter
    private URI                       uri;
    @Getter
    private final Map<String, String> headerMap        = Maps.newHashMap();
    @Getter
    private NettyRequestType          nettyRequestType = NettyRequestType.GET;

    public NettyRequest setUri(final URI uri) {
        this.uri = uri;
        return this;
    }

    public NettyRequest setHeader(final String name, final String value) {
        headerMap.put(name, value);
        return this;
    }

    public NettyRequest setRequestType(final NettyRequestType nettyRequestType) {
        this.nettyRequestType = nettyRequestType;
        return this;
    }

    public enum NettyRequestType {
        GET,
        POST;
    }
}
