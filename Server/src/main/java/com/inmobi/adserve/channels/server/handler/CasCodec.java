package com.inmobi.adserve.channels.server.handler;

import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.FullHttpRequest;

import com.inmobi.adserve.channels.server.beans.CasResponse;


/**
 * Read Path: DefaultFullHttpRequest -> CasRequest Write Path: CasResponse -> FullHttpResponse
 */
public abstract class CasCodec extends MessageToMessageCodec<FullHttpRequest, CasResponse> {

	/*
	 * private static final Logger LOG = LoggerFactory.getLogger(CasCodec.class);
	 * 
	 * @Override protected void encode(final ChannelHandlerContext ctx, final CasResponse msg, final List<Object> out)
	 * throws Exception { System.out.println("Encoding CasResponse -> HttpRequest"); FullHttpResponse httpResponse; if
	 * (msg.isNoFill()) { httpResponse = HttpUtil.fromHtmlString("<!-- mKhoj: No advt for this position -->");
	 * httpResponse.headers().set("X-MKHOJ-NOAD", "True"); } else { httpResponse =
	 * HttpUtil.fromHtmlString(msg.getHtmlSnippet()); } setKeepAlive(httpResponse, msg.isKeepAlive());
	 * out.add(httpResponse); }
	 * 
	 * @Override protected void decode(final ChannelHandlerContext ctx, final FullHttpRequest msg, final List<Object>
	 * out) throws Exception { URI uri = URI.create(msg.getUri()); String query = uri.getQuery(); if (query != null) {
	 * QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri); CasRequest casRequest =
	 * CasRequest.builder().isKeepAlive(isKeepAlive(msg))
	 * .queryStringDecoder(queryStringDecoder).serverChannel(ctx.channel()).httpRequest(msg).build();
	 * LOG.info("Decoding {} -> {}", msg, casRequest); out.add(casRequest); } if (out.isEmpty()) {
	 * LOG.error("Decoding msg->CasRequest Failed."); throw new RuntimeException(); } }
	 */
}
