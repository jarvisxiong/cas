package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.channels.util.annotations.AdvertiserIdNameMap;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * devi.chand@inmobi.com
 */
public class AuctionFilters {
    private static final Logger LOG             = LoggerFactory.getLogger(AuctionEngine.class);

    @AdvertiserIdNameMap
    @Inject
    private static Map<String, String> advertiserIdNameMap;

    public List<ChannelSegment> rtbFilters(final List<ChannelSegment> rtbSegments, CasInternalRequestParameters casInternalRequestParameters) {
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
            else if (StringUtils.isEmpty(channelSegment.getAdNetworkInterface().getCreativeId())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in creative id not present filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInCreativeIdMissingFilter);
                }
            }
            else if (StringUtils.isEmpty(channelSegment.getAdNetworkInterface().getIUrl())) {
                rtbListIterator.remove();
                LOG.debug("Dropped in sample url not present filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInSampleImageUrlMissingFilter);
                }
            }
            else if (null == channelSegment.getAdNetworkInterface().getADomain() || channelSegment.getAdNetworkInterface().getADomain().isEmpty()) {
                rtbListIterator.remove();
                LOG.debug("Dropped in advertiser domains not present filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInSampleImageUrlMissingFilter);
                }
            }
            else if (null == channelSegment.getAdNetworkInterface().getAttribute() || channelSegment.getAdNetworkInterface().getAttribute().isEmpty()) {
                rtbListIterator.remove();
                LOG.debug("Dropped in creative attribute not present filter {}", channelSegment.getAdNetworkInterface()
                        .getName());
                if (advertiserIdNameMap.containsKey(channelSegment.getChannelEntity().getAccountId())) {
                    InspectorStats.incrementStatCount(
                            advertiserIdNameMap.get(channelSegment.getChannelEntity().getAccountId()),
                            InspectorStrings.droppedInCreativeAttributesMissingFilter);
                }
            }
        }
        LOG.debug("No of rtb partners who sent AD response with bid more than bidFloor {}", rtbList.size());
        return rtbList;
    }
}
