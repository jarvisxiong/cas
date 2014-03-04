package com.inmobi.adserve.channels.util;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;


/**
 * @author abhishek.parwal
 * 
 */
public class CommonUtils {

    public static String getHost(final HttpRequest request) {
        HttpHeaders headers = request.headers();

        return headers.get("Host");
    }
}
