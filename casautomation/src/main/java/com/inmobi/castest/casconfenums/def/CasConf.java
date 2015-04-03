package com.inmobi.castest.casconfenums.def;

/**
 * @author santosh.vaidyanathan
 */

public class CasConf {

    /*
     * Partner related enum settings
     */
    public enum ChannelPartners {
        ADELPHIC, DRAWBRIDGE, IX, TAPIT, RTBD2, NEXAGE, GOOGLEADX, DMG, AMOAD, MOBILECOMMERCE
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
        MSG_SENDING_NO_AD, MSG_RTB_MANDATE_PARAM_MISSING, MSG_IX_ADRRFLAG, MSG_IX_ADRRFLAG2, MSG_IX_TERMREQ,
        MSG_IX_FORMREQ, MSG_IX_SENDREQ, MSG_IX_EXCHANGEREQ, MSG_IX_TMAX, MSG_IX_APPOBJ, MSG_IX_WAPOBJ, MSG_IX_ANDRD,
        MSG_IX_IOS, MSG_IX_AVRSN, MSG_IX_IVRSN, MSG_IX_SLOTFILTER, MSG_IX_ADRRNOADS, MSG_IX_MULTSLOTABS,
        MSG_IX_MULTSLOTICL, MSG_IX_LATLONG, MSG_IX_UA, MSG_IX_RPACNTID, MSG_IX_RPSIZEID, MSG_IX_GBIDLOW,
        MSG_IX_GBIDHGH, MSG_IX_GBIDSME, MSG_IX_GBIDLOW1, MSG_IX_GBIDHGH1, MSG_IX_GBIDSME1, MSG_IX_ADPGBIDLOW,
        MSG_IX_ADPGBIDHGH, MSG_IX_ADPGBIDSME, MSG_IX_BLIND, MSG_IX_BLINDAPP, MSG_IX_BLINDAPPBUNDLE,
        MSG_IX_BLINDAPPEXTBUNDLE, MSG_IX_BLINDWAP, MSG_IX_BLINDWAPBUNDLE, MSG_IX_BLINDWAPEXTBUNDLE,
        MSG_IX_TRANS, MSG_IX_TRANSAPP, MSG_IX_TRANSIDNAME, MSG_IX_TRANSAPP2, MSG_IX_TRANSAPPEXTBUNDLE,
        MSG_IX_TRANSSTOREURL, MSG_IX_TRANSWAP, MSG_IX_TRANSWAP2, MSG_IX_TRANSWAPEXTBUNDLE, MSG_IX_DST,
        MSG_DCP_TAPIT_CONFIG_SUCCESS, MSG_RTBD_RESPONSE, MSG_RTBD_AdRR_FLAG, MSG_DCP_AD_SERVED,
        MSG_DCP_SENDING_NO_AD, MSG_DCP_TERMINATE_CONFIG_SEARCH
    }

    /*
     * Repo Names to be refreshed by the framework
     */
    public enum Repo {
        CHANNEL_AD_GROUP_REPO, CHANNEL_REPO, SITE_META_DATA_REPO, CHANNEL_FEEDBACK_REPO, CHANNEL_SEGMENT_FEEDBACK_REPO,
        PRICING_ENGINE_REPO, SITE_ECPM_REPO
    }

    public enum LogLinesRegex {
        UUID ("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        private String regex;

        public String getRegex() {
            return regex;
        }

        LogLinesRegex(String regex) {
            this.regex = regex;
        }
    }

}
