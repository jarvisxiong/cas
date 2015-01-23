package com.inmobi.adserve.contracts.ix.request;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Device {
    private Integer dnt;
    private Integer lmt;
    private final String ua;
    private final String ip;
    private Geo geo;
    private String didsha1;
    private String didmd5;
    private String dpidsha1;
    private String dpidmd5;
    private String carrier;
    private String language;
    private String make;
    private String model;
    private String os;
    private String osv;
    private Integer connectiontype;
    private String ifa;
    private CommonExtension ext;
}
