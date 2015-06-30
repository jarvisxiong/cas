package com.inmobi.castest.utils.bidders;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.rtb.BannerExtVideo;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidExtVideo;
import com.inmobi.casthrift.rtb.BidExtensions;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.ImpressionExtensions;
import com.inmobi.casthrift.rtb.SeatBid;

/**
 * Created by aartika.rai on 07/11/14.
 */
public class RtbAdRequestHandler extends AdRequestHandler<BidRequest, BidResponse> {

    public RtbAdRequestHandler(final RequestParser<BidRequest> requestParser, final String seatId) {
        super(requestParser, seatId);
    }

    @Override
    public double getBidPrice(final BidRequest bidRequest) {
        final double bidFloor = bidRequest.getImp().get(0).getBidfloor();
        return (float) (bidFloor * (1 + Math.random())) + 2;
    }

    @Override
    protected RequestedAdType getRequestedCreativeType(final BidRequest bidRequest) {
        if (isNativeRequest(bidRequest)) {
            return RequestedAdType.NATIVE;
        }
        if (isVideoRequest(bidRequest)) {
            return RequestedAdType.INTERSTITIAL;
        }
        return RequestedAdType.BANNER;
    }

    @Override
    protected boolean isNativeRequest(final BidRequest request) {
        if (request != null) {
            final ImpressionExtensions ie = request.getImp().get(0).getExt();
            if (ie != null) {
                return ie.getNativeObject() != null;
            }
        }

        return false;
    }

    @Override
    protected boolean isVideoRequest(final BidRequest request) {
        if (request != null && request.getImp().get(0).getBanner() != null
                && request.getImp().get(0).getBanner().getExt() != null) {

            final BannerExtVideo bannerExtVideo = request.getImp().get(0).getBanner().getExt().getVideo();

            if (bannerExtVideo != null && bannerExtVideo.getLinearity() == 1 && bannerExtVideo.getMinduration() >= 15
                    && bannerExtVideo.getMaxduration() <= 30) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BidResponse makeBidResponse(final BidRequest bidRequest) {
        if (null == bidRequest.imp || bidRequest.imp.get(0) == null || bidRequest.imp.get(0).getId() == null) {
            // System.out.println("Response is " + noAdResponse.getStatus() +
            // "impression id is null so sending no ad");
            return null;
        } else {
            final Bid bid = makeDummyBidObject(bidRequest);
            final List<Bid> bidList = new ArrayList<Bid>();
            bidList.add(bid);
            final SeatBid seatBid = new SeatBid();
            seatBid.seat = seatId;
            seatBid.bid = bidList;
            final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
            seatBidList.add(seatBid);
            final BidResponse bidResponse = new BidResponse();
            bidResponse.setSeatbid(seatBidList);
            bidResponse.id = bidRequest.getId();
            bidResponse.bidid = bidRequest.getId();
            bidResponse.cur = "USD";
            return bidResponse;
        }
    }

    private Bid makeDummyBidObject(final BidRequest bidRequest) {
        final Bid bid = new Bid();
        bid.id = "ab73dd4868a0bbadf8fd7527d95136b4";
        bid.adid = "1335571993285";
        bid.price = getBidPrice(bidRequest);
        bid.impid = bidRequest.imp.get(0).getId();
        // System.out.println("imp id is" + bid.impid);
        bid.cid = "cid";
        bid.crid = "crid";

        // Needed for new filters
        bid.iurl = "http://www.inmobi.com";
        bid.adomain = new ArrayList<String>();
        bid.adomain.add("mkhoj.com");
        bid.attr = new ArrayList<Integer>();
        bid.attr.add(1);
        final RequestedAdType requestedAdFormat = getRequestedCreativeType(bidRequest);
        bid.adm = getAdMarkup(requestedAdFormat);
        if (getRequestedCreativeType(bidRequest).equals(ADCreativeType.INTERSTITIAL_VIDEO)) {
            setVideoResponse(bid);
        }
        return bid;
    }

    private void setVideoResponse(final Bid bid) {
        bid.ext = new BidExtensions();
        bid.ext.video = new BidExtVideo();
        bid.ext.video.linearity = 1;
        bid.ext.video.duration = 30;
        bid.ext.video.type = "VAST 2.0";
        bid.setNurl("http://partner-wn.dummy-bidder.com/callback/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_PRICE}");
    }
}
