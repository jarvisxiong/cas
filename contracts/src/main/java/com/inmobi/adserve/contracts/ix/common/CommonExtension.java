package com.inmobi.adserve.contracts.ix.common;

import com.inmobi.adserve.contracts.ix.request.Blind;
import com.inmobi.adserve.contracts.ix.request.DtExtensions;
import com.inmobi.adserve.contracts.ix.request.RubiconExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class CommonExtension {
    private RubiconExtension rp;
    private Blind blind;
    private DtExtensions dt;
}
