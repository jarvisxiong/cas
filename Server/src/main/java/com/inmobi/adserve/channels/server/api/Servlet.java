package com.inmobi.adserve.channels.server.api;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.beans.CasRequest;


public interface Servlet {

    void handleRequest(final HttpRequestHandler hrh, final CasRequest casRequest) throws Exception;

    String getName();

}
