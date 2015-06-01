package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.adbay.DCPAdbayAdnetwork;
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
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.testng.annotations.Test;

import java.awt.Dimension;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;

/**
 * Created by deepak on 29/5/15.
 */
public class DCPAdbayAdnetworkTest  extends TestCase{

    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private final Bootstrap clientBootstrap = null;
    private String AdbayHost = "http://adapi.about.co.kr/mad/json/publishtest/main01/top_left";
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

    @Override
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
        final String clurl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        final String clurl =
            "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
            .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        final String clurl =
            "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl,
            null, (short) 4, repositoryHelper));
    }

    @Test
    public void testAdbayRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "123qwe";
        final String clurl =
            "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpadbayadnetwork.getRequestUri().toString();
            final String expectedUrl =
                "http://adapi.about.co.kr/mad/json/publishtest/main01/top_left";
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
        final String beaconUrl =
            "http://c2.w.inmobi.com/c"
                + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
            new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null,
                0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, null, beaconUrl, (short) 4, repositoryHelper);
        final String response =
            "{\"redirect\": \"\", \"cb\": \"adbayBannerCallback\", \"creatives\": [{\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150526/e25512d5c64bdbb2c885bd19ce901b6c.jpg\", \"txt\": \"\", \"target\": \"_top\", \"h\": 50, \"w\": 320, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/publishtest/main01/top_left?aW52X2lkeD0xNDA1JnByZF9pZHg9MTEmY2FtX2lkeD00NDImYWRzX2lkeD04MzAmY3R2X2dycF9pZHg9NTc4MSZjdHZfaWR4PTQ5NDEzJnRtcF9pZHg9NTEmZGV2aWNlPTImdWk9Ym4maXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTc0NDUzMDQzNiZyZXNfdGltZT0xNDMyODgwMzYxJnRhZz1qc29uJnN2bm09NWgmcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU0JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4MjQ1Nzc3NSUyNmN0YWclM0Q4MjQ1Nzc3NV8wJTI2bHB0YWclM0RBZGJheV9CX09fMDUxM19hMSUyNmNvdXBhbmdTcmwlM0Q4MjQ1Nzc3NSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9CX08lMjZ1dG1fY2FtcGFpZ24lM0RNb2JpbGUlMjZwcmV2ZW50QnlwYXNzJTNEdHJ1ZQ==\"}], \"logo\": \"\", \"height\": 50, \"size\": 0, \"logo_target\": \"_blank\", \"is_ep\": false, \"bg-color\": \"#585858\", \"width\": 320, \"ui\": \"bn\"}"
                + "\"logo\": \"\",\"height\": 50,\"size\": 0,\"logo_target\": \"_blank\",\"is_ep\": false,\"bg-color\": \"#585858\",\"width\": 320,\"ui\": \"bn\"}";
        dcpadbayadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpadbayadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a href='http://adclk.about.co.kr/click/publishtest/main01/top_left?aW52X2lkeD0xNDA1JnByZF9pZHg9MTEmY2FtX2lkeD00NDImYWRzX2lkeD04MzAmY3R2X2dycF9pZHg9NTc4MSZjdHZfaWR4PTQ5NDEzJnRtcF9pZHg9NTEmZGV2aWNlPTImdWk9Ym4maXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTc0NDUzMDQzNiZyZXNfdGltZT0xNDMyODgwMzYxJnRhZz1qc29uJnN2bm09NWgmcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU0JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4MjQ1Nzc3NSUyNmN0YWclM0Q4MjQ1Nzc3NV8wJTI2bHB0YWclM0RBZGJheV9CX09fMDUxM19hMSUyNmNvdXBhbmdTcmwlM0Q4MjQ1Nzc3NSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9CX08lMjZ1dG1fY2FtcGFpZ24lM0RNb2JpbGUlMjZwcmV2ZW50QnlwYXNzJTNEdHJ1ZQ==' onclick=\"document.getElementById('click').src='$IMClickUrl';mraid.openExternal('http://adclk.about.co.kr/click/publishtest/main01/top_left?aW52X2lkeD0xNDA1JnByZF9pZHg9MTEmY2FtX2lkeD00NDImYWRzX2lkeD04MzAmY3R2X2dycF9pZHg9NTc4MSZjdHZfaWR4PTQ5NDEzJnRtcF9pZHg9NTEmZGV2aWNlPTImdWk9Ym4maXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTc0NDUzMDQzNiZyZXNfdGltZT0xNDMyODgwMzYxJnRhZz1qc29uJnN2bm09NWgmcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU0JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4MjQ1Nzc3NSUyNmN0YWclM0Q4MjQ1Nzc3NV8wJTI2bHB0YWclM0RBZGJheV9CX09fMDUxM19hMSUyNmNvdXBhbmdTcmwlM0Q4MjQ1Nzc3NSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9CX08lMjZ1dG1fY2FtcGFpZ24lM0RNb2JpbGUlMjZwcmV2ZW50QnlwYXNzJTNEdHJ1ZQ=='); return false;\" target=\"_blank\" style=\"text-decoration: none\"><img src='http://img.iacstatic.co.kr/adbayimg/creative/20150526/e25512d5c64bdbb2c885bd19ce901b6c.jpg'  /><br/></a><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1' height=1 width=1 border=0 style=\"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpadbayadnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdbayGetName() throws Exception {
        assertEquals(dcpadbayadnetwork.getName(), "adbaydcp");
    }
}
