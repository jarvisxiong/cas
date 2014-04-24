package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.AuctionEngineInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/***
 * Auction Engine to run different types of auctions in rtb.
 * 
 * @author Devi Chand(devi.chand@inmobi.com)
 */
public class AuctionEngine implements AuctionEngineInterface {
    private static final Logger         LOG             = LoggerFactory.getLogger(AuctionEngine.class);

    private boolean                     auctionComplete = false;
    private ChannelSegment              rtbResponse;
    private double                      secondBidPrice;
    public SASRequestParameters         sasParams;
    public CasInternalRequestParameters casInternalRequestParameters;
    private List<ChannelSegment>        rtbSegments;

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String>  advertiserIdNameMap;

    @Inject
    private static AsyncRequestMaker    asyncRequestMaker;

    public AuctionEngine() {
    }

    /***
     * RunRtbSecondPriceAuctionEngine returns the adnetwork selected after auctioning If no of rtb segments selected
     * after filtering is zero it returns the null If no of rtb segments selected after filtering is one it returns the
     * rtb adapter for the segment BidFloor is maximum of lowestEcpm and siteFloor If only 2 rtb are selected, highest
     * bid will win and would be charged the secondHighest price If only 1 rtb is selected, it will be selected for
     * sending response and will be charged the highest of secondHighest price or 90% of bidFloor
     */
    @Override
    public AdNetworkInterface runRtbSecondPriceAuctionEngine() {
        // Do not run auction 2 times.
        synchronized (this) {
            if (auctionComplete) {
                return rtbResponse == null ? null : rtbResponse.getAdNetworkInterface();
            }
            auctionComplete = true;
        }

        LOG.debug("Inside RTB auction engine");
        List<ChannelSegment> rtbList;
        // Apply rtb filters.
        rtbList = rtbFilters(rtbSegments);

        // Send null as auction response in case of 0 rtb responses.
        if (rtbList.size() == 0) {
            LOG.debug("RTB segments are {}", rtbList.size());
            rtbResponse = null;
            LOG.debug("Returning from auction engine , winner is none");
            return null;
        }
        else if (rtbList.size() == 1) {
            LOG.debug("RTB segments are {}", rtbList.size());
            rtbResponse = rtbList.get(0);
            // Take minimum of rtbFloor+0.01 and bid as secondBidprice if no of rtb
            // response are 1.
            secondBidPrice = Math.min(casInternalRequestParameters.rtbBidFloor, rtbResponse
                    .getAdNetworkInterface().getBidPriceInUsd());
            // Set encrypted bid price.
            rtbResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
            rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
            LOG.debug("Completed auction, winner is {} and secondBidPrice is {}", rtbList.get(0)
                    .getAdNetworkInterface().getName(), secondBidPrice);
            // Return as there is no need to iterate over the list.
            return rtbList.get(0).getAdNetworkInterface();
        }

        // Sort the list by their bid prices.
        LOG.debug("RTB segments are {}", rtbList.size());
        for (int i = 0; i < rtbList.size(); i++) {
            for (int j = i + 1; j < rtbList.size(); j++) {
                if (rtbList.get(i).getAdNetworkInterface().getBidPriceInUsd() < rtbList.get(j).getAdNetworkInterface()
                        .getBidPriceInUsd()) {
                    ChannelSegment channelSegment = rtbList.get(i);
                    rtbList.set(i, rtbList.get(j));
                    rtbList.set(j, channelSegment);
                }
            }
        }

        // Calculates the max price of all rtb responses.
        double maxPrice = rtbList.get(0).getAdNetworkInterface().getBidPriceInUsd();
        int secondHighestBid = 1;// Keep secondHighestBidPrice number from rtb response list.
        int lowestLatencyBid = 0;// Keep winner number from rtb response list.
        for (int i = 1; i < rtbList.size(); i++) {
            if (rtbList.get(i).getAdNetworkInterface().getBidPriceInUsd() < maxPrice) {
                secondHighestBid = i;
                break;
            }
            else if (rtbList.get(i).getAdNetworkInterface().getLatency() < rtbList.get(lowestLatencyBid)
                    .getAdNetworkInterface().getLatency()) {
                lowestLatencyBid = i;
            }
        }

        // Set rtb response for the auction ran.
        rtbResponse = rtbList.get(lowestLatencyBid);

        // Calculates the secondHighestBidPrice if no of rtb responses are more than 1.
        secondBidPrice = rtbList.get(secondHighestBid).getAdNetworkInterface().getBidPriceInUsd();
        double winnerBid = rtbList.get(lowestLatencyBid).getAdNetworkInterface().getBidPriceInUsd();
        if (winnerBid == secondBidPrice) {
            secondBidPrice = casInternalRequestParameters.rtbBidFloor;
        }

        // Ensure secondHighestBidPrice never crosses response bid.
        secondBidPrice = Math.min(secondBidPrice, rtbResponse.getAdNetworkInterface().getBidPriceInUsd());
        rtbResponse.getAdNetworkInterface().setEncryptedBid(getEncryptedBid(secondBidPrice));
        rtbResponse.getAdNetworkInterface().setSecondBidPrice(secondBidPrice);
        LOG.debug("Completed auction, winner is {} and secondBidPrice is {}", rtbList.get(lowestLatencyBid)
                .getAdNetworkInterface().getName(), secondBidPrice);
        return rtbList.get(lowestLatencyBid).getAdNetworkInterface();
    }

