package com.inmobi.adserve.channels.adnetworks;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.channels.adnetworks.googleadx.GoogleAdXAdNetwork;
import com.inmobi.adserve.channels.api.BaseAdNetworkImpl;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.IPRepository;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.SlotSizeMapEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.types.DeviceType;

import io.netty.channel.Channel;

/**
 * Created by naresh.kapse on 24/05/14.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class GoogleAdXNetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final String googleAdXStatus = "on";
    private static final String googleAdXhost = "http://www.google.com";
    private static final String inmobiAdvertiserID = "inmobi_advertiser_id";
    private static final String googleAdXPublisherID = "ca-pub-7457767528341420";
    private static Configuration mockConfig = null;
    private static GoogleAdXAdNetwork googleAdXNetwork;
    private static RepositoryHelper repositoryHelper;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("googleadx.status")).andReturn(googleAdXStatus).anyTimes();
        expect(mockConfig.getString("googleadx.advertiserId")).andReturn(inmobiAdvertiserID).anyTimes();
        expect(mockConfig.getString("googleadx.googleAdXPublisherID")).andReturn(googleAdXPublisherID).anyTimes();
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
        googleAdXNetwork = new GoogleAdXAdNetwork(mockConfig, null, base, serverChannel);

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        googleAdXNetwork.setHost(googleAdXhost);
    }

    @Test
    public void testGoogleAdXNetworkConfigureParameters() throws JSONException {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        assertTrue(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));


        // If we know the request is from FeaturePhones Or Opera then return false
        sasParams.setDeviceType(DeviceType.FEATURE_PHONE);
        entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));

        assertFalse(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));

        // If we know the request is from FeaturePhones Or Opera then return false
        sasParams
                .setUserAgent("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (S60; SymbOS; Opera Mobi/23.348; U; en) Presto/2.5.25 Version/10.54");
        entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));

        assertFalse(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper));
    }

    @Test
    public void testGoogleAdXNetworkResponseWithReferralURl() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setReferralUrl("http://www.referral.inmobi.com");
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.referral.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());

        sasParams.setReferralUrl("http://www.referral.inmobi.com?param=value");
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);
        expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.referral.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());

    }

    @Test
    public void testGoogleAdXNetworkResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        final String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
    }

    @Test
    public void testGoogleAdXNetworkResponseForDFP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\", \"useDFP\":true}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        final String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body>\n"
                        + "<script type='text/javascript'>\n" + "  var googletag = googletag || {};\n"
                        + "  googletag.cmd = googletag.cmd || [];\n" + "  (function() {\n"
                        + "    var gads = document.createElement('script');\n" + "    gads.async = true;\n"
                        + "    gads.type = 'text/javascript';\n"
                        + "    var useSSL = 'https:' == document.location.protocol;\n"
                        + "    gads.src = (useSSL ? 'https:' : 'http:') +\n"
                        + "      '//www.googletagservices.com/tag/js/gpt.js';\n"
                        + "    var node = document.getElementsByTagName('script')[0];\n"
                        + "    node.parentNode.insertBefore(gads, node);\n" + "  })();\n" + "</script>\n" + "\n"
                        + "<script type='text/javascript'>\n" + "  googletag.cmd.push(function() {\n"
                        + "    googletag.pubads().set(\"page_url\", \"http://www.inmobi.com\");\n"
                        + "    googletag.defineSlot('/14503685/AdUnitForAdx', [[320, 50]], 'div-gpt-ad-1435740571311-0').setTargeting('adxtagid', ['8a809449013c3c643cad82cb412b5857']).addService(googletag.pubads());\n"
                        + "    googletag.pubads().addEventListener('slotRenderEnded', function(event) {\n"
                        + "        if (!event.isEmpty) {\n"
                        + "            var beaconElement = document.createElement(\"img\");\n"
                        + "            beaconElement.src = \"beaconUrl\";\n"
                        + "            beaconElement.style = \"display:none\";\n"
                        + "            beaconElement.height = 1;\n" + "            beaconElement.width = 1;\n"
                        + "            beaconElement.border = 0;\n"
                        + "            document.getElementById(\"div-gpt-ad-1435740571311-0\").appendChild(beaconElement);\n"
                        + "          }\n" + "    });\n" + "    googletag.enableServices();\n" + "  });\n"
                        + "</script>\n" + "<!-- /14503685/AdUnitForAdx -->\n"
                        + "<div id='div-gpt-ad-1435740571311-0'>\n" + "<script type='text/javascript'>\n"
                        + "googletag.cmd.push(function() { googletag.display('div-gpt-ad-1435740571311-0'); });\n"
                        + "</script>\n" + "</div></body></html>\n";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
    }

    @Test
    public void testGoogleAdXNetworkResponseForAppTraffic() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setSource("APP");
        // Setting slot for 320x50
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 15, repositoryHelper);
        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
    }
}
