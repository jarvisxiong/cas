package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class App {
    private String id;
    private String name;
    private String domain;
    private List<String> cat;
    private List<String> sectioncat;
    private List<String> pagecat;
    private String ver;
    private String bundle;
    private int privacypolicy;
    private int paid;
    private Publisher publisher;
    private Content content;
    private String keywords;
    private String storeurl;
    private AppExt ext;
}
