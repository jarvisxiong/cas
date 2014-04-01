package com.inmobi.adserve.channels.server;

import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Map;

@ChannelHandler.Sharable
@Singleton
public class RequestParserHandler extends OneToOneDecoder {
    private static final Logger LOG               = LoggerFactory.getLogger(RequestParserHandler.class);

    private final RequestParser       requestParser;
    private final ThriftRequestParser thriftRequestParser;
    private final Provider<Marker>    traceMarkerProvider;
    private final Provider<Servlet>   servletProvider;
    private final URLCodec urlCodec = new URLCodec();



    @Inject
    RequestParserHandler(final RequestParser requestParser,
                       final ThriftRequestParser thriftRequestParser,
                       final Provider<Marker>    traceMarkerProvider,
                       final Provider<Servlet> servletProvider) {
        this.requestParser = requestParser;
        this.thriftRequestParser = thriftRequestParser;
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
    }
    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

        LOG.debug("inside RequestParserHandler");
        Marker traceMarker = traceMarkerProvider.get();
        HttpRequest request = (HttpRequest) msg;

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
        Map<String, List<String>> params = queryStringDecoder.getParameters();
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        String terminationReason = null;
        Servlet servlet = servletProvider.get();
        String servletName = servlet.getName();

        Integer dst = null;
        if  (servletName.equalsIgnoreCase("rtbdFill")) {
            dst = 6;
        } else if (servletName.equalsIgnoreCase("BackFill")) {
            dst = 2;
        }
        LOG.debug("Method is  {}", request.getMethod());
        if (request.getMethod() == HttpMethod.POST && null != dst) {
            AdPoolRequest adPoolRequest = new AdPoolRequest();
            TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
            try {
                tDeserializer.deserialize(adPoolRequest, request.getContent().array());
                thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
            } catch (TException ex) {
                terminationReason = ServletHandler.thriftParsingError;
                LOG.error(traceMarker, "Error in de serializing thrift ", ex);
                InspectorStats.incrementStatCount(InspectorStrings.thriftParsingError, InspectorStrings.count);
            }
        } else if (params.containsKey("args") && null != dst) {
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
        } else if (request.getMethod() == HttpMethod.GET && null != dst) {
            AdPoolRequest adPoolRequest = new AdPoolRequest();
            String rawContent = request.getHeader("adPoolRequest");
            if (StringUtils.isNotEmpty(rawContent)) {
                TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                try {
                    tDeserializer.deserialize(adPoolRequest, urlCodec.decode(rawContent.getBytes()));
                    thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
                } catch (TException ex) {
                    terminationReason = ServletHandler.thriftParsingError;
                    LOG.error(traceMarker, "Error in de serializing thrift ", ex);
                    InspectorStats.incrementStatCount(InspectorStrings.thriftParsingError, InspectorStrings.count);
                }
            }
        }
        return new RequestParameterHolder(sasParams, casInternalRequestParameters, request.getUri(), terminationReason, request);
    }
}
