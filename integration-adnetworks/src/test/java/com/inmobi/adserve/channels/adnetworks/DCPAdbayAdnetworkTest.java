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
            "{\"redirect\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM1MTA4JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU2JTI2c3BlYyUzRDElMjZhZGR0YWclM0QwJTI2Y3RhZyUzREhPTUVfMCUyNmxwdGFnJTNEQWRiYXlfbG9nb19tJTI2Y291cGFuZ1NybCUzRCUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9GTkIlMjZ1dG1fY2FtcGFpZ24lM0RNb2JpbGUlMjZwcmV2ZW50QnlwYXNzJTNEdHJ1ZSUyNnB0JTNESE9NRQ==\", \"cb\": \"adbayShoppingboxCallback50\", \"creatives\": [{\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150609/309608474d68bfdc8b1b7bf68522b609.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc2a4\\ucfe0\\ud130 \\uc548\\ubd80\\ub7ec\\uc6b4<br>\\uc804\\ub3d9 \\ud0a5\\ubcf4\\ub4dc\\u2605\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUyMzk3JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MjIyMjQxNSUyNmN0YWclM0Q5MjIyMjQxNV8wJTI2bHB0YWclM0RBZGJheV9PXzA2MDlfYSUyNmNvdXBhbmdTcmwlM0Q5MjIyMjQxNSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150513/2b397c8d7d33998013330e9b2b42f402.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc9c0\\uac11\\ucc98\\ub7fc \\uc4f0\\ub294<br>\\ub4c0\\uc5bc\\ucee4\\ubc84 \\ucf00\\uc774\\uc2a4\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ2ODY0JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MTcwMjYyMSUyNmN0YWclM0Q5MTcwMjYyMV8wJTI2bHB0YWclM0RBZGJheV9PXzA1MTNfYSUyNmNvdXBhbmdTcmwlM0Q5MTcwMjYyMSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150210/8c49a50c9d68ed56d4271fa5b19d0173.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc6b4\\uc804\\ud560\\ub54c \\ud544\\uc694\\ud55c \\uc2a4\\ub9c8\\ud2b8\\ud3f0 \\uac70\\uce58\\ub300\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTMwMjkyJnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q2NDk2NzM3NCUyNmN0YWclM0Q2NDk2NzM3NF8wJTI2bHB0YWclM0RBZGJheV9PXzAyMTBfYSUyNmNvdXBhbmdTcmwlM0Q2NDk2NzM3NCUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150522/03c02c0e27b222db81182f0dc8f17bda.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ub109\\ub109\\ud55c \\uc218\\ub0a9\\uacf5\\uac04<br>\\uc218\\ub0a9\\ubca4\\uce58\\uc1fc\\ud30c\\u2605\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ5MjIxJnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4OTI2OTQyNyUyNmN0YWclM0Q4OTI2OTQyN18wJTI2bHB0YWclM0RBZGJheV9PXzA1MjJfYiUyNmNvdXBhbmdTcmwlM0Q4OTI2OTQyNyUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150507/60faf1e27286b33111e66e23ae72c5fd.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc5b4\\ub9c8\\ubb34\\uc2dc\\ud558\\uac8c \\ud479\\uc2e0\\ud55c \\uc5d0\\uc5b4\\ubc30\\ub4dc\\u2665\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ1NjQxJnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4MjQ1Nzc3NSUyNmN0YWclM0Q4MjQ1Nzc3NV8wJTI2bHB0YWclM0RBZGJheV9PXzA1MDdfYSUyNmNvdXBhbmdTcmwlM0Q4MjQ1Nzc3NSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150206/fe9e9ae33efefb421674f985a045b7ad.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ub4e4\\uace0\\ub2e4\\ub2c8\\ub294~ \\ucd08\\uc18c\\ud615 \\uc640\\uc774\\ud30c\\uc774\\u2605\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTI5ODU2JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q2ODkxMjEwOCUyNmN0YWclM0Q2ODkxMjEwOF8wJTI2bHB0YWclM0RBZGJheV9PXzAyMDZfYiUyNmNvdXBhbmdTcmwlM0Q2ODkxMjEwOCUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150326/5d26d67b5c5080f37e20434df20d4451.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc644\\uc804 \\ud3b8\\ud574! \\ube14\\ub8e8\\ud22c\\uc2a4 \\ud5e4\\ub4dc\\uc14b\\u2605\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM3MDE5JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q3OTUxNzk1NiUyNmN0YWclM0Q3OTUxNzk1Nl8wJTI2bHB0YWclM0RBZGJheV9PXzAzMjZfYSUyNmNvdXBhbmdTcmwlM0Q3OTUxNzk1NiUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150120/b06de4189650ec03b8f84dec8e75adf5.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ud479\\uc2e0\\ud568 \\uac00\\ub4dd\\u2605 \\uc719\\uc2a4 \\uc18c\\ud30c\\ubca0\\ub4dc\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTI2MTc1JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q3ODE3NjAwNiUyNmN0YWclM0Q3ODE3NjAwNl8wJTI2bHB0YWclM0RBZGJheV9PXzAxMjBfYSUyNmNvdXBhbmdTcmwlM0Q3ODE3NjAwNiUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150603/470e820ce8456f7cd42f63182e09f21c.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ube44\\ud0a4\\ub2c8 \\uc774\\uc81c\\uadf8\\ub9cc<br>\\uc2ac\\ub9bc\\ud54f \\ub798\\uc26c\\uac00\\ub4dc\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUxMzg2JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU2JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MTgxNTI4MyUyNmN0YWclM0Q5MTgxNTI4M18wJTI2bHB0YWclM0RBZGJheV9GTkJfMDYwM19hJTI2Y291cGFuZ1NybCUzRDkxODE1MjgzJTI2dXRtX3NvdXJjZSUzRE1CJTI2dXRtX21lZGl1bSUzREFkYmF5X0ZOQiUyNnV0bV9jYW1wYWlnbiUzRE1vYmlsZSUyNnByZXZlbnRCeXBhc3MlM0R0cnVl\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150615/248816c1f1c68b693b48bc9861f92b84.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\uc644\\ubcbd\\ud55c \\uc218\\uc601\\ubcf5\\u2665<br>\\ub208\\uc774 \\ubc18\\uc9dd\\ubc18\\uc9dd\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUzMDg0JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU2JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MjU3NjY5NCUyNmN0YWclM0Q5MjU3NjY5NF8wJTI2bHB0YWclM0RBZGJheV9GTkJfMDYxNV9hJTI2Y291cGFuZ1NybCUzRDkyNTc2Njk0JTI2dXRtX3NvdXJjZSUzRE1CJTI2dXRtX21lZGl1bSUzREFkYmF5X0ZOQiUyNnV0bV9jYW1wYWlnbiUzRE1vYmlsZSUyNnByZXZlbnRCeXBhc3MlM0R0cnVl\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20141224/4f0c85ae3d01d4e037e7e5f92551c89a.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ubd88\\uc774 \\ud544\\uc694\\uc5c6\\ub294 \\ubc14\\ub85c\\ucfe1 \\ubc1c\\uc5f4\\ub3c4\\uc2dc\\ub77d\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTIxOTcyJnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q3MjExOTU5NiUyNmN0YWclM0Q3MjExOTU5Nl8wJTI2bHB0YWclM0RBZGJheV9PXzEyMThfYyUyNmNvdXBhbmdTcmwlM0Q3MjExOTU5NiUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150206/05503d64d4d254297f8a49208d343365.jpg\", \"svgIco\": false, \"target\": \"_top\", \"w\": 90, \"goods\": \"\", \"h\": 90, \"txt\": \"\\ubaa8\\ub4e0 \\uae30\\ub2a5\\uc744 \\ud558\\ub098\\ub85c \\uba40\\ud2f0 \\ud3f0\\ucf00\\uc774\\uc2a4\\u2605\", \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTI5ODM0JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q4NjAxNzkxNSUyNmN0YWclM0Q4NjAxNzkxNV8wJTI2bHB0YWclM0RBZGJheV9PXzAyMDZfYSUyNmNvdXBhbmdTcmwlM0Q4NjAxNzkxNSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\", \"shopid\": \"\"}], \"logo\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150316/a8c37a5970c961929e6257f29c15e2df.jpg\", \"height\": 50, \"size\": 70, \"logo_target\": \"_top\", \"is_ep\": false, \"bg-color\": \"#ffffff\", \"width\": 100, \"ui\": \"sb\"}";
        dcpadbayadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpadbayadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
            "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\" content=\"text/html; charset=utf-8\"/></head><style>body, html {margin: 0; padding: 0; overflow: hidden;}.template_120_20 { width: 120px; height: 19px; }.template_168_28 { width: 168px; height: 27px; }.template_216_36 { width: 216px; height: 35px;}.template_300_50 {width: 300px; height: 49px;}.template_320_50 {width: 320px; height: 49px;}.template_320_48 {width: 320px; height: 47px;}.template_468_60 {width: 468px; height: 59px;}.template_728_90 {width: 728px; height: 89px;}.container { overflow: hidden; position: relative;}.adbg { border-top: 1px solid #00577b; background: #003229;background: -moz-linear-gradient(top, #003e57 0%, #003229 100%);background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #003e57), color-stop(100%, #003229));background: -webkit-linear-gradient(top, #003e57 0%, #003229 100%);background: -o-linear-gradient(top, #003e57 0%, #003229 100%);background: -ms-linear-gradient(top, #003e57 0%, #003229 100%);background: linear-gradient(top, #003e57 0%, #003229 100%); }.right-image { position: absolute; top: 0; right: 5px; display: table-cell; vertical-align: middle}.right-image-cell { padding-left:4px!important;white-space: nowrap; vertical-align: middle; height: 100%; width:40px;background:url(http://r.edge.inmobicdn.net/IphoneActionsData/line.png) repeat-y left top; } .adtable,.adtable td{border: 0; padding: 0; margin:0;}.adtable {padding:5px;}.adtext-cell {width:100%}.adtext-cell-div {font-family:helvetica;color:#fff;float:left;v-allign:middle}.template_120_20 .adtable{padding-top:0px;}.template_120_20 .adtable .adtext-cell div{font-size: 0.42em!important;}.template_168_28 .adtable{padding-top: 2px;}.template_168_28 .adtable .adtext-cell div{font-size: 0.5em!important;}.template_168_28 .adtable .adimg{width:18px;height:18px; }.template_216_36 .adtable{padding-top: 3px;}.template_216_36 .adtable .adtext-cell div{font-size: 0.6em!important;padding-left:3px;}.template_216_36 .adtable .adimg{width:25px;height:25px;}.template_300_50 .adtable{padding-top: 7px;}.template_300_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_300_50 .adtable .adimg{width:30px;height:30px;}.template_320_48 .adtable{padding-top: 6px;}.template_320_48 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_48 .adtable .adimg{width:30px;height:30px;}.template_320_50 .adtable{padding-top: 7px;}.template_320_50 .adtable .adtext-cell div{font-size: 0.8em!important;padding-left:4px;}.template_320_50 .adtable .adimg{width:30px;height:30px;}.template_468_60 .adtable{padding-top:10px;}.template_468_60 .adtable .adtext-cell div{font-size: 1.1em!important;padding-left:8px;}.template_468_60 .adtable .adimg{width:35px;height:35px;}.template_728_90 .adtable{padding-top:19px;}.template_728_90 .adtable .adtext-cell div{font-size: 1.4em!important;padding-left:10px;}.template_728_90 .adtable .adimg{width:50px;height:50px;}</style><body style=\"margin:0;padding:0;overflow: hidden;\"><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><a style=\"text-decoration:none; \" href=\"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUyMzk3JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MjIyMjQxNSUyNmN0YWclM0Q5MjIyMjQxNV8wJTI2bHB0YWclM0RBZGJheV9PXzA2MDlfYSUyNmNvdXBhbmdTcmwlM0Q5MjIyMjQxNSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU=\" onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUyMzk3JnRtcF9pZHg9NjcmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM0MzU5MzAzJnRhZz1qc29uJnN2bm09OW0mcmVkaXJlY3Q9aHR0cCUzQS8vbS5jb3VwYW5nLmNvbS9tL2xhbmRpbmdNdWx0aS5wYW5nJTNGc2lkJTNEQWRiYXlfYSUyNnNyYyUzRDE1MDU3JTI2c3BlYyUzRDElMjZhZGR0YWclM0Q5MjIyMjQxNSUyNmN0YWclM0Q5MjIyMjQxNV8wJTI2bHB0YWclM0RBZGJheV9PXzA2MDlfYSUyNmNvdXBhbmdTcmwlM0Q5MjIyMjQxNSUyNnV0bV9zb3VyY2UlM0RNQiUyNnV0bV9tZWRpdW0lM0RBZGJheV9PJTI2dXRtX2NhbXBhaWduJTNETW9iaWxlJTI2cHJldmVudEJ5cGFzcyUzRHRydWU='); return false;\" target=\"_blank\"><div class=\"container $Template adbg\"><table class=\"adtable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><tr><td><div style=\"margin:0 auto;\"><img border=\"0\" src=\"http://img.iacstatic.co.kr/adbayimg/creative/20150609/309608474d68bfdc8b1b7bf68522b609.jpg\" width=\"38\" height=\"38\" style=\"float:left;margin:2px;\" /></div></td><td class=\"adtext-cell\"><div class=\"adtext-cell-div\" style=\"font-weight:bold;\">스쿠터 안부러운<br>전동 킥보드★</div></div></td><td class=\"right-image-cell\">&nbsp<img width=\"28\" height=\"28\" class=\"adimg\" border=\"0\" src=\"http://r.edge.inmobicdn.net/IphoneActionsData/web.png\" /></td></tr></table></div></a><img src='beaconUrl' height=1 width=1 border=0 \"display:none;\"/><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\"/></body></html>",
            dcpadbayadnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdbayGetName() throws Exception {
        assertEquals(dcpadbayadnetwork.getName(), "adbayDCP");
    }
}
