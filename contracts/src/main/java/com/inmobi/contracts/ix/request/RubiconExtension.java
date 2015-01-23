package com.inmobi.contracts.ix.request;

import java.util.List;

import lombok.Data;

/**
 * Created by ishanbhatnagar on 22/1/15.
 */
@Data
public final class RubiconExtension {
    private String zone_id;
    private String enc;
    private Long pmptier = 1L;
    private Long dpf = 1L;
    private ExtRubiconTarget target;
    private List<String> track;
    private List<String> rtb;
    private List<String> nolog;
    private Integer size_id;
    private List<Integer> alt_size_ids;
    private Companionad companionad;
    private Content content;
    private Long site_id;
    private Long account_id;
    private String xff;
    private String res;
    private Double pixelratio;
    private String mime;
    private String advid;
}
