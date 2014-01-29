package com.inmobi.adserve.channels.server.servlet;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.MatchSegments;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.List;
import java.util.Map;

@Singleton
@Path("/rtbdfill")
public class ServletRtbd implements Servlet {
    private static final Logger LOG = LoggerFactory.getLogger(ServletRtbd.class);
    private final MatchSegments matchSegments;
    private final Provider<Marker> traceMarkerProvider;
    private final RequestParser    requestParser;
    private final ThriftRequestParser    thriftRequestParser;

    @Inject
    public ServletRtbd(RequestParser requestParser, Provider<Marker> traceMarkerProvider, MatchSegments matchSegments,
                       ThriftRequestParser thriftRequestParser) {
        this.requestParser = requestParser;
        this.traceMarkerProvider = traceMarkerProvider;
        this.matchSegments = matchSegments;
        this.thriftRequestParser = thriftRequestParser;
    }

    @Override
    public void handleRequest(HttpRequestHandler hrh, QueryStringDecoder queryStringDecoder, MessageEvent e
            ) throws Exception {
        Marker traceMarker = traceMarkerProvider.get();
        InspectorStats.incrementStatCount(InspectorStrings.totalRequests);
        // Increment re Request if request came from rule engine
        InspectorStats.incrementStatCount(InspectorStrings.ruleEngineRequests);
        CasInternalRequestParameters casInternalRequestParametersGlobal = new CasInternalRequestParameters();
        SASRequestParameters sasParams = new SASRequestParameters();
        Map<String, List<String>> params = queryStringDecoder.getParameters();

        // GET method handling
        if (params.containsKey("args")) {
            try {
                hrh.jObject = requestParser.extractParams(params);
            }
            catch (JSONException exception) {
                hrh.jObject = new JSONObject();
                LOG.debug("Encountered Json Error while creating json object inside HttpRequest Handler");
                hrh.setTerminationReason(ServletHandler.jsonParsingError);
                InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            }
            requestParser.parseRequestParameters(hrh.jObject, sasParams, casInternalRequestParametersGlobal);
        } // Post method handling
        else {
            hrh.tObject = thriftRequestParser.extractParams(params);
            thriftRequestParser.parseRequestParameters(hrh.tObject, sasParams, casInternalRequestParametersGlobal, 6);
        }

        sasParams.setDst(6);
        hrh.responseSender.sasParams = sasParams;
        hrh.responseSender.casInternalRequestParameters = casInternalRequestParametersGlobal;
        ServletBackFill servletBackFill = new ServletBackFill(matchSegments, traceMarkerProvider, requestParser, thriftRequestParser);
        servletBackFill.handleRequest(hrh, queryStringDecoder, e);
    }

    @Override
    public String getName() {
        return "Rtbd";
    }
}
