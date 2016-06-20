package com.inmobi.castest.casconfenums.impl;

/**
 * @author santosh.vaidyanathan
 */

import com.inmobi.castest.casconfenums.def.CasConf.LogStringParams;

public class LogStringConf {

    public static String getLogString(final LogStringParams logString) {
        String logLine = new String();

        switch (logString) {

            case MSG_RTBD_EXT_OBJECT_NEGATIVE_DATA_SIGNALS: {
                logLine = "ext\":{\"gender\":\"M\"}}";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_MAKE_AND_MANUF_DATA_SIGNALS: {
                logLine = "\"make\":\"WHATEVER_MANUF_NAME !@#$12345\",\"model\":\"WHATEVER_MODEL_NAME !@#$12345\"";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_EXT_OBJECT_DATA_SIGNALS: {
                logLine = "\"ext\":{\"gender\":\"M\",";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_DEVICETYPE_SMARTPHONE: {
                logLine = "deviceType:SMARTPHONE";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_DEVICETYPE_FEATUREPHONE: {
                logLine = "deviceType:SMARTPHONE";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_DEVICETYPE_TABLET: {
                logLine = "deviceType:SMARTPHONE";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_MANUFNAME: {
                logLine = "manufacturerName:WHATEVER_MANUF_NAME";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_MODELNAME: {
                logLine = "modelName:WHATEVER_MODEL_NAME";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_MANUFNAME_NEGATIVE: {
                logLine = "manufacturerName:WHATEVER_MANUF_NAME !@#$12345";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_SASPARAMS_MODELNAME_NEGATIVE: {
                logLine = "modelName:WHATEVER_MODEL_NAME !@#$12345";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_RESPONSE: {
                logLine = "RTBD response json to UMP";
                System.out.println("****** Searching for \"" + logLine + "\" in the logs ! *****");
                break;
            }
            case MSG_RTBD_AdRR_FLAG: {

                logLine = "dst:RTBD,";
                break;
            }
            case MSG_SENDING_NO_AD: {
                logLine = "Sending No ads";
                break;
            }
            case MSG_RTB_MANDATE_PARAM_MISSING: {
                logLine =
                        "mandate parameters missing or request format is not compatible to partner supported response"
                                + " for dummy so exiting adapter";
                break;
            }
            case MSG_DCP_TAPIT_CONFIG_SUCCESS: {
                logLine = "Configure parameters inside tapit returned true";
                break;
            }
            case MSG_DCP_AD_SERVED: {
                logLine = "n_ads_served:1";
                break;
            }

            case MSG_DCP_SENDING_NO_AD: {
                logLine = "Sending No ads";
                break;
            }
            case MSG_DCP_TERMINATE_CONFIG_SEARCH: {
                logLine = "Terminating request as incompatible content type";
                break;
            }

            case MSG_IX_AD_SERVED: {
                logLine = "n_ads_served:1";
                break;
            }
            case MSG_IX_ADRRFLAG: {
                logLine = "dst:IX,";
                break;
            }
            case MSG_E2E_IX_ANDROID_INTERSTITAL:{
                logLine = "ad_format:INT";
                break;
            }
            case MSG_E2E_IX_ANDROID_BANNER:{
                logLine = "ad_format:RICH_BANNER";
                break;
            }
            case MSG_IX_ADRRFLAG2: {
                logLine = "requestDst:IX";
                break;
            }
            case MSG_IX_DST: {
                logLine = "dst: 8";
                break;
            }
            case MSG_IX_TERMREQ: {
                logLine = "Terminating request as incompatible content type";
                break;
            }
            case MSG_IX_FORMREQ: {
                logLine = "INSIDE CREATE BID REQUEST OBJECT";
                break;
            }
            case MSG_IX_SENDREQ: {
                logLine =
                        "POST\theaders:\tContent-Type:application/json; charset=utf-8\t"
                                + "Authorization:Basic aW5tb2JpOkY0MEdYQTk3OEg=\tHost";
                break;
            }
            case MSG_IX_EXCHANGEREQ: {
                logLine = "IX request json is:";
                break;
            }
            case MSG_IX_TMAX: {
                logLine = "\"tmax\"";
                break;
            }
            case MSG_IX_APPOBJ: {
                logLine = "\"app\":{";
                break;
            }
            case MSG_IX_WAPOBJ: {
                logLine = "\"site\":{";
                break;
            }
            case MSG_IX_ANDRD: {
                logLine = "\"os\":\"Android\",";
                break;
            }
            case MSG_IX_IOS: {
                logLine = "\"os\":\"iOS\",";
                break;
            }
            case MSG_IX_AVRSN: {
                logLine = "\"osv\":\"4.4\",";
                break;
            }
            case MSG_IX_IVRSN: {
                logLine = "\"osv\":\"6.0\",";
                break;
            }
            case MSG_IX_SLOTFILTER: {
                logLine =
                        "Request dropped since no slot in the list RqMkSlot has a mapping to InMobi slots/IX supported "
                                + "slots";
                break;
            }
            case MSG_IX_ADRRNOADS: {
                logLine = "n_ads_served:0,";
                break;
            }
            case MSG_IX_MULTSLOTABS: {
                logLine = "rqMkSlot=[100, 400, 500]";
                break;
            }
            case MSG_IX_MULTSLOTICL: {
                logLine = "rqMkSlot=[2,3,4]";
                break;
            }
            case MSG_IX_LATLONG: {
                logLine = "\"geo\":{\"lat\":41.2,\"lon\":68.14,";
                break;
            }
            case MSG_IX_UA: {
                logLine =
                        "\"device\":{\"lmt\":0,\"ua\":\"Mozilla/5.0(Linux;Android4.4.2;SM-G900TBuild/KOT49H)"
                                + "AppleWebKit/537.36(KHTML,likeGecko)Version/4.0Chrome/30.0.0.0MobileSafari/537.36\"";
                break;
            }
            case MSG_IX_RPACNTID: {
                logLine = "\"ext\":{\"rp\":{\"account_id\":11726}}},";
                break;
            }
            case MSG_IX_RPSIZEID: {
                logLine = "\"ext\":{\"rp\":{\"size_id\":43,";
                break;
            }
            case MSG_IX_GBIDLOW: {
                logLine = "\"bidfloor\":0.45,\"proxydemand\":{\"marketrate\":0.45},";
                break;
            }
            case MSG_IX_GBIDHGH: {
                logLine = "\"bidfloor\":0.45,\"proxydemand\":{\"marketrate\":14.0},";
                break;
            }
            case MSG_IX_GBIDSME: {
                logLine = "\"bidfloor\":0.45,\"proxydemand\":{\"marketrate\":0.45},";
                break;
            }
            case MSG_IX_GBIDLOW1: {
                logLine = "\"bidfloor\":0.35,\"proxydemand\":{\"marketrate\":0.35},";
                break;
            }
            case MSG_IX_GBIDHGH1: {
                logLine = "\"bidfloor\":0.35,\"proxydemand\":{\"marketrate\":14.0},";
                break;
            }
            case MSG_IX_GBIDSME1: {
                logLine = "\"bidfloor\":0.35,\"proxydemand\":{\"marketrate\":0.35},";
                break;
            }
            case MSG_IX_ADPGBIDLOW: {
                logLine = "guidanceBid:2000),";
                break;
            }
            case MSG_IX_ADPGBIDHGH: {
                logLine = "guidanceBid:14000000),";
                break;
            }
            case MSG_IX_ADPGBIDSME: {
                logLine = "guidanceBid:40000),";
                break;
            }
            case MSG_IX_BLIND: {
                logLine = "\"transparency\":{\"blind\":1},";
                break;
            }
            case MSG_IX_BLINDAPP: {
                logLine = "\"app\":{\"id\":\"37a802196f2d480b971b537315f059c1\"";
                break;
            }
            case MSG_IX_BLINDAPPBUNDLE: {
                logLine = "\"bundle\":\"com.ix.84d5c8dd-fb27-31ee-af1a-55c2850d41be\"";
                break;
            }
            case MSG_IX_BLINDAPPEXTBUNDLE: {
                logLine = "\"blind\":{\"bundle\":\"com.ix.84d5c8dd-fb27-31ee-af1a-55c2850d41be\"}";
                break;
            }
            case MSG_IX_BLINDWAP: {
                logLine = "\"site\":{\"id\":\"37a802196f2d480b971b537315f059c1\"";
                break;
            }
            case MSG_IX_BLINDWAPBUNDLE: {
                logLine =
                        "\"domain\":\"http://www.ix.com/84d5c8dd-fb27-31ee-af1a-55c2850d41be\",\"page\":\""
                                + "http://www.ix.com/84d5c8dd-fb27-31ee-af1a-55c2850d41be\"";
                break;
            }
            case MSG_IX_BLINDWAPEXTBUNDLE: {
                logLine =
                        "\"blind\":{\"domain\":\"http://www.ix.com/84d5c8dd-fb27-31ee-af1a-55c2850d41be\",\"page\":\""
                                + "http://www.ix.com/84d5c8dd-fb27-31ee-af1a-55c2850d41be\"}";
                break;
            }
            case MSG_IX_TRANS: {
                logLine =
                        "\"transparency\":{\"blind\":0,\"blindbuyers\":[3522,2666,2179,3038,3107,3420,3002,3320,2853,"
                                + "3158,3560,3600,3542,3614,3101,3676,2893]},";
                break;
            }
            case MSG_IX_TRANSAPP: {
                logLine = "app\":{\"id\":\"5f0eb5b2200740b7828cf78f9d5e1c1c\"";
                break;
            }
            case MSG_IX_TRANSAPP2: {
                logLine = "app\":{\"id\":\"42f1578f616747e59a3d623dc125f25c\"";
                break;
            }
            case MSG_IX_TRANSIDNAME: {
                logLine = "\"name\":\"Retro Snakes";
                break;
            }
            case MSG_IX_TRANSSTOREURL: {
                logLine = "\"storeurl\":\"https://itunes.apple.com/us/app/retro-snakes/id662098413";
                break;
            }
            case MSG_IX_TRANSAPPEXTBUNDLE: {
                logLine = "\"blind\":{\"bundle\":\"com.ix.84d5c8dd-fb27-31ee-af1a-55c2850d41be\"}";
                break;
            }
            case MSG_IX_TRANSWAP: {
                logLine = "\"site\":{\"id\":\"5f0eb5b2200740b7828cf78f9d5e1c1c\"";
                break;
            }
            case MSG_IX_TRANSWAP2: {
                logLine = "\"site\":{\"id\":\"42f1578f616747e59a3d623dc125f25c\"";
                break;
            }
            case MSG_IX_TRANSWAPEXTBUNDLE: {
                logLine = "\"blind\":{\"bundle\":\"com.ix.84d5c8dd-fb27-31ee-af1a-55c2850d41be\"}";
                break;
            }
            case MSG_IX_ADAPTER_CONFIG_FAIL: {
                logLine =
                        "mandate parameters missing or request format is not compatible to partner supported response for dummy so exiting adapter";
                break;
            }
            case MSG_IX_ADAPTER_CONFIG_FAIL2: {
                logLine = "Configure parameters inside IX returned false ix: BasicParams Not Available";
                break;
            }
            case MSG_IX_ADAPTER_CONFIG_FAIL_IMP_OBJ_NULL: {
                logLine = "Configure parameters inside IX returned false ix: Impression Obj is null";
                break;
            }
            case MSG_IX_CREATIVE_NATIVE: {
                logLine = "Creative type is : NATIVE";
                break;
            }
            case MSG_IX_VAST_MEDIA_PREFS: {
                logLine =
                        "{\"incentiveJSON\": \"{}\",\"video\" :{\"preBuffer\": \"WIFI\",\"skippable\": false,\"soundOn\": false }}";
                break;
            }
            case MSG_IX_PACKAGE_CSID: {
                logLine = "\"target\":{\"packages\":[\"10002\",\"10003\"]}";
                break;
            }
            case MSG_IX_PACKAGE_CSID2: {
                logLine = "\"target\":{\"packages\":[\"10003\",\"10002\"]}";
                break;
            }
            case MSG_IX_PACKAGE_NORMAL: {
                logLine = "\"target\":{\"packages\":[\"10001\"]";
                break;
            }
            case MSG_IX_TRACKERDEAL_WITHOUT_VIEWABILITY: {
                logLine = "\"target\":{\"packages\":[\"10012\"]";
                break;
            }
            case MSG_IX_TRACKERDEAL_WITH_VIEWABILITY: {
                logLine = "\"target\":{\"packages\":[\"10011\"]";
                break;
            }
            case MSG_IX_TRACKERDEAL_VIEWABILITY_TRACKER_INSTEAD_JSON: {
                logLine = "http://www.viewability_tracker.com/";
                break;
            }
            case MSG_IX_TRACKERDEAL_VIEWABILITY_TRACKER_FROM_JSON: {
                logLine = "https://ViewabilityTracker_thirdpartyjson.com/";
                break;
            }
            case MSG_IX_TRACKERDEAL_AUDIENCE_LIMIT_AD_TRACKING_MACRO: {
                logLine = "$LIMIT_AD_TRACKING";
                break;
            }
            case MSG_IX_TRACKERDEAL_AUDIENCE_IMP_CB_MACRO: {
                logLine = "$IMP_CB";
                break;
            }
            case MSG_IX_TRACKERDEAL_AUDIENCE_USER_ID_SHA256_HASHED_MACRO: {
                logLine = "$USER_ID_SHA256_HASHED";
                break;
            }
            default: {
                logLine = "FENDER HAS NO MATCHING LOG LINE TO VALIDATE FOR";
                break;
            }
        }
        return logLine;
    }
}
