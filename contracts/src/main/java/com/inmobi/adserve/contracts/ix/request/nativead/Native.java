package com.inmobi.adserve.contracts.ix.request.nativead;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.ix.request.Video;

import lombok.NonNull;

/**
 * Created by ishanbhatnagar on 23/1/15.
 */
@lombok.Data
public final class Native {
    @NonNull
    private final Integer ver = 1;
    private Integer layout;
    private Integer adunit;
    private Integer plcmtcnt = 1;
    private Integer seq = 0;
    private List<Asset> assets;
    private CommonExtension ext;

    /**
     * @param asset
     */
    public void addAsset(final boolean required, final Object assetObj) {
        if (assets == null) {
            assets = new ArrayList<>();
        }
        final Asset asset = new Asset(assets.size() + 1);
        asset.setRequired(required ? 1 : 0);
        if (assetObj instanceof Image) {
            asset.setImg((Image) assetObj);
        } else if (assetObj instanceof Title) {
            asset.setTitle((Title) assetObj);
        } else if (assetObj instanceof Video) {
            asset.setVideo((Video) assetObj);
        } else if (assetObj instanceof Data) {
            asset.setData((Data) assetObj);
        }
        // TODO: No link object?
        assets.add(asset);
    }
}
