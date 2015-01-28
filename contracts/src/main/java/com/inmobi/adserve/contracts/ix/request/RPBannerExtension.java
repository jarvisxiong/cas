package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@Data
public final class RPBannerExtension {
    private Integer size_id;
    private List<Integer> alt_size_ids;
    private String mime;
}
