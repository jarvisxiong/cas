package com.inmobi.adserve.channels.server;

import static com.inmobi.adserve.channels.util.InspectorStrings.TERMINATED_REQUESTS;
import static com.inmobi.adserve.channels.util.InspectorStrings.THRIFT_PARSING_ERROR;
import static com.inmobi.adserve.channels.util.InspectorStrings.THRIFT_PARSING_ERROR_EMPTY_ADPOOLREQUEST;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.inmobi.adserve.adpool.AdPoolRequest;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.scope.NettyRequestScope;
import com.inmobi.adserve.channels.server.api.Servlet;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;

@Sharable
@Singleton
public class RequestParserHandler extends MessageToMessageDecoder<DefaultFullHttpRequest> {
    private static final Logger LOG = LoggerFactory.getLogger(RequestParserHandler.class);

    private final ThriftRequestParser thriftRequestParser;
    private final Provider<Marker> traceMarkerProvider;
    private final Provider<Servlet> servletProvider;
    private final URLCodec urlCodec = new URLCodec();
    private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
    private final NettyRequestScope scope;
    private final Injector injector;

    @Inject
    RequestParserHandler(final ThriftRequestParser thriftRequestParser, final Provider<Marker> traceMarkerProvider,
            final Provider<Servlet> servletProvider, final Provider<HttpRequestHandler> httpRequestHandlerProvider,
            final Injector injector, final NettyRequestScope scope) {
        this.thriftRequestParser = thriftRequestParser;
        this.traceMarkerProvider = traceMarkerProvider;
        this.servletProvider = servletProvider;
        this.httpRequestHandlerProvider = httpRequestHandlerProvider;
        this.injector = injector;
        this.scope = scope;
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final DefaultFullHttpRequest request, final List<Object> out)
            throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternal = new CasInternalRequestParameters();
        String terminationReason = null;

        try {
            LOG.debug("inside RequestParserHandler");
            final Marker traceMarker = traceMarkerProvider.get();

            final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
            final Map<String, List<String>> params = queryStringDecoder.parameters();

            sasParams.setKeepAlive(HttpHeaders.isKeepAlive(request));
            final String automationTestId = request.headers().get("x-mkhoj-automation");
            if (StringUtils.isNotEmpty(automationTestId)) {
                sasParams.setAutomationTestId(automationTestId);
            }

            final String servlet = servletProvider.get().getName();
            final Integer dst = "ixFill".equalsIgnoreCase(servlet) ?
                    8 : "rtbdFill".equalsIgnoreCase(servlet) ? 
                            6 : "BackFill".equalsIgnoreCase(servlet) ? 2 : -1;

            LOG.debug("Method is  {}", request.getMethod());
            boolean isTraceEnabled = false;
            if (request.getMethod() == HttpMethod.POST && dst != -1) {
                final AdPoolRequest adPoolRequest = new AdPoolRequest();
                final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                try {
                    final byte[] adPoolRequestBytes = new byte[request.content().readableBytes()];
                    request.content().getBytes(0, adPoolRequestBytes);
                    tDeserializer.deserialize(adPoolRequest, adPoolRequestBytes);
                    isTraceEnabled = adPoolRequest.isDeprecatedTraceRequest();
                    thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternal, dst);
                } catch (final TException ex) {
                    terminationReason = CasConfigUtil.THRIFT_PARSING_ERROR;
                    LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
                    InspectorStats.incrementStatCount(TERMINATED_REQUESTS, THRIFT_PARSING_ERROR);
                }
            } else if (request.getMethod() == HttpMethod.GET && dst != -1 && params.containsKey("adPoolRequest")) {
                String rawContent = null;
                if (!params.isEmpty()) {
                    final List<String> values = params.get("adPoolRequest");
                    if (CollectionUtils.isNotEmpty(values)) {
                        rawContent = values.iterator().next();
                    }
                }
                LOG.debug("adPoolRequest: {}", rawContent);
                final AdPoolRequest adPoolRequest = new AdPoolRequest();
                if (StringUtils.isNotEmpty(rawContent)) {
                    final byte[] decodedContent = urlCodec.decode(rawContent.getBytes(CharsetUtil.UTF_8));
                    final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                    try {
                        tDeserializer.deserialize(adPoolRequest, decodedContent);
                        isTraceEnabled = adPoolRequest.isDeprecatedTraceRequest();
                        thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternal, dst);
                    } catch (final TException ex) {
                        terminationReason = CasConfigUtil.THRIFT_PARSING_ERROR;
                        LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
                        InspectorStats.incrementStatCount(TERMINATED_REQUESTS, THRIFT_PARSING_ERROR);
                    }
                } else {
                    terminationReason = CasConfigUtil.THRIFT_PARSING_ERROR;
                    LOG.error(traceMarker, "Error in de serializing thrift as adPoolRequest was empty.");
                    InspectorStats.incrementStatCount(TERMINATED_REQUESTS, THRIFT_PARSING_ERROR_EMPTY_ADPOOLREQUEST);
                }
            }
            LOG.debug("isTraceEnabled {} ", isTraceEnabled);
            if (traceMarker == null) {
                scope.seed(Marker.class, isTraceEnabled ? NettyRequestScope.TRACE_MAKER : null);
            }
        } finally {
            out.add(new RequestParameterHolder(sasParams, casInternal, request.getUri(), terminationReason, request));
            request.retain();

            final ChannelPipeline pipeline = ctx.pipeline();
            final Map<String, ChannelHandler> channelHandlerMap = pipeline.toMap();
            if (channelHandlerMap.containsKey("httpRequestHandler")) {
                pipeline.remove("httpRequestHandler");
            }
            pipeline.addLast("httpRequestHandler", httpRequestHandlerProvider.get());

            if (channelHandlerMap.containsKey("casExceptionHandler")) {
                pipeline.remove("casExceptionHandler");
            }
            pipeline.addLast("casExceptionHandler", injector.getInstance(CasExceptionHandler.class));
        }
    }
}
