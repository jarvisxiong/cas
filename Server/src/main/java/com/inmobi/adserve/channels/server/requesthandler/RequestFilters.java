package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class RequestFilters {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFilters.class);


    public static boolean isDroppedInRequestFilters(HttpRequestHandler hrh) {
        // Send noad if new-category is not present in the request
        if (ServletHandler.random.nextInt(100) >= ServletHandler.percentRollout) {
            LOG.debug("Request not being served because of limited percentage rollout");
            InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
            return true;
        }
        
        if (null == hrh.responseSender.sasParams.getCategories()) {
            LOG.error("Category field is not present in the request so sending noad");
            hrh.responseSender.sasParams.setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(ServletHandler.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(InspectorStrings.missingCategory, InspectorStrings.count);
            return true;
        }
        
        if (null == hrh.responseSender.sasParams) {
            LOG.error("Terminating request as sasParam is null");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            return true;
        }
        
        if (null == hrh.responseSender.sasParams.getSiteId()) {
            LOG.error("Terminating request as site id was missing");
            hrh.setTerminationReason(ServletHandler.missingSiteId);
            InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
            return true;
        }
        
        if (!hrh.responseSender.sasParams.getAllowBannerAds() || hrh.responseSender.sasParams.getSiteFloor() > 5) {
            LOG.error("Request not being served because of banner not allowed or site floor above threshold");
            return true;
        }
        
        if (hrh.responseSender.sasParams.getSiteType() != null
                && !ServletHandler.allowedSiteTypes.contains(hrh.responseSender.sasParams.getSiteType())) {
            LOG.error("Terminating request as incompatible content type");
            hrh.setTerminationReason(ServletHandler.incompatibleSiteType);
            InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
            return true;
        }
        
        if (hrh.responseSender.sasParams.getSdkVersion() != null) {
            try {
                if ((hrh.responseSender.sasParams.getSdkVersion().substring(0, 1).equalsIgnoreCase("i") || hrh.responseSender.sasParams
                        .getSdkVersion()
                            .substring(0, 1)
                            .equalsIgnoreCase("a"))
                        && Integer.parseInt(hrh.responseSender.sasParams.getSdkVersion().substring(1, 2)) < 3) {
                    LOG.error("Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(ServletHandler.lowSdkVersion);
                    InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
                    return true;
                }
                else
                    LOG.debug("sdk-version : " + hrh.responseSender.sasParams.getSdkVersion());
            }
            catch (StringIndexOutOfBoundsException exception) {
                LOG.error("Invalid sdk-version " + exception.getMessage());
            }
            catch (NumberFormatException exception) {
                LOG.error("Invalid sdk-version " + exception.getMessage());
            }

        }
        return false;
    }
}
