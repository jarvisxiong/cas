package com.inmobi.adserve.contracts.rtb.request;

import com.google.gson.annotations.SerializedName;
import com.inmobi.adserve.contracts.common.request.nativead.Native;

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
    private int instl = 0;
    private String tagid;
    private double bidfloor = 0.0;
    private String bidfloorcur = "USD";
    private int secure = 0;
}
