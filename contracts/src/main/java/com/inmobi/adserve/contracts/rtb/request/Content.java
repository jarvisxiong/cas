package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Content {
    private String id;
    private Integer episode;
    private String title;
    private String series;
    private String season;
    private String url;
    private List<String> cat;
    private Integer videoquality;
    private String keywords;
    private String contentrating;
    private String userrating;
    private String context;
    private Integer livestream;
    private Integer sourcerelationship;
    private Producer producer;
    private Integer len;
    private Integer qagmediarating;
    private Integer embeddable;
    private String language;
}
