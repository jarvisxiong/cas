package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.tencentdcp.TencentAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.NativeAdTemplateEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.template.config.DefaultConfiguration;
import com.inmobi.template.config.DefaultGsonDeserializerConfiguration;
import com.inmobi.template.gson.GsonManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.configuration.Configuration;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.support.membermodification.MemberModifier;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

/**
 * Created by thushara.v on 3/11/16.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({BaseAdNetworkImpl.class,NativeAdTemplateEntity.class})
public class DCPTencentAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String tencentHost = "http://ac.o2.qq.com/php/mbshow.php";
    private static final String tencentStatus = "on";
    private static final String tencentAdvId = "nexageadv1";
    private static Configuration mockConfig = null;
    private static TencentAdnetwork tencentAdnetwork;
    private static RepositoryHelper repositoryHelper;
    private static final Bootstrap clientBootstrap = null;


    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("tencento2.host")).andReturn(tencentHost).anyTimes();
        expect(mockConfig.getString("tenceto2.status")).andReturn(tencentStatus).anyTimes();
        expect(mockConfig.getString("tencento2.advertiserId")).andReturn(tencentAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @Before
    public void setUp() throws Exception {

        DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        defaultConfiguration.setGsonManager(new GsonManager(new DefaultGsonDeserializerConfiguration()));
        MemberModifier.field(TencentAdnetwork.class, "templateConfiguration").set(TencentAdnetwork.class, defaultConfiguration);

        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
        Formatter.init();
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
        tencentAdnetwork = new TencentAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        tencentAdnetwork.setName("tencento2");
        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);
        tencentAdnetwork.setHost(tencentHost);
    }

    @Test
    public void testTencentRequest() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setIem("xxxx");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final java.util.List<Long> cat = new ArrayList<Long>();
        cat.add(13l);
        sasParams.setCategories(cat);
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "10008703";
        AdapterTestHelper.setInmobiAdTrackerBuilderFactoryForTest(tencentAdnetwork);
        AdapterTestHelper.setBeaconAndClickStubs();

        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(tencentAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject(
                                "{\"slot\":\"6964_e2ab6d06fe5317dbb47628dbd5f83f40\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(tencentAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 9, repositoryHelper));
        final String actualUrl = tencentAdnetwork.getRequestUri().toString();
        final String expectedUrl = "http://ac.o2.qq.com/php/mbshow.php?channel=10008703&loc_id=6964_e2ab6d06fe5317dbb47628dbd5f83f40&imei=xxxx&inmobi_site_id=00000000-0000-0020-0000-000000000000";
        assertEquals(actualUrl,expectedUrl);
        final String response = "{\"sch_id\":\"344\",\"gid\":\"107341\",\"tag\":\"0\",\"loc_id\":\"6964_e2ab6d06fe5317dbb47628dbd5f83f40\",\"mtr_id\":\"2294\",\"appkey\":\"\",\"channel\":\"10008703\",\"res_url\":\"http://ossweb-img.qq.com/htdocs/o2m/material/20160310184808_1857615022.jpg\",\"width\":\"300\",\"height\":\"250\",\"package\":\"\",\"app_name\":\"\",\"description\":\"\",\"download_url\":\"https://itunes.apple.com/cn/app/id989080154\",\"click_3p\":\"\",\"pv_url\":\"http://ac.o2.qq.com/php/mbreportshowresult.php?loc_id=6964_e2ab6d06fe5317dbb47628dbd5f83f40&sch_id=344&mtr_id=2294&gid=107341&channel=10008703&tag=0&imei=xxxx&ifa=&os=&appkey=&source=\",\"cv_url\":\"http://ac.o2.qq.com/php/mbclick.php?loc_id=6964_e2ab6d06fe5317dbb47628dbd5f83f40&sch_id=344&mtr_id=2294&gid=107341&channel=10008703&tag=0&imei=xxxx&ifa=&os=&appkey=&source=&sign=ac74fb364c5c9fcada4687d6705367f2\",\"isDefault\":\"0\"}";
        tencentAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(tencentAdnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(tencentAdnetwork.getResponseContent(),"<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><a href='https://itunes.apple.com/cn/app/id989080154' onclick=\"document.getElementById('click').src='clickUrl';document.getElementById('partnerClick').src='http://ac.o2.qq.com/php/mbclick.php?loc_id=6964_e2ab6d06fe5317dbb47628dbd5f83f40&sch_id=344&mtr_id=2294&gid=107341&channel=10008703&tag=0&imei=xxxx&ifa=&os=&appkey=&source=&sign=ac74fb364c5c9fcada4687d6705367f2';\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://ossweb-img.qq.com/htdocs/o2m/material/20160310184808_1857615022.jpg'  /></a><img src='http://ac.o2.qq.com/php/mbreportshowresult.php?loc_id=6964_e2ab6d06fe5317dbb47628dbd5f83f40&sch_id=344&mtr_id=2294&gid=107341&channel=10008703&tag=0&imei=xxxx&ifa=&os=&appkey=&source=' height=1 width=1 border=0 style=\"display:none;\"/><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/><img id=\"partnerClick\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>");
    }

}
