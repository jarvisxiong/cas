package com.inmobi.adserve.channels.server.auction;

import static com.inmobi.adserve.channels.server.auction.AuctionEngineHelper.getClearingPrice;
import static com.inmobi.adserve.channels.server.auction.AuctionEngineHelper.mapRPChannelSegmentsToDSPChannelSegments;
import static com.inmobi.adserve.channels.server.auction.AuctionEngineHelper.updateChannelSegmentWithDSPFields;
import static com.inmobi.adserve.channels.util.InspectorStrings.ALL_SEGMENTS_DROPPED_IN_AUCTION_FILTERS;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTIONS_WITH_NO_COMPETITION;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTIONS_WITH_OPPORTUNITY_LOSS;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTIONS_WON_AT_CLEARING_PRICE;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTIONS_WON_BY_NON_TRUMP_DEALS;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTIONS_WON_BY_TRUMP_DEALS;
import static com.inmobi.adserve.channels.util.InspectorStrings.AUCTION_STATS;
import static com.inmobi.adserve.channels.util.InspectorStrings.EFFECTIVELY_FIRST_PRICE_SECOND_PRICE_AUCTIONS;
import static com.inmobi.adserve.channels.util.InspectorStrings.OPPORTUNITY_LOSS_IN_100xCPM;
import static com.inmobi.adserve.channels.util.InspectorStrings.TOTAL_AUCTIONS_CONDUCTED;
import static java.lang.Math.max;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableSet;
import java.util.Random;
import java.util.TreeSet;

import javax.inject.Inject;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.AuctionEngineInterface;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
import com.inmobi.casthrift.DemandSourceType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * Auction Engine runs a generic auction handling multiple pricing models (first price, second price), multiple deal
 * classes (trump, non-trump) and multiple floors (auction floor, deal floor, clearing price)
 *
 * TODO: Handle data vendor cost
 * TODO: Bids should be modelled as Long
 */
@Slf4j
public class AuctionEngine implements AuctionEngineInterface {
    private static final double ZERO = 0d;
    private static final Random random = new Random();
    private static final ChannelSegmentComparator CHANNEL_SEGMENT_COMPARATOR = new ChannelSegmentComparator();

    @Inject
    private static AuctionFilterApplier auctionFilterApplier;

    public SASRequestParameters sasParams;
    public CasInternalRequestParameters casParams;

    @Getter
    private Double highestBid;
    @Getter
    private boolean auctionComplete;
    @Getter
    private ChannelSegment auctionResponse;
    @Getter
    private List<ChannelSegment> unfilteredChannelSegmentList;


