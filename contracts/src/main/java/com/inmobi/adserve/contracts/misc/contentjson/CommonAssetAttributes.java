package com.inmobi.adserve.contracts.misc.contentjson;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
@Data
@NoArgsConstructor
public class CommonAssetAttributes {
    private NativeAdContentAsset adContentAsset;
    // private String name;
    private boolean isOptional;
}
