package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.adpool.AuctionType.TRUMP;
import static com.inmobi.adserve.channels.server.requesthandler.AdPoolResponseCreator.getAdIdChain;
import static com.inmobi.adserve.channels.server.requesthandler.AdPoolResponseCreator.getHighestBid;
import static com.inmobi.adserve.channels.server.requesthandler.AdPoolResponseCreator.setDealAndAttributionMetadata;
import static com.inmobi.adserve.channels.util.demand.enums.DealType.PREFERRED;
import static com.inmobi.adserve.channels.util.demand.enums.DealType.RIGHT_TO_FIRST_REFUSAL;
import static com.inmobi.adtemplate.platform.CreativeType.META;
import static com.inmobi.adtemplate.platform.CreativeType.TEXT;
import static com.inmobi.adtemplate.platform.CreativeType.VIDEO_VAST_URL;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.DealAttributionMetadata;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.util.demand.enums.AuctionType;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.gson.GsonManager;
import com.inmobi.types.AdIdChain;

import lombok.Getter;

public class AdPoolResponseCreatorTest {

    private static class IXAdNetworkForTest extends IXAdNetwork {
        static {
            templateConfiguration = new DefaultConfiguration() {
                @Override
                public GsonManager getGsonManager() {
                    return new GsonManager();
                }
            };
        }

        @Getter
        private final Double adjustbid;

        IXAdNetworkForTest(final Double adjustbid, final BaseConfiguration config) {
            super(config, null, null, null, null, null);
            this.adjustbid = adjustbid;
        }
    }

    private static class RTBAdNetworkForTest extends RtbAdNetwork {

        static {
            templateConfiguration = new DefaultConfiguration() {
                @Override
                public GsonManager getGsonManager() {
                    return new GsonManager();
                }
            };
        }

        RTBAdNetworkForTest(final BaseConfiguration config) {
            super(config, null, null, null, null, null);
        }
    }

    private static final class Adn extends BaseAdNetworkImpl {

        @Getter
        private double secondBidPriceInUsd;

