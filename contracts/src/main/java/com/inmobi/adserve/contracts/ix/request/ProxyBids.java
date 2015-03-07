package com.inmobi.adserve.contracts.ix.request;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class ProxyBids {
    private String id;
    @NonNull
    private final Double price;
}
