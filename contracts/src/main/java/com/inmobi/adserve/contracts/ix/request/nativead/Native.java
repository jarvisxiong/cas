package com.inmobi.adserve.contracts.ix.request.nativead;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.NonNull;

/**
 *
 * @author ritwik.kumar
 *
 */
@lombok.Data
public final class Native {
    @NonNull
    private final NativeReqObj requestobj;
    private final String ver = "1";
    private List<Integer> api;
    private CommonExtension ext;
}
