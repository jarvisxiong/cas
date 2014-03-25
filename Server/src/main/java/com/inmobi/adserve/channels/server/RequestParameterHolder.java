package com.inmobi.adserve.channels.server;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RequestParameterHolder {
    private SASRequestParameters sasParams;
    private CasInternalRequestParameters casInternalRequestParameters;
    private String uri;
    private String terminationReason;
    private HttpRequest httpRequest;
}
