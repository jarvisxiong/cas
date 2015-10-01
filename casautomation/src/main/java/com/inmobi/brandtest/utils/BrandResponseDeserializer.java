package com.inmobi.brandtest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;

import com.inmobi.adserve.adpool.AdInfo;
import com.inmobi.adserve.adpool.AdPoolResponse;
import com.inmobi.adserve.adpool.CreativeInfo;
import com.inmobi.adserve.adpool.NetworkAdPoolImpressionInfo;
import com.inmobi.adserve.adpool.NetworkAdPoolRenderUnitInfo;
import com.inmobi.adserve.adpool.NetworkAdPoolRequestInfo;
import com.inmobi.types.GUID;

public class BrandResponseDeserializer {

    private static NetworkAdPoolImpressionInfo networkAdPoolImpressionInfo;
    // private static AdPoolResponse adPoolResponse;
    private static List<NetworkAdPoolImpressionInfo> listOfAllAds = new ArrayList<>();
    private int totalNoOfAds;
    private NetworkAdPoolRequestInfo networkAdPoolRequestInfo;


    public BrandResponseDeserializer(final AdPoolResponse adPoolResponse) {
        // BrandResponseDeserializer.adPoolResponse = adPoolResponse;

        if (null != adPoolResponse.ads && !adPoolResponse.ads.isEmpty()) {
            for (final AdInfo adInfo : adPoolResponse.ads) {

                // deserializing poolSpecificInfo
                final byte[] respBytes = adPoolResponse.getAds().get(0).getPoolSpecificInfo();
                final TDeserializer tDeserializer = new TDeserializer(new TBinaryProtocol.Factory());
                final NetworkAdPoolRenderUnitInfo networkAdPoolRenderUnitInfo = new NetworkAdPoolRenderUnitInfo();
                try {
                    tDeserializer.deserialize(networkAdPoolRenderUnitInfo, respBytes);
                } catch (final TException e) {
                    System.out.println("* * * Error while deserializing AdPoolResponse from Brand Adpool ! * * * ");
                    e.printStackTrace();
                }
                final Map<GUID, NetworkAdPoolImpressionInfo> adPoolImpressionInfoMap =
                        networkAdPoolRenderUnitInfo.adPoolImpressionInfoMap;

                // setting creativeinfo and networkadpoolimpressioninfo for the adwrapper
                if (null != adInfo.creativeInfoList && !adInfo.creativeInfoList.isEmpty()) {
                    for (final CreativeInfo creativeInfo : adInfo.creativeInfoList) {
                        if (null != creativeInfo.impressionId) {
                            networkAdPoolImpressionInfo = adPoolImpressionInfoMap.get(creativeInfo.impressionId);
                        }
                        listOfAllAds.add(networkAdPoolImpressionInfo);
                    }
                }
            }


        }
    }


    public NetworkAdPoolImpressionInfo getNetworkAdPoolImpressionInfo() {
        if (networkAdPoolImpressionInfo != null) {
            System.out.println(networkAdPoolImpressionInfo.toString());
        }
        return networkAdPoolImpressionInfo;
    }

    public List<NetworkAdPoolImpressionInfo> getListOfAllAds() {
        System.out.println(listOfAllAds);
        return listOfAllAds;
    }
}
