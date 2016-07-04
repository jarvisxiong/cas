package com.inmobi.castest.commons.dbhelper;

/**
 * @author santosh.vaidyanathan
 */

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.inmobi.castest.casconfenums.def.QueryConf.Query;
import com.inmobi.castest.casconfenums.impl.CasQueryConf;

public class WAPGroupDBManipulation {

    public static void DeleteMultiWapChannelAdgroupInDB(final Map<String, String> wapChannelAdgroupObject)
            throws ClassNotFoundException, SQLException {
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_MULTI_WAPCHANNEL_ADGROUP_SEGMENT,
                wapChannelAdgroupObject));
        QueryManager.executeUpdateQuery(CasQueryConf
                .setQuery(Query.DELETE_MULTI_WAPCHANNEL_AD, wapChannelAdgroupObject));
    }

    public static void UpdateWapChannelAdgroupInDB(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_WAPCHANNEL_ADGROUP_SEGMENT,
                wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAPCHANNEL_ADGROUP_SEGMENT,
                wapChannelAdgroupObject));

        /*
         * WAP Channel Ad query
         */

        final ArrayList<Map> resultSetOfWapChannelAd =
                QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.SELECT_WAP_CHANNEL_AD,
                        wapChannelAdgroupObject));
        System.out.println("***RESULT SET 1 SIZE***  : " + resultSetOfWapChannelAd.size());

        if (resultSetOfWapChannelAd.size() == 0) {
            QueryManager
                    .executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_CHANNEL_AD, wapChannelAdgroupObject));
        } else {
            QueryManager
                    .executeUpdateQuery(CasQueryConf.setQuery(Query.UPDATE_WAP_CHANNEL_AD, wapChannelAdgroupObject));
        }

        try {
            if (wapChannelAdgroupObject.get("adpool_requestedadtype").equals("INTERSTITIAL")
                    || wapChannelAdgroupObject.get("adpool_requestedadtype").equals("NATIVE")) {
                wapChannelAdgroupObject.put("adpool_requestedadtype", "");
                if (resultSetOfWapChannelAd.size() == 0) {
                    QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_CHANNEL_AD,
                            wapChannelAdgroupObject));
                } else {
                    QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.UPDATE_WAP_CHANNEL_AD,
                            wapChannelAdgroupObject));
                }
            }
        } catch (Exception e) {}
        // update wap_channel table

        final ArrayList<Map> resultSetOfWapChannel =
                QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.SELECT_WAP_CHANNEL,
                        wapChannelAdgroupObject));

        System.out.println("***RESULT SET 2 SIZE***  : " + resultSetOfWapChannel.size());

        if (resultSetOfWapChannel.size() == 0) {

            QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_CHANNEL, wapChannelAdgroupObject));
        } else {

            // wapChannelAdgroupObject.putAll(resultSetOfWapChannel.get(0));
            System.out.println("******ACC SEGMENT****** : " + wapChannelAdgroupObject.get("account_segment"));

            QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.UPDATE_WAP_CHANNEL, wapChannelAdgroupObject));

        }

        // update realtime_dcp_feedback table

        // final ArrayList<Map> resultSetOfRealTimeDcpFeedback =
        // QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.selectRealTimeDcpFeedback,
        // wapChannelAdgroupObject, advertiser_id_list));
        // System.out.println("***RESULT SET 3 SIZE***  : " + resultSetOfRealTimeDcpFeedback.size());
        //
        // if (resultSetOfRealTimeDcpFeedback.size() == 0) {
        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.insertRealtimeDcpFeedbackQuery,
        // wapChannelAdgroupObject));
        // } else {
        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.updateRealtimeDCPFeedbackQuery,
        // wapChannelAdgroupObject));
        // }

        // update dcp_advertiser_burn table
        final ArrayList<Map> resultSetOfDcpAdvertiserBurnQuery =
                QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.selectDCPAdvertiserBurnQuery,
                        wapChannelAdgroupObject));
        System.out.println("***RESULT SET 4 SIZE***  : " + resultSetOfDcpAdvertiserBurnQuery.size());

        if (resultSetOfDcpAdvertiserBurnQuery.size() == 0) {
            QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.insertDCPAdvertiserBurnQuery,
                    wapChannelAdgroupObject));
        } else {
            QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.UPDATE_DCP_ADV_BURN_QUERY,
                    wapChannelAdgroupObject));
        }

        // update earnings_dcp_feedback table
        // // final ArrayList<Map> resultSetOfEarningsFeedbackQuery =
        // // QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.selectEarningsFeedBackQuery,
        // // wapChannelAdgroupObject));
        // // System.out.println("***RESULT SET 5 SIZE***  : " + resultSetOfEarningsFeedbackQuery.size());
        //
        // if (resultSetOfEarningsFeedbackQuery.size() == 0) {
        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.insertEarningsDcpFeedbackQuery,
        // wapChannelAdgroupObject));
        // } else {
        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.updatEarningsDcpFeedBackQuery,
        // wapChannelAdgroupObject));
        // }

        final ArrayList<Map> resultSetOfDcpChnSiteIncExc =
                QueryManager.executeAndGetColumnsOutput(CasQueryConf.setQuery(Query.SELECT_DCP_CHN_SITE_INC_EXC,
                        wapChannelAdgroupObject));
        System.out.println("***RESULT SET 5 SIZE***  : " + resultSetOfDcpChnSiteIncExc.size());

        if (resultSetOfDcpChnSiteIncExc.size() != 0) {
            QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.UPDATE_DCP_CHN_SITE_INC_EXC,
                    wapChannelAdgroupObject));
        }

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_WAP_SITE_UAC, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_SITE_UAC, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_WAP_SITE, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_SITE, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_WAP_PUBLISHER_IX, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_WAP_PUBLISHER_IX, wapChannelAdgroupObject));

    }

    public static void UpdateDemandSupplyData(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.updateAllSites, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.deleteSiteEcpm, wapChannelAdgroupObject));

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.insertSiteEcpm, wapChannelAdgroupObject));

        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.deletePricingEngine, wapChannelAdgroupObject));

        // QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.insertPricingEngine, wapChannelAdgroupObject));

    }

    public static void DeleteWapchannelAdgroupLikeTest(final Map<String, String> wapChannelAdgroupObject)
            throws SQLException, ClassNotFoundException {
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_WAPCHANNEL_ADGROUP_LIKE_TEST,
                wapChannelAdgroupObject));
    }

    public static void DeleteIXPackageData(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_IX_PACKAGE_DEALS, wapChannelAdgroupObject));
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_IX_PACKAGES, wapChannelAdgroupObject));

    }

    public static void InsertIXPackageData(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_IX_PACKAGES, wapChannelAdgroupObject));
        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_IX_PACKAGE_DEALS, wapChannelAdgroupObject));
        final int pack_int = 1 + Integer.parseInt(wapChannelAdgroupObject.get("package_id"));
        wapChannelAdgroupObject.put("package_id", Integer.toString(pack_int));

    }

    public static void DeleteTaretingSegmentData(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.DELETE_TARGETING_SEGMENT, wapChannelAdgroupObject));

    }

    public static void InsertTargetSegmentData(final Map<String, String> wapChannelAdgroupObject,
            final String advertiser_id_list) throws ClassNotFoundException, SQLException {

        QueryManager.executeUpdateQuery(CasQueryConf.setQuery(Query.INSERT_TARGETING_SEGMENT, wapChannelAdgroupObject));
        final int pack_int = 1 + Integer.parseInt(wapChannelAdgroupObject.get("segment_id"));
        wapChannelAdgroupObject.put("segment_id", Integer.toString(pack_int));

    }
}
