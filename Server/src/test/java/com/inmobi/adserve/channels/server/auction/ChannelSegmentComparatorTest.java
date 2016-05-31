package com.inmobi.adserve.channels.server.auction;

import java.net.URI;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.demand.enums.DealType;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class ChannelSegmentComparatorTest {

    private static final ChannelSegmentComparator csComparator = new ChannelSegmentComparator();

    private static final class Adn extends BaseAdNetworkImpl {
        @Getter
        final private double bidPriceInUsd;

        Adn(final double bid, final long latency, final boolean isTrumpDeal) {
            super(null, null);

            if (isTrumpDeal) {
                deal = DealEntity.newBuilder().dealType(DealType.RIGHT_TO_FIRST_REFUSAL).build();
            }
            this.bidPriceInUsd = bid;
            this.latency = latency;
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

    @DataProvider(name = "Channel Segment Comparator Data Provider")
    public Object[][] dataProvider() {
        final double highBid = 5.0d;
        final double lowBid = 1.0d;

        final long highLatency = 150;
        final long lowLatency = 50;

        final boolean trump = true;
        final boolean nonTrump = false;

        final int firstWins = -1;
        final int draw = 0;

        // Trump
        final ChannelSegment trumpHighBidLowLatency = genTestChannelSegment(new Adn(highBid, lowLatency, trump));
        final ChannelSegment trumpHighBidHighLatency = genTestChannelSegment(new Adn(highBid, highLatency, trump));
        final ChannelSegment trumpLowBidLowLatency = genTestChannelSegment(new Adn(lowBid, lowLatency, trump));
        final ChannelSegment trumpLowBidHighLatency = genTestChannelSegment(new Adn(lowBid, highLatency, trump));

        // Normal
        final ChannelSegment normalHighBidLowLatency = genTestChannelSegment(new Adn(highBid, lowLatency, nonTrump));
        final ChannelSegment normalHighBidHighLatency = genTestChannelSegment(new Adn(highBid, highLatency, nonTrump));
        final ChannelSegment normalLowBidLowLatency = genTestChannelSegment(new Adn(lowBid, lowLatency, nonTrump));
        final ChannelSegment normalLowBidHighLatency = genTestChannelSegment(new Adn(lowBid, highLatency, nonTrump));

        return new Object[][] {
                {"BothTrump.SameBid.FirstHasLowerLatency", trumpHighBidLowLatency, trumpHighBidHighLatency, firstWins},
                {"BothTrump.SameBid.EqualLatency", trumpHighBidLowLatency, trumpHighBidLowLatency, draw},

                {"BothTrump.FirstHasHigherBid.FirstHasLowerLatency", trumpHighBidLowLatency, trumpLowBidHighLatency, firstWins},
                {"BothTrump.FirstHasHigherBid.EqualLatency", trumpHighBidLowLatency, trumpLowBidLowLatency, firstWins},
                {"BothTrump.FirstHasHigherBid.FirstHasHigherLatency", trumpHighBidHighLatency, trumpLowBidLowLatency, firstWins},

                {"FirstHasTrump.FirstHasHigherBid.FirstHasLowerLatency", trumpHighBidLowLatency, normalLowBidHighLatency, firstWins},
                {"FirstHasTrump.FirstHasHigherBid.EqualLatency", trumpHighBidLowLatency, normalLowBidLowLatency, firstWins},
                {"FirstHasTrump.FirstHasHigherBid.FirstHasHigherLatency", trumpHighBidHighLatency, normalLowBidLowLatency, firstWins},

                {"FirstHasTrump.SameBid.FirstHasLowerLatency", trumpLowBidLowLatency, normalLowBidHighLatency, firstWins},
                {"FirstHasTrump.SameBid.EqualLatency", trumpLowBidLowLatency, normalLowBidLowLatency, firstWins},
                {"FirstHasTrump.SameBid.FirstHasHigherLatency", trumpLowBidHighLatency, normalLowBidLowLatency, firstWins},

                {"FirstHasTrump.FirstHasLowerBid.FirstHasLowerLatency", trumpLowBidLowLatency, normalHighBidHighLatency, firstWins},
                {"FirstHasTrump.FirstHasLowerBid.EqualLatency", trumpLowBidLowLatency, normalHighBidLowLatency, firstWins},
                {"FirstHasTrump.FirstHasLowerBid.FirstHasHigherLatency", trumpLowBidHighLatency, normalHighBidLowLatency, firstWins},

                {"BothNormal.SameBid.FirstHasLowerLatency", normalHighBidLowLatency, normalHighBidHighLatency, firstWins},
                {"BothNormal.SameBid.EqualLatency", normalHighBidLowLatency, normalHighBidLowLatency, draw},

                {"BothNormal.FirstHasHigherBid.FirstHasLowerLatency", normalHighBidLowLatency, normalLowBidHighLatency, firstWins},
                {"BothNormal.FirstHasHigherBid.EqualLatency", normalHighBidLowLatency, normalLowBidLowLatency, firstWins},
                {"BothNormal.FirstHasHigherBid.FirstHasHigherLatency", normalHighBidHighLatency, normalLowBidLowLatency, firstWins},
        };
    }

    @Test(dataProvider = "Channel Segment Comparator Data Provider")
    public void testCompare(final String testName, final ChannelSegment cs1, final ChannelSegment cs2,
            final int expected) throws Exception {

        log.debug("Running ChannelSegmentComparatorTest: {}", testName);
        Assert.assertEquals(csComparator.compare(cs1, cs2), expected);
        Assert.assertEquals(csComparator.compare(cs2, cs1), -expected);
        log.debug("Test passed: {}", testName);
    }

}