package com.inmobi.adserve.channels.server.requesthandler.filters.advertiser;

import java.util.List;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.requesthandler.beans.AdvertiserMatchedSegmentDetail;
import com.inmobi.adserve.channels.server.requesthandler.filters.ChannelSegmentFilter;


/**
 * @author abhishek.parwal
 *
 */
public interface AdvertiserLevelFilter extends ChannelSegmentFilter {

    public void filter(final List<AdvertiserMatchedSegmentDetail> matchedSegmentDetails,
            final SASRequestParameters sasParams);

}
