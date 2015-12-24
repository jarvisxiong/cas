package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class BidRequest {
    @NonNull
    private final String id;
    @NonNull
    private final List<Impression> imp;
    private Site site;
    private App app;
    private Device device;
    private User user;
    private Integer at;
    private Integer tmax;
    private List<String> wseat;
    private Integer allimps;
    private List<String> cur;
    private List<String> bcat;
    private List<String> badv;
}
