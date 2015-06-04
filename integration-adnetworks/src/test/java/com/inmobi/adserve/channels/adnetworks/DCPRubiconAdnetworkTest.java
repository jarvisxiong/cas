package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.rubicon.DCPRubiconAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SiteEcpmEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPRubiconAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String rubiconHost =
            "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2";
    private static final String rubiconStatus = "on";
    private static final String rubiconAdvId = "rubiconadv1";
    private static final String rubiconTest = "1";
    private static final String rubiconUser = "inmobi";
    private static final String rubiconPassword = "test";
    private static Configuration mockConfig = null;
    private static DCPRubiconAdnetwork dcpRubiconAdNetwork;
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("rubicon.host")).andReturn(rubiconHost).anyTimes();
        expect(mockConfig.getString("rubicon.status")).andReturn(rubiconStatus).anyTimes();
        expect(mockConfig.getString("rubicon.test")).andReturn(rubiconTest).anyTimes();
        expect(mockConfig.getString("rubicon.password")).andReturn(rubiconPassword).anyTimes();
        expect(mockConfig.getString("rubicon.username")).andReturn(rubiconUser).anyTimes();
        expect(mockConfig.getString("rubicon.advertiserId")).andReturn(rubiconAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        expect(mockConfig.getDouble("rubicon.eCPMPercentage")).andReturn(0.8);
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }

        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        final Channel serverChannel = createMock(Channel.class);
        prepareMockConfig();
        Formatter.init();
        dcpRubiconAdNetwork = new DCPRubiconAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
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

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpRubiconAdNetwork.setHost(rubiconHost);
    }

    @Test
    public void testDCPrubiconConfigureParametersAppBlankUid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(false,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    public void testDCPrubiconConfigureParametersAppWithUid() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSource("APP");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(true,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }



    @Test
    public void testDCPrubiconConfigureParametersIOS() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUidIFA("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(true,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPrubiconConfigureParametersWap() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(HandSetOS.webOS.getValue());
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(true,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 11, repositoryHelper));
    }

    @Test
    public void testDCPrubiconConfigureParametersBlankIP() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(false,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPrubiconConfigureParametersBlankUA() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"191002\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        assertEquals(false,
                dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testDCPrubiconRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent(URLEncoder
                        .encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                                "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "38132";
        final List<String> categoryList = new ArrayList<String>();
        categoryList.add("Games");
        categoryList.add("Business");

        final WapSiteUACEntity.Builder builder = new WapSiteUACEntity.Builder();
        builder.setCategories(categoryList);
        builder.setContentRating("4+");
        final WapSiteUACEntity uacEntity = builder.build();
        sasParams.setWapSiteUACEntity(uacEntity);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));

        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        final String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&app.category=Games%2CBusiness&i.aq_sensitivity=high&app.rating=4+&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpid_type=open-udid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String expectedUrl_for_perftype =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&app.category=Games%2CBusiness&i.aq_sensitivity=low&app.rating=4+&p_block_keys=blk6575868%2CInMobiPERF&rp_floor=0.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpid_type=open-udid&kw=38132";
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        final String actualUrl_for_perftype = dcpRubiconAdNetwork.getRequestUri().toString();


        assertEquals(new URI(expectedUrl_for_perftype).getQuery(), new URI(actualUrl_for_perftype).getQuery());
        assertEquals(new URI(expectedUrl_for_perftype).getPath(), new URI(actualUrl_for_perftype).getPath());
        dcpRubiconAdNetwork.getNingRequestBuilder();
    }

    @Test
    public void testDCPrubiconRequestUriWithMultipleUdid() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent(URLEncoder
                        .encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                                "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIDUS1("1234202cb962ac59075b964b07152d234b705432");
        sasParams.setSiteIncId(6575868);

        final SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.ecpm(0.50); // THE URL should have 0.4 (80% of network ECPM)
        sasParams.setSiteEcpmEntity(builder.build());
        sasParams.setOsId(HandSetOS.Android.getValue());
        final String externalKey = "38132";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));

        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        final String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.4&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";

        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        // Fallback to casInternalParams RTB Floor.
        sasParams.setSiteEcpmEntity(null);
        casInternalRequestParameters.setAuctionBidFloor(0.68);
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        final String actualUrl2 = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl2 =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.68&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";
        assertEquals(new URI(expectedUrl2).getQuery(), new URI(actualUrl2).getQuery());
        assertEquals(new URI(expectedUrl2).getPath(), new URI(actualUrl2).getPath());

        // Fallback to default minimum ecpm of $0.1 value.
        casInternalRequestParameters.setAuctionBidFloor(0.0);
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        final String actualUrl3 = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl3 =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";

        assertEquals(new URI(expectedUrl3).getQuery(), new URI(actualUrl3).getQuery());
        assertEquals(new URI(expectedUrl3).getPath(), new URI(actualUrl3).getPath());

        dcpRubiconAdNetwork.getNingRequestBuilder();
    }


    @Test
    public void testDCPrubiconRequestUriWithUAC() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent(URLEncoder
                        .encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                                "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIDUS1("1234202cb962ac59075b964b07152d234b705432");
        sasParams.setSiteIncId(6575868);

        final SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.ecpm(0.50); // THE URL should have 0.4 (80% of network ECPM)
        sasParams.setSiteEcpmEntity(builder.build());
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("i400");
        final String externalKey = "38132";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));

        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&accept.apis=5&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.4&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        dcpRubiconAdNetwork.getNingRequestBuilder();
    }

    @Test
    public void testDCPrubiconRequestUriWithSiteSpecificFloor() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent(URLEncoder
                        .encode("Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                                "UTF-8"));
        sasParams.setSource("APP");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidIDUS1("1234202cb962ac59075b964b07152d234b705432");
        sasParams.setSiteIncId(1387380247996547l);
        sasParams.setCountryId(94l);
        final SiteEcpmEntity.Builder builder = SiteEcpmEntity.newBuilder();
        builder.ecpm(0.50); // THE URL should have 0.4 (80% of network ECPM)
        sasParams.setSiteEcpmEntity(builder.build());
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("i400");
        final String externalKey = "38132";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));

        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.1387380247996547&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&accept.apis=5&p_block_keys=blk1387380247996547%2CInMobiFS&rp_floor=1.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        sasParams.setSiteIncId(1397202244813823l);
        sasParams.setCountryId(94l);
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.1397202244813823&app.domain=com.inmobi-exchange&ua=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&accept.apis=5&p_block_keys=blk1397202244813823%2CInMobiFS&rp_floor=1.0&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpidsha1=1234202cb962ac59075b964b07152d234b705432&device.dpid_type=udid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

        dcpRubiconAdNetwork.getNingRequestBuilder();
    }

    @Test
    public void testDCPrubiconRequestUriWithSpecificSlot() throws Exception {
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
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("a360");
        final String externalKey = "38132";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        final String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&accept.apis=3&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpidmd5=202cb962ac59075b964b07152d234b70&device.dpid_type=open-udid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

    }

    @Test
    public void testDCPrubiconRequestUriWithGPID() throws Exception {
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

        casInternalRequestParameters.setGpid("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setUidADT("1");

        sasParams.setSiteIncId(6575868);
        sasParams.setSource("APP");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSdkVersion("a360");
        final String externalKey = "38132";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<>(), 0.0d, null, null,
                        0, new Integer[] {0}));
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper);
        final String actualUrl = dcpRubiconAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://staged-by.rubiconproject.com/a/api/server.js?account_id=11726&rp_pmp_tier=2&zone_id=160212&app.bundle=com.inmobi-exchange.6575868&app.domain=com.inmobi-exchange&ua=Mozilla&ip=206.29.182.240&site_id=38132&device.os=Android&size_id=43&geo.latitude=37.4429&geo.longitude=-122.1514&device.connectiontype=0&i.aq_sensitivity=high&app.rating=4+&accept.apis=3&p_block_keys=blk6575868%2CInMobiFS&rp_floor=0.1&i.category=Business&i.iab=IAB4%2CIAB19-15%2CIAB5-15%2CIAB3&device.dpid=202cb962ac59075b964b07152d234b70&device.dpid_type=gaid&kw=38132";
        assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
        assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());

    }


    @Test
    public void testDCPrubiconParseAdWap() throws Exception {
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
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"19100\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "{\"status\" : \"ok\",\"tracking\" : \"affiliate-1234\",\"inventory\" : { \"deals\" : \"12345,98765\" },\"ads\" : [{\"status\" : \"ok\",\"impression_id\" : \"ed4122f3-f4ac-477b-9abd-89c44f252100\",\"size_id\" : \"2\",\"advertiser\" : 7,\"network\" : 123,\"seat\" : 456,\"deal\" : 789,\"type\" : \"MRAIDv2\",\"creativeapi\" : 1000,\"impression_url\" : \"http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100\",\"script\" :\"<div>testing rubicon</div>\"}]}";
        dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpRubiconAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script><div>testing rubicon</div></script><img src='http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpRubiconAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPrubiconParseAdApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setOsId(HandSetOS.Android.getValue());
        sasParams.setSource("APP");
        casInternalRequestParameters.setUid("23e2ewq445545saasw232323");
        final String externalKey = "19100";

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"3\":\"160212\",\"site\":\"19100\"}"), new ArrayList<>(), 0.0d, null, null,
                        32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        final String response =
                "{\"status\" : \"ok\",\"tracking\" : \"affiliate-1234\",\"inventory\" : { \"deals\" : \"12345,98765\" },\"ads\" : [{\"status\" : \"ok\",\"impression_id\" : \"ed4122f3-f4ac-477b-9abd-89c44f252100\",\"size_id\" : \"2\",\"advertiser\" : 7,\"network\" : 123,\"seat\" : 456,\"deal\" : 789,\"type\" : \"MRAIDv2\",\"creativeapi\" : 1000,\"impression_url\" : \"http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100\",\"script\" :\"<div>testing rubicon</div>\"}]}";
        dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(200, dcpRubiconAdNetwork.getHttpResponseStatusCode());
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script><div>testing rubicon</div></script><img src='http://ad.tracker/impression/ed4122f3-f4ac-477b-9abd-89c44f252100' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpRubiconAdNetwork.getHttpResponseContent());
    }


    @Test
    public void testDCPrubiconParseNoAd() throws Exception {
        final String response = "";
        dcpRubiconAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcpRubiconAdNetwork.getHttpResponseStatusCode());
    }


    @Test
    public void testDCPrubiconGetId() throws Exception {
        assertEquals(rubiconAdvId, dcpRubiconAdNetwork.getId());
    }

    @Test
    public void testDCPrubiconGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rubiconAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpRubiconAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcpRubiconAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPrubiconGetName() throws Exception {
        assertEquals("rubiconDCP", dcpRubiconAdNetwork.getName());
    }

}
