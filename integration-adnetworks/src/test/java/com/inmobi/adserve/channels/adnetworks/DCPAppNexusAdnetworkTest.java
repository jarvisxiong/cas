package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.appnexus.DCPAppNexusAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;


public class DCPAppNexusAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private DCPAppNexusAdnetwork dcpAppNexusAdNetwork;
    private final String appNexusHost = "http://mobile.adnxs.com/mob?psa=0&format=json";
    private final String appNexusStatus = "on";
    private final String defintiAdvId = "appNexusAdv1";
    private final String appNexusTest = "1";
    private RepositoryHelper repositoryHelper;


    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("appnexus.host")).andReturn(appNexusHost).anyTimes();
        expect(mockConfig.getString("appnexus.status")).andReturn(appNexusStatus).anyTimes();
        expect(mockConfig.getString("appnexus.test")).andReturn(appNexusTest).anyTimes();
        expect(mockConfig.getString("appnexus.advertiserId")).andReturn(defintiAdvId).anyTimes();
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
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpAppNexusAdNetwork = new DCPAppNexusAdnetwork(mockConfig, null, base, serverChannel);
        dcpAppNexusAdNetwork.setName("appnexus");
        
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpAppNexusAdNetwork.setHost(appNexusHost);
    }

    /*
     * @Test public void testDCPAppNexusConfigureParameters() { SASRequestParameters sasParams = new
     * SASRequestParameters(); CasInternalRequestParameters casInternalRequestParameters = new
     * CasInternalRequestParameters(); sasParams.setRemoteHostIp("206.29.182.240"); sasParams.setSlot("4");
     * sasParams.setSiteContentType(ContentType.PERFORMANCE); sasParams .setUserAgent(
     * "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334"
     * ); casInternalRequestParameters.latLong = "37.4429,-122.1514"; String clurl =
     * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"
     * ; sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9"); String externalKey = "f6wqjq1r5v";
     * ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(
     * defintiAdvId, null, null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
     * null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<>(), 0.0d,
     * null, null, 32, new Integer[] {0})); assertTrue(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters,
     * entity, clurl, null)); }
     */

    @Test
    public void testDCPAppNexusConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
                null, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAppNexusConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
                null, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAppNexusConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
                null, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAppnexusRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "240";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpAppNexusAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://mobile.adnxs.com/mob?psa=0&format=json&ip=206.29.182.240&ua=Mozilla&id=240&st=mobile_web&size=300x50&loc=37.4429%2C-122.1514";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    @Test
    public void testDCPAppNexusParseResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 4, repositoryHelper);
        //final String response =
        //        "{ \"status\": \"ok\", \"ads\": [{\"type\":\"banner\", \"width\":300, \"height\":50, \"content\": \"<script type=\"text/javascript\">document.write('<a href=\"http://sin1.g.adnxs.com/click?AAAAAAAA4D8fhetRuB7dPwAAAAAAAPA_H4XrUbge3T8AAAAAAADgP5D-cuXdwZBvebWPt1919EczVUxSAAAAAHHIGwB2AgAAzgIAAAIAAAA_nHoASi8EAAYAAQBVU0QAVVNEACwBMgAgCAAAK4kABQUCAQIAAAAA1h2OcAAAAAA./cnd=%21mwarOwje030Qv7jqAxjK3hAgAA../clickenc=http%3A%2F%2Fcnct.tlvmedia.com%2Fckl.php%3Fs%3D1%26c%3Dsin1CPnqvrz7q536RxACGJD9y6veu7DIbyIOMjA2LjI5LjE4Mi4yNDAoAQ..%26mx%3Danx%26t%3D1%26d%3D93478%26r%3Dhttp%253A%252F%252Ftracking1.aleadpay.com%252FAdTag%252FClick%252FY21waWQ9MTc2NTEmdHNpZD01ODAg%253Fdp%253D%2524CLICKID%2524\" target=\"_blank\"><img width=\"300\" height=\"50\" style=\"border-style: none\" src=\"http://cdn.adnxs.com/p/01/db/74/d1/01db74d156327ffbb20463fcd3bda52c.gif\"/></a>');</script>\"}] }";
        // dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        // assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 200);
        // assertEquals(
        // "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><script type=\"text/javascript\">document.write('<a href=\"http://sin1.g.adnxs.com/click?AAAAAAAA4D8fhetRuB7dPwAAAAAAAPA_H4XrUbge3T8AAAAAAADgP5D-cuXdwZBvebWPt1919EczVUxSAAAAAHHIGwB2AgAAzgIAAAIAAAA_nHoASi8EAAYAAQBVU0QAVVNEACwBMgAgCAAAK4kABQUCAQIAAAAA1h2OcAAAAAA./cnd=%21mwarOwje030Qv7jqAxjK3hAgAA../clickenc=http%3A%2F%2Fcnct.tlvmedia.com%2Fckl.php%3Fs%3D1%26c%3Dsin1CPnqvrz7q536RxACGJD9y6veu7DIbyIOMjA2LjI5LjE4Mi4yNDAoAQ..%26mx%3Danx%26t%3D1%26d%3D93478%26r%3Dhttp%253A%252F%252Ftracking1.aleadpay.com%252FAdTag%252FClick%252FY21waWQ9MTc2NTEmdHNpZD01ODAg%253Fdp%253D%2524CLICKID%2524\" target=\"_blank\"><img width=\"300\" height=\"50\" style=\"border-style: none\" src=\"http://cdn.adnxs.com/p/01/db/74/d1/01db74d156327ffbb20463fcd3bda52c.gif\"/></a>');</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
        // dcpAppNexusAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAppNexusParseNoAd() throws Exception {
        final String response = "{\"status\": \"ok\",\"ads\": []}";
        dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAppNexusParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpAppNexusAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpAppNexusAdNetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPAppNexusGetId() throws Exception {
        assertEquals(dcpAppNexusAdNetwork.getId(), "appNexusAdv1");
    }

    @Test
    public void testDCPAppNexusGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(defintiAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpAppNexusAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 4, repositoryHelper);
        assertEquals(dcpAppNexusAdNetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPAppNexusGetName() throws Exception {
        assertEquals(dcpAppNexusAdNetwork.getName(), "appnexusDCP");
    }

    @Test
    public void testDCPAppNexusIsClickUrlReq() throws Exception {
        assertTrue(dcpAppNexusAdNetwork.isClickUrlRequired());
    }
}
