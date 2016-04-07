package com.inmobi.adserve.contracts.rtb.request;

import java.util.Map;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Device {
    private Integer dnt;
    private String ua;
    private String ip;
    private Geo geo;
    private String didraw;
    private String didsha1;
    private String didmd5;
    private String dpidsha1;
    private String dpidmd5;
    private String ipv6;
    private String carrier;
    private String language;
    private String make;
    private String model;
    private String os;
    private String osv;
    private Double pxratio;
    private Integer js;
    private Integer connectiontype;
    private Integer devicetype;
    private String flashver;
    private Map<String, String> ext;
}
