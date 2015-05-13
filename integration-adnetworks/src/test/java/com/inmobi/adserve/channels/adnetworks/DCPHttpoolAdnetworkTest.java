package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.httpool.DCPHttPoolAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;


public class DCPHttpoolAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    // Httpool
    private DCPHttPoolAdNetwork dcpHttpoolAdNetwork;
    private final String httpoolHost = "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&";
    private final String httpoolStatus = "on";
    private final String httpoolAdvId = "httpooladv1";
    private final String httpoolTest = "1";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("httpool.host")).andReturn(httpoolHost).anyTimes();
        expect(mockConfig.getString("httpool.status")).andReturn(httpoolStatus).anyTimes();
        expect(mockConfig.getString("httpool.test")).andReturn(httpoolTest).anyTimes();
        expect(mockConfig.getString("httpool.advertiserId")).andReturn(httpoolAdvId).anyTimes();
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
        final SlotSizeMapEntity slotSizeMapEntityFor10 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor10);
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
        dcpHttpoolAdNetwork = new DCPHttPoolAdNetwork(mockConfig, null, base, serverChannel);
        
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        
        dcpHttpoolAdNetwork.setHost(httpoolHost);
    }

    @Test
    public void testDCPHttpoolConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPHttpoolConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPHttpoolConfigureParametersBlankExtKey() {
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
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPHttpoolConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPHttpoolRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setCategories(new ArrayList<Long>());
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null,
                (short) 9, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=miscellenous";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPHttpoolRequestUriMale() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setGender("M");
        sasParams.setCategories(new ArrayList<Long>());
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=miscellenous&dd_gnd=1";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    @Test
    public void testDCPHttpoolRequestUriFemale() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setGender("f");
        sasParams.setCategories(new ArrayList<Long>());
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=miscellenous&dd_gnd=2";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    @Test
    public void testDCPHttpoolRequestUriSegmentCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final Long[] segmentCategories = new Long[] {13l, 15l};
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=miscellenous";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    @Test
    public void testDCPHttpoolRequestUriSiteCategory() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String externalKey = "1324";
        final Long[] cats = new Long[] {13l, 15l};
        sasParams.setCategories(Arrays.asList(cats));
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=Adventure%3BBoard";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    @Test
    public void testDCPHttpoolRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(" ,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt%2Cshop&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&did=202cb962ac59075b964b07152d234b70&format=320x50&ct=miscellenous";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());        }
    }

    //This case is never valid. Requests will get dropped in Request Filters if no slot has matching is SlotSizeMapping. Even the segments won't get selected in case of empty slot.
    public void IgnoreTestDCPHttpoolRequestUriBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, Short.MAX_VALUE, repositoryHelper)) {
            final String actualUrl = dcpHttpoolAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://a.mobile.toboads.com/get?ormma=0&fh=0&sdkid=api&sdkver=100&type=rich%2Ctpt&uip=206.29.182.240&zid=1324&ua=Mozilla&test=1&geo_lat=37.4429&geo_lng=-122.1514&did=202cb962ac59075b964b07152d234b70&ct=miscellenous";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPHttpoolParseResponseImg() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "{\"status\":1337,\"ad_type\":\"rich\",\"image_url\":\"http://a.mobile.toboads.com/image?t=rich&aid=0e75e1fd-b715-46be-91df-54a6f12a639d&format=320x50\",\"impression_url\":\"http://a.mobile.toboads.com/impress?adh=7001b459-0268-4bcc-9907-da11289e592d&add=H89xkxbwcmVD-bcL-Xg5shKWiaC_yATHamJQWPRpOF1tjuLb5TADYnVNQGPqfudt4G33Q4QK38Y.&did=nodeviceid-1234567890\",\"click_url\":\"http://a.mobile.toboads.com/click?adh=7001b459-0268-4bcc-9907-da11289e592d&add=H89xkxbwcmVD-bcL-Xg5shKWiaC_yATHamJQWPRpOF1tjuLb5TADYnVNQGPqfudt4G33Q4QK38Y.&did=nodeviceid-1234567890\",\"redirect_url\":\"http://sofialive.bg/mobile\",\"extra\":{\"bg_color\":\"#000000\",\"text_color\":\"#FFFFFF\",\"refresh_time\":\"0\",\"transition\":\"0\"}}";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpHttpoolAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://a.mobile.toboads.com/click?adh=7001b459-0268-4bcc-9907-da11289e592d&add=H89xkxbwcmVD-bcL-Xg5shKWiaC_yATHamJQWPRpOF1tjuLb5TADYnVNQGPqfudt4G33Q4QK38Y.&did=nodeviceid-1234567890&url=http://sofialive.bg/mobile' onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://a.mobile.toboads.com/image?t=rich&aid=0e75e1fd-b715-46be-91df-54a6f12a639d&format=320x50'  /></a><img src='http://a.mobile.toboads.com/impress?adh=7001b459-0268-4bcc-9907-da11289e592d&add=H89xkxbwcmVD-bcL-Xg5shKWiaC_yATHamJQWPRpOF1tjuLb5TADYnVNQGPqfudt4G33Q4QK38Y.&did=nodeviceid-1234567890' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpHttpoolAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPHttpoolParseResponseHtmlTPT() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 15, repositoryHelper);
        final String response =
                "{\"status\": 1337,\"ad_type\": \"tpt\",\"content\": \"<a href=\\\"http://a.mobile.toboads.com/click?adh=dd992e3a-192b-429a-9c22-709efca656e1&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&test=1&did=123456789&url=http://labs.httpool.com\\\"><img src=\\\"http://labs.httpool.com/your_ad_here.png\\\" width=\\\"320\\\" height=\\\"50\\\" /></a>\",\"impression_url\": \"http://a.mobile.toboads.com/impress?adh=dd992e3a-192b-429a-9c22-709efca656e1&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&test=1&did=123456789\",\"extra\": {\"bg_color\": \"#000000\",\"text_color\": \"#FFFFFF\",\"refresh_time\": \"0\",\"transition\": \"0\"}}";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpHttpoolAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://a.mobile.toboads.com/click?adh=dd992e3a-192b-429a-9c22-709efca656e1&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&test=1&did=123456789&url=http://labs.httpool.com\"><img src=\"http://labs.httpool.com/your_ad_here.png\" width=\"320\" height=\"50\" /></a><img src='http://a.mobile.toboads.com/impress?adh=dd992e3a-192b-429a-9c22-709efca656e1&add=RegibO8TU8mLvGbkwfKZzdOMhDa--dmGlIFZIELVZ5gP1DZXyAiG8N8xDOKctXZ44G33Q4QK38Y.&test=1&did=123456789' height=1 width=1 border=0 style=\"display:none;\"/><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpHttpoolAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPHttpoolParseResponseTextAdWap() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("WAP");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);
        final String response =
                "{\"status\":1337,\"ad_type\":\"shop\",\"image_url\":\"http://a.mobile.toboads.com/image?t=text&aid=5f404c6e-8278-436e-838b-5daa39fe2d96&format=320x50\",\"impression_url\":\"http://a.mobile.toboads.com/impress?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\",\"click_url\":\"http://a.mobile.toboads.com/click?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\",\"redirect_url\":\"http://labs.httpool.com\",\"extra\":{\"bg_color\":\"#000000\",\"text_color\":\"#FFFFFF\",\"refresh_time\":\"0\",\"transition\":\"0\"},\"content\":\"Claritas est etiam processus dynamicus, qui sequi.\"}";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpHttpoolAdNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://a.mobile.toboads.com/click?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70&url=http://labs.httpool.com\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td><div style=\"margin:0 auto;\"><img border=\"0\" src=\"http://a.mobile.toboads.com/image?t=text&aid=5f404c6e-8278-436e-838b-5daa39fe2d96&format=320x50\" width=\"38\" height=\"38\" style=\"float:left;margin:2px;\" /></div></td><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Claritas est etiam processus dynamicus, qui sequi.</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://a.mobile.toboads.com/impress?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpHttpoolAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPHttpoolParseResponseTextAdAppSDK360() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("App");
        sasParams.setSdkVersion("i367");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4, repositoryHelper);
        final String response =
                "{\"status\":1337,\"ad_type\":\"shop\",\"image_url\":\"http://a.mobile.toboads.com/image?t=text&aid=5f404c6e-8278-436e-838b-5daa39fe2d96&format=320x50\",\"impression_url\":\"http://a.mobile.toboads.com/impress?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\",\"click_url\":\"http://a.mobile.toboads.com/click?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\",\"redirect_url\":\"http://labs.httpool.com\",\"extra\":{\"bg_color\":\"#000000\",\"text_color\":\"#FFFFFF\",\"refresh_time\":\"0\",\"transition\":\"0\"},\"content\":\"Claritas est etiam processus dynamicus, qui sequi.\"}";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpHttpoolAdNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://a.mobile.toboads.com/click?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70&url=http://labs.httpool.com\" onclick=\"document.getElementById('click').src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1';mraid.openExternal('http://a.mobile.toboads.com/click?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70&url=http://labs.httpool.com'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td><div style=\"margin:0 auto;\"><img border=\"0\" src=\"http://a.mobile.toboads.com/image?t=text&aid=5f404c6e-8278-436e-838b-5daa39fe2d96&format=320x50\" width=\"38\" height=\"38\" style=\"float:left;margin:2px;\" /></div></td><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">Claritas est etiam processus dynamicus, qui sequi.</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 \"display:none;\"/><img src=\"http://a.mobile.toboads.com/impress?adh=1475ad9d-66cf-43ac-9c59-026b3ab39593&add=NwgHspu71tCynGXEHCsy95jvkB4B4dLEBTIPESU3ODEIaudDRPK4998xDOKctXZ44G33Q4QK38Y.&test=1&did=202cb962ac59075b964b07152d234b70\" height=\"1\" width=\"1\" border=\"0\" style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpHttpoolAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPHttpoolParseNoAd() throws Exception {
        final String response = "{\"status\":0,\"error\":\"No ad available.\"}";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpHttpoolAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPHttpoolParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpHttpoolAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpHttpoolAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpHttpoolAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPHttpoolGetId() throws Exception {
        assertEquals(httpoolAdvId, dcpHttpoolAdNetwork.getId());
    }

    @Test
    public void testDCPHttpoolGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(httpoolAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpHttpoolAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 14, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpHttpoolAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPHttpoolGetName() throws Exception {
        assertEquals("httpoolDCP", dcpHttpoolAdNetwork.getName());
    }

    @Test
    public void testDCPHttpoolIsClickUrlReq() throws Exception {
        assertEquals(true, dcpHttpoolAdNetwork.isClickUrlRequired());
    }
}
