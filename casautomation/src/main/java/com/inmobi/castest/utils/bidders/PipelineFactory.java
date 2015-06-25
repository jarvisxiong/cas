package com.inmobi.castest.utils.bidders;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import com.inmobi.casthrift.ix.IXBidRequest;
import com.inmobi.casthrift.rtb.BidRequest;

/**
 * Created by aartika.rai on 10/11/14.
 */
public class PipelineFactory extends ChannelInitializer<SocketChannel> {

    private final int waitMultiplier;
    private final int adRatio;
    private final double budget;
    private final String seatId;
    private final boolean underStress;

    private final RequestParser<BidRequest> rtbBidRequestParser;
    private final RequestParser<IXBidRequest> ixBidRequestParser;

    public PipelineFactory(final int waitMultiplier, final int adRatio, final double budget, final String seatId,
            final boolean underStress) {
        this.waitMultiplier = waitMultiplier;
        this.adRatio = adRatio;
        this.budget = budget;
        this.seatId = seatId;
        this.underStress = underStress;
        rtbBidRequestParser = new RequestParser<BidRequest>(BidRequest.class, !underStress);
        ixBidRequestParser = new RequestParser<IXBidRequest>(IXBidRequest.class, !underStress);
    }

    private HttpRequestHandler getHttpRequestHandler() {
        final RtbAdRequestHandler rtbAdRequestHandler = new RtbAdRequestHandler(rtbBidRequestParser, seatId);
        final IXAdRequestHandler ixAdRequestHandler = new IXAdRequestHandler(ixBidRequestParser, seatId);

        return new HttpRequestHandler(waitMultiplier, adRatio, budget, !underStress, rtbAdRequestHandler,
                ixAdRequestHandler);
    }

    @Override
    public void initChannel(final SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new HttpRequestDecoder(), new HttpResponseEncoder(), new HttpObjectAggregator(1048576),
                getHttpRequestHandler());
    }
}
