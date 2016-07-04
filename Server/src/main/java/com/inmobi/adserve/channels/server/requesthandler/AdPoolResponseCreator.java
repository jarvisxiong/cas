package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.adpool.AuctionType.TRUMP;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.USD;
import static com.inmobi.types.PricingModel.CPM;
import static lombok.AccessLevel.PACKAGE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.Creative;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.AdNetworkInterface;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.pmp.DealAttributionMetadata;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.adserve.channels.util.Utils.ImpressionIdGenerator;
import com.inmobi.casthrift.ADCreativeType;
import com.inmobi.types.AdIdChain;
import com.inmobi.types.GUID;

import lombok.NoArgsConstructor;


@NoArgsConstructor(access = PACKAGE)
final class AdPoolResponseCreator {
    static final double BID_CONVERSION_FACTOR = Math.pow(10, 6);

    static AdPoolResponse createAdPoolResponse(final ChannelSegment channelSegment, final String adMarkup,
            final Double highestParticipatingBid) {
        final ChannelSegmentEntity csEntity = channelSegment.getChannelSegmentEntity();
        final AdNetworkInterface adn = channelSegment.getAdNetworkInterface();

        final AdInfo adInfo = new AdInfo();
        adInfo.setPricingModel(CPM);
        
        final Long highestBid = getHighestBid(adn, highestParticipatingBid);
        if (null != highestBid) {
            adInfo.setHighestBid(highestBid);
        }

        setDealAndAttributionMetadata(adInfo, adn);

        final long bid = (long) (adn.getBidPriceInUsd() * BID_CONVERSION_FACTOR);
        adInfo.setPrice(bid);
        adInfo.setBid(bid);
        if (!USD.equalsIgnoreCase(adn.getCurrency())) {
            adInfo.setOriginalCurrencyName(adn.getCurrency());
            adInfo.setBidInOriginalCurrency((long) (adn.getBidPriceInLocal() * BID_CONVERSION_FACTOR));
        }

        adInfo.setDeprecatedAdIds(Collections.singletonList(getAdIdChain(csEntity, adn.getCreativeType())));
        adInfo.setSlotServed(adn.getSelectedSlotId());
        adInfo.setCreative(new Creative(adMarkup));

        final UUID uuid = UUID.fromString(adn.getImpressionId());
        adInfo.setDeprecatedImpressionId(new GUID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits()));

        final String renderUnitId = ImpressionIdGenerator.getInstance().resetWilburyIntKey(adn.getImpressionId(), 0L);
        final UUID renderUnitUUID = UUID.fromString(renderUnitId);
        adInfo.setRenderUnitId(
                new GUID(renderUnitUUID.getMostSignificantBits(), renderUnitUUID.getLeastSignificantBits()));

        final AdPoolResponse adPoolResponse = new AdPoolResponse();
        adPoolResponse.setAds(Collections.singletonList(adInfo));
        adPoolResponse.setMinChargedValue((long) (adn.getSecondBidPriceInUsd() * BID_CONVERSION_FACTOR));

        return adPoolResponse;
    }

    static void setDealAndAttributionMetadata(final AdInfo adInfo, final AdNetworkInterface adn) {
        final DealEntity deal = adn.getDeal();
        if (null != deal) {
            adInfo.setDealId(deal.getId());
            adInfo.setAuctionType(deal.isTrumpDeal() ? TRUMP : adn.getAuctionType().getGetUMPAuctionType());

            final DealAttributionMetadata attributionMetadata = adn.getDealAttributionMetadata();
            if (null != attributionMetadata && attributionMetadata.isDataVendorAttributionRequired()) {
                adInfo.setMatched_csids(new ArrayList<>(attributionMetadata.getUsedCsids()));
            }
        } else {
            adInfo.setAuctionType(adn.getAuctionType().getGetUMPAuctionType());
        }
    }

    // TODO: Get confirmation from Aman
    static Long getHighestBid(final AdNetworkInterface adn, final Double highestBid) {
        Long returnVal = null;
        
        if (adn instanceof IXAdNetwork) {
            final Double adjustBid = ((IXAdNetwork)adn).getAdjustbid();
            final Long highestBidAtRP;
            if (null != adjustBid) {
                highestBidAtRP = (long) (adjustBid * BID_CONVERSION_FACTOR);
            } else {
                highestBidAtRP = null;
            }

            if (null != highestBid) {
                final long convertedHighestMultiFormatBid = (long) (highestBid * BID_CONVERSION_FACTOR);

                returnVal = null != highestBidAtRP ?
                        Math.max(highestBidAtRP, convertedHighestMultiFormatBid) : convertedHighestMultiFormatBid;
            } else {
                returnVal = highestBidAtRP;
            }
        } else if (null != highestBid) {
            returnVal = (long) (highestBid * BID_CONVERSION_FACTOR);
        }
        
        return returnVal;
    }
    
    static AdIdChain getAdIdChain(final ChannelSegmentEntity csEntity, final ADCreativeType adCreativeType) {
        final AdIdChain adIdChain = new AdIdChain();
        
        adIdChain.setAdgroup_guid(csEntity.getAdgroupId());
        adIdChain.setAd_guid(csEntity.getAdId(adCreativeType));
        adIdChain.setCampaign_guid(csEntity.getCampaignId());
        adIdChain.setAd(csEntity.getIncId(adCreativeType));
        adIdChain.setGroup(csEntity.getAdgroupIncId());
        adIdChain.setCampaign(csEntity.getCampaignIncId());
        adIdChain.setAdvertiser_guid(csEntity.getAdvertiserId());
        
        return adIdChain;
    }
    
}
