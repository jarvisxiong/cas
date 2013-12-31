package com.inmobi.adserve.channels.server.utils;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class CasUtils {

    private final RepositoryHelper repositoryHelper;

    @Inject
    public CasUtils(final RepositoryHelper repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    // TODO: move PricingEngineEntity fetching at handler level , when we move request parsing to handler level
    public PricingEngineEntity fetchPricingEngineEntity(final SASRequestParameters sasParams) {
        // Fetching pricing engine entity
        if (null != sasParams.getCountryStr()) {
            int country = Integer.parseInt(sasParams.getCountryStr());
            int os = sasParams.getOsId();
            return repositoryHelper.queryPricingEngineRepository(country, os);
        }
        return null;
    }

}
