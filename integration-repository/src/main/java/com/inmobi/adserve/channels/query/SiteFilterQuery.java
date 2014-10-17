package com.inmobi.adserve.channels.query;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import com.inmobi.phoenix.data.RepositoryQuery;

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
