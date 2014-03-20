package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;


@Sharable
@Singleton
public class RequestParserHandler extends MessageToMessageDecoder<DefaultFullHttpRequest> {
    private static final Logger       LOG = LoggerFactory.getLogger(RequestParserHandler.class);

    private final RequestParser       requestParser;
    private final ThriftRequestParser thriftRequestParser;
    private final Provider<Marker>    traceMarkerProvider;
    private final Provider<Servlet>   servletProvider;

    @Inject
    RequestParserHandler(final RequestParser requestParser, final ThriftRequestParser thriftRequestParser,
            final Provider<Marker> traceMarkerProvider, final Provider<Servlet> servletProvider) {
        this.requestParser = requestParser;
        this.thriftRequestParser = thriftRequestParser;
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final DefaultFullHttpRequest msg, final List<Object> out)
            throws Exception {

        LOG.debug("inside RequestParserHandler");
        Marker traceMarker = traceMarkerProvider.get();
        DefaultFullHttpRequest request = msg;

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> params = queryStringDecoder.parameters();
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        String terminationReason = null;
        Servlet servlet = servletProvider.get();
        String servletName = servlet.getName();

        Integer dst = null;
        if (servletName.equalsIgnoreCase("rtbdFill")) {
            dst = 6;
        }
        else if (servletName.equalsIgnoreCase("BackFill")) {
            dst = 2;
        }

        if (request.getMethod() == HttpMethod.POST && null != dst) {
            AdPoolRequest adPoolRequest = new AdPoolRequest();
            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
            try {
                tDeserializer.deserialize(adPoolRequest, request.content().array());
                thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
            }
            catch (TException ex) {
                terminationReason = ServletHandler.thriftParsingError;
                LOG.error(traceMarker, "Error in de serializing thrift ", ex);
                InspectorStats.incrementStatCount(InspectorStrings.thriftParsingError, InspectorStrings.count);
            }
        }
        else if (params.containsKey("args") && null != dst) {
            JSONObject jsonObject;
            try {
                jsonObject = requestParser.extractParams(params);
            }
            catch (JSONException exception) {
                terminationReason = ServletHandler.jsonParsingError;
                jsonObject = new JSONObject();
                LOG.debug("Encountered Json Error while creating json object inside ", exception);
                InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            }
            requestParser.parseRequestParameters(jsonObject, sasParams, casInternalRequestParameters);
        }
        out.add(new RequestParameterHolder(sasParams, casInternalRequestParameters, request.getUri(),
                terminationReason, request));
    }

}
