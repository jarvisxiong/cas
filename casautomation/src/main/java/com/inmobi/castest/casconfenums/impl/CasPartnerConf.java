package com.inmobi.castest.casconfenums.impl;

import java.util.Map;

import com.inmobi.castest.casconfenums.def.CasConf.ChannelPartners;

/**
 * @author santosh.vaidyanathan
 */

public class CasPartnerConf {

    public static Map<String, String> setPartnerConfig(final ChannelPartners partner,
            final Map<String, String> wapChannelAdGroup) {

        switch (partner) {
            case ADELPHIC: {

                System.out.println("inside channel partners");
                wapChannelAdGroup.put("name", "adelphic");
                wapChannelAdGroup.put("advertiser_id", "fad2f318acef40059d936c8f0027089c");
                wapChannelAdGroup.put("host", "http://ad.ipredictive.com/d/ads");
                wapChannelAdGroup.put("mandatoryUidParam", "true");
                wapChannelAdGroup.put("needAdditionalParamInWapChannelAdgroup", "true");
                wapChannelAdGroup.put("additionalParamValue",
                        "{\"pubId\": \"inmobi_2\", \"spot\": \"1\", \"site\": \"0\"}");
                wapChannelAdGroup.put("dst", "2");
                System.out.println(wapChannelAdGroup);
                break;

            }
            case DRAWBRIDGE: {
                System.out.println("Inside Test");
            }
            case TAPIT: {

                wapChannelAdGroup.put("name", "tapit");
                wapChannelAdGroup.put("advertiser_id", "4028cb1e38411ed20138515af2b6025a");
                wapChannelAdGroup.put("host", "http://r.tapit.com/adrequest.php");
                wapChannelAdGroup.put("ignorebaseAndsrctag", "true");
                wapChannelAdGroup.put("dst", "2");
                wapChannelAdGroup.put("account_segment", "7");
                break;

            }
            case IX: {

                wapChannelAdGroup.put("name", "IX");
                wapChannelAdGroup.put("advertiser_id", "f55c9d46d7704f8789015a64153a7012");
                wapChannelAdGroup.put("dst", "8");
                wapChannelAdGroup.put("account_segment", "11");
                break;
            }
            case RTBD2: {
                wapChannelAdGroup.put("name", "rtb2");
                wapChannelAdGroup.put("advertiser_id", "9ab79acef8764348a36a07b05fc3ee64");
                wapChannelAdGroup.put("dst", "6");
                wapChannelAdGroup.put("account_segment", "6");
                break;
            }
            case NEXAGE: {

                wapChannelAdGroup.put("name", "nexage");
                wapChannelAdGroup.put("advertiser_id", "72cd0cbf-a1eb-4905-b6d5-de28acb72dc8");
                wapChannelAdGroup.put("dst", "2");
                wapChannelAdGroup.put("account_segment", "7");
                break;
            }
            case GOOGLEADX: {
                wapChannelAdGroup.put("name", "googleadx");
                wapChannelAdGroup.put("advertiser_id", "ab8677d5210241a091da3e0f94a1b425");
                break;
            }
            case DMG: {
                wapChannelAdGroup.put("name", "dmg");
                wapChannelAdGroup.put("advertiser_id", "07edcdfff56b4442a8a8af5f2e5c5672");
                break;
            }
            case AMOAD: {
                wapChannelAdGroup.put("name", "amoad");
                wapChannelAdGroup.put("advertiser_id", "0d7eab3ab68c4afab94ad897ec080ef6");
                break;
            }
            default:
                break;
        }
        return wapChannelAdGroup;
    }
}
