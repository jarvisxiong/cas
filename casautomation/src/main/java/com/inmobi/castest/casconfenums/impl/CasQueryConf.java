package com.inmobi.castest.casconfenums.impl;

/**
 * @author santosh.vaidyanathan
 */

import java.util.Map;

import com.inmobi.castest.casconfenums.def.CasConf;
import com.inmobi.castest.casconfenums.def.QueryConf.Query;

public class CasQueryConf {

    public static String setQuery(final Query query, final Map<String, String> adGroup,
            final String... advertiser_id_list) {
        String queryString = new String();
        switch (query) {

            case SELECT_WAPCHANNEL_ADGROUP_SEGMENT: {
                queryString =
                        "select * from wap_channel_adgroup where advertiser_id = '" + adGroup.get("advertiser_id")
                                + "' and rc_list!=\'{-1}\' and rc_list!=\'{}\' "
                                + "and external_site_key not like '% %' order by adgroup_id desc limit 1";
                System.out.println(queryString);
                break;
            }

            case SELECT_WAP_CHANNEL: {
                queryString = "select * from wap_channel where id='" + adGroup.get("channel_id") + "'";
                System.out.println(queryString);
                break;
            }
            case SELECT_WAP_CHANNEL_AD: {
                queryString = "select * from wap_channel_ad where id = '" + adGroup.get("ad_id") + "'";
                System.out.println(queryString);
                break;
            }

            case SELECT_DCP_CHN_SITE_INC_EXC: {
                queryString =
                        "select * from dcp_channel_site_inclusion_exclusion where account_guid ='"
                                + adGroup.get("advertiser_id") + "'";
                break;
            }
            case UPDATE_DCP_CHN_SITE_INC_EXC: {
                queryString =
                        "update dcp_channel_site_inclusion_exclusion set sie_json ='{\"sites\": [], \"mode\": \"exclusion\"}' where account_guid ='"
                                + adGroup.get("advertiser_id") + "'";
                break;
            }

            case INSERT_WAP_CHANNEL_AD: {

                if (adGroup.get("adpool_responseformat").equalsIgnoreCase("NATIVE")) {
                    queryString =
                            "insert into wap_channel_ad values ('" + adGroup.get("ad_id")
                                    + "',(select max(inc_id) from wap_channel_ad)+1,9,'" + adGroup.get("advertiser_id")
                                    + "',null,'cpc','archived',null,1,'f378e4884d384f8ea28c780c8cafcd02','"
                                    + adGroup.get("adgroup_id") + "',1,now(),8)";
                } else if (adGroup.get("adpool_responseformat").equalsIgnoreCase("IMAI")
                        && adGroup.get("slot_ids").contains("14")) {
                    queryString =
                            "insert into wap_channel_ad values ('" + adGroup.get("ad_id")
                                    + "',(select max(inc_id) from wap_channel_ad)+1,11,'"
                                    + adGroup.get("advertiser_id")
                                    + "',null,'cpc','archived',null,1,'f378e4884d384f8ea28c780c8cafcd02','"
                                    + adGroup.get("adgroup_id") + "',1,now(),8)";
                } else {
                    queryString =
                            "insert into wap_channel_ad values ('" + adGroup.get("ad_id")
                                    + "',(select max(inc_id) from wap_channel_ad)+1,0,'" + adGroup.get("advertiser_id")
                                    + "',null,'cpc','archived',null,1,'f378e4884d384f8ea28c780c8cafcd02','"
                                    + adGroup.get("adgroup_id") + "',1,now(),8)";
                }

                System.out.println(queryString);
                break;
            }
            case UPDATE_WAP_CHANNEL_AD: {

                if (adGroup.get("adpool_responseformat").equalsIgnoreCase("NATIVE")) {
                    queryString =
                            "update wap_channel_ad set ad_group_id = '" + adGroup.get("adgroup_id")
                                    + "',is_banner_ad=9 where id = '" + adGroup.get("ad_id") + "'";
                } else if (adGroup.get("adpool_responseformat").equalsIgnoreCase("IMAI")
                        && adGroup.get("slot_ids").contains("14")) {
                    queryString =
                            "update wap_channel_ad set ad_group_id = '" + adGroup.get("adgroup_id")
                                    + "',is_banner_ad=11 where id = '" + adGroup.get("ad_id") + "'";
                } else {
                    queryString =
                            "update wap_channel_ad set ad_group_id = '" + adGroup.get("adgroup_id")
                                    + "',is_banner_ad=0 where id = '" + adGroup.get("ad_id") + "'";
                }
                System.out.println(queryString);
                break;
            }
            case selectDCPAdvertiserBurnQuery: {
                queryString =
                        "select advertiser_id from dcp_advertiser_burn where advertiser_id='"
                                + adGroup.get("advertiser_id") + "'";
                System.out.println(queryString);
                break;
            }
            case selectEarningsFeedBackQuery: {
                queryString =
                        "select * from earnings_dcp_feedback where account_id='" + adGroup.get("advertiser_id")
                                + "' and segment_id='" + adGroup.get("external_site_key") + "'";
                System.out.println(queryString);
                break;
            }
            case selectRealTimeDcpFeedback: {
                queryString =
                        "select advertiser_id from realtime_dcp_feedback where advertiser_id='"
                                + adGroup.get("advertiser_id") + "'";
                System.out.println(queryString);
                break;
            }

            case INSERT_WAPCHANNEL_ADGROUP_SEGMENT: {
                queryString =
                        "insert into wap_channel_adgroup (adgroup_id, ad_id, channel_id, advertiser_id, external_site_key, platform_targeting_int , rc_list , tags, status, is_test_mode, modified_on, campaign_id, slot_ids, ad_inc_id, adgroup_inc_id, all_targeting_tags, pricing_model,targeting_platform,site_ratings, manuf_model_targeting, os_version_targeting, browser_version_targeting,device_types_targeting, category_taxomony, segment_flags, additional_params, impression_ceil, ecpm_boost, boost_date, tod, campaign_inc_id,dst,ad_type_targeting,automation_test_id) values ('"
                                + adGroup.get("adgroup_id")
                                + "' , '"
                                + adGroup.get("ad_id")
                                + "' , '"
                                + adGroup.get("channel_id")
                                + "' , '"
                                + adGroup.get("advertiser_id")
                                + "' , '"
                                + adGroup.get("external_site_key")
                                + "' , '"
                                + adGroup.get("platform_targeting_int")
                                + "' , '"
                                + adGroup.get("rc_list")
                                + "' , "
                                + adGroup.get("tags")
                                + " , '"
                                + adGroup.get("status")
                                + "' , '"
                                + adGroup.get("is_test_mode")
                                + "' , "
                                + "now()"
                                + " , '"
                                + adGroup.get("campaign_id")
                                + "' , '"
                                + adGroup.get("slot_ids")
                                + "' , '"
                                + adGroup.get("ad_inc_id")
                                + "' , '"
                                + adGroup.get("adgroup_inc_id")
                                + "' , '"
                                + adGroup.get("all_targeting_tags")
                                + "' , '"
                                + adGroup.get("pricing_model")
                                + "' , '"
                                + adGroup.get("targeting_platform")
                                + "' , '"
                                + adGroup.get("site_ratings")
                                + "' , "
                                + adGroup.get("manuf_model_targeting")
                                + " , '"
                                + adGroup.get("os_version_targeting")
                                + "' , "
                                + adGroup.get("browser_version_targeting")
                                + " , '"
                                + adGroup.get("device_types_targeting")
                                + "' , '"
                                + adGroup.get("category_taxomony")
                                + "' , '"
                                + adGroup.get("segment_flags")
                                + "' , "
                                + adGroup.get("additional_params")
                                + " , '"
                                + adGroup.get("impression_ceil")
                                + "' , '"
                                + adGroup.get("ecpm_boost")
                                + "' , "
                                + "now()"
                                + " , "
                                + adGroup.get("tod")
                                + " , '"
                                + adGroup.get("campaign_inc_id")
                                + "' , '"
                                + adGroup.get("dst")
                                + "' , '"
                                + adGroup.get("ad_type_targeting") + "' , '" + adGroup.get("automation_test_id") + "')";

                System.out.println(queryString);
                break;
            }
            case updateAllSites: {
                queryString =
                        "update wap_site set pub_id= '4028cb9731d7d0ad0131e1d1996101ef', site_name='TestSite2',site_url='www.inmobi.com',"
                                + "site_type_id=2,ads_per_request=2,min_cpc=0,status='activated',earning=0,ad_approval='auto',allow_adult_ad=0,allow_banner_ad=1,"
                                + "min_cpm=0,created_on=now()-4,pub_share=55,network_for=1,require_full_size_banner=0,need_approval_mail=0,modified_on=now(),"
                                + "in_queue=1,network_type=0,brand_safe=1,new_pub_share=60,platform_type_id=1,blocked=1,accept_demog='f',content_preferences='{0,2}',"
                                + "is_arc_enabled='t',allow_self_serve='f',serve_tp_ads='t' where id in ('4028cba631d63df10131e1d3191d00cb','ff8080812e6bbe86012e6d4fe51c0024','4028cbff3b93b240013bafe7696d0221','4028cbff3af511e5013b14ae9bf50280','4028cbff3b187e27013b262f7a300142','4028cbff3b77ce76013b8dddd836029a','4028cbff3b77ce76013b91f534b20357')";
                System.out.println(queryString);
                break;
            }
            case updatEarningsDcpFeedBackQuery: {
                queryString =
                        "update earnings_dcp_feedback set server_impression =10, revenue=100,served_on=now()-1, modified_on=now() where "
                                + "account_id='" + adGroup.get("advertiser_id") + "'";
                System.out.println(queryString);
                break;
            }
            case updateRealtimeDCPFeedbackQuery: {
                queryString =
                        "update realtime_dcp_feedback set request=0, server_impression=0,served_on=now(), modified_on=now() where advertiser_id='"
                                + adGroup.get("advertiser_id") + "'";
                System.out.println(queryString);
                break;
            }

            case UPDATE_WAP_CHANNEL: {

                queryString =
                        "update wap_channel set account_id='" + adGroup.get("advertiser_id")
                                + "' , impression_ceil=1000000, account_segment='" + adGroup.get("account_segment")
                                + "',priority=2,modified_on=now() where id='" + adGroup.get("channel_id") + "'";
                System.out.println(queryString);
                break;
            }
            case UPDATE_DCP_ADV_BURN_QUERY: {
                queryString =
                        "update dcp_advertiser_burn set balance=10000000, modified_on=now() where advertiser_id='"
                                + adGroup.get("advertiser_id") + "'";
                System.out.println(queryString);
                break;
            }
            case deletePricingEngine: {
                queryString = "delete from dcp_pricing_engine where country_id in (197, 299, 286) and os_id in (3)";
                System.out.println(queryString);
                break;
            }
            case deleteSiteEcpm: {
                queryString =
                        "delete from site_country_os_ecpm  where site_id in ('4028cbff3b93b240013bafe7696d0221' , '4028cbff3af511e5013b14ae9bf50280', '4028cbff3b187e27013b262f7a300142', '4028cbff3b77ce76013b8dddd836029a', '4028cbff3b77ce76013b91f534b20357')";
                System.out.println(queryString);
                break;
            }
            case DELETE_WAPCHANNEL_ADGROUP_SEGMENT: {

                queryString = "delete from wap_channel_adgroup where adgroup_id = '" + adGroup.get("adgroup_id") + "'";
                System.out.println(queryString);
                break;
            }
            case DELETE_IX_PACKAGES: {

                queryString = "Delete from ix_packages where description = 'Fender_Driven'";
                System.out.println(queryString);
                break;
            }
            case DELETE_IX_PACKAGE_DEALS: {

                queryString = "delete from ix_package_deals where created_by ='Fender'";
                System.out.println(queryString);
                break;
            }
            case INSERT_WAP_CHANNEL: {
                queryString =
                        "insert into wap_channel values ('" + adGroup.get("channel_id") + "','test','"
                                + adGroup.get("advertiser_id") + "','test','http://test.com','test',"
                                + "'test','t','t',100,1000000,1,2,now(),'" + adGroup.get("site_ratings") + "',0,"
                                + adGroup.get("account_segment") + ")";
                System.out.println(queryString);
                break;
            }
            case insertDCPAdvertiserBurnQuery: {
                queryString =
                        "insert into dcp_advertiser_burn values ('" + adGroup.get("advertiser_id")
                                + "',10,10,100000,'Automation',now())";
                System.out.println(queryString);
                break;
            }
            case insertEarningsDcpFeedbackQuery: {
                queryString =
                        "insert into earnings_dcp_feedback values ((select coalesce(max(id) + 1 ,1) from earnings_dcp_feedback),"
                                + "'" + adGroup.get("advertiser_id") + "','" + adGroup.get("external_site_key")
                                + "',999,10,150000,888888888,1000,now()-10,now())";
                System.out.println(queryString);
                break;
            }
            case insertPricingEngine: {
                queryString =
                        "insert into dcp_pricing_engine (id,country_id,os_id,rtb_floor,dcp_floor,created_on,modified_on,supply_demand_json) values ((select max(id) from dcp_pricing_engine)+1,197,3,0.20,0,now(),now(),'{\"0\":[\"4\"]}'), ((select max(id) from dcp_pricing_engine)+1,299,3,0.20,0,now(),now(),'{\"0\":[\"5\",\"4\"]}'), ((select max(id) from dcp_pricing_engine)+1,286,3,0.20,0,now(),now(),'{\"3\":[]}')";
                System.out.println(queryString);
                break;
            }
            case insertRealtimeDcpFeedbackQuery: {
                queryString =
                        "insert into realtime_dcp_feedback values((select  coalesce(max(id)+1,1) from realtime_dcp_feedback),"
                                + "'" + adGroup.get("advertiser_id") + "','" + adGroup.get("external_site_key")
                                + "',99,0,1,1,200,now(),now())";
                System.out.println(queryString);
                break;
            }
            case insertSiteEcpm: {
                queryString =
                        "insert into site_country_os_ecpm(site_id,country_id,operating_system,ecpm,network_ecpm,modified_on) values ('4028cbff3b93b240013bafe7696d0221',217,'Android',4,0.90,now()) , ('4028cbff3af511e5013b14ae9bf50280',301,'Android',1.1,0.90,now()), ('4028cbff3b187e27013b262f7a300142',197,'Android',4,0.90,now()), ('4028cbff3b77ce76013b8dddd836029a',299,'Android',4,0.90,now()), ('4028cbff3b77ce76013b91f534b20357',286,'Android',1.1,0.90,now())";
                System.out.println(queryString);
                break;
            }
            case INSERT_IX_PACKAGES: {
                final String csidParam =
                        CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getCSIDParamName() != null
                                ? CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getCSIDParamName()
                                : "";
                final String manufParam =
                        CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getManufModelParamName() != null
                                ? CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id"))
                                        .getManufModelParamName() : "[]";

                String language_targeting_list = "{}";
                String placement_slot_ids = "{}";
                String site_ids = "{}";
                if ( Integer.parseInt(adGroup.get("package_id")) >= 10005) {
                    if (Integer.parseInt(adGroup.get("package_id")) == 10006) {
                        language_targeting_list = "{en,ar}";
                    } else  {
                        language_targeting_list = "{ch}";
                    }
                    placement_slot_ids = "{9}";
                    site_ids = "{479e420ee7d6422c9bedec33d82baecd}";
                }
                queryString =
                        "insert into ix_packages(id, name, description,rp_data_segment_id,pmp_class,country_ids, "
                            + "inventory_types, os_ids, carrier_ids,site_categories,connection_types,"
                            + "app_store_categories,site_ids, zip_codes, cs_ids, scheduled_tods, "
                            + "placement_slot_ids,is_active,data_vendor_cost,city_ids, "
                            + "geo_source_types,os_version_targeting , manuf_model_targeting,"
                            + " language_targeting_list) values (" + adGroup.get("package_id") + ",'Fender_Deal',"
                            + "'Fender_Driven','"
                            + CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getDealName() + "','RIGHT_TO_FIRST_REFUSAL','{"
                            + CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getCountryId() + "}',"
                            + "'{APP,BROWSER}','{5,3}','{}','{FAMILY_SAFE,PERFORMANCE}','{}','{}','" + site_ids +"',"
                            + "'{}','{}','{}','" + placement_slot_ids + "','t',1,'{}','{}', '[{\"osId\":3, "
                    + "\"range\":[]},{\"osId\":5, "
                            + "\"range\":[]}]' , '"
                            + manufParam + "', '" + language_targeting_list + "')";


                System.out.println(queryString);
                break;
            }
            case INSERT_IX_PACKAGE_DEALS: {
                queryString =
                        "Insert into ix_package_deals (rp_deal_id, package_id, created_by,access_type, deal_floor, modified_on) values ('"
                                + CasConf.PackageDeals.valueOf("TEST_" + adGroup.get("package_id")).getDealName()
                                + "'," + adGroup.get("package_id")
                                + ",'Fender','RIGHT_TO_FIRST_REFUSAL_DEAL',1, now())";
                System.out.println(queryString);
                break;
            }
            case SELECT_WAP_SITE_UAC: {
                queryString =
                    "select * from wap_site_uac where id = '" + adGroup.get("site_id") + "'";
                System.out.println(queryString);
                break;
            }
            case INSERT_WAP_SITE_UAC: {
                queryString =
                    "insert into wap_site_uac (id, site_url, site_type_id, market_id, content_rating, app_type, categories,"
                        + " currency, downloads, price, rating, rating_count, created_on,\n"
                        + "  site_mod_on, uac_mod_on, coppa_enabled, gpm_mod_on, modified_on, block_mod_on, title, bundle_id) values ('"
                        + adGroup.get("site_siteid")
                        + "' , '"
                        + adGroup.get("site_url")
                        + "' , "
                        + Integer.valueOf(adGroup.get("site_type_id"))
                        + " , '"
                        + adGroup.get("site_market_id")
                        + "' , '"
                        + adGroup.get("content_rating")
                        + "' , '"
                        + adGroup.get("app_type")
                        + "' , '"
                        + adGroup.get("categories")
                        + "' , '"
                        + adGroup.get("currency")
                        + "' , '"
                        + adGroup.get("downloads")
                        + "' , '"
                        + adGroup.get("price")
                        + "' , '"
                        + adGroup.get("rating")
                        + "' , '"
                        + adGroup.get("rating_count")
                        + "' , "
                        + "now()"
                        + " , "
                        + "now()"
                        + " , "
                        + "now()"
                        + " , "
                        + adGroup.get("coppa_enabled")
                        + " , "
                        + "now()"
                        + " , "
                        + "now()"
                        + " , "
                        + "now()"
                        + " , '"
                        + adGroup.get("title")
                        + "' , '"
                        + adGroup.get("bundle_id")
                        + "')";

                System.out.println(queryString);
                break;
            }
            case DELETE_WAP_SITE_UAC: {

                queryString = "delete from wap_site_uac where id = '" + adGroup.get("site_siteid") + "'";
                System.out.println(queryString);
                break;
            }
            case SELECT_WAP_SITE: {
                queryString =
                    "select * from wap_site where id = '" + adGroup.get("site_id") + "'";
                System.out.println(queryString);
                break;
            }
            case INSERT_WAP_SITE: {
                queryString =
                    "insert into wap_site (id, status, pub_id, site_type_id, modified_on, is_site_transparent) values ('"
                        + adGroup.get("site_siteid")
                        + "' , '"
                        + adGroup.get("site_status")
                        + "' , '"
                        + adGroup.get("site_siteid")
                        + "' , "
                        + Integer.valueOf(adGroup.get("site_type_id"))
                        + ", "
                        + "now()"
                        + " , "
                        + adGroup.get("is_site_transparent")
                        + ")";
                System.out.println(queryString);
                break;
            }
            case DELETE_WAP_SITE: {
                queryString = "delete from wap_site where id = '" + adGroup.get("site_siteid") + "'";
                System.out.println(queryString);
                break;
            }
            case INSERT_WAP_PUBLISHER_IX: {
                queryString =
                    "insert into wap_publisher_ix (id) values ('"
                        + adGroup.get("site_siteid")
                        + "')";

                System.out.println(queryString);
                break;
            }
            case DELETE_WAP_PUBLISHER_IX: {
                queryString = "delete from wap_publisher_ix where id = '" + adGroup.get("site_siteid") + "'";
                System.out.println(queryString);
                break;
            }
            default:
                break;
        }
        return queryString;
    }
}
