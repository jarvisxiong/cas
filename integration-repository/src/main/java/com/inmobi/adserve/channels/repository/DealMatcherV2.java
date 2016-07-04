package com.inmobi.adserve.channels.repository;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.googlecode.cqengine.resultset.ResultSet;
import com.inmobi.adserve.channels.entity.pmp.DealEntity;
import com.inmobi.casthrift.DemandSourceType;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DealMatcherV2 {
    public static Set<DealEntity> getDeals(final RepositoryHelper repositoryHelper,
            final Set<Integer> packageIds, final DemandSourceType dst, final String dspGUID) {

        log.debug("Inside DealMatcherV2");
        final ResultSet<DealEntity> rs = repositoryHelper.lazilyFindMatchingDeals(packageIds, dst, dspGUID);

        if (log.isDebugEnabled()) {
            log.debug("Number of deals selected: {}", rs.size());
            // TODO: Print deals
            log.debug("Deal selected: {}");
        }

        return ImmutableSet.copyOf(rs.iterator());
    }

}
