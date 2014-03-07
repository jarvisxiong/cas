package com.inmobi.adserve.channels.server.utils;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.beans.CasContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class CasUtils {
    private static final Logger    LOG = LoggerFactory.getLogger(CasUtils.class);

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

    public Double getRtbFloor(final CasContext casContext, final SASRequestParameters sasRequestParameters) {
        PricingEngineEntity pricingEngineEntity = casContext.getPricingEngineEntity();
        if (pricingEngineEntity == null) {
            casContext.setPricingEngineEntity(pricingEngineEntity);
        }

        return pricingEngineEntity == null ? 0 : pricingEngineEntity.getRtbFloor();
    }

    public Double getNetworkEcpm(final CasContext casContext, final SASRequestParameters sasParams) {
        SiteEcpmEntity siteEcpmEntity = repositoryHelper.querySiteEcpmRepository(sasParams.getSiteId(),
                Integer.valueOf(sasParams.getCountryStr()), sasParams.getOsId());
        double networkEcpm = 0.0;
        if (null != siteEcpmEntity) {
            networkEcpm = siteEcpmEntity.getNetworkEcpm();
        }
        return networkEcpm;
    }

}
