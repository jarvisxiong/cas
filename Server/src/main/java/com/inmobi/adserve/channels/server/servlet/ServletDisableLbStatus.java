package com.inmobi.adserve.channels.server.servlet;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServerStatusInfo;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.beans.CasRequest;


@Singleton
@Path("/disablelbstatus")
public class ServletDisableLbStatus implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletDisableLbStatus.class);

    @Override
    public void handleRequest(final HttpRequestHandler hrh, final CasRequest casRequest) throws Exception {
        if (hrh.isRequestFromLocalHost()) {
            hrh.responseSender.sendResponse("OK", casRequest.serverChannel());
            ServerStatusInfo.statusCode = 404;
            ServerStatusInfo.statusString = "NOT_OK";
            LOG.debug("asked to shut down the server");
        }
        else {
            hrh.responseSender.sendResponse("NOT AUTHORIZED", casRequest.serverChannel());
        }
    }

    @Override
    public String getName() {
        return "disablelbstatus";
    }

}
