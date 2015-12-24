package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@lombok.Data
public final class User {
    private String id;
    private String buyeruid;
    private Integer yob;
    private String gender;
    private String keywords;
    private String customdata;
    private Geo geo;
    private List<Data> data;
}
