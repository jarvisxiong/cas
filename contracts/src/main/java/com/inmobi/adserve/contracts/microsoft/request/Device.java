package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class Device {
    private String dnt;
    private String lmt;
    private String ua;
    private String ip;
    private String ipv6;
    private String ifa;
    private String language;
    private String make;
    private String hwv;
    private String os;
    private String osv;
    private String osvname;
    private String connectiontype;
    private String carrier;
    private String devicetype;
    private String h;
    private String w;
    private String ppi;
    private Geo geo;
    private String Didsha256;
    private String dpidsha256;
    private String Macsha256;
}
