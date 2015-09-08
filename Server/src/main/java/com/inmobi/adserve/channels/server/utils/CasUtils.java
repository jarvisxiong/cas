package com.inmobi.adserve.channels.server.utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.CasConfigUtil;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.util.config.GlobalConstant;
import com.inmobi.segment.impl.AdTypeEnum;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;


/**
 * @author abhishek.parwal
 * @author ritwik.kumar
 *
 */
@Singleton
public class CasUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CasUtils.class);
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

    // No longer being used
    public Double getRtbFloor(final CasContext casContext) {
        final PricingEngineEntity pricingEngineEntity = casContext.getPricingEngineEntity();
        return pricingEngineEntity == null ? 0 : pricingEngineEntity.getRtbFloor();
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

    /**
     * 
     * @param sasParams
     * @return
     */
    public SiteEcpmEntity getNetworkSiteEcpm(final SASRequestParameters sasParams) {
        return repositoryHelper.querySiteEcpmRepository(sasParams.getSiteId(), sasParams.getCountryId().intValue(),
                sasParams.getOsId());
    }

    public boolean isVideoSupported(final SASRequestParameters sasParams) {
        boolean isSupported = false;

        LOG.debug("Checking for VAST video support");
        // TODO: Move to config
        if (CollectionUtils.isEmpty(sasParams.getProcessedMkSlot()) || !(
            sasParams.getProcessedMkSlot().contains((short) 14) || sasParams.getProcessedMkSlot()
                .contains((short) 32))) {
            LOG.debug("Not qualified for VAST video as processed slots do not contain 14 or 32");
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

        String osVersion = sasParams.getOsMajorVersion();
        if (StringUtils.isNotEmpty(osVersion)) {
            int osMajorVersion;
            try {
                if (osVersion.contains(".")) {
                    osVersion = osVersion.substring(0, osVersion.indexOf("."));
                }
                osMajorVersion = Integer.parseInt(osVersion);
            } catch (final NumberFormatException e) {
                LOG.debug("Exception while parsing osMajorVersion {}", e);
                return false;
            }

            // Only requests from Android 4.0 or iOS 6.0 and higher are supported.
            if (sasParams.getOsId() == SASRequestParameters.HandSetOS.Android.getValue() && osMajorVersion >= 4
                    || sasParams.getOsId() == SASRequestParameters.HandSetOS.iOS.getValue() && osMajorVersion >= 6) {
                isSupported = true;
            } else {
                LOG.debug("Not qualified for VAST video as only requests from Android 4.0 or iOS 6.0 and higher are "
                    + "supported");
            }
        }
        return isSupported;
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
