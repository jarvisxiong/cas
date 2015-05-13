package com.inmobi.adserve.contracts.misc.contentjson;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 7/5/15.
 */

@Getter
@Setter
@NoArgsConstructor
public class NativeContentJsonObject {
    private List<ImageAsset> imageAssets;
    private List<TextAsset> textAssets;
    private List<OtherAsset> otherAssets;
}
