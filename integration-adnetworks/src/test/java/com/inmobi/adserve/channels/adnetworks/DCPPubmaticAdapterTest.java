package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.pubmatic.DCPPubmaticAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;


public class DCPPubmaticAdapterTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private DCPPubmaticAdNetwork dcpPubmaticAdnetwork;
    private final String pubmaticHost = "http://showads.pubmatic.com/AdServer/AdServerServlet";
    private final String pubmaticStatus = "on";
    private final String pubmaticAdvId = "pubmaticadv1";
    private final String pubId = "2685";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("pubmatic.pubId")).andReturn(pubId).anyTimes();
        expect(mockConfig.getString("pubmatic.host")).andReturn(pubmaticHost).anyTimes();
        expect(mockConfig.getString("pubmatic.status")).andReturn(pubmaticStatus).anyTimes();
        expect(mockConfig.getString("pubmatic.advertiserId")).andReturn(pubmaticAdvId).anyTimes();
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
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor9);
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
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpPubmaticAdnetwork = new DCPPubmaticAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void DCPPubmaticAdapterTestConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("iphone");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"1231\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper),
                true);
    }

    @Test
    public void testDCPPubmaticConfigureParametersBlankIP() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setSource("iphone");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"1231\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper),
                false);
    }

    @Test
    public void testpubmaticConfigureParametersBlankExtKey() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setSource("iphone");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"1231\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper),
                false);
    }

    @Test
    public void testDCPPubmaticConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSource("iphone");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper),
                false);
    }

    @Test
    public void testDCPPubmaticRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("iphone");
        final String externalKey = "33327";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"1231\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            dcpPubmaticAdnetwork.makeAsyncRequest();
            final String actualUrl = dcpPubmaticAdnetwork.getRequestUrl();
            assertEquals(pubmaticHost, actualUrl);
        }
    }

    @Test
    public void testDCPPubmaticRequestUriSegmentCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("iphone");
        final String externalKey = "33327";
        sasParams.setCategories(Arrays.asList(new Long[] {7l, 8l}));
        final Long[] segmentCategories = new Long[] {13l, 15l};
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=37.4429,-122.1514&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=48&pageURL=00000000-0000-0000-0000-000000000000&keywords=Education%2CEntertainment&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticRequestUriSegmentRONSiteCategories() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("iphone");
        final String externalKey = "33327";
        sasParams.setCategories(Arrays.asList(new Long[] {7l, 8l}));
        final Long[] segmentCategories = new Long[] {1l};
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                        new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=37.4429,-122.1514&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=48&pageURL=00000000-0000-0000-0000-000000000000&keywords=Education%2CEntertainment&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticRequestParams() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        sasParams.setSource("iphone");
        final String externalKey = "33327";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=37.4429,-122.1514&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=48&pageURL=00000000-0000-0000-0000-000000000000&keywords=miscellenous&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticRequestParamsWithCountry() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        sasParams.setSource("iphone");
        sasParams.setCountryCode("US");
        final String externalKey = "33327";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=37.4429,-122.1514&country=USA&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=48&pageURL=00000000-0000-0000-0000-000000000000&keywords=miscellenous&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticRequestParamsBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(",-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        sasParams.setSource("android");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(
                        AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null, 0, null, null,
                                true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                                false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                        "{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=,-122.1514&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=50&pageURL=00000000-0000-0000-0000-000000000000&keywords=miscellenous&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticRequestParamsBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        sasParams.setSource("wap");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"9\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 9, repositoryHelper)) {
            final String actualParams = dcpPubmaticAdnetwork.getRequestParams();
            final String expectedParams =
                    "timezone=0&frameName=test&inIframe=1&adVisibility=0&adPosition=-1x-1&operId=201&pubId=2685&adId=36844&siteId=33327&loc=37.4429,-122.1514&udid=202cb962ac59075b964b07152d234b70&kadwidth=320&kadheight=48&pageURL=00000000-0000-0000-0000-000000000000&keywords=miscellenous&kltstamp=";
            assertEquals(expectedParams, actualParams.substring(0, actualParams.lastIndexOf("kltstamp=") + 9));
        }
    }

    @Test
    public void testDCPPubmaticParseResponseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "33327";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(
                        AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null, 0, null, null,
                                true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                                false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                        "{\"4\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "{\"PubMatic_Bid\":{\"ecpm\":1.000000,\"creative_tag\":\"<a target=\\\"_blank\\\" href=\\\"http://pubmatic.com\\\">\n<div style=\\\"left: 0px; top: 0px; width: 320px;\nheight: 50px; background-color:#003366; color:#ffffff; text-align:center;\\\" >\n<h4> PubMatic 320x50 Test Ad </h4>\n</div>\n</a>\",\"tracking_url\":\"http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED\",\"click_tracking_url\":\"http://track.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=3&clickData=aHR0cDovL3RyYWNrLnB1Ym1hdGljLmNvbS9BZFNlcnZlci9BZERpc3BsYXlUcmFja2VyU2VydmxldD9vcGVySWQ9MyZwdWJJZD0yNjg1JnNpdGVJZD0zMDcxMyZhZElkPTI4NjUyJmthZHNpemVpZD0yMDYxNTg0MzAyMzgmaW5kaXJlY3RBZElkPTQ3OTY1JmFkU2VydmVySWQ9MTIzOCZtb2JmbGFnPTEmaXNtb2JpbGVhcHA9MSZQdWJjbGt1cmw9&url=\",\"autorefresh_time\":0,\"prefetch_data\":0}}";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a target=\"_blank\" href=\"http://pubmatic.com\"> <div style=\"left: 0px; top: 0px; width: 320px; height: 50px; background-color:#003366; color:#ffffff; text-align:center;\" > <h4> PubMatic 320x50 Test Ad </h4> </div> </a><img src='http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPubmaticAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPubmaticParseResponseAdWAP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "33327";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(
                        AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null, 0, null, null,
                                true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                                false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                        "{\"4\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "{\"PubMatic_Bid\":{\"ecpm\":1.000000,\"creative_tag\":\"<a target=\\\"_blank\\\" href=\\\"http://pubmatic.com\\\">\n<div style=\\\"left: 0px; top: 0px; width: 320px;\nheight: 50px; background-color:#003366; color:#ffffff; text-align:center;\\\" >\n<h4> PubMatic 320x50 Test Ad </h4>\n</div>\n</a>\",\"tracking_url\":\"http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED\",\"click_tracking_url\":\"http://track.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=3&clickData=aHR0cDovL3RyYWNrLnB1Ym1hdGljLmNvbS9BZFNlcnZlci9BZERpc3BsYXlUcmFja2VyU2VydmxldD9vcGVySWQ9MyZwdWJJZD0yNjg1JnNpdGVJZD0zMDcxMyZhZElkPTI4NjUyJmthZHNpemVpZD0yMDYxNTg0MzAyMzgmaW5kaXJlY3RBZElkPTQ3OTY1JmFkU2VydmVySWQ9MTIzOCZtb2JmbGFnPTEmaXNtb2JpbGVhcHA9MSZQdWJjbGt1cmw9&url=\",\"autorefresh_time\":0,\"prefetch_data\":0}}";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a target=\"_blank\" href=\"http://pubmatic.com\"> <div style=\"left: 0px; top: 0px; width: 320px; height: 50px; background-color:#003366; color:#ffffff; text-align:center;\" > <h4> PubMatic 320x50 Test Ad </h4> </div> </a><img src='http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPubmaticAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPubmaticParseResponseAdApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        final String externalKey = "33327";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(
                        AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null, 0, null, null,
                                true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                                false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                        "{\"4\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "{\"PubMatic_Bid\":{\"ecpm\":1.000000,\"creative_tag\":\"<a target=\\\"_blank\\\" href=\\\"http://pubmatic.com\\\">\n<div style=\\\"left: 0px; top: 0px; width: 320px;\nheight: 50px; background-color:#003366; color:#ffffff; text-align:center;\\\" >\n<h4> PubMatic 320x50 Test Ad </h4>\n</div>\n</a>\",\"tracking_url\":\"http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED\",\"click_tracking_url\":\"http://track.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=3&clickData=aHR0cDovL3RyYWNrLnB1Ym1hdGljLmNvbS9BZFNlcnZlci9BZERpc3BsYXlUcmFja2VyU2VydmxldD9vcGVySWQ9MyZwdWJJZD0yNjg1JnNpdGVJZD0zMDcxMyZhZElkPTI4NjUyJmthZHNpemVpZD0yMDYxNTg0MzAyMzgmaW5kaXJlY3RBZElkPTQ3OTY1JmFkU2VydmVySWQ9MTIzOCZtb2JmbGFnPTEmaXNtb2JpbGVhcHA9MSZQdWJjbGt1cmw9&url=\",\"autorefresh_time\":0,\"prefetch_data\":0}}";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a target=\"_blank\" href=\"http://pubmatic.com\"> <div style=\"left: 0px; top: 0px; width: 320px; height: 50px; background-color:#003366; color:#ffffff; text-align:center;\" > <h4> PubMatic 320x50 Test Ad </h4> </div> </a><img src='http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPubmaticAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPubmaticParseResponseAdAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a372");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "33327";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(
                        AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null, 0, null, null,
                                true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                                false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                        "{\"4\":\"36844\"}"), new ArrayList<Integer>(), 0.0d, null, null, 32,
                                new Integer[] {0}));
        dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);

        final String response =
                "{\"PubMatic_Bid\":{\"ecpm\":1.000000,\"creative_tag\":\"<a target=\\\"_blank\\\" href=\\\"http://pubmatic.com\\\">\n<div style=\\\"left: 0px; top: 0px; width: 320px;\nheight: 50px; background-color:#003366; color:#ffffff; text-align:center;\\\" >\n<h4> PubMatic 320x50 Test Ad </h4>\n</div>\n</a>\",\"tracking_url\":\"http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED\",\"click_tracking_url\":\"http://track.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=3&clickData=aHR0cDovL3RyYWNrLnB1Ym1hdGljLmNvbS9BZFNlcnZlci9BZERpc3BsYXlUcmFja2VyU2VydmxldD9vcGVySWQ9MyZwdWJJZD0yNjg1JnNpdGVJZD0zMDcxMyZhZElkPTI4NjUyJmthZHNpemVpZD0yMDYxNTg0MzAyMzgmaW5kaXJlY3RBZElkPTQ3OTY1JmFkU2VydmVySWQ9MTIzOCZtb2JmbGFnPTEmaXNtb2JpbGVhcHA9MSZQdWJjbGt1cmw9&url=\",\"autorefresh_time\":0,\"prefetch_data\":0}}";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a target=\"_blank\" href=\"http://pubmatic.com\"> <div style=\"left: 0px; top: 0px; width: 320px; height: 50px; background-color:#003366; color:#ffffff; text-align:center;\" > <h4> PubMatic 320x50 Test Ad </h4> </div> </a><img src='http://aktrack.pubmatic.com/AdServer/AdDisplayTrackerServlet?operId=201&pubId=2685&siteId=30713&adId=28652&adServerId=1238&kefact=1.000000&kaxefact=1.000000&kadNetFrequecy=0&kadwidth=300&kadheight=50&kadsizeid=30&kltstamp=1360156017&indirectAdId=47965&adServerOptimizerId=1&ranreq=0.15638034541708&kpbmtpfact=0.000000&mobflag=1&ismobileapp=1&pageURL=NOPAGEURLSPECIFIED' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpPubmaticAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPubmaticParseNoAd() throws Exception {
        final String response = "";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPPubmaticParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpPubmaticAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpPubmaticAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPPubmaticGetId() throws Exception {
        assertEquals(dcpPubmaticAdnetwork.getId(), "pubmaticadv1");
    }

    @Test
    public void testDCPPubmaticGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "33327";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(pubmaticAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpPubmaticAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper);
        assertEquals(dcpPubmaticAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPPubmaticGetName() throws Exception {
        assertEquals(dcpPubmaticAdnetwork.getName(), "pubmatic");
    }

    @Test
    public void testDCPPubmaticIsClickUrlReq() throws Exception {
        assertEquals(dcpPubmaticAdnetwork.isClickUrlRequired(), false);
    }
}
