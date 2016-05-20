package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.InspectorStrings.MOVIE_BOARD_REQUEST_DROPPED_AS_PARENT_VIEW_WIDTH_WAS_INVALID;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.APP;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NATIVE_STRING;

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
}
