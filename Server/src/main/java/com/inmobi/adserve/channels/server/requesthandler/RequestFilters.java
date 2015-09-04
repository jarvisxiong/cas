package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.casthrift.DemandSourceType;


public class RequestFilters {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFilters.class);

    public boolean isDroppedInRequestFilters(final HttpRequestHandler hrh) {
        if (null != hrh.getTerminationReason()) {
            LOG.debug("Request not being served because of the termination reason {}", hrh.getTerminationReason());
            if (CasConfigUtil.JSON_PARSING_ERROR.equalsIgnoreCase(hrh.getTerminationReason())) {
                InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
            } else {
                InspectorStats.incrementStatCount(InspectorStrings.THRIFT_PARSING_ERROR, InspectorStrings.COUNT);
            }
            return true;
        }

        final SASRequestParameters sasParams = hrh.responseSender.getSasParams();

        if (null == sasParams) {
            LOG.error("Terminating request as sasParam is null");
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
            return true;
        }

        if (null == sasParams.getCategories()) {
            LOG.error("Category field is not present in the request so sending noad");
            sasParams.setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(CasConfigUtil.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(InspectorStrings.MISSING_CATEGORY, InspectorStrings.COUNT);
            return true;
        }

        if (null == sasParams.getSiteId()) {
            LOG.error("Terminating request as site id was missing");
            hrh.setTerminationReason(CasConfigUtil.MISSING_SITE_ID);
            InspectorStats.incrementStatCount(InspectorStrings.MISSING_SITE_ID, InspectorStrings.COUNT);
            return true;
        }

        if (!sasParams.getAllowBannerAds()) {
            LOG.info("Request not being served because of banner not allowed.");
            InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_BANNER_NOT_ALLOWED_FILTER,
                    InspectorStrings.COUNT);
            return true;
        }

        if (sasParams.isRewardedVideo()) {
            LOG.info("Request not being served because rewarded video is not supported.");
            InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_REWARDED_NOT_ALLOWED_FILTER,
                InspectorStrings.COUNT);
            return true;
        }

        if (sasParams.getSiteContentType() != null
                && !CasConfigUtil.allowedSiteTypes.contains(sasParams.getSiteContentType()
                        .name())) {
            LOG.info("Terminating request as incompatible content type");
            hrh.setTerminationReason(CasConfigUtil.INCOMPATIBLE_SITE_TYPE);
            InspectorStats.incrementStatCount(InspectorStrings.INCOMPATIBLE_SITE_TYPE, InspectorStrings.COUNT);
            return true;
        }
        final String tempSdkVersion = sasParams.getSdkVersion();
        if (null != tempSdkVersion) {
            try {
                if (("i".equalsIgnoreCase(tempSdkVersion.substring(0, 1)) || "a".equalsIgnoreCase(tempSdkVersion
                        .substring(0, 1))) && Integer.parseInt(tempSdkVersion.substring(1, 2)) < 3) {
                    LOG.info("Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(CasConfigUtil.LOW_SDK_VERSION);
                    InspectorStats.incrementStatCount(InspectorStrings.LOW_SDK_VERSION, InspectorStrings.COUNT);
                    return true;
                } else {
                    LOG.debug("sdk-version : {}", tempSdkVersion);
                }
            } catch (final StringIndexOutOfBoundsException exception) {
                LOG.error("Invalid sdk-version, Exception raised {}", exception);
            } catch (final NumberFormatException exception) {
                LOG.error("Invalid sdk-version, Exception raised {}", exception);
            }
        }

        if (sasParams.getProcessedMkSlot().isEmpty()) {
            incrementStats(sasParams);
            LOG.info("Request dropped since no slot in the list RqMkSlot has a mapping to InMobi slots/IX supported slots");
            return true;
        }
        return false;
    }

    /**
     * Increment stats for DST Level and also for all slots that were requested from UMP
     * 
     * @param sasParams
     */
    private void incrementStats(final SASRequestParameters sasParams) {
        final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
        final StringBuilder buildDst =
                new StringBuilder(dst != null ? dst.name() : String.valueOf(sasParams.getDst())).append("-").append(
                        InspectorStrings.DROPPED_IN_INVALID_SLOT_REQUEST_FILTER);
        // Increment stats for DST
        InspectorStats.incrementStatCount(buildDst.toString());
        // Increment stats all slots that were requested from UMP
        final List<Short> requestedSlots = sasParams.getRqMkSlot();
        if (CollectionUtils.isNotEmpty(requestedSlots)) {
            for (final Short slotId : requestedSlots) {
                final StringBuilder buildslots = new StringBuilder(buildDst);
                buildslots.append("-").append(slotId);
                InspectorStats.incrementStatCount(buildslots.toString());
            }
        }
    }

}
