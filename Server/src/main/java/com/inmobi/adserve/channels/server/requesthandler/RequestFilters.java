package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.channels.util.InspectorStrings.COUNT;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_CUSTOM_TEMPLATE_NOT_ALLOWED_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_BANNER_NOT_ALLOWED_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.DROPPED_IN_INVALID_SLOT_REQUEST_FILTER;
import static com.inmobi.adserve.channels.util.InspectorStrings.INCOMPATIBLE_SITE_TYPE;
import static com.inmobi.adserve.channels.util.InspectorStrings.JSON_PARSING_ERROR;
import static com.inmobi.adserve.channels.util.InspectorStrings.LOW_SDK_VERSION;
import static com.inmobi.adserve.channels.util.InspectorStrings.MISSING_CATEGORY;
import static com.inmobi.adserve.channels.util.InspectorStrings.MISSING_SITE_ID;
import static com.inmobi.adserve.channels.util.InspectorStrings.THRIFT_PARSING_ERROR;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.api.SASParamsUtils;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.casthrift.DemandSourceType;


public class RequestFilters {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFilters.class);

    public boolean isDroppedInRequestFilters(final HttpRequestHandler hrh) {
        if (null != hrh.getTerminationReason()) {
            LOG.debug("Request not being served because of the termination reason {}", hrh.getTerminationReason());
            if (CasConfigUtil.JSON_PARSING_ERROR.equalsIgnoreCase(hrh.getTerminationReason())) {
                InspectorStats.incrementStatCount(JSON_PARSING_ERROR, COUNT);
            } else {
                InspectorStats.incrementStatCount(THRIFT_PARSING_ERROR, COUNT);
            }
            return true;
        }

        final SASRequestParameters sasParams = hrh.responseSender.getSasParams();
        if (null == sasParams) {
            LOG.info("Terminating request as sasParam is null");
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            InspectorStats.incrementStatCount(JSON_PARSING_ERROR, COUNT);
            return true;
        }

        if (CollectionUtils.isEmpty(sasParams.getCategories())) {
            LOG.info("Category field is not present in the request so sending noad");
            sasParams.setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(CasConfigUtil.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(MISSING_CATEGORY, COUNT);
            return true;
        }

        if (null == sasParams.getSiteId()) {
            LOG.info("Terminating request as site id was missing");
            hrh.setTerminationReason(CasConfigUtil.MISSING_SITE_ID);
            InspectorStats.incrementStatCount(MISSING_SITE_ID, COUNT);
            return true;
        }

        if (!sasParams.getAllowBannerAds()) {
            LOG.info("Request not being served because of banner not allowed.");
            InspectorStats.incrementStatCount(DROPPED_IN_BANNER_NOT_ALLOWED_FILTER, COUNT);
            return true;
        }

        final boolean isNativeReq = SASParamsUtils.isNativeRequest(sasParams);
        // CT Present and CAU not present, means it is CT request to drop it (For native CT has Native Template
        if (!isNativeReq && CollectionUtils.isEmpty(sasParams.getCauMetadataSet())
                && CollectionUtils.isNotEmpty(sasParams.getCustomTemplateSet())) {
            LOG.info("Request not being served because Custom Template is not supported");
            InspectorStats.incrementStatCount(DROPPED_CUSTOM_TEMPLATE_NOT_ALLOWED_FILTER, COUNT);
            return true;
        }

        if (sasParams.getSiteContentType() != null
                && !CasConfigUtil.allowedSiteTypes.contains(sasParams.getSiteContentType().name())) {
            LOG.info("Terminating request as incompatible content type");
            hrh.setTerminationReason(CasConfigUtil.INCOMPATIBLE_SITE_TYPE);
            InspectorStats.incrementStatCount(INCOMPATIBLE_SITE_TYPE, COUNT);
            return true;
        }
        final String tempSdkVersion = sasParams.getSdkVersion();
        if (null != tempSdkVersion) {
            try {
                if (("i".equalsIgnoreCase(tempSdkVersion.substring(0, 1)) || "a".equalsIgnoreCase(tempSdkVersion
                        .substring(0, 1))) && Integer.parseInt(tempSdkVersion.substring(1, 2)) < 3) {
                    LOG.info("Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(CasConfigUtil.LOW_SDK_VERSION);
                    InspectorStats.incrementStatCount(LOW_SDK_VERSION, COUNT);
                    return true;
                } else {
                    LOG.debug("sdk-version : {}", tempSdkVersion);
                }
            } catch (final StringIndexOutOfBoundsException exception) {
                LOG.info("Invalid sdk-version, Exception raised {}", exception);
            } catch (final NumberFormatException exception) {
                LOG.info("Invalid sdk-version, Exception raised {}", exception);
            }
        }

        if (sasParams.getProcessedMkSlot().isEmpty()) {
            /* Commenting to reduce stats, un-comment on need basis */
            // incrementStats(sasParams);
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
    protected void incrementStats(final SASRequestParameters sasParams) {
        final DemandSourceType dst = DemandSourceType.findByValue(sasParams.getDst());
        final StringBuilder buildDst =
                new StringBuilder(dst != null ? dst.name() : String.valueOf(sasParams.getDst())).append("-").append(
                        DROPPED_IN_INVALID_SLOT_REQUEST_FILTER);
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
