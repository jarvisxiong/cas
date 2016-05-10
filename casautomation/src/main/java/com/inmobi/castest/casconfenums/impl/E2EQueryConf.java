package com.inmobi.castest.casconfenums.impl;


/**
 * Created by navaneeth on 26/2/16.
 */

import com.inmobi.castest.casconfenums.def.QueryConf;
import com.inmobi.castest.casconfenums.def.QueryConf.E2EQuery;
import com.inmobi.castest.commons.iohelper.YamlDataIOHelper;

import java.io.FileNotFoundException;
import java.util.Map;

public class E2EQueryConf {

    public static String setE2EQuery(final QueryConf.E2EQuery query, String Site_id, String testcase) {
        String quereyString = new String();
        try {
            Map<String, String> map = YamlDataIOHelper.readE2ETestParams(testcase);
            switch (query) {
                case SELECT_WAP_SITE_SITE_ID:
                    quereyString = "select * from wap_site limit 1";
                    break;
                case UPDATE_WAP_SITE:
                    quereyString =
                        "update wap_site set blocked = null, status  = 'activated', old_sdk_support = true, serve_ad =true, allow_adult_ad = 0, platform_type_id  = "
                            + map.get("os_id") + " where id ='" + Site_id + "'";
                    break;
                case SELECT_WAP_PUBLISHER_IX:
                    quereyString =
                        "select * from wap_publisher_ix where id=(select pub_id from wap_site where id='" + Site_id
                            + "') ";
                    break;
                case SELECT_WAP_PUBLISHER_ID:
                    quereyString = "select pub_id from wap_site where id ='" + Site_id + "'";
                    break;
                case INSEERT_WAP_PUBLISHER_IX:
                    quereyString = "insert into wap_publisher_ix values ('" + Site_id + "',2,'{}',now())";
                    break;
                case UPDATE_WAP_PUBLISHER_IX:
                    quereyString =
                        "update wap_publisher_ix set exchange_setting =2 where id=(select pub_id from wap_site where id='"
                            + Site_id + "')";
                    break;
                case DELETE_SITE_TAGS:
                    quereyString = "delete from site_tags where id=" + map.get("ad_number");
                    break;
                case UPDATE_PLACEMENT:
                    quereyString = "update placement set status_id = 1 where site_id ='" + Site_id + "'";
                    break;
                case INSERT_SITE_TAGS:
                    quereyString = "INSERT INTO site_tags VALUES (" + map.get("ad_number") + ",'" + Site_id
                        + "','{1}',now(),now())";
                    break;
                case SELECT_SITE_ADVERTISER_PREFERENCE:
                    quereyString = "select * from site_advertiser_preference where site_id = '" + Site_id + "'";
                    break;
                case DELETE_SITE_ADVERTISER_PREFERENCE:
                    quereyString = "delete from site_advertiser_preference where site_id = '" + Site_id + "'";
                    break;
                case SELECT_PLACEMENT_TEMPLATE:
                    quereyString = "select * from placement where site_id ='" + Site_id + "'";
                    break;
                case UPDATE_PLACEMENT_TEMPLATE:
                    quereyString =
                        "update placement_template set all_default_templates_enabled = true, fallback_enabled= true where placement_id =(select id from placement where site_id ='"
                            + Site_id + "' and type=3)";
                    break;
                case DELETE_SITE_CATEGORY_MANUAL:
                    quereyString = "delete from site_category_manual where id = " + map.get("ad_number");
                    break;
                case INSERT_SITE_CATEGORY_MANUAL:
                    quereyString = "insert into site_category_manual  values (" + map.get("ad_number") + ",'" + Site_id
                        + "','{\"71\": 100}','admin',now(),now())";
                    break;
                case DELETE_SITE_CATEGORY_OFFFLINE:
                    quereyString = "delete from site_category_offline where site_guid ='" + Site_id + "'";
                    break;
                case INSERT_SITE_CATEGORY_OFFFLINE:
                    quereyString = "insert into site_category_offline values('" + Site_id
                        + "','{\"71\": 100}','{\"hhi\":{\"2\":24.0,\"3\":14.000000000000002,\"1\":32.0,\"7\":5.0,\"6\":13.0,\"5\":12.0},\"haschildren\":{\"2\":37.0,\"1\":63.0},\"age\":{\"4\":39.0,\"2\":17.0,\"3\":20.0,\"1\":12.0,\"6\":84.0,\"5\":13.0},\"gender\":{\"2\":51.0,\"1\":49.0},\"language\":{\"149\":2.0,\"52\":6.0,\"127\":1.0,\"37\":1.0,\"62\": (...)',now(),now())";
                    break;
                case DELETE_PRICING_REPOSITORY:
                    quereyString = "delete from pricing_repository where id =10000" + map.get("ad_number");
                    break;
                case INSERT_INTO_PRICING_REPOSITORY:
                    quereyString = "INSERT INTO pricing_repository VALUES (10000" + map.get("ad_number")
                        + ", true, now(),'{\"demandSource\": \"IX\", \"floor\": 0.01, \"segment\": {\"site_id\":[\""
                        + Site_id + "\"]}}')";
                    break;
                case DELETE_RULES_REPOSITORY:
                    quereyString = "delete from rules_repository  where id =10000" + map.get("ad_number");
                    break;
                case INSERT_RULES_REPOSITORY:
                    quereyString = "INSERT INTO rules_repository VALUES (10000" + map.get("ad_number")
                        + ", true, now(), '{\"demandSource\":\"IX\", \"demandType\": \"PROGRAMMATIC\", \"trafficPercent\": 100, \"segment\": {\"site_id\":[\""
                        + Site_id + "\"]}}')";
                    break;
                default:
                    System.out.println("error");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return quereyString;
    }


}


