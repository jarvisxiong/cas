package com.inmobi.adserve.contracts.oneDigitalAd.request;

import lombok.Data;

import java.util.List;

/**
 * Created by ghanshyam_sv on 22/6/16.
 */
@Data
public class App {
    private String id;
    private List<String> cat;
    private String app;
    private String bundleurl;
    private Double price;
}
