package com.inmobi.adserve.contracts.ix.request;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Asset {
    private final Integer id;
    private Integer required;
    private Title title;
    private Image img;
    private Video video;
    private Data data;
    private CommonExtension ext;
}
