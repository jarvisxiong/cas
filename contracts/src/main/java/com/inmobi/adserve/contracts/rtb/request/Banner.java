package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class Banner {
    private String id;
    private Integer w;
    private Integer h;
    private Integer pos;
    private List<Integer> btype;
    private List<Integer> battr;
    private List<String> mimes;
    private int topframe = 0;
    private List<Integer> expdir;
    private List<Integer> api;
    private BannerExtensions ext;


    // TODO: Figure out enum interface / Add enums
    public enum API_FRAMEWORKS {
        VPAID_1_1(1), VPAID_2_0(2), MRAID_1(3), ORMMA(4), MRAID_2(5);

        private Integer value;

        private API_FRAMEWORKS(final Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }
}
