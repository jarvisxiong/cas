package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Video {
    private Integer w;
    private Integer h;
    @NonNull
    private final List<String> mimes;
    @NonNull
    private final Integer minduration;
    @NonNull
    private final Integer maxduration;
    private Integer protocol;
    private List<Integer> protocols;
    private Integer startdelay;
    private Integer linearity;
    private Integer sequence = 1;
    private Integer maxbitrate;
    private Integer boxingallowed = 1;
    private List<Integer> playbackmethod;
    private Integer pos = 0;
    private List<Banner> companionad;
    private List<Integer> api;
    private List<Integer> companiontype;
    private VideoExtension ext;
}
