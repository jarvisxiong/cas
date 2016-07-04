package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class BidRequest {
    private String _type;
    private Imp imp;
    private App app;
    private Device device;
    private User user;
    private Regs regs;
    private Integer test;
    private String query;
    private List<String> content;
    private String URL;
    private String referralURL;
    private String queryType;
    private Map<String,String> publisherData;
}
