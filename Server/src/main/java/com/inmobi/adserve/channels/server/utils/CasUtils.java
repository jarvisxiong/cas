package com.inmobi.adserve.channels.server.utils;

import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android;
import static com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.iOS;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.casthrift.DemandSourceType;
import com.inmobi.segment.impl.AdTypeEnum;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;


@Singleton
public class CasUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CasUtils.class);
    protected static final String MINIMUM_SUPPORTED_IOS_VERSION_FOR_VIDEO_CONFIG_KEY = "adtype.vast.minimumSupportedIOSVersion";
    protected static final String MINIMUM_SUPPORTED_ANDROID_VERSION_FOR_VIDEO_CONFIG_KEY = "adtype.vast.minimumSupportedAndroidVersion";
    private final RepositoryHelper repositoryHelper;

    @Inject
    public CasUtils(final RepositoryHelper repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    public PricingEngineEntity fetchPricingEngineEntity(final SASRequestParameters sasParams) {
        // Fetching pricing engine entity
        if (null != sasParams.getCountryId()) {
            final int country = sasParams.getCountryId().intValue();
            final int os = sasParams.getOsId();
            return repositoryHelper.queryPricingEngineRepository(country, os);
        }
        return null;
    }

    /**
     *
     * @param httpRequest
     * @return
     */
    public static String getHost(final HttpRequest httpRequest) {
        final HttpHeaders headers = httpRequest.headers();
        return headers.get("Host");
    }

    public boolean isVideoSupported(final SASRequestParameters sasParams) {
        LOG.debug("Checking for VAST video support");
        if (DemandSourceType.IX.getValue() != sasParams.getDst()) {
            LOG.debug("Not qualified for VAST video as DST was not IX");
            return false;
        }

        if (RequestedAdType.VAST == sasParams.getRequestedAdType()) {
            return true;
        }

        if (RequestedAdType.INTERSTITIAL != sasParams.getRequestedAdType()) {
            LOG.debug("Not qualified for VAST video as RequestAdType was not INTERSTITIAL");
            return false;
        }

        // Only requests from app are supported
        if (!GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource())) {
            LOG.debug("Not qualified for VAST video as source is not APP");
            return false;
        }
        // Slot Size check in AsyncRequestMaker since slots would differ for each segment
        // Minimum SDK version check is in IXAdNetwork since we cannot access adapter specific configs from this
        // location

        // Check site/placement level publisher controls for video
        final List<AdTypeEnum> supportedAdTypes = sasParams.getPubControlSupportedAdTypes();
        if (CollectionUtils.isEmpty(supportedAdTypes) || !supportedAdTypes.contains(AdTypeEnum.VIDEO)) {
            LOG.debug("Not qualified for VAST video as publisher controls disallow video");
            return false;
        }

        // Check for minimum sdk version
        final int minimumSdkVerForVAST = CasConfigUtil.getServerConfig()
                .getInt("adtype.vast.minimumSupportedSdkVersion", 450);
        if (!Formatter.isRequestFromSdkVersionOnwards(sasParams, minimumSdkVerForVAST)) {
            LOG.debug("Not qualified for VAST video as request failed minimum SDK check. Minimum Supported SDK: {}",
                    minimumSdkVerForVAST);
            return false;
        }

        return checkMinimumOSVersionForVideo(sasParams.getOsId(), sasParams.getOsMajorVersion(), CasConfigUtil
                .getServerConfig());
    }

    protected static boolean checkMinimumOSVersionForVideo(final int osId, final String osMajorVersionStr,
            final Configuration serverConfig) {
        boolean checkPassed = false;

        try {
            if (null != osMajorVersionStr) {
                final double osVersion = Double.valueOf(osMajorVersionStr);
                final String minOsVersionConfigKey;

                if (Android.getValue() == osId) {
                    minOsVersionConfigKey = MINIMUM_SUPPORTED_ANDROID_VERSION_FOR_VIDEO_CONFIG_KEY;
                } else if (iOS.getValue() == osId) {
                    minOsVersionConfigKey = MINIMUM_SUPPORTED_IOS_VERSION_FOR_VIDEO_CONFIG_KEY;
                } else {
                    minOsVersionConfigKey = null;
                }

                if (null != minOsVersionConfigKey) {
                    final double minimumOsVersion = serverConfig.getDouble(minOsVersionConfigKey);
                    if (osVersion >= minimumOsVersion) {
                        checkPassed = true;
                    }
                }
            }
        } catch (final NumberFormatException nfe) {
            LOG.debug("Exception while parsing osMajorVersion string. Failing OS check for video.");
        }

        return checkPassed;
    }

    /**
     *
     * @param params
     * @param jsonKey
     * @return
     * @throws JSONException
     * @throws UnsupportedEncodingException
     */
    public static JSONObject extractParams(final Map<String, List<String>> params, final String jsonKey)
            throws JSONException, UnsupportedEncodingException {
        if (params != null && !params.isEmpty()) {
            final List<String> values = params.get(jsonKey);
            if (CollectionUtils.isNotEmpty(values)) {
                final String stringVal = values.iterator().next();
                return new JSONObject(stringVal);
            }
        }
        return null;
    }
}
