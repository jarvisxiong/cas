package com.inmobi.adserve.channels.server.requesthandler;

import com.inmobi.adserve.channels.server.HttpRequestHandler;
import com.inmobi.adserve.channels.server.ServletHandler;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;

import java.util.ArrayList;


public class RequestFilters {

    public static boolean isDroppedInRequestFilters(HttpRequestHandler hrh, DebugLogger logger) {
        // Send noad if new-category is not present in the request
        if (ServletHandler.random.nextInt(100) >= ServletHandler.percentRollout) {
            logger.debug("Request not being served because of limited percentage rollout");
            InspectorStats.incrementStatCount(InspectorStrings.droppedRollout, InspectorStrings.count);
            return true;
        }
        else if (null == hrh.responseSender.sasParams.getCategories()) {
            hrh.logger.error("Category field is not present in the request so sending noad");
            hrh.responseSender.sasParams.setCategories(new ArrayList<Long>());
            hrh.setTerminationReason(ServletHandler.MISSING_CATEGORY);
            InspectorStats.incrementStatCount(InspectorStrings.missingCategory, InspectorStrings.count);
            return true;
        }
        else if (null == hrh.responseSender.sasParams) {
            logger.error("Terminating request as sasParam is null");
            hrh.setTerminationReason(ServletHandler.jsonParsingError);
            InspectorStats.incrementStatCount(InspectorStrings.jsonParsingError, InspectorStrings.count);
            return true;
        }
        else if (null == hrh.responseSender.sasParams.getSiteId()) {
            logger.error("Terminating request as site id was missing");
            hrh.setTerminationReason(ServletHandler.missingSiteId);
            InspectorStats.incrementStatCount(InspectorStrings.missingSiteId, InspectorStrings.count);
            return true;
        }
        else if (!hrh.responseSender.sasParams.getAllowBannerAds() || hrh.responseSender.sasParams.getSiteFloor() > 5) {
            logger.error("Request not being served because of banner not allowed or site floor above threshold");
            return true;
        }
        else if (hrh.responseSender.sasParams.getSiteType() != null
                && !ServletHandler.allowedSiteTypes.contains(hrh.responseSender.sasParams.getSiteType())) {
            logger.error("Terminating request as incompatible content type");
            hrh.setTerminationReason(ServletHandler.incompatibleSiteType);
            InspectorStats.incrementStatCount(InspectorStrings.incompatibleSiteType, InspectorStrings.count);
            return true;
        }
        else if (hrh.responseSender.sasParams.getSdkVersion() != null) {
            try {
                if ((hrh.responseSender.sasParams.getSdkVersion().substring(0, 1).equalsIgnoreCase("i") || hrh.responseSender.sasParams
                        .getSdkVersion()
                            .substring(0, 1)
                            .equalsIgnoreCase("a"))
                        && Integer.parseInt(hrh.responseSender.sasParams.getSdkVersion().substring(1, 2)) < 3) {
                    logger.error("Terminating request as sdkVersion is less than 3");
                    hrh.setTerminationReason(ServletHandler.lowSdkVersion);
                    InspectorStats.incrementStatCount(InspectorStrings.lowSdkVersion, InspectorStrings.count);
                    return true;
                }
                else
                    logger.debug("sdk-version : " + hrh.responseSender.sasParams.getSdkVersion());
            }
            catch (StringIndexOutOfBoundsException exception) {
                logger.error("Invalid sdk-version " + exception.getMessage());
            }
            catch (NumberFormatException exception) {
                logger.error("Invalid sdk-version " + exception.getMessage());
            }

        }
        return false;
    }
}
