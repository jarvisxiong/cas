package com.inmobi.adserve.channels.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;

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
import org.json.JSONException;
import org.json.JSONObject;
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
import com.inmobi.adserve.channels.server.requesthandler.RequestParser;
import com.inmobi.adserve.channels.server.requesthandler.ThriftRequestParser;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

@Sharable
@Singleton
public class RequestParserHandler extends MessageToMessageDecoder<DefaultFullHttpRequest> {
  private static final Logger LOG = LoggerFactory.getLogger(RequestParserHandler.class);

  private final RequestParser requestParser;
  private final ThriftRequestParser thriftRequestParser;
  private final Provider<Marker> traceMarkerProvider;
  private final Provider<Servlet> servletProvider;
  private final URLCodec urlCodec = new URLCodec();
  private final Provider<HttpRequestHandler> httpRequestHandlerProvider;
  private final NettyRequestScope scope;
  private final Injector injector;

  @Inject
  RequestParserHandler(final RequestParser requestParser, final ThriftRequestParser thriftRequestParser,
      final Provider<Marker> traceMarkerProvider, final Provider<Servlet> servletProvider,
      final Provider<HttpRequestHandler> httpRequestHandlerProvider, final Injector injector,
      final NettyRequestScope scope) {
    this.requestParser = requestParser;
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
    final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    String terminationReason = null;

    try {
      LOG.debug("inside RequestParserHandler");
      final Marker traceMarker = traceMarkerProvider.get();

      final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
      final Map<String, List<String>> params = queryStringDecoder.parameters();

      sasParams.setKeepAlive(HttpHeaders.isKeepAlive(request));

      final Servlet servlet = servletProvider.get();
      final String servletName = servlet.getName();

      Integer dst = null;
      if ("rtbdFill".equalsIgnoreCase(servletName)) {
        dst = 6;
      } else if ("BackFill".equalsIgnoreCase(servletName)) {
        dst = 2;
      } else if ("ixFill".equalsIgnoreCase(servletName)) {
        dst = 8;
      }
      LOG.debug("Method is  {}", request.getMethod());
      boolean isTraceEnabled = false;
      if (request.getMethod() == HttpMethod.POST && null != dst) {
        final AdPoolRequest adPoolRequest = new AdPoolRequest();
        final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
        try {
          final byte[] adPoolRequestBytes = new byte[request.content().readableBytes()];
          request.content().getBytes(0, adPoolRequestBytes);
          tDeserializer.deserialize(adPoolRequest, adPoolRequestBytes);
          isTraceEnabled = adPoolRequest.isTraceRequest();
          thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
        } catch (final TException ex) {
          terminationReason = CasConfigUtil.THRIFT_PARSING_ERROR;
          LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
          InspectorStats.incrementStatCount(InspectorStrings.THRIFT_PARSING_ERROR, InspectorStrings.COUNT);
        }
      } else if (params.containsKey("args") && null != dst) {
        JSONObject jsonObject;
        try {
          jsonObject = requestParser.extractParams(params);
        } catch (final JSONException exception) {
          terminationReason = CasConfigUtil.JSON_PARSING_ERROR;
          jsonObject = new JSONObject();
          LOG.debug("Encountered Json Error while creating json object inside ", exception);
          InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
        }
        requestParser.parseRequestParameters(jsonObject, sasParams, casInternalRequestParameters);
      } else if (request.getMethod() == HttpMethod.GET && null != dst && params.containsKey("adPoolRequest")) {
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
          final byte[] decodedContent = urlCodec.decode(rawContent.getBytes());
          LOG.debug("Decoded String : {}", decodedContent);
          final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
          try {
            tDeserializer.deserialize(adPoolRequest, decodedContent);
            thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
          } catch (final TException ex) {
            terminationReason = CasConfigUtil.THRIFT_PARSING_ERROR;
            LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
            InspectorStats.incrementStatCount(InspectorStrings.THRIFT_PARSING_ERROR, InspectorStrings.COUNT);
          }
        }
      }
      LOG.debug("isTraceEnabled {} ", isTraceEnabled);
      if (traceMarker == null) {
        scope.seed(Marker.class, isTraceEnabled ? NettyRequestScope.TRACE_MAKER : null);
      }
    } finally {
      out.add(new RequestParameterHolder(sasParams, casInternalRequestParameters, request.getUri(), terminationReason,
          request));
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
