package com.inmobi.adserve.channels.adnetworks;

// Created by Dhanasekaran K P on 30/7/14.
//

import com.inmobi.adserve.channels.adnetworks.madhouse.DCPMadHouseAdNetwork;
import com.inmobi.adserve.channels.api.*;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
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
        private final String madhouseAdvId = "c2f394befcff3f03";

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
            sasParams.setSlot((short) 21);

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
        sasParams.setSlot((short) 21);

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
        sasParams.setSlot((short) 21);

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
        sasParams.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.2; nl-nl; GT-I9300 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        sasParams.setSlot((short) 21);
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
        String expectedUrl = "http://beta.api.main-servers.com/napi/90002830?adtype=2&os=1&oid=202cb962ac59075b964b07152d234b70&idfa=23e2ewq445545&width=480&height=75&lat=37.4429&lon=-122.1514&ip=206.29.182.240&ua=Mozilla%252F5.0%2B%2528Linux%253B%2BU%253B%2BAndroid%2B4.1.2%253B%2Bnl-nl%253B%2BGT-I9300%2BBuild%252FJZO54K%2529%2BAppleWebKit%252F534.30%2B%2528KHTML%252C%2Blike%2BGecko%2529%2BVersion%252F4.0%2BMobile%2BSafari%252F534.30&pcat=5&pid=c2f394befcff3f03";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testDCPMadHouseParseResponse() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();

        casInternalRequestParameters.blockedCategories = new ArrayList<Long>(Arrays.asList(new Long[] {50l, 51l}));
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.uid = "202cb962ac59075b964b07152d234b70";
        casInternalRequestParameters.uidIFA = "23e2ewq445545";
        casInternalRequestParameters.uidADT = "0";
        casInternalRequestParameters.uidIDUS1 = "202cb962ac59075b964b07152d234b70";

        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla/5.0 (Linux; U; Android 4.1.2; nl-nl; GT-I9300 Build/JZO54K) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        sasParams.setSource("APP");
        sasParams.setOsId(SASRequestParameters.HandSetOS.iOS.getValue());
        sasParams.setSlot((short) 21);
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

        // Response to be parsed.
        String response = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
            + "<smartmad>\n"
            + " <adcode>\n"
            + "  <adspaceid>90002830</adspaceid>\n"
            + "  <returncode>200</returncode>\n"
            + "  <adtype>2</adtype>\n"
            + "  <thclkurl>http%3A%2F%2Fservbeta02.dsp.main-servers.com%2Fcli%2Fgijei29e35881bmofk30031032l78occb558.NDQ1NTQ1..MjNlMmV3cQ.MjNlMmV3cTQ0NTU0NQ.MjNlMmV3cTQ0NTU0NQ--1bmoey-%2Fapi1.0.5005%2F80000026\n"
            + "  </thclkurl>\n"
            + "  <adhtml><![CDATA[<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0 user-scalable=no\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>SMAdUnit</title></head><body style=\"margin:0px auto; padding:0; overflow:hidden; background-color:transparent;\"><p>\n"
            + "<a href=\"http://betaa.appsrv1.main-servers.com/clitp/jf73yb3755895uw5m9433231066lfjl78occb5.........-1-1hl2mm/napi1.0.0000/90002830\"><img src=\"http://sm2.mdn2.net/dspbeta02/gijei29e35881bmofk30031032l78occb558.NDQ1NTQ1..MjNlMmV3cQ.MjNlMmV3cTQ0NTU0NQ.MjNlMmV3cTQ0NTU0NQ--1bmoey-/52/33/58.jpg\" /></a>\n"
            + "<img src=\"http://servbeta02.dsp.main-servers.com/ben/gijei29e35881bmofk30031032l78occb558.NDQ1NTQ1..MjNlMmV3cQ.MjNlMmV3cTQ0NTU0NQ.MjNlMmV3cTQ0NTU0NQ--1bmoey-/api1.0.5005/80000026\" width=\"0\" height=\"0\" border=\"0\" /></p>\n"
            + "</body></html>]]></adhtml>\n"
            + " </adcode>\n"
            + "</smartmad>";

        dcpMadhouseAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        dcpMadhouseAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpMadhouseAdNetwork.getHttpResponseStatusCode(), 200);

        String expectedResponse = "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"initial-scale=1.0 user-scalable=no\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/><title>SMAdUnit</title></head><body style=\"margin:0px auto; padding:0; overflow:hidden; background-color:transparent;\"><p>\n"
            + "<a href=\"http://betaa.appsrv1.main-servers.com/clitp/jf73yb3755895uw5m9433231066lfjl78occb5.........-1-1hl2mm/napi1.0.0000/90002830\"><img src=\"http://sm2.mdn2.net/dspbeta02/gijei29e35881bmofk30031032l78occb558.NDQ1NTQ1..MjNlMmV3cQ.MjNlMmV3cTQ0NTU0NQ.MjNlMmV3cTQ0NTU0NQ--1bmoey-/52/33/58.jpg\" /></a>\n"
            + "<img src=\"http://servbeta02.dsp.main-servers.com/ben/gijei29e35881bmofk30031032l78occb558.NDQ1NTQ1..MjNlMmV3cQ.MjNlMmV3cTQ0NTU0NQ.MjNlMmV3cTQ0NTU0NQ--1bmoey-/api1.0.5005/80000026\" width=\"0\" height=\"0\" border=\"0\" /></p>\n"
            + "</body></html><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        String actualResponse = dcpMadhouseAdNetwork.getHttpResponseContent();

        assertEquals(actualResponse, expectedResponse);
    }
}