package com.inmobi.adserve.channels.server.requesthandler.beans;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;


/**
 * @author abhishek.parwal
 * 
 */
@AllArgsConstructor
public class AdvertiserMatchedSegmentDetail {

    @Getter
    private final List<ChannelSegment> channelSegmentList;

}
