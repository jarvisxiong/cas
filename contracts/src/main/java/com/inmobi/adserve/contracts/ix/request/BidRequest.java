package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class BidRequest {
    private String id;
    @NonNull
    private final List<Impression> imp;
    private Site site;
    private App app;
    private Device device;
    private User user;
    private Integer tmax;
    private Regulations regs;
    private CommonExtension ext;
}
