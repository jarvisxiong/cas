package com.inmobi.adserve.channels.adnetworks;

// Created by Dhanasekaran K P on 30/7/14.
//

import com.inmobi.adserve.channels.adnetworks.madhouse.DCPMadHouseAdNetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import io.netty.channel.Channel;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class DCPMadHouseAdNetworkTest extends TestCase{
        private Configuration mockConfig = null;
        private final String debug = "debug";
        private final String loggerConf = "/tmp/channel-server.properties";

        private DCPMadHouseAdNetwork dcpMadhouseAdNetwork;
        private final String madhouseHost = "beta.api.main-servers.com/napi/90002830";
        private final String madhouseStatus = "on";
        private final String madhouseAdvId = "madhouseadv1";

        public void prepareMockConfig() {
            mockConfig = createMock(Configuration.class);
            expect(mockConfig.getString("madhouse.host")).andReturn(madhouseHost).anyTimes();
            expect(mockConfig.getString("madhouse.status")).andReturn(madhouseStatus).anyTimes();
            expect(mockConfig.getString("madhouse.advertiserId")).andReturn(madhouseAdvId).anyTimes();
            expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
            expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
            expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
            replay(mockConfig);
        }

        @Override
        public void setUp() throws Exception {
            File f;
            f = new File(loggerConf);
            if (!f.exists()) {
                f.createNewFile();
            }
            Formatter.init();
            Channel serverChannel = createMock(Channel.class);
            HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
            prepareMockConfig();
            SlotSizeMapping.init();
            dcpMadhouseAdNetwork = new DCPMadHouseAdNetwork(mockConfig, null, base, serverChannel);
        }

        @Test
        public void testDCPMadhouseConfigureParameters() throws JSONException {
            SASRequestParameters sasParams = new SASRequestParameters();
            CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

            casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] {50l, 51l}));
            casInternalRequestParameters.latLong = "37.4429,-122.1514";
            casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
            casInternalRequestParameters.uidIFA = "23e2ewq445545";
            casInternalRequestParameters.uidADT = "0";
            casInternalRequestParameters.uidIDUS1 = "202cb962ac59075b964b07152d234b70";

            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
            sasParams.setSource("APP");
            sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
            sasParams.setSlot((short) 10);

            // Set Categories.
            List categories = new ArrayList();
            categories.add(1L);
            categories.add(2L);
            categories.add(3L);
            sasParams.setCategories(categories);

            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                madhouseAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 0));

            assertTrue(dcpMadhouseAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
        }

    @Test
    public void testDCPMadhouseConfigureParametersBlankIP() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] {50l, 51l}));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        casInternalRequestParameters.uidIDUS1 = "202cb962ac59075b964b07152d234b70";

        sasParams.setRemoteHostIp(null); // Blank IP.
        sasParams.setUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        sasParams.setSlot((short) 10);

        // Set Categories.
        List categories = new ArrayList();
        categories.add(1L);
        categories.add(2L);
        categories.add(3L);
        sasParams.setCategories(categories);

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            madhouseAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        assertFalse(dcpMadhouseAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPMadhouseConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] {50l, 51l}));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        casInternalRequestParameters.uidIDUS1 = "202cb962ac59075b964b07152d234b70";

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(""); // Blank UA.
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        sasParams.setSlot((short) 10);

        // Set Categories.
        List categories = new ArrayList();
        categories.add(1L);
        categories.add(2L);
        categories.add(3L);
        sasParams.setCategories(categories);

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            madhouseAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        assertTrue(dcpMadhouseAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPMadhouseRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] {50l, 51l}));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        casInternalRequestParameters.uidIDUS1 = "202cb962ac59075b964b07152d234b70";

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0)");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        sasParams.setSlot((short) 10);
        sasParams.setSiteIncId(1000000007L);

        // Set Categories.
        List categories = new ArrayList();
        categories.add(1L);
        categories.add(2L);
        categories.add(3L);
        sasParams.setCategories(categories);

        String externalKey = "4246";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            madhouseAdvId, "adgroupid", null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
            + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
            + "/9cddca11?beacon=true";

        dcpMadhouseAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);

        String blindedSiteId = (new UUID(1000000009L, 1000000007L)).toString();
        String actualUrl = dcpMadhouseAdNetwork.getRequestUri().toString();

        // Compare the expected URL with actual URL.
        String expectedUrl = "http://beta.api.main-servers.com/napi/90002830?adtype=5&os=iOS&oid=202cb962ac59075b964b07152d234b70&idfa=23e2ewq445545&width=300&height=250&lat=37.4429&lon=-122.1514&ip=206.29.182.240&ua=Mozilla%252F5.0%2B%2528compatible%253B%2BMSIE%2B9.0%253B%2BWindows%2BNT%2B6.1%253B%2BTrident%252F5.0%2529&pcat&pid=madhouseadv1";
        assertEquals(expectedUrl, actualUrl);
    }
}
