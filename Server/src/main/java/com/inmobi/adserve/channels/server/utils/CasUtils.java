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
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.beans.CasContext;
import com.inmobi.adserve.channels.util.config.GlobalConstant;

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

    public static String getHost(final HttpRequest httpRequest) {
        final HttpHeaders headers = httpRequest.headers();
        return headers.get("Host");
    }

    public boolean isRequestFromLocalHost(final HttpRequest httpRequest) {
        final String host = getHost(httpRequest);
        if (host != null && host.startsWith("localhost")) {
            return true;
        }
        return false;
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
        if (!(sasParams.getProcessedMkSlot().contains((short) 14) || sasParams.getProcessedMkSlot()
                .contains((short) 32))) {
            return false;
        }

        // Only requests from app are supported
        if (!GlobalConstant.APP.equalsIgnoreCase(sasParams.getSource())) {
            return false;
        }
        // Slot Size check in AsyncRequestMaker since slots would differ for each segment
        // Minimum SDK version check is in IXAdNetwork since we cannot access adapter specific configs from this
        // location

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
