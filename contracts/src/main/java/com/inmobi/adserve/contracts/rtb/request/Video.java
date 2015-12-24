package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by avinash.kumar on 12/14/15.
 */

@Data
public final class Video {
    @NonNull
    private final List<String> mimes;
    @NonNull
    private final Integer linearity;
    @NonNull
    private final Integer minduration;
    @NonNull
    private final Integer maxduration;
    @NonNull
    private final Integer protocol;
    private Integer w;
    private Integer h;
    private Integer startdelay;
    private int sequence = 1;
    private List<Integer> battr;
    private Integer maxextended;
    private Integer minbitrate;
    private Integer maxbitrate;
    private int boxingallowed = 1;
    private List<Integer> playbackmethod;
    private List<Integer> delivery;
    private int pos = 0;
    private List<Banner> companionad;
    private List<Integer> api;
    private List<Integer> companiontype;
}
