package com.inmobi.adserve.contracts.oneDigitalAd.request;

import lombok.Data;

/**
 * Created by ghanshyam_sv on 22/6/16.
 */
@Data
public class Geo {
    private String country;
    private String region;
    private String city;
    private String zip;
    private String lat;
    private String lon;
    private Boolean gps;
    private Boolean js;
    private String ip;
}
