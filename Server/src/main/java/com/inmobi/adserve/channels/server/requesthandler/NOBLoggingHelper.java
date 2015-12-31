package com.inmobi.adserve.channels.server.requesthandler;

import static com.inmobi.adserve.types.IntegrationType.ANDROID;
import static com.inmobi.adserve.types.IntegrationType.BDFL;
import static com.inmobi.adserve.types.IntegrationType.COPE;
import static com.inmobi.adserve.types.IntegrationType.IOS;
import static com.inmobi.adserve.types.IntegrationType.JSAC;
import static com.inmobi.adserve.types.IntegrationType.JSWP;
import static com.inmobi.adserve.types.IntegrationType.MATZ;
import static com.inmobi.adserve.types.IntegrationType.MVBS;
import static com.inmobi.adserve.types.IntegrationType.OCO6;
import static com.inmobi.adserve.types.IntegrationType.QEQE;
import static com.inmobi.adserve.types.IntegrationType.SAND;
import static com.inmobi.adserve.types.IntegrationType.SIOS;
import static com.inmobi.adserve.types.IntegrationType.SPEC;
import static com.inmobi.adserve.types.IntegrationType.SWPH;
import static com.inmobi.adserve.types.IntegrationType.UNKNOWN;
import static com.inmobi.adserve.types.IntegrationType.W1DO;

import org.apache.commons.lang.StringUtils;

import com.inmobi.adserve.adpool.IntegrationDetails;
import com.inmobi.adserve.adpool.IntegrationMethod;
import com.inmobi.adserve.adpool.IntegrationOrigin;
import com.inmobi.adserve.adpool.IntegrationType;
import com.inmobi.adserve.adpool.RequestedAdType;
import com.inmobi.casthrift.RequestSource;

/**
 * This class tries to map AdPoolRequest parameters to their equivalent NOB counterparts for use in CAS RR.
 * note: A 1 to 1 mapping is not always possible.
 */
final class NOBLoggingHelper {

    static String mapRequestedAdTypeToAdFormat(final RequestedAdType requestedAdType) {
        return RequestedAdType.INTERSTITIAL == requestedAdType ? "INT" : "RICH_BANNER";
    }

    static RequestSource mapIntegrationDetailsToRequestSource(final IntegrationDetails integrationDetails) {
        final RequestSource requestSource;

        if (null != integrationDetails) {
            requestSource = new RequestSource();

            if (integrationDetails.isSetIntegrationMethod() && null != integrationDetails.getIntegrationMethod()) {
                requestSource.setIntegration_method(getIntegrationMethod(integrationDetails.getIntegrationMethod()));
            }

            if (integrationDetails.isSetIntegrationVersion()) {
                requestSource.setVersion(getIntegrationVersion(integrationDetails.getIntegrationVersion()));
            }

            if (integrationDetails.isSetIntegrationOrigin() && null != integrationDetails.getIntegrationOrigin()) {
                requestSource.setReq_origin(getIntegrationOrigin(integrationDetails.getIntegrationOrigin()));
            }

            // Populating equivalent NOB value instead of UMP RR value
            if (integrationDetails.isSetIntegrationType() && null != integrationDetails.getIntegrationType()) {
                requestSource.setIntegration_family(getIntegrationFamily(integrationDetails.getIntegrationType()));
            }

            if (integrationDetails.isSetIntegrationThirdPartyName()) {
                final String integrationThirdPartyName = integrationDetails.getIntegrationThirdPartyName();
                if (StringUtils.isNotBlank(integrationThirdPartyName)) {
                    requestSource.setThird_party_name(integrationDetails.getIntegrationThirdPartyName());

                    requestSource.setIs_direct_integration(integrationThirdPartyName.equalsIgnoreCase("dir"));
                }
            }
        } else {
            requestSource = null;
        }

        return requestSource;
    }

    // For mapping, see https://github.corp.inmobi.com/adserving/adserve-commons/blob/develop/common-types/src/main/java/com/inmobi/adserve/types/IntegrationType.java
    protected static String getIntegrationFamily(final IntegrationType integrationType) {
        final String integrationFamily;

        if (null != integrationType) {
            switch (integrationType) {
                case ANDROID_SDK:
                    integrationFamily = SAND.getFamily();
                    break;
                case IOS_SDK:
                    integrationFamily = SIOS.getFamily();
                    break;
                case WINDOWS_CSHARP_SDK:
                    integrationFamily = SWPH.getFamily();
                    break;
                case WINDOWS_JS_SDK:
                    integrationFamily = JSWP.getFamily();
                    break;
                case JSAC:
                    integrationFamily = JSAC.getFamily();
                    break;
                case PHP:
                    integrationFamily = QEQE.getFamily();
                    break;
                case PERL:
                    integrationFamily = BDFL.getFamily();
                    break;
                case JSP:
                    integrationFamily = OCO6.getFamily();
                    break;
                case ASP:
                    integrationFamily = MVBS.getFamily();
                    break;
                case ASP_NET:
                    integrationFamily = COPE.getFamily();
                    break;
                case RUBY:
                    integrationFamily = MATZ.getFamily();
                    break;
                case HTML_OLD:
                    integrationFamily = W1DO.getFamily();
                    break;
                case SPECS:
                    integrationFamily = SPEC.getFamily();
                    break;
                case ANDROID_API:
                    integrationFamily = ANDROID.getFamily();
                    break;
                case IOS_API:
                    integrationFamily = IOS.getFamily();
                    break;
                default:
                    integrationFamily = UNKNOWN.getFamily();
            }
        } else {
            integrationFamily = UNKNOWN.getFamily();
        }
        return integrationFamily;
    }

    protected static String getIntegrationOrigin(final IntegrationOrigin integrationOrigin) {
        final String requestOrigin;

        if (null != integrationOrigin) {
            switch (integrationOrigin) {
                case CLIENT:
                    requestOrigin = "cl";
                    break;
                case SERVER:
                    requestOrigin = "svr";
                    break;
                default:
                    requestOrigin = "uk";
            }
        } else {
            requestOrigin = "uk";
        }
        return requestOrigin;
    }

    protected static String getIntegrationVersion(final int integrationVersion) {
        final StringBuilder integrationVersionStr = new StringBuilder(5);

        for (final char c: String.valueOf(integrationVersion).toCharArray()) {
            integrationVersionStr.append(c).append('.');
        }

        if (integrationVersionStr.length() > 0) {
            integrationVersionStr.deleteCharAt(integrationVersionStr.length()-1);
        }
        return integrationVersionStr.toString();
    }

    protected static String getIntegrationMethod(final IntegrationMethod adPoolIntegrationMethod) {
        final String integrationMethod;

        if (null != adPoolIntegrationMethod) {
            switch (adPoolIntegrationMethod) {
                case SDK:
                    integrationMethod = "sdk";
                    break;
                case API:
                    integrationMethod = "api";
                    break;
                case AD_CODE:
                    integrationMethod = "adc";
                    break;
                default:
                    integrationMethod = "uk";
            }
        } else {
            integrationMethod = "uk";
        }
        return integrationMethod;
    }

}
