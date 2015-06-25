package com.inmobi.castest.utils.bidders;

import org.apache.thrift.TBase;

import com.google.gson.Gson;

public class RequestParser<T extends TBase> {

    private final Class<T> clazz;

    private boolean parseRequestAlways = true;

    private T parsedRequest;

    public RequestParser(final Class<T> clazz, final boolean parseRequestAlways) {
        this.clazz = clazz;
        this.parseRequestAlways = parseRequestAlways;
    }

    public T getParsedRequest(final String request) {
        if (parseRequestAlways || parsedRequest == null) {
            parsedRequest = parseRequest(request);
        }
        return parsedRequest;
    }

    private T parseRequest(String request) {
        final Gson gson = new Gson();
        request = request.replaceAll("native", "nativeObject");
        return gson.fromJson(request, clazz);
        // System.out.println("bid request is " + bidRequest);
    }
}
