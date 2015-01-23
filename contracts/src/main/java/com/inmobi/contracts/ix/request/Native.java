package com.inmobi.contracts.ix.request;

import java.util.List;

import com.inmobi.contracts.ix.common.CommonExtension;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Native {
    private final Integer ver = 1;
    private Integer layout;
    private Integer adunit;
    private Integer plcmtcnt = 1;
    private Integer seq = 0;
    private List<Asset> assets;
    private CommonExtension ext;
}
