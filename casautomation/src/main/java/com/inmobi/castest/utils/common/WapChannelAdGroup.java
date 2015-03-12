package com.inmobi.castest.utils.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.inmobi.castest.casconfenums.def.CasConf.WapChannelAdGroupTemplates;
import com.inmobi.castest.casconfenums.def.QueryConf.Query;
import com.inmobi.castest.casconfenums.impl.CasQueryConf;
import com.inmobi.castest.commons.dbhelper.QueryManager;

public class WapChannelAdGroup {

    WapChannelAdGroup adGroupInstance = new WapChannelAdGroup();

    public static void main(final String[] args) {
        // TODO Auto-generated method stub

    }

    public static Map<String, String> setWapChannelAdGroup(final WapChannelAdGroupTemplates prop,
        final Map<String, String> wapChannelAdGroup) throws ClassNotFoundException, SQLException {
        ArrayList<Map> wapGroup = new ArrayList<Map>();
        // prop = prop.toLowerCase();
        switch (prop) {
            case GENERIC_WAP_ADGROUP: {
                wapChannelAdGroup.put("advertiser_id", "4028cbe0373b25ce01379c082d4113cf");
                final String queryToExecute =
                        CasQueryConf.setQuery(Query.SELECT_WAPCHANNEL_ADGROUP_SEGMENT, wapChannelAdGroup);
                wapGroup = QueryManager.executeAndGetColumnsOutput(queryToExecute);
                System.out.println(wapGroup);

            }

            default:
                break;
        }

        return wapGroup.get(0);
    }

    public static Map<String, String> setWapChannelAdGroup(final Map<String, String> wapChannelAdGroup)
            throws ClassNotFoundException, SQLException {
        ArrayList<Map> wapGroup = new ArrayList<Map>();
        final String queryToExecute = CasQueryConf.setQuery(Query.SELECT_WAPCHANNEL_ADGROUP_SEGMENT, wapChannelAdGroup);
        wapGroup = QueryManager.executeAndGetColumnsOutput(queryToExecute);
        if (wapGroup.size() != 0) {
            return wapGroup.get(0);
        } else {
            Map<String, String> wapGroupFromDefaultAdv = new LinkedHashMap<String, String>();
            wapGroupFromDefaultAdv =
                    setWapChannelAdGroup(WapChannelAdGroupTemplates.GENERIC_WAP_ADGROUP, wapGroupFromDefaultAdv);
            wapGroupFromDefaultAdv.put("advertiser_id", wapChannelAdGroup.get("advertiser_id"));
            return wapGroupFromDefaultAdv;

        }
    }
}
