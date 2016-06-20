/**
 *
 */
package com.inmobi.adserve.contracts.common.request.nativead;

import java.util.ArrayList;
import java.util.List;

import com.inmobi.adserve.contracts.iab.NativeLayoutId;
import com.inmobi.adserve.contracts.ix.common.CommonExtension;
import com.inmobi.adserve.contracts.ix.request.Video;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author ritwik.kumar
 *
 */
@Getter
@Setter
public class NativeReqObj {
    @NonNull
    private final Integer layout;
    private String ver;
    private Integer adunit;
    private Integer plcmtcnt = 1;
    private Integer seq = 0;
    private List<Asset> assets;
    private CommonExtension ext;

    public NativeReqObj(final NativeLayoutId layout) {
        this.layout = layout.getKey();
    }

    /**
     * @param asset
     */
    public void addAsset(final boolean required, final Object assetObj) {
        if (null != assetObj) {
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


}
