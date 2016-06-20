package com.inmobi.castest.commons.dbhelper;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.inmobi.castest.casconfenums.def.CasConf.ChannelPartners;
import com.inmobi.castest.casconfenums.impl.CasPartnerConf;
import com.inmobi.castest.commons.generichelper.RepoRefreshHelper;
import com.inmobi.castest.commons.iohelper.FenderTestIOHelper;
import com.inmobi.castest.commons.iohelper.YamlDataIOHelper;
import com.inmobi.castest.utils.common.WapChannelAdGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateDBWithWAPAdGroupData {

    public static void updateDBWithData(final String testCaseName, final boolean isRepoRefreshRequired) throws Exception {

        Map<String, String> wapChannelAdGroup = new LinkedHashMap<String, String>();
        Map<String, String> map = new HashMap<String, String>();
        int numberofAdgroup;
        final String allAdvetisers = YamlDataIOHelper.readTestIndex().get(testCaseName.toUpperCase()).toUpperCase();

        String[] advertisers = allAdvetisers.split(",");
        System.out.println(advertisers.length);
        map = CasPartnerConf.setPartnerConfig(ChannelPartners.valueOf(advertisers[0]), map);
        System.out.println("\n\nMAP: \n\n" + map);
        wapChannelAdGroup.putAll(map);
        System.out.println("\n\nWAP CHAN AD GRP : \n\n" + wapChannelAdGroup);
        wapChannelAdGroup = WapChannelAdGroup.setWapChannelAdGroup(wapChannelAdGroup);

        wapChannelAdGroup.put("fender_test_case_id", testCaseName);

        /*
         * Set Default values for the adgroup for the particular testcase
         */

        WAPChannelAdgroupVitals.setDefaultTestValues(wapChannelAdGroup);

        map = CasPartnerConf.setPartnerConfig(ChannelPartners.valueOf(advertisers[0]), map);
        System.out.println("\n\nMAP: \n\n" + map);
        wapChannelAdGroup.putAll(map);

        System.out.println("\n\nWAP CHAN AD GRP : \n\n" + wapChannelAdGroup);

        // Updating test case level params
        map = FenderTestIOHelper.setTestParams(testCaseName);

        System.out.println("*** wapChannelAdGroup ***" + wapChannelAdGroup);

        wapChannelAdGroup.putAll(map);
        System.out.println("params : " + map);
        System.out.println("wap channel adgroup advertiser Id : " + wapChannelAdGroup.get("advertiser_id"));

        final String advertiserIdList = wapChannelAdGroup.get("advertiser_id") + "','"
            + CasPartnerConf.setPartnerConfig(ChannelPartners.RTBD2, new LinkedHashMap<String, String>()).get("advertiserId");

        System.out.println("****** advertiser list  ****** :" + advertiserIdList);

        wapChannelAdGroup = RefineWapAdGroupData.refineData(wapChannelAdGroup);

        /*
         *
         * Adding/Setting the 1. automation_test_id param 2. ad_id as testCaseId
         * and 3. adgroup_id as the testCaseID for insertion into the DB
         */

        WAPChannelAdgroupVitals.overrideTestValues(wapChannelAdGroup);


        final String shortlistingAdgroup = wapChannelAdGroup.get("adgroup_id");
        System.out.println("shortlistingAdgroup " + shortlistingAdgroup);

        WAPGroupDBManipulation.UpdateWapChannelAdgroupInDB(wapChannelAdGroup, advertiserIdList);

        try {
            if ((numberofAdgroup = Integer.parseInt(wapChannelAdGroup.get("multi_ad").toString())) > 0)
                ;
            WAPGroupDBManipulation.DeleteMultiWapChannelAdgroupInDB(wapChannelAdGroup);
            while (--numberofAdgroup > 0) {
                if (numberofAdgroup < advertisers.length) {
                    map = CasPartnerConf.setPartnerConfig(ChannelPartners.valueOf(advertisers[numberofAdgroup]), map);
                    System.out.println("\n\nMAP: \n\n" + map);
                    wapChannelAdGroup.putAll(map);
                    wapChannelAdGroup.put("channel_id", wapChannelAdGroup.get("channel_id").substring(0,
                        wapChannelAdGroup.get("channel_id").length() - 1) + (numberofAdgroup+5));

                }

                wapChannelAdGroup.put("adgroup_id", wapChannelAdGroup.get("adgroup_id") + "$" + numberofAdgroup);
                wapChannelAdGroup.put("ad_id", wapChannelAdGroup.get("ad_id") + "$" + numberofAdgroup);

                WAPGroupDBManipulation.UpdateWapChannelAdgroupInDB(wapChannelAdGroup, advertiserIdList);

                wapChannelAdGroup.put("adgroup_id", wapChannelAdGroup.get("adgroup_id").substring(0,
                    wapChannelAdGroup.get("adgroup_id").length() - 2));
                wapChannelAdGroup.put("ad_id", wapChannelAdGroup.get("ad_id").substring(0,
                    wapChannelAdGroup.get("ad_id").length() - 2));

            }
        } catch (final Exception e) {
            System.out.println("NO MULTI Ad");
        }
        // Use with caution
        //        if (null != wapChannelAdGroup.get("multiformat_request")
        //                && 2 == Integer.parseInt(wapChannelAdGroup.get("multiformat_request"))) {
        //            wapChannelAdGroup.put("ad_type_targeting", wapChannelAdGroup.get("ad_type_targeting2"));
        //            wapChannelAdGroup.put("adgroup_id", new UUID(0l, 0l).toString());
        //            WAPGroupDBManipulation.UpdateWapChannelAdgroupInDB(wapChannelAdGroup, advertiserIdList);
        //        }

        System.out.println(" *** update demand supply related data in database ***");
        WAPGroupDBManipulation.UpdateDemandSupplyData(wapChannelAdGroup, advertiserIdList);

        if (isRepoRefreshRequired) {
            RepoRefreshHelper.RefreshRepo();
        }
    }

    /*
     * This is the new method that loads all the test case data in the DB
     */

    public static void updateDBWithAllData() throws Exception {

        final HashMap<String, String> testCasesFromDataFile = YamlDataIOHelper.readTestIndex();

        System.out.println(testCasesFromDataFile);

        for (final String key : testCasesFromDataFile.keySet()) {
            updateDBWithData(key, false);
        }

    }

    public static void updateDBWithIXPackageData() throws ClassNotFoundException, SQLException {

        final HashMap<String, String> ixPackageData = new HashMap<String, String>();
        ixPackageData.put("package_id", "10001");
        WAPGroupDBManipulation.DeleteIXPackageData(ixPackageData, "");
        while (Integer.parseInt(ixPackageData.get("package_id")) <= 10016) {
            WAPGroupDBManipulation.InsertIXPackageData(ixPackageData, "");
        }

    }

    public static void updateDBWithTargetingSegment() throws ClassNotFoundException, SQLException {

        final HashMap<String, String> targetingsegmentData = new HashMap<String, String>();
        targetingsegmentData.put("segment_id", "100001");
        WAPGroupDBManipulation.DeleteTaretingSegmentData(targetingsegmentData, "");
        while (Integer.parseInt(targetingsegmentData.get("segment_id")) <= 100004) {
            WAPGroupDBManipulation.InsertTargetSegmentData(targetingsegmentData, "");
        }

    }

    public static void main(final String[] args) throws Exception {

        updateDBWithAllData();
        updateDBWithIXPackageData();
        updateDBWithTargetingSegment();
        RepoRefreshHelper.RefreshRepo();

    }

}
