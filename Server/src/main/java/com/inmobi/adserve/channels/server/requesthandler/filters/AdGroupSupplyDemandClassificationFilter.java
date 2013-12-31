package com.inmobi.adserve.channels.server.requesthandler.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentFeedbackEntity;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.config.ServerConfig;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
public class AdGroupSupplyDemandClassificationFilter extends AbstractAdGroupLevelFilter {
    private static final Logger    LOG = LoggerFactory.getLogger(AdGroupSupplyDemandClassificationFilter.class);
    private final RepositoryHelper repositoryHelper;
    private final ServerConfig     serverConfig;

    /**
     * @param traceMarkerProvider
     */
    protected AdGroupSupplyDemandClassificationFilter(final Provider<Marker> traceMarkerProvider,
            final RepositoryHelper repositoryHelper, final ServerConfig serverConfig) {
        super(traceMarkerProvider, InspectorStrings.droppedInSupplyDemandClassificationFilter);
        this.repositoryHelper = repositoryHelper;
        this.serverConfig = serverConfig;
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        SiteEcpmEntity siteEcpmEntity = getSiteEcpmEntity(sasParams);
        byte supplyClass = getSupplyClass(siteEcpmEntity);
        channelSegment.setPrioritisedECPM(calculatePrioritisedECPM(channelSegment, casContext));
        byte demandClass = getDemandClass(siteEcpmEntity, channelSegment.getPrioritisedECPM());

        LOG.debug(traceMarker, "Supply Class {} , Demand class is {} for adgroup {}", supplyClass, demandClass,
                channelSegment.getChannelSegmentEntity().getAdgroupId());

        PricingEngineEntity pricingEngineEntity = casContext.getPricingEngineEntity();

        if (pricingEngineEntity == null) {
            return !(PricingEngineEntity.DEFAULT_SUPPLY_DEMAND_MAPPING[supplyClass][demandClass] == 1);
        }
        else {
            return !(pricingEngineEntity.isSupplyAcceptsDemand(supplyClass, demandClass));
        }

    }

    private double calculatePrioritisedECPM(final ChannelSegment channelSegment, final CasContext casContext) {
        Marker traceMarker = traceMarkerProvider.get();

        ChannelSegmentFeedbackEntity channelSegmentFeedbackEntity = channelSegment
                .getChannelSegmentCitrusLeafFeedbackEntity();
        double eCPM = channelSegmentFeedbackEntity.getECPM();
        int impressionsRendered = channelSegmentFeedbackEntity.getBeacons();
        LOG.debug(traceMarker, "Impressions Rendered {} for adGroup{}", impressionsRendered, channelSegment
                .getChannelSegmentEntity().getAdgroupId());

        double eCPMBoost;

        if (casContext.getSumOfSiteImpressions() == 0) {
            eCPMBoost = 0;
        }
        else {
            if (impressionsRendered == 0) {
                impressionsRendered = 1;
            }
            eCPMBoost = Math.sqrt(serverConfig.getNormalizingFactor() * Math.log(casContext.getSumOfSiteImpressions())
                    / impressionsRendered);

        }

        int manualPriority = channelSegment.getChannelEntity().getPriority();
        manualPriority = manualPriority > 5 ? 1 : 5 - manualPriority;
        double prioritisedECPM = (eCPM + eCPMBoost) * manualPriority;
        LOG.debug(traceMarker, "Ecpm: {} Boost: {} Priority: {}", eCPM, eCPMBoost, manualPriority);
        LOG.debug(traceMarker, "PrioritisedECPM={}", prioritisedECPM);
        return prioritisedECPM;
    }

    private byte getDemandClass(final SiteEcpmEntity siteEcpmEntity, final double prioritisedECPM) {
        if (siteEcpmEntity == null) {
            return serverConfig.getDefaultDemandClass();
        }
        else {
            return getEcpmClass(prioritisedECPM, siteEcpmEntity.getNetworkEcpm());
        }
    }

    private byte getSupplyClass(final SiteEcpmEntity siteEcpmEntity) {
        Marker traceMarker = traceMarkerProvider.get();

        if (siteEcpmEntity == null) {
            LOG.debug(traceMarker, "SiteEcpmEntity is null, thus returning default class");
            return serverConfig.getDefaultSupplyClass();
        }
        else {
            LOG.debug(traceMarker, "SupplyClassFloors {} ", serverConfig.getSupplyClassFloors());
            LOG.debug(traceMarker, "Site ecpm is {} Network ecpm is {}", siteEcpmEntity.getEcpm(),
                    siteEcpmEntity.getNetworkEcpm());
            return getEcpmClass(siteEcpmEntity.getEcpm(), siteEcpmEntity.getNetworkEcpm());
        }
    }

    /**
     * @param sasParams
     * @return SiteEcpmEntity
     */
    private SiteEcpmEntity getSiteEcpmEntity(final SASRequestParameters sasParams) {
        return repositoryHelper.querySiteEcpmRepository(sasParams.getSiteId(),
                Integer.valueOf(sasParams.getCountryStr()), sasParams.getOsId());
    }

    private byte getEcpmClass(final Double ecpm, final Double networkEcpm) {
        Marker traceMarker = traceMarkerProvider.get();

        LOG.debug(traceMarker, "Ecpm is {} network ecpm {}", ecpm, networkEcpm);
        double ratio = ecpm / (networkEcpm > 0 ? networkEcpm : 1);
        byte ecpmClass = 0;
        for (Double floor : serverConfig.getSupplyClassFloors()) {
            if (ratio >= floor) {
                return ecpmClass;
            }
            ecpmClass++;
        }
        return ecpmClass;
    }

}
