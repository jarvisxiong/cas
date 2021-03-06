package com.inmobi.castest.commons.dbhelper;

import java.util.Map;

public class WAPChannelAdgroupVitals {

    public static Map<String, String> setDefaultTestValues(final Map<String, String> wapChannelAdGroup) {

        wapChannelAdGroup.put("slot_ids", "{9}");
        wapChannelAdGroup.put("os_id", "3");
        if (wapChannelAdGroup.get("fender_test_case_id").charAt(4) == '1') {
            wapChannelAdGroup.put("dst", "2");
        } else if (wapChannelAdGroup.get("fender_test_case_id").charAt(4) == '2') {
            wapChannelAdGroup.put("dst", "6");
        } else {
            wapChannelAdGroup.put("dst", "8");
        }
        wapChannelAdGroup.put("site_ratings", "0");
        wapChannelAdGroup.put("targeting_platform", "1");
        wapChannelAdGroup.put("carrier_country", "94");
        wapChannelAdGroup.put("new_category", "70,71");
        wapChannelAdGroup.put("adpool_responseformat", "imai");
        wapChannelAdGroup.put("ad_type_targeting", "0");

        if (wapChannelAdGroup.get("dst").equals("8")) {
            wapChannelAdGroup.put("additional_params",
                    "'{\"mime\":\"html\",\"default\": \"160214\",\"site\": \"38132\"}'");

        }
        wapChannelAdGroup.put("site_url", "www.test.com");
        wapChannelAdGroup.put("site_type_id", "1");
        wapChannelAdGroup.put("coppa_enabled", "false");
        wapChannelAdGroup.put("is_site_transparent", "false");

        return wapChannelAdGroup;
    }

    public static Map<String, String> overrideTestValues(final Map<String, String> wapChannelAdGroup) {
        wapChannelAdGroup.put("ad_id", wapChannelAdGroup.get("fender_test_case_id"));
        wapChannelAdGroup.put("automation_test_id", wapChannelAdGroup.get("fender_test_case_id"));
        wapChannelAdGroup.put("adgroup_id", wapChannelAdGroup.get("fender_test_case_id"));
        wapChannelAdGroup.put("status", "true");
        if (wapChannelAdGroup.get("site_siteid") == null ) {
            wapChannelAdGroup.put("site_siteid", wapChannelAdGroup.get("fender_test_case_id"));
        }
        return wapChannelAdGroup;
    }
}
