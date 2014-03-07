package com.inmobi.adserve.channels.server.requesthandler.filters;

import com.inmobi.adserve.channels.server.constants.ChannelSegmentFilterOrder;


/**
 * @author abhishek.parwal
 * 
 */
public interface ChannelSegmentFilter {

    void setOrder(final ChannelSegmentFilterOrder priority);

    ChannelSegmentFilterOrder getOrder();

}
