package com.inmobi.adserve.channels.api;

import java.util.Map;


public class ThirdPartyAdResponse
{

    public enum ResponseStatus
    {
        SUCCESS,
        FAILURE_REQUEST_ERROR,
        FAILURE_NO_AD,
        FAILURE_NETWORK_ERROR,
        FAILURE_TIME_OUT,
        BOOTSTRAP_ERROR,
        HTTPREQUEST_ERROR,
        PIPELINE_ERROR,
        INVALID_RESPONSE,
        SOCKET_ERROR,
        MANDATE_PARAM_MISSING,
        MALFORMED_URL
    };

    public ResponseStatus responseStatus;
    public String         response;
    public Map            responseHeaders;

    public enum ResponseFormat
    {
        HTML,
        WML,
        JSON
    };

    public ResponseFormat responseFormat;
    public String         adStatus;
    // Latency in ms.
    public long           latency;
    public String         impressionId = null;
    public String         clickUrl     = null;
}
