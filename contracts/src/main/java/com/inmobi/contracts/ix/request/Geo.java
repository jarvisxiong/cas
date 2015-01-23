package com.inmobi.contracts.ix.request;

import com.inmobi.contracts.ix.common.CommonExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Geo {
    private Double lat;
    private Double lon;
    private String country;
    private String region;
    private String metro;
    private String city;
    private String zip;
    private Integer type;
    private CommonExtension ext;
}