    @Override
    public synchronized AdNetworkInterface runAuctionEngine() {
        log.debug("Inside Auction Engine");

        // Do not run the auction twice.
        if (auctionComplete) {
            return auctionResponse == null ? null : auctionResponse.getAdNetworkInterface();
        }
        auctionComplete = true;

        final DemandSourceType dst = sasParams.getDemandSourceType();
        log.debug("Conducting Auction for dst: {}", dst);

        log.debug("No. of segments before filtration: {}", unfilteredChannelSegmentList.size());
        final List<ChannelSegment> filteredChannelSegmentList = auctionFilterApplier
                .applyFilters(new ArrayList<>(unfilteredChannelSegmentList), casParams);
        log.debug("No. of segments after filtration: {}", filteredChannelSegmentList.size());

        if (filteredChannelSegmentList.isEmpty()) {
            log.debug("No winner as all segments were dropped in auction filters");
            InspectorStats.incrementStatCount(AUCTION_STATS, dst + ALL_SEGMENTS_DROPPED_IN_AUCTION_FILTERS);

            auctionResponse = null;
            return null;
        }

        return conductAuction(filteredChannelSegmentList);
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

    final AdNetworkInterface conductAuction(final List<ChannelSegment> filteredChannelSegmentList) {
        final DemandSourceType dst = sasParams.getDemandSourceType();
        InspectorStats.incrementStatCount(AUCTION_STATS, dst + TOTAL_AUCTIONS_CONDUCTED);

        if (log.isDebugEnabled()) {
            log.debug("Auction participants:");
            for(final ChannelSegment cs : filteredChannelSegmentList) {
                final AdNetworkInterface adn = cs.getAdNetworkInterface();
                final String adnName = adn.getName();
                final String impressionId = adn.getImpressionId();
                final double bid = adn.getBidPriceInUsd();
                final long latency = adn.getLatency();
                final DealEntity deal = adn.getDeal();
                final AuctionType auctionType = adn.getAuctionType();
                final String dealId = null != deal ? deal.getId() : null;
                final boolean hasTrumpDeal = null != deal && deal.isTrumpDeal();

                log.debug("AdvName: {}, ImpressionId: {}, Bid: {}, Latency: {}, AuctionType: {}, Deal: {} (Trump: {})",
                        adnName, impressionId, bid, latency, auctionType, dealId, hasTrumpDeal);
            }
        }

        // Can potentially be done in O(1) but, sorting here instead as the resulting code is more cleaner
        Collections.sort(filteredChannelSegmentList, CHANNEL_SEGMENT_COMPARATOR);

        final ChannelSegment winningChannelSegment = filteredChannelSegmentList.get(0);
        final AdNetworkInterface winningAdNetwork = winningChannelSegment.getAdNetworkInterface();
        final AuctionType winnerAuctionType = winningAdNetwork.getAuctionType();
        final double winnerBid = winningAdNetwork.getBidPriceInUsd();
        final DealEntity winnerDeal = winningAdNetwork.getDeal();

        if (log.isDebugEnabled()) {
            final String adnName = winningAdNetwork.getName();
            final String impressionId = winningAdNetwork.getImpressionId();
            final AuctionType auctionType = winningAdNetwork.getAuctionType();
            final String dealId = null != winnerDeal ? winnerDeal.getId() : null;

            log.debug("Winner: AdvName: {}, ImpressionId: {}, Bid: {}, AuctionType: {}, Deal: {}",
                    adnName, impressionId, winnerBid, auctionType, dealId);
        }

        final double highestBid = filteredChannelSegmentList.stream()
                .map(channelSegment -> channelSegment.getAdNetworkInterface().getBidPriceInUsd())
                .max(Comparator.naturalOrder())
                .get();
        log.debug("Highest bid: {}", highestBid);

        switch (winnerAuctionType) {
            case FIRST_PRICE:
                winningAdNetwork.setSecondBidPrice(winnerBid);
                break;
            case SECOND_PRICE:
                log.debug("Computing second highest bid");

                // Can potentially be done in O(1) but, using a TreeSet instead as the resulting code is more cleaner
                final NavigableSet<Double> allBids = new TreeSet<>();
                filteredChannelSegmentList.stream()
                        .filter(cs -> cs != winningChannelSegment)
                        .map(ChannelSegment::getAdNetworkInterface)
                        .map(AdNetworkInterface::getBidPriceInUsd)
                        .forEach(allBids::add);

                final double auctionBidFloor = casParams.getAuctionBidFloor();
                final double dealFloor = null != winnerDeal ? defaultIfNull(winnerDeal.getFloor(), ZERO) : ZERO;
                final double effectiveFloor = max(auctionBidFloor, dealFloor);
                log.debug("Auction Floor: {}", auctionBidFloor);
                log.debug("Deal Floor: {}", dealFloor);

                double secondHighestBid = defaultIfNull(allBids.floor(winnerBid), ZERO);
                log.debug("Second Highest Bid: {}", secondHighestBid);
                if (ZERO == secondHighestBid) {
                    InspectorStats.incrementStatCount(AUCTION_STATS, dst + AUCTIONS_WITH_NO_COMPETITION);
                }
                secondHighestBid = max(secondHighestBid, effectiveFloor);

                final double clearingPrice = getClearingPrice(winnerBid, casParams);
                final double randomisedClearingPrice = clearingPrice * (0.98 + random.nextDouble() * 0.02);
                log.debug("Clearing Price: {}", randomisedClearingPrice);

                if (randomisedClearingPrice > secondHighestBid) {
                    InspectorStats.incrementStatCount(AUCTION_STATS, dst + AUCTIONS_WON_AT_CLEARING_PRICE);
                    secondHighestBid = randomisedClearingPrice;
                    log.debug("Using clearing price as the effective second highest bid");
                }
                log.debug("Effective Second Highest Bid: {}", secondHighestBid);

                if (secondHighestBid == winnerBid) {
                    InspectorStats.incrementStatCount(AUCTION_STATS, dst + EFFECTIVELY_FIRST_PRICE_SECOND_PRICE_AUCTIONS);
                }

                winningAdNetwork.setSecondBidPrice(secondHighestBid);
                break;
            default:
                // FIRST_PRICE is assumed by default
                winningAdNetwork.setSecondBidPrice(winnerBid);
                break;
        }

        if (null != winnerDeal) {
            if (winnerDeal.isTrumpDeal()) {
                InspectorStats.incrementStatCount(AUCTION_STATS, dst + AUCTIONS_WON_BY_TRUMP_DEALS);
            } else {
                InspectorStats.incrementStatCount(AUCTION_STATS, dst + AUCTIONS_WON_BY_NON_TRUMP_DEALS);
            }
        }

        if (winnerBid < highestBid) {
            final long opportunityLoss = (long) ((highestBid - winnerBid) * 100);
            InspectorStats.incrementStatCount(AUCTION_STATS, dst + AUCTIONS_WITH_OPPORTUNITY_LOSS);
            InspectorStats.incrementStatCount(AUCTION_STATS, dst + OPPORTUNITY_LOSS_IN_100xCPM, opportunityLoss);
            log.debug("Opportunity loss: {}", opportunityLoss);
            this.highestBid = highestBid;
        }

        auctionResponse = winningChannelSegment;

        if (DemandSourceType.IX.getValue() == sasParams.getDst()) {
            // For all ads that pass auction filters, we update their channel segments with DSP specific info. The rest
            // are left with RP parent specific info.
            unfilteredChannelSegmentList =
                    mapRPChannelSegmentsToDSPChannelSegments(unfilteredChannelSegmentList, filteredChannelSegmentList);
            auctionResponse = updateChannelSegmentWithDSPFields(auctionResponse);
        } else {
            winningAdNetwork.setEncryptedBid(ImpressionIdGenerator.getInstance()
                    .getEncryptedBid(winningAdNetwork.getSecondBidPriceInUsd()));
        }

        return winningAdNetwork;
    }

    @Override
    public boolean isAuctionResponseNull() {
        return auctionResponse == null;
    }

    public void setUnfilteredChannelSegmentList(final List<ChannelSegment> unfilteredChannelSegmentList) {
        this.unfilteredChannelSegmentList = unfilteredChannelSegmentList;
    }

}
