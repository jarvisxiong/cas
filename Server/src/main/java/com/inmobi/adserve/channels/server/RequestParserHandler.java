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

	private final Injector injector;

	@Inject
	RequestParserHandler(final RequestParser requestParser, final ThriftRequestParser thriftRequestParser, final Provider<Marker> traceMarkerProvider,
			final Provider<Servlet> servletProvider, final Provider<HttpRequestHandler> httpRequestHandlerProvider, final Injector injector) {
		this.requestParser = requestParser;
		this.thriftRequestParser = thriftRequestParser;
		this.traceMarkerProvider = traceMarkerProvider;
		this.servletProvider = servletProvider;
		this.httpRequestHandlerProvider = httpRequestHandlerProvider;
		this.injector = injector;
	}

	@Override
	protected void decode(final ChannelHandlerContext ctx, final DefaultFullHttpRequest request, final List<Object> out) throws Exception {

		SASRequestParameters sasParams = new SASRequestParameters();
		CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
		String terminationReason = null;

		try {
			LOG.debug("inside RequestParserHandler");
			Marker traceMarker = traceMarkerProvider.get();

			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
			Map<String, List<String>> params = queryStringDecoder.parameters();

			sasParams.setKeepAlive(HttpHeaders.isKeepAlive(request));

			Servlet servlet = servletProvider.get();
			String servletName = servlet.getName();

			Integer dst = null;
			if (servletName.equalsIgnoreCase("rtbdFill")) {
				dst = 6;
			} else if (servletName.equalsIgnoreCase("BackFill")) {
				dst = 2;
			} else if (servletName.equalsIgnoreCase("ixFill")) {
				dst = 7;
			}
			LOG.debug("Method is  {}", request.getMethod());
			if (request.getMethod() == HttpMethod.POST && null != dst) {
				AdPoolRequest adPoolRequest = new AdPoolRequest();
				TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
				try {
					byte[] adPoolRequestBytes = new byte[request.content().readableBytes()];
					request.content().getBytes(0, adPoolRequestBytes);
					tDeserializer.deserialize(adPoolRequest, adPoolRequestBytes);
					thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
				} catch (TException ex) {
					terminationReason = CasConfigUtil.thriftParsingError;
					LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
					InspectorStats.incrementStatCount(InspectorStrings.thriftParsingError, InspectorStrings.count);
				}
			} else if (params.containsKey("args") && null != dst) {
				JSONObject jsonObject;
				try {
					jsonObject = requestParser.extractParams(params);
				} catch (JSONException exception) {
					terminationReason = CasConfigUtil.jsonParsingError;
					jsonObject = new JSONObject();
					LOG.debug("Encountered Json Error while creating json object inside ", exception);
					InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
				}
				requestParser.parseRequestParameters(jsonObject, sasParams, casInternalRequestParameters);
			} else if (request.getMethod() == HttpMethod.GET && null != dst && params.containsKey("adPoolRequest")) {

				String rawContent = null;
				if (!params.isEmpty()) {
					List<String> values = params.get("adPoolRequest");
					if (CollectionUtils.isNotEmpty(values)) {
						rawContent = values.iterator().next();
					}
				}

				LOG.debug("adPoolRequest: {}", rawContent);

				AdPoolRequest adPoolRequest = new AdPoolRequest();

				if (StringUtils.isNotEmpty(rawContent)) {
					byte[] decodedContent = urlCodec.decode(rawContent.getBytes());
					LOG.debug("Decoded String : {}", decodedContent);
					TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
					try {
						tDeserializer.deserialize(adPoolRequest, decodedContent);
						thriftRequestParser.parseRequestParameters(adPoolRequest, sasParams, casInternalRequestParameters, dst);
					} catch (TException ex) {
						terminationReason = CasConfigUtil.thriftParsingError;
						LOG.debug(traceMarker, "Error in de serializing thrift ", ex);
						InspectorStats.incrementStatCount(InspectorStrings.thriftParsingError, InspectorStrings.count);
					}
				}
			}
		} finally {
			out.add(new RequestParameterHolder(sasParams, casInternalRequestParameters, request.getUri(), terminationReason, request));
			request.retain();

			ChannelPipeline pipeline = ctx.pipeline();
			Map<String, ChannelHandler> channelHandlerMap = pipeline.toMap();
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