        Adn(final AuctionType auctionType, final String dealId, final boolean isTrump, final Set<Integer> matchedCsids) {
            super(null, null);

            if (StringUtils.isNotBlank(dealId)) {
                this.deal = DealEntity.newBuilder().id(dealId).dealType(isTrump ? RIGHT_TO_FIRST_REFUSAL : PREFERRED).build();
            }

            if (CollectionUtils.isNotEmpty(matchedCsids)) {
                this.dealAttributionMetadata = new DealAttributionMetadata(null, matchedCsids, null, true);
            }
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

    @DataProvider(name = "Highest Bid Data Provider")
    public Object[][] dataProvider() {
        final BaseConfiguration testConfig = new BaseConfiguration();
        testConfig.setProperty("null.accountId", 0);
        testConfig.setProperty("null.isWnRequired", false);
        testConfig.setProperty("null.siteBlinded", false);
        final IXAdNetwork ixAdNetworkWithAdjustBid = new IXAdNetworkForTest(5.0, testConfig);
        final IXAdNetwork ixAdNetworkWithoutAdjustBid = new IXAdNetworkForTest(null, testConfig);
        final RtbAdNetwork rtbAdNetwork = new RTBAdNetworkForTest(testConfig);

        final long highestBidLarge = (long) (6 * AdPoolResponseCreator.BID_CONVERSION_FACTOR);
        final long highestBidMedium = (long) (5 * AdPoolResponseCreator.BID_CONVERSION_FACTOR);

        return new Object[][] {{"IX.WithoutAdjustBid.HighestBidNotSet", ixAdNetworkWithoutAdjustBid, null, null}, {"IX.WithAdjustBid.HighestBidNotSet", ixAdNetworkWithAdjustBid, null, highestBidMedium}, {"IX.WithoutAdjustBid.HighestBidSet", ixAdNetworkWithoutAdjustBid, 5.0, highestBidMedium}, {"IX.AdjustBid<HighestBid", ixAdNetworkWithAdjustBid, 6.0, highestBidLarge}, {"IX.AdjustBid=HighestBid", ixAdNetworkWithAdjustBid, 5.0, highestBidMedium}, {"IX.AdjustBid>HighestBid", ixAdNetworkWithAdjustBid, 4.0, highestBidMedium}, {"RTBD.HighestBidNotSet", rtbAdNetwork, null, null}, {"RTBD.HighestBidSet", rtbAdNetwork, 5.0, highestBidMedium},};
    }

    @Test(dataProvider = "Highest Bid Data Provider")
    public void testHighestBid(final String testName, final AdNetworkInterface adn, final Double highestBid,
            final Long expected) {
        Assert.assertEquals(getHighestBid(adn, highestBid), expected);
    }

    @DataProvider(name = "Deal and Attribution Data Provider")
    public Iterator<Object[]> dataProvider2() {

        final List<Object[]> tests = new ArrayList<>();
        // Making the tests agnostic of dst and auction type
        for (final DemandSourceType dst : DemandSourceType.values()) {
            for (final AuctionType auctionType: AuctionType.values()) {
                final Object[][] test = {
                        {"NoDeal", null, false, auctionType, null},
                        {"Deal.NonTrump.NoAttribution", "deal", false, auctionType, null},
                        {"Deal.Trump.NoAttribution", "deal", true, auctionType, null},
                        {"Deal.NonTrump.Attribution", "deal", false, auctionType, ImmutableSet.of(1)},
                        {"Deal.Trump.Attribution", "deal", true, auctionType, ImmutableSet.of(1)},
                };

                tests.addAll(Arrays.asList(test));
            }
        }

        return tests.iterator();
    }

    @Test(dataProvider = "Deal and Attribution Data Provider")
    public void testDealAttribution(final String testName, final String dealId, final boolean isTrumpDeal,
            final AuctionType auctionType, final Set<Integer> matchedCsids) {

        final AdNetworkInterface adn = new Adn(auctionType, dealId, isTrumpDeal, matchedCsids);
        final AdInfo adInfo = new AdInfo();
        setDealAndAttributionMetadata(adInfo, adn);

        Assert.assertEquals(adInfo.getDealId(), dealId);
        Assert.assertEquals(adInfo.getAuctionType(), isTrumpDeal ? TRUMP : auctionType.getGetUMPAuctionType());
        Assert.assertEquals(adInfo.getMatched_csids(), matchedCsids);
    }

    @Test
    public void testGetAdIdChain() {
        final ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        final String testAdgroupId = "testAdgroupId";
        final String[] adIds = {"testAdIdNative", "testAdIdBanner", "testAdIdVideo"};
        final String testCampaignId = "testCampaignId";
        final Long[] incIds = {123L, 124L, 125L};
        final Integer[] adFormatIds = {META.getValue(), TEXT.getValue(), VIDEO_VAST_URL.getValue()};
        final long adgroupIncId = 456L;
        final long campaignIncId = 789L;
        final String testAdvertiserId = "testAdvertiserId";

        builder.setAdgroupId(testAdgroupId);
        builder.setAdIds(adIds);
        builder.setCampaignId(testCampaignId);
        builder.setIncIds(incIds);
        builder.setAdFormatIds(adFormatIds);
        builder.setAdgroupIncId(adgroupIncId);
        builder.setCampaignIncId(campaignIncId);
        builder.setAdvertiserId(testAdvertiserId);
        final ChannelSegmentEntity cs = builder.build();

        for (final ADCreativeType creativeType : ADCreativeType.values()) {
            final AdIdChain adIdChain = getAdIdChain(cs, creativeType);
            Assert.assertEquals(adIdChain.getAdgroup_guid(), cs.getAdgroupId());
            Assert.assertEquals(adIdChain.getCampaign_guid(), cs.getCampaignId());
            Assert.assertEquals(adIdChain.getGroup(), cs.getAdgroupIncId());
            Assert.assertEquals(adIdChain.getCampaign(), cs.getCampaignIncId());
            Assert.assertEquals(adIdChain.getAdvertiser_guid(), cs.getAdvertiserId());

            if (adIdChain.getAd_guid().equalsIgnoreCase(StringUtils.EMPTY)) {
                Assert.fail("Could not find ad id for corresponding Creative Type");
            } else {
                Assert.assertEquals(adIdChain.getAd_guid(), cs.getAdId(creativeType));
            }

            if (-1 == adIdChain.getAd()) {
                Assert.fail("Could not find ad inc id for corresponding Creative Type");
            } else {
                Assert.assertEquals(adIdChain.getAd(), cs.getIncId(creativeType));
            }

        }

    }


}
