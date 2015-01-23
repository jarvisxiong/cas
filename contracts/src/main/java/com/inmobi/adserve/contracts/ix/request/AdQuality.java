package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public class AdQuality {
    private List<String> tags;
    private String sensitivity = "high";
}
