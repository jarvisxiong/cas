package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.util.Modules;
import com.inmobi.adserve.channels.adnetworks.placeiq.DCPPlaceIQAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
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
public class DCPPlaceIQAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String placeiqHost = "http://test.ads.placeiq.com/2/ad";
    private static final String placeiqStatus = "on";
    private static final String placeiqAdvId = "placeiqadv1";
    private static final String placeiqTest = "1";
    private static final String placeiqSeed = "EJoU6f9DsqDyyxB";
    private static final String placeiqPartnerId = "IMB";
    private static final String placeiqRequestFormat = "ss";
    private static final String placeiqResponseFormat = "xml";
    private static Configuration mockConfig = null;
    private static DCPPlaceIQAdnetwork dcpPlaceIQAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("placeiq.host")).andReturn(placeiqHost).anyTimes();
        expect(mockConfig.getString("placeiq.status")).andReturn(placeiqStatus).anyTimes();
        expect(mockConfig.getString("placeiq.test")).andReturn(placeiqTest).anyTimes();
        expect(mockConfig.getString("placeiq.advertiserId")).andReturn(placeiqAdvId).anyTimes();
        expect(mockConfig.getString("placeiq.partnerId")).andReturn(placeiqPartnerId).anyTimes();
        expect(mockConfig.getString("placeiq.seed")).andReturn(placeiqSeed).anyTimes();
        expect(mockConfig.getString("placeiq.requestFormat")).andReturn(placeiqRequestFormat).anyTimes();
        expect(mockConfig.getString("placeiq.responseFormat")).andReturn(placeiqResponseFormat).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        LifecycleInjector
                .builder()
                .withModules(Modules.combine(new AbstractModule() {

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
                .usingBasePackages("com.inmobi.adserve.channels.server.netty",
                        "com.inmobi.adserve.channels.api.provider").build().createInjector();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor9 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor9.getDimension()).andReturn(new Dimension(320, 48)).anyTimes();
        replay(slotSizeMapEntityFor9);
        final SlotSizeMapEntity slotSizeMapEntityFor11 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor11.getDimension()).andReturn(new Dimension(728, 90)).anyTimes();
        replay(slotSizeMapEntityFor11);
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
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        replay(repositoryHelper);
        dcpPlaceIQAdNetwork = new DCPPlaceIQAdnetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpPlaceIQAdNetwork.setHost(placeiqHost);
    }

    @Test
    public void testDCPPlaceiqConfigureParametersAndroid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersWithGeoAndNoUid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersIOS() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        casInternalRequestParameters.setTrackingAllowed(true);
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersWap() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.webOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPPlaceiqRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "PlaceIQ_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://test.ads.placeiq.com/2/ad?RT=ss&ST=xml&PT=IMB&AU=PlaceIQ_test_7%2Fbz%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x50&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STG%2CRMG%2CMRD";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
    }

    @Test
    public void testDCPPlaceiqRequestUriWithNoCat() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setGpid("ABDC-ASDW-EWFJ-FHSA");
        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "PlaceIQ_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://test.ads.placeiq.com/2/ad?RT=ss&ST=xml&PT=IMB&AU=PlaceIQ_test_7%2Fuc%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x50&GR=ABDC-ASDW-EWFJ-FHSA&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STG%2CRMG%2CMRD";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPlaceiqRequestUriWithInterstitial() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "PlaceIQ_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://test.ads.placeiq.com/2/ad?RT=ss&ST=xml&PT=IMB&AU=PlaceIQ_test_7%2Fuc%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x480&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STG%2CRMG%2CMRD%2CMRI";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPlaceiqRequestUriWithSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(1l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "PlaceIQ_test_7";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper)) {
            final String actualUrl = dcpPlaceIQAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://test.ads.placeiq.com/2/ad?RT=ss&ST=xml&PT=IMB&AU=PlaceIQ_test_7%2Fuc%2F6456fc%2F0&IP=206.29.182.240&UA=Mozilla&DO=Android&LT=37.4429&LG=-122.1514&SZ=320x50&AM=202cb962ac59075b964b07152d234b70&AP=6575868&AT=STG%2CRMG%2CMRD";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPPlaceiqParseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PLACEIQ><CONTENT>&lt;div class=���piq_creative���&gt;<![CDATA[<a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"><img width=\"320\" height=\"50\" border=\"0\"src=\"http://tpc.googlesyndication.com/pageadimg/imgad?id=CICAgIDQnf39JBDAAhgyKAEyCDCZUVf6Jre0\"/></a> <span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=testsizemacro0613&c=truste01cont1&w=320&h=50&sid=0\"></script></span></div>]]></CONTENT><NETWORK>50024410</NETWORK><CREATIVEID>20520035890</CREATIVEID><LINEITEMID>42680050</LINEITEMID><CLICKTHRU><![CDATA[\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"]]></CLICKTHRU><ADTYPE>STG</ADTYPE></PLACEIQ>";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
        final String outputHttpResponseContent =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div class=���piq_creative���><a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=B0ta75SXDUfqjFe_Q6QGwwYGgCuKKzYgDAAAAEAEgquv7HjgAWLLM27hMYMmGgIDMo8AXugEJZ2ZwX2ltYWdlyAEJwAIC4AIA6gIdNTEwMjQyOTAvUGxhY2VJUV90ZXN0aW5nX3NpdGX4AvzRHoADAZAD6AKYA-ADqAMB4AQBoAYW2AYC&num=0&sig=AOD64_2De-k5A7OhskDIPybdJjPZLbA6bw&client=ca-pub-9004609665008229&adurl=\"><img width=\"320\" height=\"50\" border=\"0\"src=\"http://tpc.googlesyndication.com/pageadimg/imgad?id=CICAgIDQnf39JBDAAhgyKAEyCDCZUVf6Jre0\"/></a> <span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=testsizemacro0613&c=truste01cont1&w=320&h=50&sid=0\"></script></span></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(outputHttpResponseContent, dcpPlaceIQAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPlaceiqParseAdApiV2() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PLACEIQ><AD><CONTENT><![CDATA[<div class=\"piq_creative\"><a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=BBF86NyAYU8XHGc_IlwfetoCQC7LfoYwEAAAAEAEgquv7HjgAWLLLsv57YMmGgIDIo5AZugEJZ2ZwX2ltYWdlyAEJmAL4VcACAuACAOoCGzUxMDI0MjkwL0lNQi9iei8yMjM5ZC9jNTA2Y_gC_dEekAPwAZgDpAOoAwHgBAGgBh_YBgI&num=0&sig=AOD64_1F9gXWVq4ylpsGiQZ9qnfnS36NLQ&client=ca-pub-9004609665008229&adurl=http://ad.doubleclick.net/clk;279559162;106636670;q\"><img width=\"320\" height=\"50\" border=\"0\" src=\"http://pagead2.googlesyndication.com/pagead/imgad?id=CICAgKDjqb-JxwEQARgBMggy5RQCEdqLCA\"/></a><img src=\"http://ad.doubleclick.net/ad/N763.1227592.PLACEIQ.COM/B7992138.10;sz=1x1;ord=117643749?\" width=\"1\" height=\"1\" border=\"0\"> <img src=\"http://pubads.g.doubleclick.net/pagead/adview?ai=BBF86NyAYU8XHGc_IlwfetoCQC7LfoYwEAAAAEAEgquv7HjgAWLLLsv57YMmGgIDIo5AZugEJZ2ZwX2ltYWdlyAEJmAL4VcACAuACAOoCGzUxMDI0MjkwL0lNQi9iei8yMjM5ZC9jNTA2Y_gC_dEekAPwAZgDpAOoAwHgBAGgBh_YBgI&sigh=REoZLoiBLvA&template_id=10025290&adurl=http://t1.pub.placeiq.com/tracking_pixel.gif?LA=33.61920166015625&LO=-112.00399780273438&AP=DFP&AU=50024410&PT=IMB&OI=148950370&LI=51563770&CC=33282631090&RI=IMB1394089984.2872542855534&IP=72.62.90.104&DM=69ff1e29aed56894442d43ce94cb47d7&DS=&DI=&UM=&UO=&DA=\" width=\"0\" height=\"0\" border=\"0\"><span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=51563770&c=truste01cont1&plc=tr&w=320&h=50&sid=m8yahC5-_5X9qFjPLtwWBcrqcUwdEHFGax3SgpqPsy8I51t3IDE4ZYU-0pNNVYkv_XVrKqwR1AlexYSis3yKqrirEjDvfU23EBEeEZc3f-by5tPAN59MIt_gwl_MGYkFViBpKkTo2addzvxwGUXps5YCFV43fVOPvYJLNT40LT4\"></script></span></div>]]></CONTENT><NETWORK>50024410</NETWORK><CREATIVEID>33282631090</CREATIVEID><LINEITEMID>51563770</LINEITEMID><CLICKTHRU><![CDATA[http://adclick.g.doubleclick.net/aclk?sa=L&ai=BBF86NyAYU8XHGc_IlwfetoCQC7LfoYwEAAAAEAEgquv7HjgAWLLLsv57YMmGgIDIo5AZugEJZ2ZwX2ltYWdlyAEJmAL4VcACAuACAOoCGzUxMDI0MjkwL0lNQi9iei8yMjM5ZC9jNTA2Y_gC_dEekAPwAZgDpAOoAwHgBAGgBh_YBgI&num=0&sig=AOD64_1F9gXWVq4ylpsGiQZ9qnfnS36NLQ&client=ca-pub-9004609665008229&adurl=]]></CLICKTHRU><ADTYPE>STG</ADTYPE></AD></PLACEIQ>";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div class=\"piq_creative\"><a href=\"http://adclick.g.doubleclick.net/aclk?sa=L&ai=BBF86NyAYU8XHGc_IlwfetoCQC7LfoYwEAAAAEAEgquv7HjgAWLLLsv57YMmGgIDIo5AZugEJZ2ZwX2ltYWdlyAEJmAL4VcACAuACAOoCGzUxMDI0MjkwL0lNQi9iei8yMjM5ZC9jNTA2Y_gC_dEekAPwAZgDpAOoAwHgBAGgBh_YBgI&num=0&sig=AOD64_1F9gXWVq4ylpsGiQZ9qnfnS36NLQ&client=ca-pub-9004609665008229&adurl=http://ad.doubleclick.net/clk;279559162;106636670;q\"><img width=\"320\" height=\"50\" border=\"0\" src=\"http://pagead2.googlesyndication.com/pagead/imgad?id=CICAgKDjqb-JxwEQARgBMggy5RQCEdqLCA\"/></a><img src=\"http://ad.doubleclick.net/ad/N763.1227592.PLACEIQ.COM/B7992138.10;sz=1x1;ord=117643749?\" width=\"1\" height=\"1\" border=\"0\"> <img src=\"http://pubads.g.doubleclick.net/pagead/adview?ai=BBF86NyAYU8XHGc_IlwfetoCQC7LfoYwEAAAAEAEgquv7HjgAWLLLsv57YMmGgIDIo5AZugEJZ2ZwX2ltYWdlyAEJmAL4VcACAuACAOoCGzUxMDI0MjkwL0lNQi9iei8yMjM5ZC9jNTA2Y_gC_dEekAPwAZgDpAOoAwHgBAGgBh_YBgI&sigh=REoZLoiBLvA&template_id=10025290&adurl=http://t1.pub.placeiq.com/tracking_pixel.gif?LA=33.61920166015625&LO=-112.00399780273438&AP=DFP&AU=50024410&PT=IMB&OI=148950370&LI=51563770&CC=33282631090&RI=IMB1394089984.2872542855534&IP=72.62.90.104&DM=69ff1e29aed56894442d43ce94cb47d7&DS=&DI=&UM=&UO=&DA=\" width=\"0\" height=\"0\" border=\"0\"><span id=\"te-clearads-js-truste01cont1\"><script type=\"text/javascript\" src=\"http://choices.truste.com/ca?pid=placeiq01&aid=placeiq01&cid=51563770&c=truste01cont1&plc=tr&w=320&h=50&sid=m8yahC5-_5X9qFjPLtwWBcrqcUwdEHFGax3SgpqPsy8I51t3IDE4ZYU-0pNNVYkv_XVrKqwR1AlexYSis3yKqrirEjDvfU23EBEeEZc3f-by5tPAN59MIt_gwl_MGYkFViBpKkTo2addzvxwGUXps5YCFV43fVOPvYJLNT40LT4\"></script></span></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpPlaceIQAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPlaceiqParseNoAd() throws Exception {
        final String response =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE PLACEIQ_AD_RESPONSE SYSTEM \"http://ads.placeiq.com/1/ad/placeiq_no_ad_response.dtd\"><PLACEIQ><NOAD></NOAD></PLACEIQ>";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
    }

    @Test
    public void testDCPPlaceiqParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpPlaceIQAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpPlaceIQAdNetwork.getHttpResponseStatusCode());
        assertEquals("", dcpPlaceIQAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPPlaceiqGetId() throws Exception {
        assertEquals(placeiqAdvId, dcpPlaceIQAdNetwork.getId());
    }

    @Test
    public void testDCPPlaceiqGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(placeiqAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpPlaceIQAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpPlaceIQAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPPlaceiqGetName() throws Exception {
        assertEquals("placeiqDCP", dcpPlaceIQAdNetwork.getName());
    }

}
