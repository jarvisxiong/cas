package com.inmobi.adserve.contracts.ix.request;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@Data
public final class RPSiteExtension {
    @NonNull
    private final Integer site_id;
}
