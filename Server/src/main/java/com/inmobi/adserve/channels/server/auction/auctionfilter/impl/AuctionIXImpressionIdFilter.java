package com.inmobi.adserve.channels.server.auction.auctionfilter.impl;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.server.auction.auctionfilter.AbstractAuctionFilter;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Marker;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * This filter checks whether:
 * 1) the response impression object id is present in the set of ids of the Impression objects sent in the request
 * 2) there is only one bid object in the bidResponse (therefore, only one response impression object id)
 */

@Singleton
public class AuctionIXImpressionIdFilter extends AbstractAuctionFilter {

    @Inject
    protected AuctionIXImpressionIdFilter(Provider<Marker> traceMarkerProvider, final ServerConfig serverConfiguration) {
        super(traceMarkerProvider, InspectorStrings.droppedInAuctionIxImpressionIdFilter, serverConfiguration);
        isApplicableRTBD = false;
        isApplicableIX = true;
    }

    @Override
    protected boolean failedInFilter(ChannelSegment rtbSegment, CasInternalRequestParameters casInternalRequestParameters) {
        if (rtbSegment.getAdNetworkInterface() instanceof IXAdNetwork) {
            IXAdNetwork ixAdNetwork = (IXAdNetwork) rtbSegment.getAdNetworkInterface();
            try {
                int responseImpressionId = Integer.parseInt(ixAdNetwork.getRtbImpressionId());
                if (ixAdNetwork.getResponseBidObjCount() == 1
                        && responseImpressionId >= 1 && responseImpressionId <= ixAdNetwork.getImpressionObjCount()) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }

        }
        return true;
    }
}



