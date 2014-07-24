package com.inmobi.adserve.channels.adnetworks;

//
// Created by Dhanasekaran K P on 17/7/14.
//

import com.inmobi.adserve.channels.adnetworks.miaozhen.DCPMiaozhenAdNetwork;
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
import java.util.UUID;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class DCPMiaozhenAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private DCPMiaozhenAdNetwork dcpMiaozhenAdNetwork;
    private final String miaozhenHost = "http://s.x.cn.miaozhen.com/bx?v=0";
    private final String miaozhenStatus = "on";
    private final String miaozhenAdvId = "miaozhenadv1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("miaozhen.host")).andReturn(miaozhenHost).anyTimes();
        expect(mockConfig.getString("miaozhen.status")).andReturn(miaozhenStatus).anyTimes();
        expect(mockConfig.getString("miaozhen.advertiserId")).andReturn(miaozhenAdvId).anyTimes();
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
        dcpMiaozhenAdNetwork = new DCPMiaozhenAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPMiaozhenConfigureParameters() throws JSONException {
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
            miaozhenAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        assertTrue(dcpMiaozhenAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPMiaozhenConfigureParametersBlankIP() {
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
            miaozhenAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        assertFalse(dcpMiaozhenAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPMiaozhenConfigureParametersBlankUA() {
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
            miaozhenAdvId, "adgroupid", null, null, 0, null, null, true, true, null, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        assertTrue(dcpMiaozhenAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null));
    }

    @Test
    public void testDCPMiaozhenRequestUri() throws Exception {
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
        sasParams.setSiteIncId(12314321432L);

        String externalKey = "4246";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
            miaozhenAdvId, "adgroupid", null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
            null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
            new ArrayList<Integer>(), 0.0d, null, null, 0));

        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
            + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
            + "/9cddca11?beacon=true";

        dcpMiaozhenAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);

        String blindedSiteId = (new UUID(1234L, 12314321432L)).toString();
        String actualUrl = dcpMiaozhenAdNetwork.getRequestUri().toString();

        // Compare the expected URL with actual URL after eliminating its last parameter.
        String expectedUrl = "http://s.x.cn.miaozhen.com/bx?v=0&m_app=com.inmobi-exchange.00000000-0000-0000-0000-0002ddfda218&m_ua=Mozilla%2F5.0+%28compatible%3B+MSIE+9.0%3B+Windows+NT+6.1%3B+Trident%2F5.0%29&m_ip=206.29.182.240&m_adw=300&m_adh=250&m_pos=37.4429%2C-122.1514&m_net=2&m_os=iOS&m0=202cb962ac59075b964b07152d234b70&m5=23e2ewq445545&m_int=1&l=4246";
        actualUrl = actualUrl.substring(0, actualUrl.lastIndexOf('&'));

        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testDCPMiaozhenParseResponse() throws Exception {
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

        String externalKey = "4246";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
                miaozhenAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, 0, true, null,
                null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 0));

        String beaconUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
            + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
            + "/9cddca11?beacon=true";

        // Response to be parsed.
        String response = "{\n"
            + "    \"pid\": \"4246\",\n"
            + "    \"type\": \"I\",\n"
            + "    \"etype\": \"N\",\n"
            + "    \"src\": \"http://i.x.stfile.com:80/5/8e/58e4effc1b571d82e032687b3075c674.jpg\",\n"
            + "    \"adw\": 320,\n"
            + "    \"adh\": 50,\n"
            + "    \"pm\": {\n"
            + "        \"0\": [\n"
            + "            \"http://g.x.cn.miaozhen.com/x.gif?bp=2&gg=0&ci=null&bf=0&icp=0&l=4246&m=96&bb=702&br=u7BpJ4gkncVQlENPGsISkCikzbMULFbrzk43oTzoLjYzFFDoF3_3Vh51f7kx3H6u_RZzH8znLg1B3h1h2Q3vSQZ0MYQ909e1A_3Qn4_sIWwZ5y717EfQdwY5-I0wRsSs&be=1640\"\n"
            + "        ]\n"
            + "    },\n"
            + "    \"cm\": [],\n"
            + "    \"ldp\": \"http://e.x.cn.miaozhen.com/r.gif?bp=2&ci=null&bf=0&l=4246&m=96&bb=702&br=u7BpJ4gkncVQlENPGsISkCikzbMULFbrzk43oTzoLjYzFFDoF3_3Vh51f7kx3H6u_RZzH8znLg1B3h1h2Q3vSQZ0MYQ909e1A_3Qn4_sIWwZ5y717EfQdwY5-I0wRsSs&be=1640&bo=15BF475B7E18&o=http%3A%2F%2Fwww.miaozhen.com\",\n"
            + "    \"meta\": {\n"
            + "        \"duration\": 0,\n"
            + "        \"check\": 1\n"
            + "    }\n"
            + "}";

        dcpMiaozhenAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl);
        dcpMiaozhenAdNetwork.parseResponse(response, HttpResponseStatus.OK);

        // Check URI and Response.
        String testURI = dcpMiaozhenAdNetwork.getRequestUri().toString();
        String testResponse = dcpMiaozhenAdNetwork.getHttpResponseContent();
        int i = 1;
    }

    @Test
    public void testDCPMiaozhenParseNoAd() throws Exception {
        String response = "";
        dcpMiaozhenAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMiaozhenAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPMiaozhenParseEmptyResponseCode() throws Exception {
        String response = "";
        dcpMiaozhenAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpMiaozhenAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpMiaozhenAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMiaozhenGetName() throws Exception {
        assertEquals("miaozhen", dcpMiaozhenAdNetwork.getName());
    }

    @Test
    public void testDCPMiaozhenIsClickUrlReq() throws Exception {
        assertTrue(dcpMiaozhenAdNetwork.isClickUrlRequired());
    }
}
