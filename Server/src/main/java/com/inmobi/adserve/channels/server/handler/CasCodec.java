package com.inmobi.adserve.channels.server.handler;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setKeepAlive;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.server.beans.CasRequest;
import com.inmobi.adserve.channels.server.beans.CasResponse;


/**
 * Read Path: DefaultFullHttpRequest -> CasRequest Write Path: CasResponse -> FullHttpResponse
 */
public class CasCodec extends MessageToMessageCodec<FullHttpRequest, CasResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(CasCodec.class);

    @Override
    protected void encode(final ChannelHandlerContext ctx, final CasResponse msg, final List<Object> out)
            throws Exception {
        System.out.println("Encoding CasResponse -> HttpRequest");
        FullHttpResponse httpResponse;
        if (msg.isNoFill()) {
            httpResponse = HttpUtil.fromHtmlString("<!-- mKhoj: No advt for this position -->");
            httpResponse.headers().set("X-MKHOJ-NOAD", "True");
        }
        else {
            httpResponse = HttpUtil.fromHtmlString(msg.getHtmlSnippet());
        }
        setKeepAlive(httpResponse, msg.isKeepAlive());
        out.add(httpResponse);
    }

    @Override
    protected void decode(final ChannelHandlerContext ctx, final FullHttpRequest msg, final List<Object> out)
            throws Exception {
        URI uri = URI.create(msg.getUri());
        String query = uri.getQuery();
        if (query != null) {
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(uri);
            CasRequest casRequest = CasRequest.builder().isKeepAlive(isKeepAlive(msg))
                    .queryStringDecoder(queryStringDecoder).serverChannel(ctx.channel()).httpRequest(msg).build();
            LOG.info("Decoding {} -> {}", msg, casRequest);
            out.add(casRequest);
        }
        if (out.isEmpty()) {
            LOG.error("Decoding msg->CasRequest Failed.");
            throw new RuntimeException();
        }
    }
}
