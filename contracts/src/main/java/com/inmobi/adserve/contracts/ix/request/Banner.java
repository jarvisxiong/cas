package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import lombok.Data;


/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Banner {
    private String id;
    private Integer w;
    private Integer h;
    private Integer pos;
    private List<Integer> expdir;
    private List<Integer> api;
    private BannerExtension ext;
}
