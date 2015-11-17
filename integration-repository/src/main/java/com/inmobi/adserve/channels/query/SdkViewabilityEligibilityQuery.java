package com.inmobi.adserve.channels.query;

import com.inmobi.phoenix.data.RepositoryQuery;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by ishan.bhatnagar on 04/11/15.
 */
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SdkViewabilityEligibilityQuery implements RepositoryQuery {
    private Integer countryId;
    private String adType;
    private Integer dst;
}
