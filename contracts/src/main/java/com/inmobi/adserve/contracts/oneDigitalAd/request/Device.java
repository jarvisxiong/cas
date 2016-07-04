package com.inmobi.adserve.contracts.oneDigitalAd.request;

import lombok.Data;

/**
 * Created by ghanshyam_sv on 22/6/16.
 */
@Data
public class Device {
    private int connectiontype;
    private String os;
    private String osv;
    private String make;
    private String model;
    private String ua;
    private String ip;
    private Integer devicetype;
}
