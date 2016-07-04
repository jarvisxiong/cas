package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class Content {
    private String url;
    private String title;
    private List<String> cat;
    private Integer context;
    private String Language;
    private String Country;
    private String PreferredLanguage;
    private List<String> keywords;
    private Map<String,List<String>> ext;
}
