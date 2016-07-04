package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.webmoblink.DCPWebmoblinkAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.DefaultLazyInmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilder;
import com.inmobi.adserve.channels.api.trackers.InmobiAdTrackerBuilderFactory;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DocumentBuilderHelper;
import com.inmobi.adserve.channels.util.JaxbHelper;
import com.netflix.governator.guice.LifecycleInjector;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
@PowerMockIgnore({"javax.management.*"})
public class DCPWebmoblinkAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String webmoblinkHost = "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx";
    private static final String webmoblinkStatus = "on";
    private static final String webmoblinkAccountid = "123";
    private static final String webmoblinkResponseFormat = "xml";
    private static final String webmoblinkAdvId = "54321";
    private static final String webmoblinkMode = "LIVE";
    private static final String webmoblinkAdFormat = "ANY";
    private static Configuration mockConfig = null;
    private static DCPWebmoblinkAdNetwork dcpWebmoblinkAdNetwork;
    private static RepositoryHelper repositoryHelper;

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

    @Before
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
                install(new FactoryModuleBuilder()
                        .implement(InmobiAdTrackerBuilder.class, DefaultLazyInmobiAdTrackerBuilder.class)
                        .build(Key.get(InmobiAdTrackerBuilderFactory.class, DefaultLazyInmobiAdTrackerBuilderFactory.class)));
                requestStaticInjection(BaseAdNetworkImpl.class);
            }
        }), new TestScopeModule())
                .usingBasePackages("com.inmobi.adserve.channels.server.netty", "com.inmobi.adserve.channels.api.provider")
                .build().createInjector();
        final SlotSizeMapEntity slotSizeMapEntityFor1 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor1.getDimension()).andReturn(new Dimension(120, 20)).anyTimes();
        replay(slotSizeMapEntityFor1);
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor10 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor10.getDimension()).andReturn(new Dimension(300, 250)).anyTimes();
        replay(slotSizeMapEntityFor10);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
        final SlotSizeMapEntity slotSizeMapEntityFor12 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor12.getDimension()).andReturn(new Dimension(468, 60)).anyTimes();
        replay(slotSizeMapEntityFor12);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9))
                .andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11))
                .andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 12))
                .andReturn(slotSizeMapEntityFor12).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);
        dcpWebmoblinkAdNetwork = new DCPWebmoblinkAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testWebmoblinkConfigureParametersBlankUA() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        sasParams.setSiteIncId(123456);
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
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
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
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
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
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
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us";

        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

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
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        if (dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
            String expectedUrl =
                    "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
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
        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setUidMd5("");
        sasParams.setCountryCode("us");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=4";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setGpid("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=7";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
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
        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setUidMd5("");
        sasParams.setCountryCode("us");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        Long[] cats = {10l, 13l, 30l};
        sasParams.setCategories(Arrays.asList(cats));
        String externalSiteKey = "10023";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Long>(), 0.0d,
                        null, null, 32, new Integer[] {0}));
        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        String actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=6";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setUidSO1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=3";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setUidIDUS1("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=3";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setUidIFA("202cb962ac59075b964b07152d234b70");
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        actualUrl = dcpWebmoblinkAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://www.webmoblink-api.mobi/API2/MobileAPI.aspx?aid=123&pid=10023&sid=00000000-0000-0020-0000-00000001e240&mo=LIVE&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334&ip=206.29.182.240&format=ANY&result=xml&cc=us&did=202cb962ac59075b964b07152d234b70&didtype=1";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

    }


    @Test
    public void testWebmoblinkParseAd() throws Exception {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        String externalKey = "19100";

        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<Long>(), 0.0d, null,
                        null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        String response =
                "<?xml version=\"1.0\"?> <adResponse><status>0</status><clickUrl> http://buschgardens.com/bg/ </clickUrl><imageUrl> http://webmoblink.com/uploadedfiles/buschgarden3021.gif </imageUrl> <adText>Enjoy two parks, any two days</adText><firePixel> http://webmoblink.com/api/tracksystem/2345 </firePixel></adResponse>";
        dcpWebmoblinkAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpWebmoblinkAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=' http://buschgardens.com/bg/ ' onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://webmoblink.com/uploadedfiles/buschgarden3021.gif'  /><br/>Enjoy two parks, any two days</a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
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
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(webmoblinkAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<Long>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpWebmoblinkAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpWebmoblinkAdNetwork.getImpressionId());
    }

    @Test
    public void testWebmoblinkGetName() throws Exception {
        assertEquals("webmoblinkDCP", dcpWebmoblinkAdNetwork.getName());
    }

}
