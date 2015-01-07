package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Created by thushara on 30/12/14.
 */
public class DCPAmoAdAdnetworkTest extends junit.framework.TestCase {
    private org.apache.commons.configuration.Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private com.inmobi.adserve.channels.adnetworks.amoad.DCPAmoAdAdNetwork dcpAmoAdAdNetwork;
    private final String amoAdAdvId = "amoadadv1";
    private com.inmobi.adserve.channels.repository.RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(org.apache.commons.configuration.Configuration.class);
        expect(mockConfig.getString("amoad.host")).andReturn(null).anyTimes();
        expect(mockConfig.getString("amoad.advertiserId")).andReturn(amoAdAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        java.io.File f;
        f = new java.io.File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final io.netty.channel.Channel serverChannel = createMock(io.netty.channel.Channel.class);
        final com.inmobi.adserve.channels.api.HttpRequestHandlerBase
                base = createMock(com.inmobi.adserve.channels.api.HttpRequestHandlerBase.class);
        prepareMockConfig();
        com.inmobi.adserve.channels.api.Formatter.init();
        com.netflix.governator.guice.LifecycleInjector
                .builder()
                .withModules(com.google.inject.util.Modules.combine(new com.google.inject.AbstractModule() {

                    @Override
                    public void configure() {
                        bind(com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider.class).toInstance(createMock(com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider.class));
                        bind(com.inmobi.adserve.channels.util.JaxbHelper.class).asEagerSingleton();
                        bind(com.inmobi.adserve.channels.util.DocumentBuilderHelper.class).asEagerSingleton();
                        requestStaticInjection(com.inmobi.adserve.channels.api.BaseAdNetworkImpl.class);
                    }
                }), new TestScopeModule())
                .usingBasePackages("com.inmobi.adserve.channels.server.netty",
                        "com.inmobi.adserve.channels.api.provider").build().createInjector();
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor4 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor4.getDimension()).andReturn(new java.awt.Dimension(300, 50)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor4);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor9 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor9.getDimension()).andReturn(new java.awt.Dimension(320, 48)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor9);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor11 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor11.getDimension()).andReturn(new java.awt.Dimension(728, 90)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor11);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor14 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor14.getDimension()).andReturn(new java.awt.Dimension(320, 480)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor14);
        final com.inmobi.adserve.channels.entity.SlotSizeMapEntity
                slotSizeMapEntityFor15 = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.entity.SlotSizeMapEntity.class);
        org.easymock.EasyMock.expect(slotSizeMapEntityFor15.getDimension()).andReturn(new java.awt.Dimension(320, 50)).anyTimes();
        org.easymock.EasyMock.replay(slotSizeMapEntityFor15);
        repositoryHelper = org.easymock.EasyMock.createMock(com.inmobi.adserve.channels.repository.RepositoryHelper.class);
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        org.easymock.EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        org.easymock.EasyMock.replay(repositoryHelper);
        dcpAmoAdAdNetwork = new com.inmobi.adserve.channels.adnetworks.amoad.DCPAmoAdAdNetwork(mockConfig, null, base, serverChannel);
    }

    @org.testng.annotations.Test
    public void testDCPConfigureParametersAndroid() throws org.json.JSONException {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(amoAdAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpAmoAdAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper));
    }

    @org.testng.annotations.Test
    public void testDCPAmoAdRequestUri() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(java.util.Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final java.util.List<Long> category = new java.util.ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android.getValue());
        final String externalKey = "AmoAd";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(amoAdAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        dcpAmoAdAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper);
        assertNull(dcpAmoAdAdNetwork.getRequestUri());
    }


    @org.testng.annotations.Test
    public void testDCPAmoAdParseAd() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(java.util.Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(amoAdAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{}"),
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpAmoAdAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        final String response = "";
        dcpAmoAdAdNetwork.generateJsAdResponse();
        assertEquals(200, dcpAmoAdAdNetwork.getHttpResponseStatusCode());
        final String outputHttpResponseContent =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><!-- AMoAd Zone: [Inmobi ] --><div class=\"amoad_frame sid_62056d310111552c1081c48959720547417af886416a2ebac81d12f901043a9a container_div color_#0000cc-#444444-#ffffff-#0000FF-#009900 sp\"></div><script src='http://j.amoad.com/js/aa.js' type='text/javascript' charset='utf-8'></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(outputHttpResponseContent, dcpAmoAdAdNetwork.getHttpResponseContent());
    }


    @org.testng.annotations.Test
    public void testDCPAmoAdGetId() throws Exception {
        assertEquals(amoAdAdvId, dcpAmoAdAdNetwork.getId());
    }

    @org.testng.annotations.Test
    public void testDCPAmoAdGetImpressionId() throws Exception {
        final com.inmobi.adserve.channels.api.SASRequestParameters
                sasParams = new com.inmobi.adserve.channels.api.SASRequestParameters();
        final com.inmobi.adserve.channels.api.CasInternalRequestParameters
                casInternalRequestParameters = new com.inmobi.adserve.channels.api.CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final com.inmobi.adserve.channels.entity.ChannelSegmentEntity entity =
                new com.inmobi.adserve.channels.entity.ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(amoAdAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new org.json.JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new java.util.ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpAmoAdAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 11, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpAmoAdAdNetwork.getImpressionId());
    }

    @org.testng.annotations.Test
    public void testDCPAmoAdGetName() throws Exception {
        assertEquals("amoad", dcpAmoAdAdNetwork.getName());
    }

    @org.testng.annotations.Test
    public void testDCPAmoAdIsClickUrlReq() throws Exception {
        assertEquals(false, dcpAmoAdAdNetwork.isClickUrlRequired());
    }
}
