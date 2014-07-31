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
import java.util.UUID;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class DCPMadHouseAdNetworkTest extends TestCase{
        private Configuration mockConfig = null;
        private final String debug = "debug";
        private final String loggerConf = "/tmp/channel-server.properties";

        private DCPMadHouseAdNetwork dcpMadhouseAdNetwork;
        private final String madhouseHost = "http://s.x.cn.madhouse.com/bx?v=0";
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

        // Compare the expected URL with actual URL after eliminating its last parameter.
        String expectedUrl = "http://s.x.cn.madhouse.com/bx?v=0&m_app=com.inmobi-exchange.00000000-0000-0000-0000-00003b9aca07&m_ua=Mozilla%2F5.0+%28compatible%3B+MSIE+9.0%3B+Windows+NT+6.1%3B+Trident%2F5.0%29&m_ip=206.29.182.240&m_adw=300&m_adh=250&m_pos=37.4429%2C-122.1514&m_net=2&m_os=iOS&m0=202cb962ac59075b964b07152d234b70&m5=23e2ewq445545&m_int=1&l=4246";
        actualUrl = actualUrl.substring(0, actualUrl.lastIndexOf('&'));

        assertEquals(expectedUrl, actualUrl);
    }
}
