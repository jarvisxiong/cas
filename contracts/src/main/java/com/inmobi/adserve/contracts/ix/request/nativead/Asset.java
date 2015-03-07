package com.inmobi.adserve.contracts.ix.request.nativead;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.ix.request.Video;

import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Asset {
    @NonNull
    private final Integer id;
    private Integer required = 0;
    private Title title;
    private Image img;
    private Video video;
    private Data data;
    private CommonExtension ext;

    public enum AssetType {
        TITLE, IMAGE, VIDEO, DATA;
    }
}
