package com.inmobi.adserve.contracts.rtb.request;


import java.util.List;

@lombok.Data
public class PMP {
    private int private_auction = 0;
    private List<Deal> deals;
}
