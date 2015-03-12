package com.inmobi.castest.utils.common;

import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.ResponseFormat;

public class ResponseBuilder {

    private boolean isRtbd = false;
    private int statusCode;
    private byte[] responseData;
    private ResponseFormat responseFormat;
    private AdPoolResponse adPoolResponse;

    public boolean isRtbd() {
        return isRtbd;
    }

    public ResponseBuilder setIsRtbd(final boolean isRtbd) {
        this.isRtbd = isRtbd;
        return this;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public ResponseBuilder setStatusCode(final int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public ResponseBuilder setResponseData(final byte[] responseData) {
        this.responseData = responseData;
        return this;
    }

    public ResponseFormat getResponseFormat() {
        return responseFormat;
    }

    public ResponseBuilder setResponseFormat(final ResponseFormat responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    public AdPoolResponse getAdPoolResponse() {
        return adPoolResponse;
    }

    public ResponseBuilder setAdPoolResponse(final AdPoolResponse adPoolResponse) {
        this.adPoolResponse = adPoolResponse;
        return this;
    }

    public ResponseBuilder build() {
        final ResponseBuilder responseBuilder = new ResponseBuilder();
        responseBuilder.setStatusCode(statusCode);
        responseBuilder.setResponseData(responseData);
        responseBuilder.setResponseFormat(responseFormat);
        responseBuilder.setAdPoolResponse(adPoolResponse);

        return responseBuilder;
    }
}