    public List<ChannelSegment> rtbFilters(final List<ChannelSegment> rtbSegments) {
        List<ChannelSegment> rtbList = new ArrayList<ChannelSegment>();
        LOG.debug("No of rtb partners who sent response are {}", rtbList.size());
        // Ad filter.
        for (ChannelSegment rtbSegment : rtbSegments) {
            if (rtbSegment.getAdNetworkInterface().getAdStatus().equalsIgnoreCase("AD")) {
                rtbList.add(rtbSegment);
            }
            else {
                LOG.debug("Dropped in NO AD filter {}", rtbSegment.getAdNetworkInterface().getName());
            }
        }
        LOG.debug("No of rtb partners who sent AD response are {}", rtbList.size());
        // BidFloor filter.
        Iterator<ChannelSegment> rtbListIterator = rtbList.iterator();
        while (rtbListIterator.hasNext()) {
            ChannelSegment channelSegment = rtbListIterator.next();
            if (channelSegment.getAdNetworkInterface().getBidPriceInUsd() < casInternalRequestParameters.rtbBidFloor) {
                rtbListIterator.remove();
                LOG.debug("Dropped in bidfloor filter {}", channelSegment.getAdNetworkInterface().getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInRtbBidFloorFilter);
                }
            }
            else if (!channelSegment.getChannelEntity().getAccountId()
                    .equalsIgnoreCase(channelSegment.getAdNetworkInterface().getSeatId())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in seat id mismatch filter {}", channelSegment.getAdNetworkInterface().getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInRtbSeatidMisMatchFilter);
                }
            }
            else if (!channelSegment.getAdNetworkInterface().getImpressionId()
                    .equalsIgnoreCase(channelSegment.getAdNetworkInterface().getRtbImpressionId())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in impression id mismatch filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInRtbImpressionIdMisMatchFilter);
                }
            }
            else if (!casInternalRequestParameters.auctionId.equalsIgnoreCase(channelSegment.getAdNetworkInterface()
                    .getAuctionId())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in auction id mismatch filter {}", channelSegment.getAdNetworkInterface().getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInRtbAuctionIdMisMatchFilter);
                }
            }
            else if (!RtbAdNetwork.getCurrenciesSupported().contains(
                    channelSegment.getAdNetworkInterface().getCurrency())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in currency not supported filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInRtbCurrencyNotSupportedFilter);
                }
            }
        }
        LOG.debug("No of rtb partners who sent AD response with bid more than bidFloor {}", rtbList.size());
        return rtbList;
    }

    @Override
    public boolean isAuctionComplete() {
        return auctionComplete;
    }

    public ChannelSegment getRtbResponse() {
        return rtbResponse;
    }

    @Override
    public double getSecondBidPrice() {
        return secondBidPrice;
    }

    @Override
    public boolean isAllRtbComplete() {
        if (rtbSegments == null) {
            return false;
        }
        if (rtbSegments.size() == 0) {
            return true;
        }
        for (ChannelSegment channelSegment : rtbSegments) {
            if (!channelSegment.getAdNetworkInterface().isRequestCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isRtbResponseNull() {
        return rtbResponse == null;
    }

    public List<ChannelSegment> getRtbSegments() {
        return rtbSegments;
    }

    public void setRtbSegments(final List<ChannelSegment> rtbSegments) {
        this.rtbSegments = rtbSegments;
    }

    public String getEncryptedBid(final Double bid) {
        long winBid = (long) (bid * Math.pow(10, 6));
        return asyncRequestMaker.getImpressionId(winBid);
    }

    public double calculateRTBFloor(final double siteFloor, final double highestEcpm, final double segmentFloor,
            final double countryFloor, final double networkSiteEcpm) {
        double rtbFloor;
        rtbFloor = Math.max(siteFloor, highestEcpm);
        rtbFloor = Math.max(rtbFloor, segmentFloor);
        rtbFloor = Math.max(rtbFloor, countryFloor);
        rtbFloor = Math.max(rtbFloor, networkSiteEcpm);
        return rtbFloor;
    }
}
