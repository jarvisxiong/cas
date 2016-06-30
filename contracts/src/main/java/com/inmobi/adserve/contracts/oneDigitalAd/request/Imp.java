package com.inmobi.adserve.contracts.oneDigitalAd.request;

import lombok.Data;

import java.util.List;

/**
 * Created by ghanshyam_sv on 22/6/16.
 */
@Data
public class Imp {
    private List<Integer> battr;
    private Integer w;
    private Integer h;
}
