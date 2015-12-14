package com.inmobi.adserve.channels.adnetworks.rtb;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetworkHelper;
import com.inmobi.adserve.channels.api.NativeResponseMaker;
import com.inmobi.adserve.channels.util.InspectorStats;
import com.inmobi.adserve.channels.util.InspectorStrings;
import com.inmobi.adserve.contracts.ix.request.nativead.Asset;
import com.inmobi.adserve.contracts.ix.response.nativead.Native;

import lombok.Builder;

/**
 * Created by avinash.kumar on 12/14/15.
 */

@Builder()
public class NativeAd {

    protected static final String DEFAULT_EMPTY_STRING = StringUtils.EMPTY;
    protected static final String TERM = "TERM";
    private static final Logger LOG = LoggerFactory.getLogger(NativeAd.class);

    private Native nativeObj;
    private String advertiserName;
    private Map<Integer, Asset> mandatoryAssetMap;
    private Map<Integer, Asset> nonMandatoryAssetMap;
    private String impressionId;
    private String adStatus;
    private String beaconUrl;
    private Long placementId;
    private String nurl;
    private NativeResponseMaker nativeResponseMaker;

    public String generateResponseContent(){
        LOG.debug("nativeAdBuilding");
        String responseContent;
        try {
            final com.inmobi.template.context.App templateContext =
                IXAdNetworkHelper.validateAndBuildTemplateContext(nativeObj, mandatoryAssetMap,
                    nonMandatoryAssetMap, impressionId);
            if (null == templateContext) {
                adStatus = TERM;
                responseContent = DEFAULT_EMPTY_STRING;
                LOG.debug("Native Ad Building failed as native object failed validation");
                // Raising exception in parse response if native object failed validation.
                InspectorStats.incrementStatCount(advertiserName, InspectorStrings.NATIVE_PARSE_RESPONSE_EXCEPTION);
            } else {
                final Map<String, String> params = new HashMap<>();
                params.put("beaconUrl", beaconUrl);
                params.put("winUrl", beaconUrl + RTBCallbackMacros.WIN_BID_GET_PARAM + RTBCallbackMacros.DEAL_GET_PARAM);
                params.put("placementId", String.valueOf(placementId));
                params.put("nUrl", nurl);
                responseContent = nativeResponseMaker.makeIXResponse(templateContext, params);
            }
        } catch (final Exception e) {
            adStatus = TERM;
            responseContent = DEFAULT_EMPTY_STRING;
            LOG.error(
                "Some exception is caught while filling the native template for placementId = {}, advertiser = {}"
                    + ", exception = {}", placementId, advertiserName, e);
            InspectorStats.incrementStatCount(advertiserName, InspectorStrings.NATIVE_VM_TEMPLATE_ERROR);
        }

        return responseContent;
    }
}
