package com.inmobi.adserve.contracts.rtb.request;

import java.util.List;

import lombok.Data;

/**
 * Created by avinash.kumar on 12/14/15.
 */
@Data
public final class BannerExtVideo {
    private final Integer linearity;
    private final Integer minduration;
    private final Integer maxduration;
    private final List<String> type;
}
