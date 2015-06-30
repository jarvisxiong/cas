package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.adnetworks.openx.OpenxAdNetwork;
import com.inmobi.adserve.channels.adnetworks.tapit.DCPTapitAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.SASRequestParameters.HandSetOS;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class AdNetworksTest {
    private static final String debug = "debug";
    private static final String openxAdvertiserId = "9999";
    private static final String openxHost = "http://openx.com/get?auid=";
    private static final Bootstrap clientBootstrap = null;
    private static final String tapitHost = "http://r.tapit.com/adrequest.php";
    private static final String tapitStatus = "on";
    private static final String tapitResponseFormat = "json";
    private static final String tapitAdvId = "54321";
    private static final String tapitTest = "0";
    private static Configuration mockConfig = null;
    private static OpenxAdNetwork openxAdNetwork;
    private static DCPTapitAdNetwork dcpTapitAdNetwork;
    private static RepositoryHelper repositoryHelper;


    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("tapit.host")).andReturn(tapitHost).anyTimes();
        expect(mockConfig.getString("tapit.status")).andReturn(tapitStatus).anyTimes();
        expect(mockConfig.getString("tapit.responseFormat")).andReturn(tapitResponseFormat).anyTimes();
        expect(mockConfig.getString("tapit.test")).andReturn(tapitTest).anyTimes();
        expect(mockConfig.getString("tapit.advertiserId")).andReturn(tapitAdvId).anyTimes();
        expect(mockConfig.getString("openx.host")).andReturn(openxHost).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        File f;
        f = new File("/tmp/channel-server.properties");
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor4 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor4.getDimension()).andReturn(new Dimension(300, 50)).anyTimes();
        replay(slotSizeMapEntityFor4);
        final SlotSizeMapEntity slotSizeMapEntityFor14 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor14.getDimension()).andReturn(new Dimension(320, 480)).anyTimes();
        replay(slotSizeMapEntityFor14);
        final SlotSizeMapEntity slotSizeMapEntityFor15 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor15.getDimension()).andReturn(new Dimension(320, 50)).anyTimes();
        replay(slotSizeMapEntityFor15);
        final SlotSizeMapEntity slotSizeMapEntityForNull = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityForNull.getDimension()).andReturn(new Dimension(0, 0)).anyTimes();
        replay(slotSizeMapEntityForNull);
        repositoryHelper = createMock(RepositoryHelper.class);
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4))
                .andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14))
                .andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15))
                .andReturn(slotSizeMapEntityFor15).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository(Short.MAX_VALUE))
                .andReturn(slotSizeMapEntityForNull).anyTimes();
        replay(repositoryHelper);
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        openxAdNetwork = new OpenxAdNetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpTapitAdNetwork = new DCPTapitAdNetwork(mockConfig, clientBootstrap, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        openxAdNetwork.setHost("http://localhost:8080");
        dcpTapitAdNetwork.setHost("http://localhost:8080");
    }

    public static ChannelSegmentEntity.Builder getChannelSegmentEntityBuilder(final String advertiserId,
            final String adgroupId, final String[] adIds, final String channelId, final long platformTargeting,
            final Long[] rcList, final Long[] tags, final boolean status, final boolean isTestMode,
            final String externalSiteKey, final Timestamp modified_on, final String campaignId, final Long[] slotIds,
            final Long[] incIds, final boolean allTags, final String pricingModel, final Integer[] siteRatings,
            final int targetingPlatform, final ArrayList<Integer> osIds, final boolean udIdRequired,
            final boolean zipCodeRequired, final boolean latlongRequired, final boolean richMediaOnly,
            final boolean appUrlEnabled, final boolean interstitialOnly, final boolean nonInterstitialOnly,
            final boolean stripUdId, final boolean stripZipCode, final boolean stripLatlong,
            final JSONObject additionalParams, final List<Long> manufModelTargetingList, final double ecpmBoost,
            final Timestamp eCPMBoostDate, final Long[] tod, final long adGroupIncId, final Integer[] AdFormatIds) {
        final ChannelSegmentEntity.Builder builder = ChannelSegmentEntity.newBuilder();
        builder.setAdvertiserId(advertiserId);
        builder.setAdvertiserId(advertiserId);
        builder.setAdgroupId(adgroupId);
        builder.setAdIds(adIds);
        builder.setChannelId(channelId);
        builder.setPlatformTargeting(platformTargeting);
        builder.setRcList(rcList);
        builder.setTags(tags);
        builder.setCategoryTaxonomy(tags);
        builder.setAllTags(allTags);
        builder.setStatus(status);
        builder.setTestMode(isTestMode);
        builder.setExternalSiteKey(externalSiteKey);
        builder.setModified_on(modified_on);
        builder.setCampaignId(campaignId);
        builder.setSlotIds(slotIds);
        builder.setIncIds(incIds);
        builder.setAdgroupIncId(incIds[0]);
        builder.setPricingModel(pricingModel);
        builder.setSiteRatings(siteRatings);
        builder.setTargetingPlatform(targetingPlatform);
        builder.setOsIds(osIds);
        builder.setUdIdRequired(udIdRequired);
        builder.setLatlongRequired(latlongRequired);
        builder.setStripZipCode(zipCodeRequired);
        builder.setRestrictedToRichMediaOnly(richMediaOnly);
        builder.setAppUrlEnabled(appUrlEnabled);
        builder.setInterstitialOnly(interstitialOnly);
        builder.setNonInterstitialOnly(nonInterstitialOnly);
        builder.setStripUdId(stripUdId);
        builder.setStripLatlong(stripLatlong);
        builder.setStripZipCode(stripZipCode);
        builder.setAdditionalParams(additionalParams);
        builder.setManufModelTargetingList(manufModelTargetingList);
        builder.setEcpmBoost(ecpmBoost);
        builder.setEcpmBoostExpiryDate(eCPMBoostDate);
        builder.setTod(tod);
        builder.setAdgroupIncId(adGroupIncId);
        builder.setAdFormatIds(AdFormatIds);
        return builder;
    }

    @Test
    public void testDCPTapitConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");

        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true,
                dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPTapitConfigureParametersForBlockingOpera() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Opera%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper));
    }

    @Test
    public void testDCPTapitConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false,
                dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper));
    }

    @Test
    public void testOpenxRequestUriWithIFA() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("1234");
        casInternalRequestParameters.setUidIFA("dfjksahfdjksahdkaw2e23231");
        sasParams.setCountryCode("us");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        final String externalKey = "118398";
        sasParams.setSiteIncId(18);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String actualUrl = openxAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did.ia=dfjksahfdjksahdkaw2e23231&did.iat=1&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testOpenxRequestUriWithO1() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("1234");
        casInternalRequestParameters.setUidO1("dfjksahfdjksahdkaw2e23231");
        sasParams.setOsId(HandSetOS.iOS.getValue());
        sasParams.setCountryCode("us");
        final String externalKey = "118398";
        sasParams.setSiteIncId(18);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String actualUrl = openxAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did.iat=1&did.o1=dfjksahfdjksahdkaw2e23231&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testOpenxRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("1234");
        sasParams.setCountryCode("us");
        final String externalKey = "118398";
        sasParams.setSiteIncId(18);
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, new JSONObject(),
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String actualUrl = openxAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://openx.com/get?auid=118398&cnt=us&ip=206.29.182.240&lat=37.4429&lon=-122.1514&lt=3&c.siteId=00000000-0000-0020-0000-000000000012&did=1234";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPTapitRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.setUidIFA("202cb962ac59075b964b07152d234b70");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&lat=37.4429&long=-122.1514&enctype=raw&idfa=202cb962ac59075b964b07152d234b70&w=320.0&h=50.0&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPTapitRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong(" ,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("iphone");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper)) {
            final String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&enctype=sha1&udid=202cb962ac59075b964b07152d234b70&w=320.0&h=50.0&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPTapitRequestUriBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("android");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, Short.MAX_VALUE, repositoryHelper)) {
            final String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&lat=37.4429&long=-122.1514&enctype=md5&udid=202cb962ac59075b964b07152d234b70&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPTapitRequestUriWithGPID() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteIncId(18);
        casInternalRequestParameters.setGpid("ASAD-SDSSD-SDADADAD-AAQW");
        casInternalRequestParameters.setTrackingAllowed(true);
        casInternalRequestParameters.setUidMd5("202cb962ac59075b964b07152d234b70");
        sasParams.setSource("android");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {32L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, Short.MAX_VALUE, repositoryHelper)) {
            final String actualUrl = dcpTapitAdNetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://r.tapit.com/adrequest.php?format=json&ip=206.29.182.240&ua=Mozilla&zone=19100&lat=37.4429&long=-122.1514&enctype=md5&udid=202cb962ac59075b964b07152d234b70&adid=ASAD-SDSSD-SDADADAD-AAQW&tpsid=00000000-0000-0020-0000-000000000012";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testOpenxResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);

        final String response =
                "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseWap() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);

        final String response =
                "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);

        final String response =
                "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testOpenxResponseAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(openxAdvertiserId, null, null, null, 0, null,
                        null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null,
                        false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));

        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getClickUrl")).toReturn("clickUrl");
        openxAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper);

        final String response =
                "<div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script>    ";
        openxAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(openxAdNetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><div id='beacon_79353898' style='position: absolute; left: 0px; top: 0px; visibility: hidden;'><img src='http://mediaservices-d.openxenterprise.com/ma/1.0/ri?ai=147ef9de-ac38-70bf-b4aa-366291a13406&ts=1fHNpZD00NjU0OXxhdWlkPTIyMjAwOXxhaWQ9NTQ3NjM3fHB1Yj01ODg0NXxsaWQ9MzEzOTA5fHQ9MTB8cmlkPTliMTg5NzE3LTc3NWQtNGEyYS1iMjk0LTZmZGU0NGIwZjQzNHxvaWQ9Mjk1MzZ8Ym09QlVZSU5HLk5PTkdVQVJBTlRFRUR8cGM9VVNEfHA9MzAwfGFjPVVTRHxwbT1QUklDSU5HLkNQTXxydD0xMzQ5OTUxODE2fHByPTMwMHxhZHY9MjAzODU&cb=79353898'/></div><script src=\"http://sjc.ads.nexage.com/js/admax/admax_api.js\"></script><script>var suid = getSuid();var admax_vars = { dcn: \"8a809449013333b278e3f67d30e90d65\",        cn: \"inMobi\",   pos: \"inmobironentertainment_320x50\",           \"req(loc)\": \"32.759,-97.333\"};if (suid){    admax_vars[\"u(id)\"]=suid;}admaxAd(admax_vars);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                openxAdNetwork.getHttpResponseContent());
    }

    @Test
    public void testTapitParseResponseBanner() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getClickUrl")).toReturn("clickUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String response =
                    "{\"type\":\"banner\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348' onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://i.tapit.com/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50'  width='320'   height='50' /></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlWap() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String response =
                    "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String response =
                    "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseHtmlAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 14, repositoryHelper)) {
            final String response =
                    "{\"type\":\"html\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"\",\"adtext\":\"\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&cid=106800&adtype=1&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&adnetwork=1&tpsid=1659216000069348&w=320&h=50\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href=\"http://c.tapit.com/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\" target=\"_blank\"><img src=\"http://i.tapit.com/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\" width=\"320\" height=\"50\" alt=\"\"/></a><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseTextWap() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getClickUrl")).toReturn("clickUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String response =
                    "{\"type\":\"text\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"sample title\",\"adtext\":\"sample text\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><a style=\"text-decoration:none; \" href=\"http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\" onclick=\"document.getElementById('click').src='clickUrl';\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">sample text</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='beaconUrl' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testTapitParseResponseTextAppSDK360() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(getChannelSegmentEntityBuilder(tapitAdvId, null, null, null, 0, null, null,
                        true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false,
                        false, false, false, false, false, false, false, false, false, null, new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        MemberModifier.suppress(BaseAdNetworkImpl.class.getDeclaredMethod("buildInmobiAdTracker"));
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getBeaconUrl")).toReturn("beaconUrl");
        MemberModifier.stub(BaseAdNetworkImpl.class.getDeclaredMethod("getClickUrl")).toReturn("clickUrl");
        if (dcpTapitAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String response =
                    "{\"type\":\"text\",\"html\":\"<a href=\\\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;w=320.0&amp;h=50.0&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;adnetwork=1&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;tpsid=1659216000069348\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/i.tapit.com\\/adimage.php?zone=6579&amp;cid=106800&amp;adtype=1&amp;xid=dd3b9df3694433f01389c222f8059f39&amp;ip=174.141.213.29&amp;udid=&amp;ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&amp;adnetwork=1&amp;tpsid=1659216000069348&amp;w=320&amp;h=50\\\" width=\\\"320\\\" height=\\\"50\\\" alt=\\\"\\\"\\/><\\/a>\",\"adId\":\"106800\",\"adWidth\":\"320\",\"adHeight\":\"50\",\"cpc\":0.025,\"adtitle\":\"sample title\",\"adtext\":\"sample text\",\"clickurl\":\"http:\\/\\/c.tapit.com\\/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\",\"imageurl\":\"\",\"domain\":\"c.tapit.com\"}";
            dcpTapitAdNetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(200, dcpTapitAdNetwork.getHttpResponseStatusCode());
            assertEquals(
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348\" onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://c.tapit.com/advalidate.php?zone=6579&cid=106800&adtype=1&w=320.0&h=50.0&xid=dd3b9df3694433f01389c222f8059f39&ip=174.141.213.29&udid=&adnetwork=1&ua=Mozilla%2F5.0+%28Linux%3B+U%3B+Android+2.3.4%3B+en-us%3B+LG-MS910+Build%2FGINGERBREAD%29+AppleWebKit%2F533.1+%28KHTML%2C+like+Gecko%29+Version%2F4.0+Mobile+Safari%2F533.1&tpsid=1659216000069348'); return false;\" target=\"_blank\"><div class=\"container template_300_50 adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">sample text</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='beaconUrl' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
                    dcpTapitAdNetwork.getHttpResponseContent());
        }
    }

}
