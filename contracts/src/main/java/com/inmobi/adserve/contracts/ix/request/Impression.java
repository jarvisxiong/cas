package com.inmobi.adserve.contracts.ix.request;

import com.google.gson.annotations.SerializedName;
import com.inmobi.adserve.contracts.common.request.nativead.Native;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class Impression {
    @NonNull
    private final String id;
    private Banner banner;
    private Video video;
    @SerializedName("native")
    private Native nat;
    private String tagid;
    private Double bidfloor = 0.0;
    private ProxyDemand proxydemand;
    private Integer instl = 0;
    private Integer secure = 0;
    private ImpressionExtension ext;
    // https://jira.corp.inmobi.com/browse/PROG-1052
    private String displaymanager;
    private String displaymanagerver;
}
