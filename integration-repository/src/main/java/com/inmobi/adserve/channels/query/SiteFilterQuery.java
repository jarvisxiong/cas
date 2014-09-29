package com.inmobi.adserve.channels.query;

import com.inmobi.phoenix.data.RepositoryQuery;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by yasir.imteyaz on 27/09/14.
 */
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SiteFilterQuery implements RepositoryQuery {

    private String siteId;
    private Integer ruleType;
}
