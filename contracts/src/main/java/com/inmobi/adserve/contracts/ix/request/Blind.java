package com.inmobi.adserve.contracts.ix.request;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
// Do not add any default value as same object is used in many places
public final class Blind {
    private String name;
    private String domain;
    private String bundle;
    private String page;
}
