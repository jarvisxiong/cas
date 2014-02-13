package com.inmobi.adserve.channels.server;


import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class RequestParameterHolder {
    private SASRequestParameters sasParams;
    private CasInternalRequestParameters casInternalRequestParameters;
    private String uri;
    private String terminationReason;
}
