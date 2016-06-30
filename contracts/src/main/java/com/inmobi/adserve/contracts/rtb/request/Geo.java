package com.inmobi.adserve.contracts.rtb.request;

import lombok.Data;


@Data
public final class Geo {
    private Double lat;
    private Double lon;
    private String country;
    private String region;
    private String regionfips104;
    private String metro;
    private String city;
    private String zip;
    private Integer type;
}
