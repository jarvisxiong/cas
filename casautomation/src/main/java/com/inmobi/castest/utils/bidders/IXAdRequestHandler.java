package com.inmobi.castest.utils.bidders;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.casthrift.ix.Bid;
import com.inmobi.casthrift.ix.IXBidRequest;
import com.inmobi.casthrift.ix.IXBidResponse;
import com.inmobi.casthrift.ix.SeatBid;
import com.inmobi.casthrift.ix.Video;

/**
 * Created by aartika.rai on 07/11/14.
 */
public class IXAdRequestHandler extends AdRequestHandler<IXBidRequest, IXBidResponse> {

    public IXAdRequestHandler(final RequestParser<IXBidRequest> requestParser, final String seatId) {
        super(requestParser, seatId);
    }

    @Override
    public IXBidResponse makeBidResponse(final IXBidRequest bidRequest) {
        if (null == bidRequest.imp || bidRequest.imp.get(0) == null || bidRequest.imp.get(0).getId() == null) {
            return null;
        } else {
            final Bid bid = makeDummyBidObject(bidRequest);
            final List<Bid> bidList = new ArrayList<Bid>();
            bidList.add(bid);
            final SeatBid seatBid = new SeatBid();
            seatBid.seat = seatId;
            seatBid.bid = bidList;
            seatBid.buyer = "2770";
            final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
            seatBidList.add(seatBid);
            final IXBidResponse bidResponse = new IXBidResponse();
            bidResponse.setSeatbid(seatBidList);
            bidResponse.id = bidRequest.getId();
            bidResponse.bidid = bidRequest.getId();
            return bidResponse;
        }
    }

    @Override
    public double getBidPrice(final IXBidRequest bidRequest) {
        final double bidFloor = bidRequest.getImp().get(0).getBidfloor();
        return (float) (bidFloor * (1 + Math.random())) + 2;
    }

    @Override
    protected RequestedAdType getRequestedCreativeType(final IXBidRequest bidRequest) {
        return super.getRequestedCreativeType(bidRequest);
    }

    @Override
    protected boolean isNativeRequest(final IXBidRequest bidRequest) {
        return false;
    }

    @Override
    protected boolean isVideoRequest(final IXBidRequest bidRequest) {
        if (bidRequest != null) {
            final Video video = bidRequest.getImp().get(0).getVideo();
            if (video != null) {
                return true;
            }
        }
        return false;
    }

    private Bid makeDummyBidObject(final IXBidRequest bidRequest) {
        final Bid bid = new Bid();
        bid.id = "ab73dd4868a0bbadf8fd7527d95136b4";
        bid.price = getBidPrice(bidRequest);
        bid.impid = bidRequest.imp.get(0).getId();
        bid.setCrid("CRID");
        bid.setEstimated(0);
        bid.setDealid("DealWaleBabaJi");
        bid.setPmptier(3);
        bid.setH(320);
        bid.setW(480);
        bid.setNurl("http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}");
        bid.setAqid("Test_AQID");
        final RequestedAdType requestedAdFormat = getRequestedCreativeType(bidRequest);
        bid.adm = getAdMarkup(requestedAdFormat);

        return bid;
    }
}
