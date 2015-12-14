package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@lombok.Data
public class AppStore {
    private String rating;
    private String cat;
    private List<String> seccat;
}
