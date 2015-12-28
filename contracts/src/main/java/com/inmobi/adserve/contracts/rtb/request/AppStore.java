package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class AppStore {
    private String rating;
    private String cat;
    private List<String> seccat;
}
