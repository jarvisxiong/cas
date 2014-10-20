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

		if (null == hrh.responseSender.sasParams) {
			LOG.error("Terminating request as sasParam is null");
			hrh.setTerminationReason(CasConfigUtil.JSON_PARSING_ERROR);
			InspectorStats.incrementStatCount(InspectorStrings.JSON_PARSING_ERROR, InspectorStrings.COUNT);
			return true;
		}

		if (null == hrh.responseSender.sasParams.getCategories()) {
			LOG.error("Category field is not present in the request so sending noad");
			hrh.responseSender.sasParams.setCategories(new ArrayList<Long>());
			hrh.setTerminationReason(CasConfigUtil.MISSING_CATEGORY);
			InspectorStats.incrementStatCount(InspectorStrings.MISSING_CATEGORY, InspectorStrings.COUNT);
			return true;
		}

		if (null == hrh.responseSender.sasParams.getSiteId()) {
			LOG.error("Terminating request as site id was missing");
			hrh.setTerminationReason(CasConfigUtil.MISSING_SITE_ID);
			InspectorStats.incrementStatCount(InspectorStrings.MISSING_SITE_ID, InspectorStrings.COUNT);
			return true;
		}

		if (!hrh.responseSender.sasParams.getAllowBannerAds()) {
			LOG.error("Request not being served because of banner not allowed.");
			InspectorStats.incrementStatCount(InspectorStrings.DROPPED_IN_BANNER_NOT_ALLOWED_FILTER,
					InspectorStrings.COUNT);
			return true;
		}

		if (hrh.responseSender.sasParams.getSiteType() != null
				&& !CasConfigUtil.allowedSiteTypes.contains(hrh.responseSender.sasParams.getSiteType())) {
			LOG.error("Terminating request as incompatible content type");
			hrh.setTerminationReason(CasConfigUtil.INCOMPATIBLE_SITE_TYPE);
			InspectorStats.incrementStatCount(InspectorStrings.INCOMPATIBLE_SITE_TYPE, InspectorStrings.COUNT);
			return true;
		}
		final String tempSdkVersion = hrh.responseSender.sasParams.getSdkVersion();

		if (null != tempSdkVersion) {
			try {
				if (("i".equalsIgnoreCase(tempSdkVersion.substring(0, 1)) || "a".equalsIgnoreCase(tempSdkVersion
						.substring(0, 1))) && Integer.parseInt(tempSdkVersion.substring(1, 2)) < 3) {
					LOG.error("Terminating request as sdkVersion is less than 3");
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

		if (DemandSourceType.IX.getValue() == hrh.responseSender.sasParams.getDst()
				&& -1 == hrh.responseSender.sasParams.getSlot()) {
			LOG.error("Request for ix dropped since no slot in the list RqMkSlot has a mapping to Rubicon's slots");
			return true;
		}

		return false;
	}
}
