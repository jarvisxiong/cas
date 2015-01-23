package com.inmobi.contracts.ix.request;

import java.util.List;

import com.inmobi.contracts.ix.common.CommonExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class BidRequest {
    private String id;
    private final List<Impression> imp;
    private Site site;
    private App app;
    private Device device;
    private User user;
    private Integer tmax;
    private Regulations regs;
    private CommonExtension ext;
}

