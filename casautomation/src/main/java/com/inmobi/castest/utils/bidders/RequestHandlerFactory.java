package com.inmobi.castest.utils.bidders;

import com.inmobi.casthrift.ix.IXBidRequest;
import com.inmobi.casthrift.rtb.BidRequest;

/**
 * Created by aartika.rai on 07/11/14.
 */
public class RequestHandlerFactory {

    private final HttpRequestHandler httpRequestHandler;

    public HttpRequestHandler getHttpRequestHandler() {
        return httpRequestHandler;
    }

    public RequestHandlerFactory(final int waitMultiplier, final int adRatio, final double budget, final String seatId,
            final boolean underStress) {
        final RequestParser<BidRequest> rtbRequestParser = new RequestParser<BidRequest>(BidRequest.class, underStress);
        final RequestParser<IXBidRequest> ixRequestParser =
                new RequestParser<IXBidRequest>(IXBidRequest.class, underStress);
        final RtbAdRequestHandler rtbAdRequestHandler = new RtbAdRequestHandler(rtbRequestParser, seatId);
        final IXAdRequestHandler ixAdRequestHandler = new IXAdRequestHandler(ixRequestParser, seatId);

        httpRequestHandler =
                new HttpRequestHandler(waitMultiplier, adRatio, budget, underStress, rtbAdRequestHandler,
                        ixAdRequestHandler);
    }
}
