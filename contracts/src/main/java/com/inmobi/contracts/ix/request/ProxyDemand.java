package com.inmobi.contracts.ix.request;

import java.util.List;

import com.inmobi.contracts.ix.common.CommonExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class ProxyDemand {
    private Double marketrate;
    private List<ProxyBids> bids;
    private CommonExtension ext;
}
