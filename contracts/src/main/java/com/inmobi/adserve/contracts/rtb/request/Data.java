package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@lombok.Data
public class Data {
    private String id;
    private String name;
    private List<Segment> segment;
}
