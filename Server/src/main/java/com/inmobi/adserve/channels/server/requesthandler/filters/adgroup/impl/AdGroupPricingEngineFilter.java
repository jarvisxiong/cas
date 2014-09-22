package com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.impl;

import java.util.Date;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.server.requesthandler.ChannelSegment;
import com.inmobi.adserve.channels.server.requesthandler.filters.adgroup.AbstractAdGroupLevelFilter;
import com.inmobi.adserve.channels.util.InspectorStrings;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class AdGroupPricingEngineFilter extends AbstractAdGroupLevelFilter {
    private static final Logger LOG = LoggerFactory.getLogger(AdGroupPricingEngineFilter.class);

    /**
     * @param traceMarkerProvider
     */
    @Inject
    protected AdGroupPricingEngineFilter(final Provider<Marker> traceMarkerProvider) {
        super(traceMarkerProvider, InspectorStrings.DROPPED_IN_PRICING_ENGINE_FILTER);
    }

    @Override
    protected boolean failedInFilter(final ChannelSegment channelSegment, final SASRequestParameters sasParams,
            final CasContext casContext) {

        Marker traceMarker = traceMarkerProvider.get();

        PricingEngineEntity pricingEngineEntity = casContext.getPricingEngineEntity();

        Double dcpFloor = pricingEngineEntity == null ? null : pricingEngineEntity.getDcpFloor();

        if (dcpFloor != null) {
            // applying the boost
            Date eCPMBoostExpiryDate = channelSegment.getChannelSegmentEntity().getEcpmBoostExpiryDate();
            Date today = new Date();
            double ecpm = channelSegment.getChannelSegmentCitrusLeafFeedbackEntity().getECPM();
            if (null != eCPMBoostExpiryDate && eCPMBoostExpiryDate.compareTo(today) > 0) {
                LOG.debug(traceMarker, "EcpmBoost is applied for {}", channelSegment.getChannelSegmentEntity()
                        .getAdgroupId());
                LOG.debug(traceMarker, "Ecpm before boost is {}", ecpm);
                ecpm = ecpm + channelSegment.getChannelSegmentEntity().getEcpmBoost();
                LOG.debug(traceMarker, "Ecpm after boost is {}", ecpm);
            }

            // applying dcp floor
            int percentage;
            if (dcpFloor > 0.0) {
                percentage = (int) ((ecpm / dcpFloor) * 100);
            } else {
                percentage = 150;
            }

            // Allow percentage of times any segment
            if (percentage > 100) {
                percentage = 100;
            } else if (percentage == 100) {
                percentage = 50;
            } else if (percentage >= 80) {
                percentage = 10;
            } else {
                percentage = 1;
            }

            return CasConfigUtil.random.nextInt(100) >= percentage;
        }

        return false;
    }

}
