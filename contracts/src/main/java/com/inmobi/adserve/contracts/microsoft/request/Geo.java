package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class Geo {
    private String lat;
    private String lon;
    private String country;
    private String city;
    private String region;
    private String zip;
    private String utcoffset;
}
