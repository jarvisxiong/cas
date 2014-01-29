package com.inmobi.adserve.channels.util;

import java.util.List;
import java.util.Map;

import org.jboss.netty.handler.codec.http.HttpRequest;


/**
 * @author abhishek.parwal
 * 
 */
public class CommonUtils {

    public static String getHost(final HttpRequest request) {
        List<Map.Entry<String, String>> headers = request.getHeaders();
        String host = null;

        for (Map.Entry<String, String> header : headers) {
            if (header.getKey().equalsIgnoreCase("Host")) {
                host = header.getValue();
            }
        }
        return host;
    }
}
