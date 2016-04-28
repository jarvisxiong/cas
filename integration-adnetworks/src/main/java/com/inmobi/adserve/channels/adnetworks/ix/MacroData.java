package com.inmobi.adserve.channels.adnetworks.ix;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.types.DeviceType;

import lombok.Data;
import lombok.Getter;

/**
 * Created by avinash.kumar on 4/6/16.
 */
@Data
public class MacroData {
    public static final String $IMP_ID = "$IMP_ID";
    public static final String $IMP_CB = "$IMP_CB";
    public static final String $SI_BLIND = "$SI_BLIND";
    public static final String $USER_ID = "$USER_ID";
    public static final String $USER_ID_SHA256_HASHED = "$USER_ID_SHA256_HASHED";
    public static enum HASH_TYPE  {MD5, SHA1, SHA256};
    public static enum ESCAPE_TYPE {HTML, JS, HTML_JS};
    public static final String $UDID = "$UDID";
    public static final String $O1 = "$O1";
    public static final String $UM5 = "$UM5";
    public static final String $SO1 = "$SO1";
    public static final String $IDA = "$IDA";
    public static final String $IDV = "$IDV";
    public static final String $GPID = "$GPID";
    public static final String $USER_LOC = "$USER_LOC";
    public static final String $SI_RAW = "$SI_RAW"; // bundle id raw
    public static final String $SECURE = "$SECURE";
    public static final String $UA = "$UA";
    public static final String $CREATIVE_NAME = "$CREATIVE_NAME";
    public static final String $GEO_CC = "$GEO_CC";
    public static final String $HANDSET_TYPE = "$HANDSET_TYPE";
    public static final String $LIMIT_AD_TRACKING = "$LIMIT_AD_TRACKING";
    public static final String EMPTY_STRING = "";
    public static final String[] UID_PRIORITY_LIST = {"$IDA","$GPID","$O1","$SO1","$UM5","$UDID","$IDV"};
    @Getter
    private Map<String, String> macroMap = new HashMap<>();

    public MacroData (final CasInternalRequestParameters casInternalRequestParameters,
        final SASRequestParameters sasParams) {
        String impCb = casInternalRequestParameters.getAuctionId();
        impCb = StringUtils.isNotBlank(impCb) ? impCb.replace("-", "") : EMPTY_STRING;
        addInMacroData($IMP_CB, impCb);
        addInMacroData($IMP_ID, sasParams.getImpressionId());
        addInMacroData($USER_LOC, casInternalRequestParameters.getLatLong());
        addInMacroData($SI_RAW, sasParams.getSiteId());
        addInMacroData($SECURE, String.valueOf(sasParams.isSecureRequest()));
        addInMacroData($GPID, casInternalRequestParameters.getGpid());
        addInMacroData($UDID, casInternalRequestParameters.getUid());
        addInMacroData($O1, casInternalRequestParameters.getUidO1());
        addInMacroData($UM5, casInternalRequestParameters.getUidMd5());
        addInMacroData($IDA, casInternalRequestParameters.getUidIFA());
        addInMacroData($IDV, casInternalRequestParameters.getUidIFV());
        addInMacroData($SO1, casInternalRequestParameters.getUidSO1());
        addInMacroData($UA, sasParams.getUserAgent());
        addInMacroData($GEO_CC, sasParams.getCountryCode());
        final DeviceType deviceType = sasParams.getDeviceType();
        addInMacroData($HANDSET_TYPE, null != deviceType ? deviceType.name() : EMPTY_STRING);
        addInMacroData($LIMIT_AD_TRACKING, casInternalRequestParameters.isTrackingAllowed() ? "0" : "1");
        addInMacroData($USER_ID, getUserId());
    }

    private void addInMacroData(final String macro, String value) {
        macroMap.put(macro, StringUtils.isNotBlank(value) ? value : EMPTY_STRING);
    }

    public String getUserId() {
        String uid = EMPTY_STRING;
        for (final String userId : UID_PRIORITY_LIST) {
            if (StringUtils.isNotBlank(macroMap.get(userId))) {
                uid = macroMap.get(userId);
                break;
            }
        }
        return uid;
    }

    public String toString() {
        return macroMap.toString();
    }
}
