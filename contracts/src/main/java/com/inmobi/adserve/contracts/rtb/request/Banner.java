package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;


@Data
public final class Banner {
    private String id;
    private Integer w;
    private Integer h;
    private Integer pos;
    private List<Integer> btype;
    private List<Integer> battr;
    private List<String> mimes;
    private int topframe = 0;
    private List<Integer> expdir;
    private List<Integer> api;
    private BannerExtensions ext;
}
