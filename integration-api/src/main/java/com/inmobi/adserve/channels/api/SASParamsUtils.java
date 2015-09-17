package com.inmobi.adserve.channels.api;

import static com.inmobi.adserve.channels.util.config.GlobalConstant.APP;
import static com.inmobi.adserve.channels.util.config.GlobalConstant.NATIVE_STRING;

import com.inmobi.adserve.adpool.RequestedAdType;

/**
 * 
 * @author ritwik.kumar
 *
 */
public class SASParamsUtils {

    /**
     * 
     * @param sasParams
     * @return
     */
    public static boolean isNativeRequest(final SASRequestParameters sasParams) {
        return APP.equalsIgnoreCase(sasParams.getSource())
                && (NATIVE_STRING.equals(sasParams.getRFormat()) || RequestedAdType.NATIVE == sasParams
                        .getRequestedAdType());
    }
    
}
