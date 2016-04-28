package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Producer {
    private String id;
    private String name;
    private List<String> cat;
    private String domain;
}
