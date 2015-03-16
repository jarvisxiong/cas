package com.inmobi.adserve.channels.api;

import java.util.Map;

import lombok.Data;

@Data
public class ThirdPartyAdResponse {
    public enum ResponseStatus {
        SUCCESS, FAILURE_REQUEST_ERROR, FAILURE_NO_AD, FAILURE_NETWORK_ERROR, FAILURE_TIME_OUT, BOOTSTRAP_ERROR, HTTPREQUEST_ERROR, PIPELINE_ERROR, INVALID_RESPONSE, SOCKET_ERROR, MANDATE_PARAM_MISSING, MALFORMED_URL
    }

    private ResponseStatus responseStatus;
    private String response;
    private Map<?, ?> responseHeaders;

    public enum ResponseFormat {
        HTML, WML, JSON
    }

    private ResponseFormat responseFormat;
    private String adStatus;
    // Latency in ms.
    private long latency;
    private String impressionId = null;
    private String clickUrl = null;
}
