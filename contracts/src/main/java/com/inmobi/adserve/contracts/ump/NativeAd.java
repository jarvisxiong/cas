package com.inmobi.adserve.contracts.ump;

import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * Created by avinash.kumar on 4/29/16.
 */
@Data
public class NativeAd {
    private final String pubContent;
    private final String contextCode;
    private final String namespace;
    private final String landingPage;
    private final Map<Integer, Map<String, List<String>>> eventTracking;
}
