package com.inmobi.adserve.contracts.ix.request;

import java.util.List;

import com.inmobi.template.gson.GsonContract;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
// Required parameters are enforced by @NonNull
@Getter
@Setter
@NoArgsConstructor
@GsonContract
public final class RubiconExtension {
    private String zone_id;
    private String enc;
    private Integer pmptier = 1;
    private Integer dpf = 1;
    private ExtRubiconTarget target;
    private ExtRubiconTarget track;
    private ExtRubiconTarget rtb;
    private ExtRubiconTarget nolog;
    private Integer size_id;
    private List<Integer> alt_size_ids;
    private Companionad companionad;
    private Content content;
    private Integer site_id;
    private Integer account_id;
    private String xff;
    private String res;
    private Double pixelratio;
    private String mime;
    // Advertiser Id
    private String advid;
    private String adtype;
    private Integer consent = 1;
}
