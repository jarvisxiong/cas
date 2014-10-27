package com.inmobi.adserve.channels.server.utils;

import com.google.inject.Singleton;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.PricingEngineEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.server.beans.CasContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * @author abhishek.parwal
 * 
 */
@Singleton
public class CasUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CasUtils.class);

    private static final String APP = "APP";

    private final RepositoryHelper repositoryHelper;


    @Inject
    public CasUtils(final RepositoryHelper repositoryHelper) {
        this.repositoryHelper = repositoryHelper;
    }

    // TODO: move PricingEngineEntity fetching at handler level , when we move request parsing to handler level
    public PricingEngineEntity fetchPricingEngineEntity(final SASRequestParameters sasParams) {
        // Fetching pricing engine entity
        if (null != sasParams.getCountryId()) {
            final int country = sasParams.getCountryId().intValue();
            final int os = sasParams.getOsId();
            return repositoryHelper.queryPricingEngineRepository(country, os);
        }
        return null;
    }

    public Double getRtbFloor(final CasContext casContext, final SASRequestParameters sasRequestParameters) {
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

    public Double getNetworkSiteEcpm(final CasContext casContext, final SASRequestParameters sasParams) {
        final SiteEcpmEntity siteEcpmEntity =
                repositoryHelper.querySiteEcpmRepository(sasParams.getSiteId(), sasParams.getCountryId().intValue(),
                        sasParams.getOsId());
        double networkEcpm = 0.0;
        if (null != siteEcpmEntity) {
            networkEcpm = 0.5 * siteEcpmEntity.getEcpm();
        }
        return networkEcpm;
    }

    public boolean isBannerVideoSupported(final SASRequestParameters sasParams) {
        boolean isSupported = false;

        // Only requests from app are supported
        if (!APP.equalsIgnoreCase(sasParams.getSource())) {
            return false;
        }
        // Only requests from SDK 370 onwards are supported
        if (!requestFromSDK370Onwards(sasParams)) {
            return false;
        }
        // Only slot size 320x480 and 480x320 are supported
        final Short slot = sasParams.getSlot();
        if (14 != slot && 32 != slot) {
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
            }
        }
        return isSupported;
    }

    private boolean requestFromSDK370Onwards(final SASRequestParameters sasParams) {
        if (StringUtils.isBlank(sasParams.getSdkVersion())) {
            return false;
        }
        try {
            final String os = sasParams.getSdkVersion();
            if ((os.startsWith("i") || os.startsWith("a"))
                    && Integer.parseInt(sasParams.getSdkVersion().substring(1)) >= 370) {
                return true;
            }
        } catch (final StringIndexOutOfBoundsException e1) {
            LOG.debug("Invalid sdkversion {}", e1);
        } catch (final NumberFormatException e2) {
            LOG.debug("Invalid sdkversion {}", e2);
        }
        return false;
    }

}
