package com.inmobi.adserve.channels.server.requesthandler.beans;

import java.util.List;

import lombok.Data;

import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;


/**
 * @author abhishek.parwal
 * 
 */
@Data
public class AdvertiserMatchedSegmentDetail {

    private String               advertiserId;
    private List<String>         adGroups;
    private List<ChannelSegment> channelSegmentList;
}
