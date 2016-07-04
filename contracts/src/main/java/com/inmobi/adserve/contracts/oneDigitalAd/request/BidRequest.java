package com.inmobi.adserve.contracts.oneDigitalAd.request;

import lombok.Data;

/**
 * Created by ghanshyam_sv on 22/6/16.
 */
@Data
public class BidRequest {
    private String reqId;
    private String stsid;
    private Imp imp;
    private App app;
    private Device device;
    private Geo geo;
    private Deviceids deviceids;
}
