package com.inmobi.adserve.contracts.misc.contentjson;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */
@NoArgsConstructor
@Data
public class TextAsset {
    private CommonAssetAttributes commonAttributes;
    private int maxChars;
}
