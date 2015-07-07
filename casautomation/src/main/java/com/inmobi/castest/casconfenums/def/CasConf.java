package com.inmobi.castest.casconfenums.def;


/**
 * @author santosh.vaidyanathan
 */

public class CasConf {

    public enum ChannelPartners {
        /*
         * Partner related enum settings
         */
        ADELPHIC, DRAWBRIDGE, IX, TAPIT, RTBD2, NEXAGE, GOOGLEADX, DMG, AMOAD, MOBILECOMMERCE, TABOOLA
    }
    /*
     * Wap Channel adgroup settings
     */
    public enum WapChannelAdGroupTemplates {
        GENERIC_WAP_ADGROUP,
    }

    /*
     * Pre defined log strings to be validated from the logs
     */
    public enum LogStringParams {

        MSG_SENDING_NO_AD, MSG_RTB_MANDATE_PARAM_MISSING, MSG_IX_ADRRFLAG, MSG_IX_ADRRFLAG2, MSG_IX_TERMREQ, MSG_IX_FORMREQ, MSG_IX_SENDREQ, MSG_IX_EXCHANGEREQ, MSG_IX_TMAX, MSG_IX_APPOBJ, MSG_IX_WAPOBJ, MSG_IX_ANDRD, MSG_IX_IOS, MSG_IX_AVRSN, MSG_IX_IVRSN, MSG_IX_SLOTFILTER, MSG_IX_ADRRNOADS, MSG_IX_MULTSLOTABS, MSG_IX_MULTSLOTICL, MSG_IX_LATLONG, MSG_IX_UA, MSG_IX_RPACNTID, MSG_IX_RPSIZEID, MSG_IX_GBIDLOW, MSG_IX_GBIDHGH, MSG_IX_GBIDSME, MSG_IX_GBIDLOW1, MSG_IX_GBIDHGH1, MSG_IX_GBIDSME1, MSG_IX_ADPGBIDLOW, MSG_IX_ADPGBIDHGH, MSG_IX_ADPGBIDSME, MSG_IX_BLIND, MSG_IX_BLINDAPP, MSG_IX_BLINDAPPBUNDLE, MSG_IX_BLINDAPPEXTBUNDLE, MSG_IX_BLINDWAP, MSG_IX_BLINDWAPBUNDLE, MSG_IX_BLINDWAPEXTBUNDLE, MSG_IX_TRANS, MSG_IX_TRANSAPP, MSG_IX_TRANSIDNAME, MSG_IX_TRANSAPP2, MSG_IX_TRANSAPPEXTBUNDLE, MSG_IX_TRANSSTOREURL, MSG_IX_TRANSWAP, MSG_IX_TRANSWAP2, MSG_IX_TRANSWAPEXTBUNDLE, MSG_IX_DST, MSG_DCP_TAPIT_CONFIG_SUCCESS, MSG_RTBD_RESPONSE, MSG_RTBD_AdRR_FLAG, MSG_DCP_AD_SERVED, MSG_DCP_SENDING_NO_AD, MSG_DCP_TERMINATE_CONFIG_SEARCH, MSG_IX_ADAPTER_CONFIG_FAIL, MSG_IX_ADAPTER_CONFIG_FAIL2, MSG_IX_CREATIVE_NATIVE, MSG_RTBD_SASPARAMS_MODELNAME_NEGATIVE, MSG_RTBD_SASPARAMS_MANUFNAME_NEGATIVE, MSG_RTBD_SASPARAMS_MODELNAME, MSG_RTBD_SASPARAMS_MANUFNAME, MSG_RTBD_SASPARAMS_DEVICETYPE_TABLET, MSG_RTBD_SASPARAMS_DEVICETYPE_FEATUREPHONE, MSG_RTBD_SASPARAMS_DEVICETYPE_SMARTPHONE, MSG_RTBD_EXT_OBJECT_DATA_SIGNALS, MSG_RTBD_MAKE_AND_MANUF_DATA_SIGNALS, MSG_RTBD_EXT_OBJECT_NEGATIVE_DATA_SIGNALS, MSG_IX_AD_SERVED, MSG_IX_VAST_AD_TAG_URI_END, MSG_IX_VAST_AD_TAG_URI, MSG_IX_VAST_MEDIA_PREFS, MSG_IX_ADAPTER_CONFIG_FAIL_IMP_OBJ_NULL, MSG_IX_PACKAGE_CSID, MSG_IX_PACKAGE_NORMAL, MSG_IX_PACKAGE_CSID2

    }

    /*
     * Repo Names to be refreshed by the framework
     */
    public enum Repo {
        CHANNEL_AD_GROUP_REPO, CHANNEL_REPO, SITE_META_DATA_REPO, CHANNEL_FEEDBACK_REPO, CHANNEL_SEGMENT_FEEDBACK_REPO, PRICING_ENGINE_REPO, SITE_ECPM_REPO
    }
    public enum PackageDeals {
        TEST_10001(150, "NormalDeal"), TEST_10002(200, "CSIDDeal", "[[1],[2,4]]"), TEST_10003(200, "CSIDDeal2",
                "[[1,2],[4]]"), TEST_10004(250, "ManufDeal", "",
                "[{“manufId”:7, “modelIds”:[129,567,789], “incl”:”true”}]"), TEST_10005(300, "ModelIdWifi", "fummy");
        private int countryId;
        private String csidParam;
        private String dealName;
        private String manufModel;

        PackageDeals(final int countryId, final String dealName) {
            // this.packageId = packageId;
            this.countryId = countryId;
            this.dealName = dealName;
        }

        PackageDeals(final int countryId, final String dealName, final String csidParam) {
            this.countryId = countryId;
            this.dealName = dealName;
            this.csidParam = csidParam;

        }

        PackageDeals(final int countryId, final String dealName, final String csidParam, final String manufModel) {
            this.countryId = countryId;
            this.dealName = dealName;
            this.csidParam = csidParam;
            this.manufModel = manufModel;

        }

        // public int getPackageId() {
        // return packageId;
        // }
        public int getCountryId() {
            return countryId;
        }

        public String getDealName() {
            return dealName;
        }

        public String getCSIDParamName() {
            return csidParam;
        }

        public String getManufModelParamName() {
            return manufModel;
        }
    }

    public enum LogLinesRegex {
        UUID("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        private final String regex;

        public String getRegex() {
            return regex;
        }

        LogLinesRegex(final String regex) {
            this.regex = regex;
        }
    }
}
