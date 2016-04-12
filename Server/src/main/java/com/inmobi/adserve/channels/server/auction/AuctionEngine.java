package com.inmobi.adserve.channels.server.auction;

import static com.inmobi.adserve.channels.server.auction.AuctionEngineHelper.mapRPChannelSegmentsToDSPChannelSegments;
import static com.inmobi.adserve.channels.server.auction.AuctionEngineHelper.updateChannelSegmentWithDSPFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.AuctionEngineInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.DemandSourceType;


/***
 * Auction Engine to run different types of auctions in rtbd, ix.
 *
 * @author Devi Chand
 * @author Ishan Bhatnagar
 */
public class AuctionEngine implements AuctionEngineInterface {
    private static final Logger LOG = LoggerFactory.getLogger(AuctionEngine.class);
    @Inject
    private static AuctionFilterApplier auctionFilterApplier;
    public SASRequestParameters sasParams;
    public CasInternalRequestParameters casInternalRequestParameters;
    private boolean auctionComplete;
    private ChannelSegment auctionResponse;
    private double secondBidPrice;
    private List<ChannelSegment> unfilteredChannelSegmentList;
    private static Random random = new Random();

    /**
     * Runs the auction for RTBD (Second Price), IX (First Price)
     */
    @Override
    public synchronized AdNetworkInterface runAuctionEngine() {
        // Do not run the auction twice.
        if (auctionComplete) {
            return auctionResponse == null ? null : auctionResponse.getAdNetworkInterface();
        }
        auctionComplete = true;

        List<ChannelSegment> filteredChannelSegmentList;
        if (!unfilteredChannelSegmentList.isEmpty()) {
            LOG.debug("Inside {} auction engine", DemandSourceType.findByValue(sasParams.getDst()).toString());

            // Apply filtration only when we have at least 1 channelSegment
            filteredChannelSegmentList = auctionFilterApplier
                    .applyFilters(new ArrayList<>(unfilteredChannelSegmentList), casInternalRequestParameters);
        } else {
            filteredChannelSegmentList = new ArrayList<>();
        }

        LOG.debug("No. of filtered {} segments are {}", DemandSourceType.findByValue(sasParams.getDst()).toString(),
                filteredChannelSegmentList.size());

        // Send auction response as null in case of 0 rtb/ix responses.
        if (filteredChannelSegmentList.isEmpty()) {
            auctionResponse = null;
            LOG.debug("Returning from auction engine, all segments were dropped. No winner.");
            return null;
        }

        // TODO: Refactor the two auctions into one (minimising QA effort for now)
        int winnerIndex = 0;
        final AdNetworkInterface firstAdNetwork = filteredChannelSegmentList.get(0).getAdNetworkInterface();
        double highestBid = firstAdNetwork.getBidPriceInUsd();
        double lowestLatency = firstAdNetwork.getLatency();

        if (DemandSourceType.RTBD.getValue() == sasParams.getDst()) {
            /*
                Run a second price auction
            
                Iterate over all the channel segments, and find the channel segment with the highest bid (if there are
                any conflicts, then the one with the lowest latency is chosen).
            
                SecondBidPrice is set to the second highest distinct bid (defaulting to the auctionBidFloor if all the
                bids are the same.
             */
            // Acts as the default secondBidPrice
            double secondHighestDistinctBid = casInternalRequestParameters.getAuctionBidFloor();

            for (int index = 1; index < filteredChannelSegmentList.size(); ++index) {
                final AdNetworkInterface adNetworkInterface =
                        filteredChannelSegmentList.get(index).getAdNetworkInterface();
                final double bid = adNetworkInterface.getBidPriceInUsd();
                final double latency = adNetworkInterface.getLatency();

                if (bid < highestBid) {
                    secondHighestDistinctBid = Math.max(secondHighestDistinctBid, bid);
                } else if (bid > highestBid) {
                    winnerIndex = index;
                    lowestLatency = latency;
                    secondHighestDistinctBid = highestBid;
                    highestBid = bid;
                } else if (latency < lowestLatency) {
                    // If bid == highestBid, then choose the one with the lowest latency
                    winnerIndex = index;
                    lowestLatency = latency;
                }
            }
            Double clearingPrice = getClearingPrice(highestBid, casInternalRequestParameters);
            LOG.debug("Clearing Price: " + clearingPrice);
            clearingPrice = clearingPrice * (0.98 + random.nextDouble() * 0.02);
            LOG.debug("Clearing Price +  Random(0.98, 1.00): " + clearingPrice);

            if (clearingPrice >= secondHighestDistinctBid) {
                secondBidPrice = clearingPrice;
                InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS, InspectorStrings.CLEARING_PRICE_WON);
            } else {
                secondBidPrice = secondHighestDistinctBid;
            }
        } else if (DemandSourceType.IX.getValue() == sasParams.getDst()) {
            /*
                Run a first price auction
                Iterate over all the channel segments, and find the channel segment with the highest bid (if there are
                any conflicts, then the one with the lowest latency is chosen). Channel segments with Trump deals are
                given preference.
             */
            boolean doesWinnerHaveTrumpDeal = hasTrumpDeal(firstAdNetwork);
            final boolean ixMultiFormatAuction = filteredChannelSegmentList.size() > 1 ? true : false;
            int ixMultiFormatAuctionTrumpDealsCount = doesWinnerHaveTrumpDeal ? 1 : 0;

            for (int index = 1; index < filteredChannelSegmentList.size(); ++index) {
                final AdNetworkInterface adNetworkInterface =
                        filteredChannelSegmentList.get(index).getAdNetworkInterface();
                final double bid = adNetworkInterface.getBidPriceInUsd();
                final double latency = adNetworkInterface.getLatency();
                final boolean hasTrumpDeal = hasTrumpDeal(adNetworkInterface);
                if (hasTrumpDeal) {
                    ++ixMultiFormatAuctionTrumpDealsCount;
                }

                if (!doesWinnerHaveTrumpDeal && hasTrumpDeal) {
                    doesWinnerHaveTrumpDeal = true;
                    winnerIndex = index;
                    lowestLatency = latency;
                    highestBid = bid;
                } else if (doesWinnerHaveTrumpDeal == hasTrumpDeal) {
                    // When both or none of doesWinnerHaveTrumpDeal and hasTrumpDeal are true
                    if (bid > highestBid) {
                        winnerIndex = index;
                        lowestLatency = latency;
                        highestBid = bid;
                    } else if (bid == highestBid && latency < lowestLatency) {
                        // If bid == highestBid, then choose the one with the lowest latency
                        winnerIndex = index;
                        lowestLatency = latency;
                    }
                }
            }

            if (ixMultiFormatAuction) {
                InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                        InspectorStrings.MULTI_FORMAT_AUCTIONS_TOTAL);
                switch (ixMultiFormatAuctionTrumpDealsCount) {
                    case 0:
                        InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                                InspectorStrings.MULTI_FORMAT_AUCTIONS_NO_TRUMP);
                        break;
                    case 1:
                        InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                                InspectorStrings.MULTI_FORMAT_AUCTIONS_SINGLE_TRUMP);
                        break;
                    default:
                        InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                                InspectorStrings.MULTI_FORMAT_AUCTIONS_MULTIPLE_TRUMP);
                        break;
                }
                LOG.debug("IX multi-format auction run. ({} Trump Deals)", ixMultiFormatAuctionTrumpDealsCount);
                final IXAdNetwork winningIXAdNetwork =
                        (IXAdNetwork) filteredChannelSegmentList.get(winnerIndex).getAdNetworkInterface();
                if (winningIXAdNetwork.isSegmentVideoSupported()) {
                    LOG.debug("Winning ad type is VAST_VIDEO");
                    InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                            InspectorStrings.MULTI_FORMAT_AUCTIONS_VAST_VIDEO_WINS);
                } else {
                    LOG.debug("Winning ad type is INTERSTITIAL");
                    InspectorStats.incrementStatCount(InspectorStrings.AUCTION_STATS,
                            InspectorStrings.MULTI_FORMAT_AUCTIONS_STATIC_WINS);
                }
                if (winningIXAdNetwork.isTrumpDeal()) {
                    LOG.debug("Winner has trump deal: {}", winningIXAdNetwork.getDealId());
                }
            }

            secondBidPrice = highestBid;
        } else {
            // Unhandled DST
            auctionResponse = null;
            LOG.debug("Returning from auction engine, as unhandled DST was encountered. No winner.");
            return null;
        }
        auctionResponse = filteredChannelSegmentList.get(winnerIndex);
        final AdNetworkInterface winningAdNetwork = filteredChannelSegmentList.get(winnerIndex).getAdNetworkInterface();
        if (DemandSourceType.IX.getValue() == sasParams.getDst()) {
            // For all ads that pass auction filters, we update their channel segments with DSP specific info. The rest
            // are left with RP parent specific info.
            unfilteredChannelSegmentList =
                    mapRPChannelSegmentsToDSPChannelSegments(unfilteredChannelSegmentList, filteredChannelSegmentList);
            auctionResponse = updateChannelSegmentWithDSPFields(auctionResponse);
        } else {
            winningAdNetwork.setEncryptedBid(ImpressionIdGenerator.getInstance().getEncryptedBid(secondBidPrice));
        }
        winningAdNetwork.setSecondBidPrice(secondBidPrice);

        LOG.debug("Auction complete, winner is {}, secondBidPrice is {}", winningAdNetwork.getName(), secondBidPrice);
        return winningAdNetwork;
    }

    @Override
    public boolean isAuctionComplete() {
        return auctionComplete;
    }

    public ChannelSegment getAuctionResponse() {
        return auctionResponse;
    }

    @Override
    public double getSecondBidPrice() {
        return secondBidPrice;
    }

    @Override
    public boolean areAllChannelSegmentRequestsComplete() {
        if (unfilteredChannelSegmentList == null) {
            return false;
        }
        if (unfilteredChannelSegmentList.isEmpty()) {
            return true;
        }
        // TODO: For loop can be replaced with counter
        for (final ChannelSegment channelSegment : unfilteredChannelSegmentList) {
            if (!channelSegment.getAdNetworkInterface().isRequestCompleted()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isAuctionResponseNull() {
        return auctionResponse == null;
    }

    public List<ChannelSegment> getUnfilteredChannelSegmentList() {
        return unfilteredChannelSegmentList;
    }

    public void setUnfilteredChannelSegmentList(final List<ChannelSegment> unfilteredChannelSegmentList) {
        this.unfilteredChannelSegmentList = unfilteredChannelSegmentList;
    }

    /**
     * Computes the clearing price for the auction.
     *
     * Assumptions: highestBid >= demandDensity (alpha*omega) [This is enforced in the Auction Bid Filter]
     * InmobiLongTermRevenue >= demandDensity [Enforced while initialising]
     *
     * Ask Price Range = [demandDensity, InmobiLongTermRevenue] (equivalent to [alpha*omega, beta*omega])
     *
     * The ask price range is divided into publisherYield(gamma) intervals, with the clearing price being the highest
     * ask price <= highest bid.
     *
     * Calculation Logic:
     *
     * Difference = (InmobiLongTermRevenue-demandDensity)/publisherYield (equivalent to omega*(beta-alpha)/gamma)
     *
     * If Difference != 0, index = min(Floor((HighestBid-demandDensity)/difference), publisherYield) (index is always >=
     * 0 as highestBid >= demandDensity) Clearing Price = difference*index + demandDensity Else Clearing Price =
     * demandDensity
     *
     * @param highestBid
     * @param casParams
     * @return The clearing price for the auction.
     */
    protected static double getClearingPrice(final double highestBid, final CasInternalRequestParameters casParams) {
        final double demandDensity = casParams.getDemandDensity();
        final double longTermRevenue = casParams.getLongTermRevenue();
        final int publisherYield = casParams.getPublisherYield() >= 1 ? casParams.getPublisherYield() : 1; // Sanity

        final double diff = (longTermRevenue - demandDensity) / publisherYield;

        if (0 != diff) {
            final int index = Math.min((int) ((highestBid - demandDensity) / diff), publisherYield);
            return diff * index + demandDensity;
        } else {
            return demandDensity;
        }
    }

    /**
     * Determines whether the deal present in the AdNetworkInterface is a TRUMP deal or not.
     * 
     * @param adNetworkInterface
     * @return
     */
    private static boolean hasTrumpDeal(final AdNetworkInterface adNetworkInterface) {
        boolean hasTrumpDeal = false;
        if (adNetworkInterface instanceof IXAdNetwork) {
            hasTrumpDeal = ((IXAdNetwork) adNetworkInterface).isTrumpDeal();
        }
        return hasTrumpDeal;
    }
}
