package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Site {
    private String id;
    private String name;
    private String domain;
    private List<String> cat;
    private List<String> sectionCat;
    private List<String> pageCat;
    private String page;
    private Integer privacypolicy;
    private String ref;
    private String search;
    private Publisher publisher;
    private Content content;
    private List<String> keywords;
    private Map<String, String> ext;
}
