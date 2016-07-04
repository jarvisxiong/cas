package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.adpool.IntegrationMethod.SDK;
import static com.inmobi.adserve.adpool.IntegrationType.ANDROID_SDK;
import static com.inmobi.adserve.adpool.IntegrationType.IOS_SDK;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS;
import static com.inmobi.adserve.channels.util.InspectorStrings.MOVIE_BOARD_REQUEST_DROPPED_AS_PARENT_VIEW_WIDTH_WAS_INVALID;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.APP;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NATIVE_STRING;

import org.apache.commons.lang3.StringUtils;

import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.util.InspectorStats;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author ritwik.kumar
 *
 */
@Slf4j
public class SASParamsUtils {
    public static final String A_PARENTVIEWWIDTH = "a-parentviewwidth";
    protected static final int MIN_SDK_WITH_MRAID = 360;
    protected static final int MIN_IOS_SDK_VERSION_WITH_SCHEMA_BASED_URL_SUPPORT = 500;
    protected static final double MIN_IOS_VERSION_WITH_NON_BLOCKING_APP_OPEN_MODAL = 9.2;

    public static boolean isNativeRequest(final SASRequestParameters sasParams) {
        return APP.equalsIgnoreCase(sasParams.getSource())
                && (NATIVE_STRING.equals(sasParams.getRFormat()) || RequestedAdType.NATIVE == sasParams
                        .getRequestedAdType());
    }

    public static boolean isRequestEligibleForMovieBoard(final SASRequestParameters sasParams) {
        final boolean movieBoardSupported = APP.equalsIgnoreCase(sasParams.getSource())
                && (RequestedAdType.INLINE_BANNER == sasParams.getRequestedAdType())
                && sasParams.getSupplyCapabilities().contains(SupplyCapabilitiesEnum.INLINE_BANNER_VAST.getId())
                && (sasParams.isNoJsTracking());

        boolean validParentViewWidth = false;
        if (movieBoardSupported) {
            if (null != sasParams.getAdPoolParamsMap() && sasParams.getAdPoolParamsMap().containsKey(A_PARENTVIEWWIDTH)) {
                try {
                    sasParams.setMovieboardParentViewWidth(Integer.parseInt(sasParams.getAdPoolParamsMap().get(A_PARENTVIEWWIDTH)));
                    validParentViewWidth = true;
                } catch (final NumberFormatException nfe) {
                    log.debug("Invalid parent view width for movieboard request. Dropping request. Exception: {}", nfe);
                    InspectorStats.incrementStatCount(MOVIE_BOARD_REQUEST_DROPPED_AS_PARENT_VIEW_WIDTH_WAS_INVALID);
                }
            }
        }

        return movieBoardSupported && validParentViewWidth;
    }

    public static boolean isSDK(final IntegrationDetails details) {
        boolean isSDK = false;

        if (null != details && details.isSetIntegrationMethod()) {
            if (SDK == details.getIntegrationMethod() && details.isSetIntegrationType()) {
                final IntegrationType type = details.getIntegrationType();
                isSDK = ANDROID_SDK == type || IOS_SDK == type;
            }
        }

        return isSDK;
    }

    public static boolean isDeeplinkingSupported(final SASRequestParameters sasParams) {
        boolean canSupport = false;

        if (sasParams.isRequestFromSDK()) {
            final int sdkVersion = sasParams.getIntegrationDetails().getIntegrationVersion();

            if (sdkVersion >= MIN_SDK_WITH_MRAID) {
                if (Android.getValue() == sasParams.getOsId()) {
                    canSupport = true;
                } else if (iOS.getValue() == sasParams.getOsId()){
                    if (sdkVersion >= MIN_IOS_SDK_VERSION_WITH_SCHEMA_BASED_URL_SUPPORT) {
                        canSupport = true;
                    } else {
                        if (StringUtils.isNotBlank(sasParams.getOsMajorVersion())) {
                            try {
                                final double osVersion = Double.valueOf(sasParams.getOsMajorVersion());
                                if (osVersion < MIN_IOS_VERSION_WITH_NON_BLOCKING_APP_OPEN_MODAL) {
                                    canSupport = true;
                                }
                            } catch (final Exception e) {
                                log.debug("Exception while parsing osMajorVersion string. Deeplinking not supported");
                            }
                        }
                    }
                }
            }
        }
        return canSupport;
    }
}
