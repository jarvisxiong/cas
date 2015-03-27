package com.inmobi.adserve.channels.server.requesthandler;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (null == hrh.responseSender.getSasParams()) {
            LOG.error("Terminating request as sasParam is null");
            hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
            InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
            return true;
        }

        if (null == hrh.responseSender.getSasParams().getCategories()) {
            LOG.error("Category field is not present in the request so sending noad");
            hrh.responseSender.getSasParams().setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(CasConfigUtil.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(InspectorStrings.MISSING_CATEGORY, InspectorStrings.COUNT);
            return true;
        }

        if (null == hrh.responseSender.getSasParams().getSiteId()) {
            LOG.error("Terminating request as site id was missing");
            hrh.setTerminationReason(CasConfigUtil.MISSING_SITE_ID);
            InspectorStats.incrementStatCount(InspectorStrings.MISSING_SITE_ID, InspectorStrings.COUNT);
            return true;
        }

        if (!hrh.responseSender.getSasParams().getAllowBannerAds()) {
            LOG.info("Request not being served because of banner not allowed.");
            InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_BANNER_NOT_ALLOWED_FILTER,
                    InspectorStrings.COUNT);
            return true;
        }

        if (hrh.responseSender.getSasParams().getSiteContentType() != null
                && !CasConfigUtil.allowedSiteTypes.contains(hrh.responseSender.getSasParams().getSiteContentType().name())) {
            LOG.info("Terminating request as incompatible content type");
            hrh.setTerminationReason(CasConfigUtil.INCOMPATIBLE_SITE_TYPE);
            InspectorStats.incrementStatCount(InspectorStrings.INCOMPATIBLE_SITE_TYPE, InspectorStrings.COUNT);
            return true;
        }
        final String tempSdkVersion = hrh.responseSender.getSasParams().getSdkVersion();

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

        if (hrh.responseSender.getSasParams().getProcessedMkSlot().isEmpty()) {
            if (DemandSourceType.IX.getValue() == hrh.responseSender.getSasParams().getDst()) {
                InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_IX_INVALID_SLOT_REQUEST_FILTER);
            } else if (DemandSourceType.RTBD.getValue() == hrh.responseSender.getSasParams().getDst()) {
                InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_RTBD_INVALID_SLOT_REQUEST_FILTER);
            } else {
                InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_DCP_INVALID_SLOT_REQUEST_FILTER);
            }

            LOG.info("Request dropped since no slot in the list RqMkSlot has a mapping to InMobi slots/IX supported slots");
            return true;
        }

        return false;
    }
}
