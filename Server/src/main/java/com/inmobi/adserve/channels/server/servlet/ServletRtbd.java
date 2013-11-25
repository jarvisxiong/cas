package com.inmobi.adserve.channels.server.servlet;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;


public class ServletRtbd implements Servlet {
    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e,
            DebugLogger logger) throws Exception {
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        // Increment re Request if request came from rule engine
        InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        CasInternalRequestParameters casInternalRequestParametersGlobal = new CasInternalRequestParameters();
        SASRequestParameters sasParams = new SASRequestParameters();
        Map<String, List<String>> params = queryStringDecoder.getParameters();

        // GET method handling
        if (params.containsKey("args")) {
            try {
                hrh.jObject = RequestParser.extractParams(params);
            }
            catch (JSONException exception) {
                hrh.jObject = new JSONObject();
                logger.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
                hrh.setTerminationReason(ServletHandler.jsonParsingError);
                InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            }
            RequestParser.parseRequestParameters(hrh.jObject, sasParams, casInternalRequestParametersGlobal, logger);
        } // Post method handling
        else {
            hrh.tObject = ThriftRequestParser.extractParams(params);
            ThriftRequestParser.parseRequestParameters(hrh.tObject, sasParams, casInternalRequestParametersGlobal,
                logger, 6);
        }

        sasParams.setDst(6);
        hrh.responseSender.sasParams = sasParams;
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        ServletBackFill servletBackFill = new ServletBackFill();
        servletBackFill.handleRequest(hrh, queryStringDecoder, e, logger);
    }

    @Override
    public String getName() {
        return "Rtbd";
    }
}
