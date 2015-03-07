package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Site {
    private String id;
    private String name;
    private String domain;
    private List<String> cat;
    private String page;
    private Publisher publisher;
    private Content content;
    private List<String> keywords;
    private List<String> blocklists;
    private AdQuality aq;
    private Transparency transparency;
    private CommonExtension ext;
}
