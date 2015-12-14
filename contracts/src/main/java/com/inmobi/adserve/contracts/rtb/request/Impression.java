package com.inmobi.adserve.contracts.rtb.request;

import java.util.Arrays;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.inmobi.adserve.contracts.ix.request.nativead.Native;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Impression {
    @NonNull
    private final String id;
    private Banner banner;
    private Video video;
    @SerializedName("native")
    private Native nat;
    private String displaymanager;
    private String displaymanagerver;
    private Integer instl = 0;
    private String tagid;
    private Double bidfloor = 0.0;
    private String bidfloorcur = "USD";
    private List<String> iframebuster = Arrays.asList("None");
    private Integer secure = 0;
}
