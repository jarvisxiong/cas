package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

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
import junit.framework.TestCase;

/**
 * Created by naresh.kapse on 24/05/14.
 */
public class GoogleAdXNetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private GoogleAdXAdNetwork googleAdXNetwork;
    private final String googleAdXStatus = "on";
    private final String googleAdXhost = "http://www.google.com";
    private final String inmobiAdvertiserID = "inmobi_advertiser_id";
    private final String googleAdXPublisherID = "ca-pub-7457767528341420";
    private RepositoryHelper repositoryHelper;

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
        assertTrue(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper));


        // If we know the request is from FeaturePhones Or Opera then return false
        sasParams.setDeviceType(DeviceType.FEATURE_PHONE);
        entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));

        assertFalse(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper));

        // If we know the request is from FeaturePhones Or Opera then return false
        sasParams
                .setUserAgent("Opera/9.80 (J2ME/MIDP; Opera Mini/9.80 (S60; SymbOS; Opera Mobi/23.348; U; en) Presto/2.5.25 Version/10.54");
        entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));

        assertFalse(googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, null, (short) 15, repositoryHelper));
    }

    @Test
    public void testGoogleAdXNetworkResponseWithReferralURl() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setReferralUrl("http://www.referral.inmobi.com");
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));

        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.referral.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());

        sasParams.setReferralUrl("http://www.referral.inmobi.com?param=value");
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);
        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);
        expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.referral.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());

    }

    @Test
    public void testGoogleAdXNetworkResponse() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        final String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;google_page_url = \"http://www.inmobi.com\";</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
    }

    @Test
    public void testGoogleAdXNetworkResponseForDFP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        // Setting slot for 320x50
        sasParams.setAppUrl("http://www.inmobi.com");

        final String externalKey = "8a809449013c3c643cad82cb412b5857";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\", \"useDFP\":true}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);

        final String expectedResponse =
                "<html><head><title></title><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type='text/javascript' src='http://www.googletagservices.com/tag/js/gpt.js'>\n"
                        + "  googletag.pubads().set(\"page_url\", \"http://www.inmobi.com\");\n"
                        + "  googletag.pubads().definePassback('/14503685/AdUnitForAdx', [[320, 50]]).setTargeting('adxtagid', ['8a809449013c3c643cad82cb412b5857']).display();\n"
                        + "  googletag.pubads().addEventListener('slotRenderEnded', function(event) {\n"
                        + "        if (!event.isEmpty) {\n"
                        + "            document.write(\"<img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style='display:none;'/>\");        }\n"
                        + "    });\n"
                        + "</script></body></html>";
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
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(googleAdXPublisherID, null,
                        null, null, 0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        new JSONObject("{\"pos\":\"header\"}"), new ArrayList<>(), 0.0d, null, null, 32,
                        new Integer[] {0}));
        googleAdXNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 15, repositoryHelper);

        googleAdXNetwork.generateJsAdResponse();
        assertEquals(googleAdXNetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\">google_ad_client = \"ca-pub-7457767528341420\";google_ad_slot = \"8a809449013c3c643cad82cb412b5857\";google_ad_width = 320;google_ad_height = 50;</script><script type=\"text/javascript\" src=\"//pagead2.googlesyndication.com/pagead/show_ads.js\"></script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, googleAdXNetwork.getHttpResponseContent());
    }
}
