package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Transparency {
    private Integer blind = 0;
    private List<Integer> buyers;
    private List<Integer> blindbuyers;
}
