package com.inmobi.adserve.contracts.microsoft.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by deepak.jha on 3/22/16.
 */
@Data
public class App {
    private String id;
    private String name;
    private List<String> cat;
    private String ver;
    private String bundle;
    private String Language;
    private String Country;
    private String storeurl;
    private List<String> keywords;
    private String RequestAgent;
    private Content content;
    private Publisher publisher;
    private Map<String,String> ext;

}
