package com.inmobi.adserve.channels.adnetworks;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.webmoblink.DCPWebmoblinkAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.netflix.governator.guice.LifecycleInjector;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

public class DCPWebmoblinkAdnetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private DCPWebmoblinkAdNetwork dcpWebmoblinkAdNetwork;
    private final String webmoblinkHost = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx";
    private final String webmoblinkStatus = "on";
    private final String webmoblinkAccountid = "123";
    private final String webmoblinkResponseFormat = "xml";
    private final String webmoblinkAdvId = "54321";
    private final String webmoblinkMode = "LIVE";
    private final String webmoblinkAdFormat = "ANY";
    private RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("webmoblink.host")).andReturn(webmoblinkHost).anyTimes();
        expect(mockConfig.getString("webmoblink.accountId")).andReturn(webmoblinkAccountid).anyTimes();
        expect(mockConfig.getString("webmoblink.status")).andReturn(webmoblinkStatus).anyTimes();
        expect(mockConfig.getString("webmoblink.mode")).andReturn(webmoblinkMode).anyTimes();
        expect(mockConfig.getString("webmoblink.adformat")).andReturn(webmoblinkAdFormat).anyTimes();
        expect(mockConfig.getString("webmoblink.resformat")).andReturn(webmoblinkResponseFormat).anyTimes();
        expect(mockConfig.getString("webmoblink.advertiserId")).andReturn(webmoblinkAdvId).anyTimes();
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
        LifecycleInjector.builder().withModules(Modules.combine(new AbstractModule() {

            @Override
            public void configure() {
                bind(AsyncHttpClientProvider.class).toInstance(createMock(AsyncHttpClientProvider.class));
                bind(JaxbHelper.class).asEagerSingleton();
                bind(DocumentBuilderHelper.class).asEagerSingleton();
                requestStaticInjection(BaseAdNetworkImpl.class);
            }
        }), new TestScopeModule())
                .usingBasePackages("com.inmobi.adserve.channels.server.netty", "com.inmobi.adserve.channels.api.provider")
                .build().createInjector();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor1);
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
        final SlotSizeMapEntity slotSizeMapEntityFor12 = EasyMock.createMock(SlotSizeMapEntity.class);
        EasyMock.expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        EasyMock.replay(slotSizeMapEntityFor12);
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
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        EasyMock.expect(repositoryHelper.querySlotSizeMapRepository((short)15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        EasyMock.replay(repositoryHelper);
        dcpWebmoblinkAdNetwork = new DCPWebmoblinkAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteIncId(123456);
        sasParams.setAllParametersJson("{\"carrier\": [365,0,\"us\",10224,10225]}");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper));
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankCategories() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setCarrierId(365);
        sasParams.setCountryId(0l);
        sasParams.setSiteIncId(123456);
        sasParams.setCountryCode("us");
        String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                        + "/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper));
    }

    @Test
    public void testWebmoblinkConfigureParametersInvalidCarrier() throws JSONException {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setCarrierId(365);
        sasParams.setCountryId(0l);
        sasParams.setSiteIncId(123456);
        sasParams.setCountryCode("us");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1"
                        + "/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper));
    }

    @Test
    public void testWebmoblinkRequestUri() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setCarrierId(365);
        sasParams.setCountryId(0l);
        sasParams.setSiteIncId(123456);
        sasParams.setCountryCode("us");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us";
        assertEquals(expectedUrl, actualUrl);

    }

    @Test
    public void testWebmoblinkRequestUriBlankCountry() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setCarrierId(365);
        // sasParams.setCountryId(0l);
        // sasParams.setCountryCode("us");
        sasParams.setSiteIncId(123456);
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        if (dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper)) {
            String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
            String expectedUrl =
                    "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testWebmoblinkRequestUriAndroid() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setCarrierId(365);
        sasParams.setCountryId(0l);
        sasParams.setSiteIncId(123456);
        sasParams.setSource("APP");
        sasParams.setOsId(3);
        casInternalRequestParameters.setUidADT("1");
        casInternalRequestParameters.setUidMd5("");
        sasParams.setCountryCode("us");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(expectedUrl, actualUrl);

        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(expectedUrl, actualUrl);

        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(expectedUrl, actualUrl);

        casInternalRequestParameters.setUidADT("1");
        casInternalRequestParameters.setGpid("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=7";
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    public void testWebmoblinkRequestUriIOS() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setCarrierId(365);
        sasParams.setCountryId(0l);
        sasParams.setSiteIncId(123456);
        sasParams.setSource("APP");
        sasParams.setOsId(5);
        casInternalRequestParameters.setUidADT("1");
        casInternalRequestParameters.setUidMd5("");
        sasParams.setCountryCode("us");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=6";
        assertEquals(expectedUrl, actualUrl);

        casInternalRequestParameters.setUidSO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=3";
        assertEquals(expectedUrl, actualUrl);

        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=3";
        assertEquals(expectedUrl, actualUrl);
        casInternalRequestParameters.setUidADT("1");
        casInternalRequestParameters.setUidIFA("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=1";
        assertEquals(expectedUrl, actualUrl);

    }


    @Test
    public void testWebmoblinkParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";
        String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd"
                        + "-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";

        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Integer>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        String response =
                "<?xml version=\"1.0\"?> <adResponse><status>0</status><clickUrl> http://buschgardens.com/bg/ </clickUrl><imageUrl> http://webmoblink.com/uploadedfiles/buschgarden3021.gif </imageUrl> <adText>Enjoy two parks, any two days</adText><firePixel> http://webmoblink.com/api/tracksystem/2345 </firePixel></adResponse>";
        dcpWebmoblinkAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpWebmoblinkAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=' http://buschgardens.com/bg/ ' onclick=\"document.getElementById('click').src='$IMClickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://webmoblink.com/uploadedfiles/buschgarden3021.gif'  /><br/>Enjoy two parks, any two days</a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                dcpWebmoblinkAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPMableParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpWebmoblinkAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpWebmoblinkAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpWebmoblinkAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testWebmoblinkGetId() throws Exception {
        assertEquals(webmoblinkAdvId, dcpWebmoblinkAdNetwork.getId());
    }

    @Test
    public void testWebmoblinkGetImpressionId() throws Exception {
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
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpWebmoblinkAdNetwork.getImpressionId());
    }

    @Test
    public void testWebmoblinkGetName() throws Exception {
        assertEquals("webmoblink", dcpWebmoblinkAdNetwork.getName());
    }

}
