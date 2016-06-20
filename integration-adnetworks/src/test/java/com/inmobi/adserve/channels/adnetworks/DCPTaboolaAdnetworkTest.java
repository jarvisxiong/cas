package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberMatcher;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.adnetworks.taboola.DCPTaboolaAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity.TemplateClass;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.gson.GsonManager;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by thushara.v on 19/06/15.
 */


@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseAdNetworkImpl.class, NativeAdTemplateEntity.class})
public class DCPTaboolaAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String taboolaHost =
            "http://api.taboola.com/1.1/json/inmobi/recommendations.get?app.type=mobile&app.apikey=fc1200c7a7aa52109d762a9f005b149abef01479&rec.visible=false&source.type=text&user.session=init";
    private static final String taboolaNotification =
            "http://api.taboola.com//json/inmobi/recommendations.notify-visible?app.type=mobile&app.apikey=fc1200c7a7aa52109d762a9f005b149abef01479&response.id=%s";
    private static final String taboolaIcon = "http://api2.taboola.com/taboola";
    private static final String taboolaStatus = "on";
    private static final String taboolaAdvId = "taboolaadv1";

    private static Configuration mockConfig = null;
    private static DCPTaboolaAdnetwork dcptaboolaAdNetwork;
    private static RepositoryHelper repositoryHelper;
    private static WapSiteUACEntity wapSiteUACEntity;
    private static NativeAdTemplateEntity templateEntity;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("taboola.host")).andReturn(taboolaHost).anyTimes();
        expect(mockConfig.getString("taboola.status")).andReturn(taboolaStatus).anyTimes();
        expect(mockConfig.getString("taboola.icon")).andReturn(taboolaIcon).anyTimes();
        expect(mockConfig.getString("taboola.notification")).andReturn(taboolaNotification).anyTimes();
        expect(mockConfig.getString("taboola.advertiserId")).andReturn(taboolaAdvId).anyTimes();
        expect(mockConfig.getList("taboola.oemSites")).andReturn(new ArrayList<>(Arrays.asList("5a5b2dcb31804111b3830a686bbe8db6"))).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.setGsonManager(new GsonManager());
        MemberMatcher.field(DCPTaboolaAdnetwork.class, "templateConfiguration").set(DCPTaboolaAdnetwork.class,
                defaultConfiguration);
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }


        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        final Channel serverChannel = createMock(Channel.class);
        prepareMockConfig();
        Formatter.init();
        final SlotSizeMapEntity slotSizeMapEntityFor0 = createMock(SlotSizeMapEntity.class);
        expect(slotSizeMapEntityFor0.getDimension()).andReturn(new Dimension(0, 0)).anyTimes();
        replay(slotSizeMapEntityFor0);
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
        expect(repositoryHelper.querySlotSizeMapRepository((short) 0)).andReturn(slotSizeMapEntityFor0).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15).anyTimes();


        wapSiteUACEntity = createMock(WapSiteUACEntity.class);
        expect(wapSiteUACEntity.isTransparencyEnabled()).andReturn(true).anyTimes();
        expect(wapSiteUACEntity.getAppTitle()).andReturn("Taboola").anyTimes();
        expect(wapSiteUACEntity.getSiteName()).andReturn("Taboola").anyTimes();
        expect(wapSiteUACEntity.getSiteUrl()).andReturn("http://abc.com").anyTimes();
        expect(wapSiteUACEntity.getBundleId()).andReturn("a.b.c").anyTimes();
        replay(wapSiteUACEntity);
        templateEntity = createMock(NativeAdTemplateEntity.class);
        expect(templateEntity.getMandatoryKey()).andReturn("layoutConstraint.1");
        replay(templateEntity);
        expect(repositoryHelper.queryNativeAdTemplateRepository(1l, TemplateClass.STATIC)).andReturn(templateEntity)
                .anyTimes();
        replay(repositoryHelper);

        dcptaboolaAdNetwork = new DCPTaboolaAdnetwork(mockConfig, clientBootstrap, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        final IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
    }

    @Test
    public void testDCPtaboolaConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setOsId(SASRequestParameters.HandSetOS.Android.getValue());
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("23e2ewq445545");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setSiteId("abcd");
        sasParams.setPlacementId(1l);
        sasParams.setWapSiteUACEntity(wapSiteUACEntity);
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(taboolaAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, new Long[] {0L}, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(true, dcptaboolaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 0, repositoryHelper));
    }



    @Test
    public void testDCPtaboolaConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        sasParams.setSiteId("abcd");
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(taboolaAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, new Long[] {0L}, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(false, dcptaboolaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 0, repositoryHelper));
    }


    @Test
    public void testDCPtaboolaRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList(new String[] {"IAB10", "IAB21", "IAB12"}));
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(URLEncoder.encode(
                "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_5 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Mobile/11B601",
                "UTF-8"));
        sasParams.setSource("APP");
        sasParams.setPlacementId(1l);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final java.util.List<Long> category = new ArrayList<Long>();
        category.add(3l);
        sasParams.setCategories(category);
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setSiteIncId(6575868);
        sasParams.setOsId(SASRequestParameters.HandSetOS.Android.getValue());
        sasParams.setSiteId("5a5b2dcb31804111b3830a686bbe8db6");
        sasParams.setRqMkAdcount((short)2);
        final String externalKey = "inmobi";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(taboolaAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));

        dcptaboolaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 0,
                repositoryHelper);
        final String actualUrl = dcptaboolaAdNetwork.getRequestUri().toString();
        final String expectedUrl =
                "http://api.taboola.com/1.1/json/inmobi/recommendations.get?app.type=mobile&app.apikey=fc1200c7a7aa52109d762a9f005b149abef01479&rec.visible=false&source.type=text&user.session=init&rec.count=2&app.name=Taboola&source.id=a.b.c&source.placement=00000000-0000-0000-0000-0000006456fc&source.url=http://abc.com&user.realip=206.29.182.240&user.agent=Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+7_0_5+like+Mac+OS+X%29+AppleWebKit%2F537.51.1+%28KHTML%2C+like+Gecko%29+Mobile%2F11B601&rec.thumbnail.height=150&rec.thumbnail.width=150&user.id=202cb962ac59075b964b07152d234b70";
        assertEquals(actualUrl, expectedUrl);

    }

    @Test
    public void testDCPtaboolaParseNoAd() throws Exception {
        final String response = "";
        dcptaboolaAdNetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(500, dcptaboolaAdNetwork.getHttpResponseStatusCode());
    }


    @Test
    public void testDCPtaboolaGetId() throws Exception {
        assertEquals(taboolaAdvId, dcptaboolaAdNetwork.getId());
    }

    @Test
    public void testDCPtaboolaGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSiteId("abcd");
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(taboolaAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"spot\":\"1_testkey\",\"pubId\":\"inmobi_1\",\"site\":0}"), new ArrayList<>(),
                        0.0d, null, null, 32, new Integer[] {0}));
        dcptaboolaAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 0,
                repositoryHelper);
        assertEquals("4f8d98e2-4bbd-40bc-8795-22da170700f9", dcptaboolaAdNetwork.getImpressionId());
    }

    @Test
    public void testDCPtaboolaGetName() throws Exception {
        assertEquals("taboolaDCP", dcptaboolaAdNetwork.getName());
    }

}

