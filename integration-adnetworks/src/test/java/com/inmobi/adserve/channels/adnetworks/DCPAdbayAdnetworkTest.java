package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.annotations.Test;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.adbay.DCPAdbayAdnetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;
import junit.framework.TestCase;

/**
 * Created by deepak on 29/5/15.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPAdbayAdnetworkTest  extends TestCase{

    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;
    private String AdbayHost = "http://ad.about.co.kr/mad/json/InMobi/total01/bottom_middle";
    private String AdbayAdvId = "123qwe";
    private DCPAdbayAdnetwork dcpadbayadnetwork;
    private RepositoryHelper repositoryHelper;


    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("adbaydcp.host")).andReturn(AdbayHost).anyTimes();
        expect(mockConfig.getString("adbaydcp.advertiserId")).andReturn(AdbayAdvId).anyTimes();
        expect(mockConfig.getString("adbaydcp.status")).andReturn("on").anyTimes();
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
        Formatter.init();
        final Channel serverChannel = createMock(Channel.class);
        final HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        prepareMockConfig();
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

        dcpadbayadnetwork = new DCPAdbayAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpadbayadnetwork.setName("Adbay");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpadbayadnetwork.setHost(AdbayHost);
    }


    @Test
    public void testDCPAdbayConfigureParameters() {

        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setGpid("gpidtest123");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
            .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testAdbayRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "123qwe";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpadbayadnetwork.getRequestUri().toString();
            final String expectedUrl =
                "http://ad.about.co.kr/mad/json/InMobi/total01/bottom_middle";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPAdbayParseResponse() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        //casInternalRequestParameters.setUdid("weweweweee");
        casInternalRequestParameters.setUid("uid");
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("app");
        sasParams.setSdkVersion("i360");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSiteContentType(ContentType.PERFORMANCE);
        final String externalKey = "19100";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"//www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "<html xmlns=\"//www.w3.org/1999/xhtml\" lang=\"ko\">\n" + "<head>\n"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0,maximum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\" />\n"
                    + "\n" + "<title>AdBay</title>\n" + "<script type=\"text/javascript\">\n"
                    + "if (typeof window.jQuery == 'undefined') { document.writeln('<scri'+'pt type=\"text/javascri'+'pt\" src=\"http://scri'+'pt.about.co.kr/os2/common/js/jquery-1.8.2.min.js\"></scri'+'pt>'); }\n"
                    + "document.writeln('<scr'+'ipt type=\"text/javascript\" src=\"http://script.about.co.kr/templates/script/cm/jquery.base64.js\"></scr'+'ipt>');\n"
                    + "document.writeln('<scr'+'ipt type=\"text/javascript\" src=\"http://script.about.co.kr/templates/script/cm/adbay.controller.v1.5.js\"></scr'+'ipt>');\n"
                    + "var _adbayInventoryData = { INV_WIDTH : 320, INV_HEIGHT : 50, GOODS_WIDTH : 0, GOODS_HEIGHT : 0, JSONP_URL : \"http://adapi.about.co.kr/mad/jsonp/publishtest/main01/top_left\", INV_UIS : \"bn\" };\n"
                    + "</script>\n" + "\n" + "</head>\n" + "<body>\n"
                    + "<div id=\"adbayHouseContent\" style=\"display:none;\"><iframe id=\"adbayHouseContentFrame\" src=\"\" frameborder=\"0\" scrolling=\"no\" topmargin=\"0\" leftmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" width=\"320%\" height=\"50\"></iframe></div>\n"
                    + "\n" + "<!-- bn -->\n"
                    + "<link rel='stylesheet' type='text/css' href='http://script.about.co.kr/templates/css/bn/mobile/v1/banner.css' />\n"
                    + "<script type=\"text/javascript\">\n" + "function openLink(url)\n" + "{\n"
                    + "\tvar newWin = window.open(url, '_blank');\n" + "}\n" + "function adbayBannerCallback()\n"
                    + "{\t\n" + "\tif(adbayAdsContents['creatives'][0]['target'] == \"_top\")\n" + "\t{\n"
                    + "\t\tvar code\t= '<a class=\"bnr\" href=\"'+adbayAdsContents['creatives'][0]['click']+'\" target=\"_top\"><img src=\"'+adbayAdsContents['creatives'][0]['src']+'\" width=\"'+adbayAdsContents['creatives'][0]['w']+'\" height=\"'+adbayAdsContents['creatives'][0]['h']+'\" /></a>';\n"
                    + "\t}\n" + "\telse\n" + "\t{\n"
                    + "\t\tvar code\t= '<a class=\"bnr\" href=\"javascript:openLink(\\''+adbayAdsContents['creatives'][0]['click']+'\\');\"><img src=\"'+adbayAdsContents['creatives'][0]['src']+'\" width=\"'+adbayAdsContents['creatives'][0]['w']+'\" height=\"'+adbayAdsContents['creatives'][0]['h']+'\" /></a>';\n"
                    + "\t}\n" + "\t$(\"#adbay_bn_layer\").html(code);\n" + "\t$(\"#mobild_bnr\").show();\n"
                    + "\t$(\"#adbay_bn_layer\").css('background', adbayAdsContents['bg-color']);\n"
                    + "noImageCheck();\n" + "}\n" + "</script>\n"
                    + "<div class='mobild_bnr' id='mobild_bnr' style='display:none;'>\n"
                    + "<div class='inner' style='background:lime'>\n" + "<div class='in' id='adbay_bn_layer'>\n"
                    + "</div>\n" + "</div>\n" + "</div>\n" + "</body>\n" + "</html>";
        dcpadbayadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpadbayadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"//www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
                    + "<html xmlns=\"//www.w3.org/1999/xhtml\" lang=\"ko\">\n" + "<head>\n"
                    + "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n"
                    + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0,maximum-scale=1.0, user-scalable=no, target-densitydpi=medium-dpi\" />\n"
                    + "\n" + "<title>AdBay</title>\n" + "<script type=\"text/javascript\">\n"
                    + "if (typeof window.jQuery == 'undefined') { document.writeln('<scri'+'pt type=\"text/javascri'+'pt\" src=\"http://scri'+'pt.about.co.kr/os2/common/js/jquery-1.8.2.min.js\"></scri'+'pt>'); }\n"
                    + "document.writeln('<scr'+'ipt type=\"text/javascript\" src=\"http://script.about.co.kr/templates/script/cm/jquery.base64.js\"></scr'+'ipt>');\n"
                    + "document.writeln('<scr'+'ipt type=\"text/javascript\" src=\"http://script.about.co.kr/templates/script/cm/adbay.controller.v1.5.js\"></scr'+'ipt>');\n"
                    + "var _adbayInventoryData = { INV_WIDTH : 320, INV_HEIGHT : 50, GOODS_WIDTH : 0, GOODS_HEIGHT : 0, JSONP_URL : \"http://adapi.about.co.kr/mad/jsonp/publishtest/main01/top_left\", INV_UIS : \"bn\" };\n"
                    + "</script>\n" + "\n" + "</head>\n" + "<body>\n"
                    + "<div id=\"adbayHouseContent\" style=\"display:none;\"><iframe id=\"adbayHouseContentFrame\" src=\"\" frameborder=\"0\" scrolling=\"no\" topmargin=\"0\" leftmargin=\"0\" marginwidth=\"0\" marginheight=\"0\" width=\"320%\" height=\"50\"></iframe></div>\n"
                    + "\n" + "<!-- bn -->\n"
                    + "<link rel='stylesheet' type='text/css' href='http://script.about.co.kr/templates/css/bn/mobile/v1/banner.css' />\n"
                    + "<script type=\"text/javascript\">\n" + "function openLink(url)\n" + "{\n"
                    + "\tvar newWin = window.open(url, '_blank');\n" + "}\n" + "function adbayBannerCallback()\n"
                    + "{\t\n" + "\tif(adbayAdsContents['creatives'][0]['target'] == \"_top\")\n" + "\t{\n"
                    + "\t\tvar code\t= '<a class=\"bnr\" href=\"'+adbayAdsContents['creatives'][0]['click']+'\" target=\"_top\"><img src=\"'+adbayAdsContents['creatives'][0]['src']+'\" width=\"'+adbayAdsContents['creatives'][0]['w']+'\" height=\"'+adbayAdsContents['creatives'][0]['h']+'\" /></a>';\n"
                    + "\t}\n" + "\telse\n" + "\t{\n"
                    + "\t\tvar code\t= '<a class=\"bnr\" href=\"javascript:openLink(\\''+adbayAdsContents['creatives'][0]['click']+'\\');\"><img src=\"'+adbayAdsContents['creatives'][0]['src']+'\" width=\"'+adbayAdsContents['creatives'][0]['w']+'\" height=\"'+adbayAdsContents['creatives'][0]['h']+'\" /></a>';\n"
                    + "\t}\n" + "\t$(\"#adbay_bn_layer\").html(code);\n" + "\t$(\"#mobild_bnr\").show();\n"
                    + "\t$(\"#adbay_bn_layer\").css('background', adbayAdsContents['bg-color']);\n"
                    + "noImageCheck();\n" + "}\n" + "</script>\n"
                    + "<div class='mobild_bnr' id='mobild_bnr' style='display:none;'>\n"
                    + "<div class='inner' style='background:lime'>\n" + "<div class='in' id='adbay_bn_layer'>\n"
                    + "</div>\n" + "</div>\n" + "</div>\n" + "</body>\n"
                    + "</html><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
            dcpadbayadnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdbayGetName() throws Exception {
        assertEquals(dcpadbayadnetwork.getName(), "adbayDCP");
    }
}
