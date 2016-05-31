package com.inmobi.adserve.channels.server.auction;

import static com.inmobi.adserve.channels.util.demand.enums.AuctionType.FIRST_PRICE;
import static com.inmobi.adserve.channels.util.demand.enums.AuctionType.SECOND_PRICE;
import static com.inmobi.adserve.channels.util.demand.enums.DealType.PREFERRED;
import static com.inmobi.adserve.channels.util.demand.enums.DealType.RIGHT_TO_FIRST_REFUSAL;
import static java.lang.Math.abs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
import com.inmobi.casthrift.DemandSourceType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class AuctionEngineTest {

    private static final class Adn extends BaseAdNetworkImpl {

        @Getter
        private final double bidPriceInUsd;

        @Getter
        private double secondBidPriceInUsd;

        Adn(final double bid, final double dealFloor, final AuctionType auctionType, final boolean isTrump) {
            super(null, null);

            this.deal = DealEntity.newBuilder()
                        .floor(dealFloor)
                        .dealType(isTrump ? RIGHT_TO_FIRST_REFUSAL : PREFERRED)
                        .build();
            this.bidPriceInUsd = bid;
            this.auctionType = auctionType;
        }

        @Override
        public void setSecondBidPrice(final Double price) {
            this.secondBidPriceInUsd = price;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public URI getRequestUri() throws Exception {
            return null;
        }
    }

    private static ChannelSegment genTestChannelSegment(final Adn adn) {
        return new ChannelSegment(null, null, null, null, null, adn, 0);
    }

    @BeforeClass
    public static void setup() {
        ImpressionIdGenerator.init((short) 123, (byte) 10);
    }

    // Assumption: It is assumed that auction and deal floors have already been enforced
    @DataProvider(name = "Auction Engine Data Provider")
    public Iterator<Object[]> dataProvider() {
        final double bid = 4.0d;
        final boolean WITHIN_2_PERCENT = true;
        final boolean EXACT = false;

        final List<Object[]> tests = new ArrayList<>();
        // Making the tests agnostic of dst
        for (final DemandSourceType dst : DemandSourceType.values()) {
            final Object[][] test = {
                    {"FirstPrice", bid, FIRST_PRICE, 0.0, 0.0, 0.0, 0.0, dst, bid, EXACT},
                    {"FirstPrice.BidFloor", bid, FIRST_PRICE, 0.0, bid-1, 0.0, 0.0, dst, bid, EXACT},
                    {"FirstPrice.DealFloor", bid, FIRST_PRICE, 0.0, 0.0, bid-1, 0.0, dst, bid, EXACT},
                    {"FirstPrice.ClearingPrice", bid, FIRST_PRICE, 0.0, 0.0, 0.0, bid-1, dst, bid, EXACT},
                    {"FirstPrice.SecondBid", bid, FIRST_PRICE, bid-1, 0.0, 0.0, 0.0, dst, bid, EXACT},

                    {"SecondPrice.BidFloor<Bid ", bid, SECOND_PRICE, 0.0, bid-1, 0.0, 0.0, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor=Bid", bid, SECOND_PRICE, 0.0, bid, 0.0, 0.0, dst, bid, EXACT},
                    {"SecondPrice.DealFloor<Bid ", bid, SECOND_PRICE, 0.0, 0.0, bid-1, 0.0, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor=Bid", bid, SECOND_PRICE, 0.0, 0.0, bid, 0.0, dst, bid, EXACT},
                    {"SecondPrice.ClearingPrice<Bid ", bid, SECOND_PRICE, 0.0, 0.0, 0.0, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.ClearingPrice=Bid", bid, SECOND_PRICE, 0.0, 0.0, 0.0, bid, dst, bid, WITHIN_2_PERCENT},
                    {"SecondPrice.SecondBid<Bid ", bid, SECOND_PRICE, bid-1, 0.0, 0.0, 0.0, dst, bid-1, EXACT},
                    {"SecondPrice.SecondBid=Bid", bid, SECOND_PRICE, bid, 0.0, 0.0, 0.0, dst, bid, EXACT},
                    {"SecondPrice.SecondBid>Bid", bid, SECOND_PRICE, bid+1, 0.0, 0.0, 0.0, dst, 0.0, EXACT},

                    // Second bid <= bid
                    {"SecondPrice.SecondBid<BidFloor<DealFloor<ClearingPrice", bid, SECOND_PRICE, bid-4, bid-3, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.BidFloor<SecondBid<DealFloor<ClearingPrice", bid, SECOND_PRICE, bid-3, bid-4, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.BidFloor=SecondBid<DealFloor<ClearingPrice", bid, SECOND_PRICE, bid-3, bid-3, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.SecondBid<DealFloor<BidFloor<ClearingPrice", bid, SECOND_PRICE, bid-4, bid-2, bid-3, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.DealFloor<SecondBid<BidFloor<ClearingPrice", bid, SECOND_PRICE, bid-3, bid-2, bid-4, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.DealFloor=SecondBid<BidFloor<ClearingPrice", bid, SECOND_PRICE, bid-3, bid-2, bid-3, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.BidFloor<DealFloor<SecondBid<ClearingPrice", bid, SECOND_PRICE, bid-2, bid-4, bid-3, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.DealFloor<BidFloor<SecondBid<ClearingPrice", bid, SECOND_PRICE, bid-2, bid-3, bid-4, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.DealFloor=BidFloor<SecondBid<ClearingPrice", bid, SECOND_PRICE, bid-2, bid-3, bid-3, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    
                    {"SecondPrice.SecondBid<BidFloor<ClearingPrice<DealFloor", bid, SECOND_PRICE, bid-4, bid-3, bid-1, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor<SecondBid<ClearingPrice<DealFloor", bid, SECOND_PRICE, bid-3, bid-4, bid-1, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor=SecondBid<ClearingPrice<DealFloor", bid, SECOND_PRICE, bid-3, bid-3, bid-1, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.SecondBid<ClearingPrice<BidFloor<DealFloor", bid, SECOND_PRICE, bid-4, bid-2, bid-1, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<SecondBid<BidFloor<DealFloor", bid, SECOND_PRICE, bid-3, bid-2, bid-1, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=SecondBid<BidFloor<DealFloor", bid, SECOND_PRICE, bid-3, bid-2, bid-1, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor<ClearingPrice<SecondBid<DealFloor", bid, SECOND_PRICE, bid-2, bid-4, bid-1, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<BidFloor<SecondBid<DealFloor", bid, SECOND_PRICE, bid-2, bid-3, bid-1, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=BidFloor<SecondBid<DealFloor", bid, SECOND_PRICE, bid-2, bid-3, bid-1, bid-3, dst, bid-1, EXACT},
                    
                    {"SecondPrice.SecondBid<DealFloor<ClearingPrice<BidFloor", bid, SECOND_PRICE, bid-4, bid-1, bid-3, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor<SecondBid<ClearingPrice<BidFloor", bid, SECOND_PRICE, bid-3, bid-1, bid-4, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor=SecondBid<ClearingPrice<BidFloor", bid, SECOND_PRICE, bid-3, bid-1, bid-3, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.SecondBid<ClearingPrice<DealFloor<BidFloor", bid, SECOND_PRICE, bid-4, bid-1, bid-2, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<SecondBid<DealFloor<BidFloor", bid, SECOND_PRICE, bid-3, bid-1, bid-2, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=SecondBid<DealFloor<BidFloor", bid, SECOND_PRICE, bid-3, bid-1, bid-2, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor<ClearingPrice<SecondBid<BidFloor", bid, SECOND_PRICE, bid-2, bid-1, bid-4, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<DealFloor<SecondBid<BidFloor", bid, SECOND_PRICE, bid-2, bid-1, bid-3, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=DealFloor<SecondBid<BidFloor", bid, SECOND_PRICE, bid-2, bid-1, bid-3, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.SecondBid<DealFloor<ClearingPrice<BidFloor", bid, SECOND_PRICE, bid-4, bid-1, bid-3, bid-2, dst, bid-1, EXACT},
                    
                    {"SecondPrice.DealFloor<BidFloor<ClearingPrice<SecondBid", bid, SECOND_PRICE, bid-1, bid-3, bid-4, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor=BidFloor<ClearingPrice<SecondBid", bid, SECOND_PRICE, bid-1, bid-3, bid-3, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor<ClearingPrice<DealFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-4, bid-2, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<BidFloor<DealFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-3, bid-2, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=BidFloor<DealFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-3, bid-2, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor<ClearingPrice<BidFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-2, bid-4, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<DealFloor<BidFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-2, bid-3, bid-4, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice=DealFloor<BidFloor<SecondBid", bid, SECOND_PRICE, bid-1, bid-2, bid-3, bid-3, dst, bid-1, EXACT},
                    
                    // Second bid >  bid
                    {"SecondPrice.DealFloor<SecondBid<BidFloor<ClearingPrice", bid, SECOND_PRICE, bid+1, bid-3, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.BidFloorDealFloor<SecondBid<ClearingPrice", bid, SECOND_PRICE, bid+1, bid-3, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.DealFloor=BidFloor<ClearingPrice", bid, SECOND_PRICE, bid+1, bid-2, bid-2, bid-1, dst, bid-1, WITHIN_2_PERCENT},
                    {"SecondPrice.ClearingPrice<BidFloor<DealFloor", bid, SECOND_PRICE, bid+1, bid-2, bid-1, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor<ClearingPrice<DealFloor", bid, SECOND_PRICE, bid+1, bid-3, bid-1, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.BidFloor~=ClearingPrice<DealFloor", bid, SECOND_PRICE, bid+1, bid-2, bid-1, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.DealFloor<ClearingPrice<BidFloor", bid, SECOND_PRICE, bid+1, bid-1, bid-3, bid-2, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice<DealFloor<BidFloor", bid, SECOND_PRICE, bid+1, bid-1, bid-3, bid-3, dst, bid-1, EXACT},
                    {"SecondPrice.ClearingPrice~=DealFloor<BidFloor", bid, SECOND_PRICE, bid+1, bid-1, bid-2, bid-2, dst, bid-1, EXACT},
                    
            };

            tests.addAll(Arrays.asList(test));
        }

        return tests.iterator();
    }

    @Test(dataProvider = "Auction Engine Data Provider")
    public void testConductAuction(final String testName, final Double bid, final AuctionType auctionType,
            final double secondBid, final double auctionFloor, final double dealFloor, final double approxClearingPrice,
            final DemandSourceType dst, final double expectedBid, final boolean approx) {

        final ChannelSegment participant = genTestChannelSegment(new Adn(bid, dealFloor, auctionType, true));
        final ChannelSegment participant2 = genTestChannelSegment(new Adn(secondBid, 0.0, auctionType, false));
        final AuctionEngine auctionEngine = new AuctionEngine();

        final CasInternalRequestParameters casParams = new CasInternalRequestParameters();
        casParams.setAuctionBidFloor(auctionFloor);
        casParams.setLongTermRevenue(approxClearingPrice);
        casParams.setDemandDensity(approxClearingPrice);
        casParams.setPublisherYield(1);
        auctionEngine.casParams = casParams;

        final SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setDemandSourceType(dst);
        auctionEngine.sasParams = sasParams;

        final AdNetworkInterface winnerAdn = auctionEngine.conductAuction(Arrays.asList(participant, participant2));

        log.debug("Running Auction Test: {}", testName);
        Assert.assertEquals(participant.getAdNetworkInterface(), winnerAdn);

        if (approx) {
            Assert.assertTrue(abs(winnerAdn.getSecondBidPriceInUsd() - expectedBid) / expectedBid <= 0.02);
        } else {
            Assert.assertEquals(winnerAdn.getSecondBidPriceInUsd(), expectedBid);
        }
        Assert.assertTrue(winnerAdn.getSecondBidPriceInUsd() >= auctionFloor);
        Assert.assertTrue(winnerAdn.getSecondBidPriceInUsd() >= dealFloor);
        Assert.assertTrue(winnerAdn.getSecondBidPriceInUsd() <= bid);
        log.debug("Test passed: {}", testName);
    }

}