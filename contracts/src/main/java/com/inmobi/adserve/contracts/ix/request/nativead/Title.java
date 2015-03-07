package com.inmobi.adserve.contracts.ix.request.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Title {
    @NonNull
    private final Integer len;
    private CommonExtension ext;
}
