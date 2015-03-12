package com.inmobi.castest.commons.dbhelper;

import java.util.Map;

public class RefineWapAdGroupData {

    public static Map<String, String> refineData(final Map<String, String> wapChnAdGrp) {
        if (wapChnAdGrp.get("carrier_country") != null) {
            if (!wapChnAdGrp.get("carrier_country").contains("{")) {
                wapChnAdGrp.put("rc_list", "{" + wapChnAdGrp.get("carrier_country") + "}");
            }
        }

        // 3= android ,5= ios
        if (wapChnAdGrp.get("os_id") != null) {
            wapChnAdGrp.put("os_version_targeting", "{\"os\":[{\"id\":" + wapChnAdGrp.get("os_id")
                    + ",\"incl\":true}]}");
        }
        if (wapChnAdGrp.get("device_osid") != null) {
            wapChnAdGrp.put("os_version_targeting", "{\"os\":[{\"id\":" + wapChnAdGrp.get("device_osid")
                    + ",\"incl\":true}]}");
        }

        // 1= APP ,2= WAP
        if (wapChnAdGrp.get("site_inventorytype") != null) {
            if (wapChnAdGrp.get("site_inventorytype").toUpperCase().equals("APP")) {
                wapChnAdGrp.put("targeting_platform", "1");
            } else if (wapChnAdGrp.get("site_inventorytype").toUpperCase().equals("WAP")) {
                wapChnAdGrp.put("targeting_platform", "2");
            }
        }
        if (wapChnAdGrp.get("targeting_platform") != null) {
            if (wapChnAdGrp.get("targeting_platform").toUpperCase().equals("APP")) {
                wapChnAdGrp.put("targeting_platform", "1");
            } else if (wapChnAdGrp.get("targeting_platform").toUpperCase().equals("WAP")) {
                wapChnAdGrp.put("targeting_platform", "2");
            }
        }
        if (wapChnAdGrp.get("manuf_model_targeting") != null) {
            wapChnAdGrp.put("manuf_model_targeting", "'" + wapChnAdGrp.get("manuf_model_targeting") + "'");

        }

        if (wapChnAdGrp.get("new_category") != null) {
            wapChnAdGrp.put("category_taxomony", "{" + wapChnAdGrp.get("new_category") + "}");
        }

        // currently set the site_rating to {0,2}
        if (wapChnAdGrp.get("site_ratings") != null) {
            if (!wapChnAdGrp.get("site_ratings").contains("{")) {
                System.out.println("******************* DOES NOT CONTAIN { OR } ***************************");
                wapChnAdGrp.put("site_ratings", "{" + wapChnAdGrp.get("site_ratings") + "}");
            }
        }
        if (wapChnAdGrp.get("handset_mfg_list") != null) {
            if (!wapChnAdGrp.get("handset_mfg_list").contains("{")) {
                wapChnAdGrp.put("handset_mfg_list", "'{" + wapChnAdGrp.get("handset_mfg_list") + "}'");
            } else {
                wapChnAdGrp.put("handset_model_list", "'" + wapChnAdGrp.get("handset_mfg_list") + "'");
            }
        }
        if (wapChnAdGrp.get("handset_model_list") != null) {
            if (!wapChnAdGrp.get("handset_model_list").contains("{")) {
                wapChnAdGrp.put("handset_model_list", "'{" + wapChnAdGrp.get("handset_model_list") + "}'");
            } else {
                wapChnAdGrp.put("handset_model_list", "'" + wapChnAdGrp.get("handset_model_list") + "'");
            }
        }
        if (wapChnAdGrp.get("tod") != null) {
            if (!wapChnAdGrp.get("tod").contains("{")) {
                wapChnAdGrp.put("tod", "'{" + wapChnAdGrp.get("tod") + "}'");
            } else {
                wapChnAdGrp.put("tod", "'" + wapChnAdGrp.get("tod") + "'");
            }
        }
        if (wapChnAdGrp.get("tags") != null) {
            if (!wapChnAdGrp.get("tags").contains("{")) {
                wapChnAdGrp.put("tags", "'{" + wapChnAdGrp.get("tags") + "}'");
            } else {
                wapChnAdGrp.put("tags", "'" + wapChnAdGrp.get("tags") + "'");
            }
        }

        if (wapChnAdGrp.get("adpool_selectedslots") != null) {
            if (!wapChnAdGrp.get("adpool_selectedslots").contains("{")) {
                wapChnAdGrp.put("slot_ids", "{" + wapChnAdGrp.get("adpool_selectedslots") + "}");
            } else {
                wapChnAdGrp.put("slot_ids", wapChnAdGrp.get("adpool_selectedslots"));
            }
        }
        // if (wapChnAdGrp.get("slot_ids") != null) {
        // if (!wapChnAdGrp.get("slot_ids").contains("{"))
        // wapChnAdGrp.put("slot_ids", "{" + wapChnAdGrp.get("slot_ids")
        // + "}");
        // }
        return wapChnAdGrp;

    }
}
