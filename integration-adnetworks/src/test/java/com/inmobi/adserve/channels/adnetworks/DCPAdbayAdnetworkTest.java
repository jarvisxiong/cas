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
public class DCPAdbayAdnetworkTest extends TestCase {

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
        expect(repositoryHelper.querySlotSizeMapRepository((short) 4)).andReturn(slotSizeMapEntityFor4).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 9)).andReturn(slotSizeMapEntityFor9).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 11)).andReturn(slotSizeMapEntityFor11).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 14)).andReturn(slotSizeMapEntityFor14).anyTimes();
        expect(repositoryHelper.querySlotSizeMapRepository((short) 15)).andReturn(slotSizeMapEntityFor15).anyTimes();
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
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                        + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null, 0, null, null, true, true,
                                                              externalKey, null, null, null, new Long[] {0L}, true,
                                                              null, null, 0, null, false, false, false, false, false,
                                                              false, false, false, false, false, null,
                                                              new ArrayList<>(), 0.0d, null, null, 32,
                                                              new Integer[] {0}));
        assertTrue(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                                                         repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                        + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null, 0, null, null, true, true,
                                                              externalKey, null, null, null, new Long[] {0L}, true,
                                                              null, null, 0, null, false, false, false, false, false,
                                                              false, false, false, false, false, null,
                                                              new ArrayList<>(), 0.0d, null, null, 32,
                                                              new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                                                          repositoryHelper));
    }

    @Test
    public void testDCPAdbayConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(
                "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534"
                        + ".46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null, 0, null, null, true, true,
                                                              externalKey, null, null, null, new Long[] {0L}, true,
                                                              null, null, 0, null, false, false, false, false, false,
                                                              false, false, false, false, false, null,
                                                              new ArrayList<>(), 0.0d, null, null, 32,
                                                              new Integer[] {0}));
        assertFalse(dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                                                          repositoryHelper));
    }

    @Test
    public void testAdbayRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "123qwe";
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null, 0, null, null, true, true,
                                                              externalKey, null, null, null, new Long[] {0L}, true,
                                                              null, null, 0, null, false, false, false, false, false,
                                                              false, false, false, false, false, null,
                                                              new ArrayList<>(), 0.0d, null, null, 0,
                                                              new Integer[] {0}));
        if (dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                                                  repositoryHelper)) {
            final String actualUrl = dcpadbayadnetwork.getRequestUri().toString();
            final String expectedUrl = "http://ad.about.co.kr/mad/json/InMobi/total01/bottom_middle";
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
        final ChannelSegmentEntity entity = new ChannelSegmentEntity(
                AdNetworksTest.getChannelSegmentEntityBuilder(AdbayAdvId, null, null, null, 0, null, null, true, true,
                                                              externalKey, null, null, null, new Long[] {0L}, true,
                                                              null, null, 0, null, false, false, false, false, false,
                                                              false, false, false, false, false, null,
                                                              new ArrayList<>(), 0.0d, null, null, 32,
                                                              new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpadbayadnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4,
                                              repositoryHelper);
        final String response =
                "{\"redirect\": \"http://adclk.about.co"
                        + ".kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM1MTA4JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X2xvZ29fbQ==\", \"cb\": \"adbayShoppingboxCallback50\", \"creatives\": [{\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150326/5d26d67b5c5080f37e20434df20d4451.jpg\", \"txt\": \"\\uc644\\uc804 \\ud3b8\\ud574! \\ube14\\ub8e8\\ud22c\\uc2a4 \\ud5e4\\ub4dc\\uc14b\\u2605\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM3MDE5JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDMyNl9h\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150522/03c02c0e27b222db81182f0dc8f17bda.jpg\", \"txt\": \"\\ub109\\ub109\\ud55c \\uc218\\ub0a9\\uacf5\\uac04<br>\\uc218\\ub0a9\\ubca4\\uce58\\uc1fc\\ud30c\\u2605\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ5MjIxJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDUyMl9i\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20141224/4f0c85ae3d01d4e037e7e5f92551c89a.jpg\", \"txt\": \"\\ubd88\\uc774 \\ud544\\uc694\\uc5c6\\ub294 \\ubc14\\ub85c\\ucfe1 \\ubc1c\\uc5f4\\ub3c4\\uc2dc\\ub77d\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTIxOTcyJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMTIxOF9j\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150618/8dc4bb67732f122e2614ab12e28f376f.jpg\", \"txt\": \"\\uc544\\uc2dd\\uc2a4 G1<br>20,860\\uc6d0!\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTU0MTA0JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X0ZOQl8wNjE4X2E=\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150210/8c49a50c9d68ed56d4271fa5b19d0173.jpg\", \"txt\": \"\\uc6b4\\uc804\\ud560\\ub54c \\ud544\\uc694\\ud55c \\uc2a4\\ub9c8\\ud2b8\\ud3f0 \\uac70\\uce58\\ub300\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTMwMjkyJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDIxMF9h\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150206/05503d64d4d254297f8a49208d343365.jpg\", \"txt\": \"\\ubaa8\\ub4e0 \\uae30\\ub2a5\\uc744 \\ud558\\ub098\\ub85c \\uba40\\ud2f0 \\ud3f0\\ucf00\\uc774\\uc2a4\\u2605\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTI5ODM0JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDIwNl9h\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150706/40498096c741a4452f6794ce9aad2f4c.jpg\", \"txt\": \"3\\ub9cc\\uc6d0 \\ucd94\\uac00\\ud560\\uc778<br>\\uc544\\ub514\\ub2e4\\uc2a4 \\ud2b9\\uac00\\u2605\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTU3NjgwJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X0ZOQl8wNzA2X2E=\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150507/60faf1e27286b33111e66e23ae72c5fd.jpg\", \"txt\": \"\\uc5b4\\ub9c8\\ubb34\\uc2dc\\ud558\\uac8c \\ud479\\uc2e0\\ud55c \\uc5d0\\uc5b4\\ubc30\\ub4dc\\u2665\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ1NjQxJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDUwN19h\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150603/470e820ce8456f7cd42f63182e09f21c.jpg\", \"txt\": \"\\ube44\\ud0a4\\ub2c8 \\uc774\\uc81c\\uadf8\\ub9cc<br>\\uc2ac\\ub9bc\\ud54f \\ub798\\uc26c\\uac00\\ub4dc\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUxMzg2JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X0ZOQl8wNjAzX2E=\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150206/fe9e9ae33efefb421674f985a045b7ad.jpg\", \"txt\": \"\\ub4e4\\uace0\\ub2e4\\ub2c8\\ub294~ \\ucd08\\uc18c\\ud615 \\uc640\\uc774\\ud30c\\uc774\\u2605\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTI5ODU2JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDIwNl9i\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150617/fa30491c119b24ed742b0c017fa3fa94.jpg\", \"txt\": \"\\ud479\\uc2e0\\ud568 \\uac00\\ub4dd\\u2605<br>\\uc719\\uc2a4 \\uc18c\\ud30c\\ubca0\\ub4dc\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTUzNzIwJnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDEyMF9h\"}, {\"src\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150513/2b397c8d7d33998013330e9b2b42f402.jpg\", \"txt\": \"\\uc9c0\\uac11\\ucc98\\ub7fc \\uc4f0\\ub294<br>\\ub4c0\\uc5bc\\ucee4\\ubc84 \\ucf00\\uc774\\uc2a4\", \"target\": \"_blank\", \"h\": 90, \"w\": 90, \"svgIco\": false, \"click\": \"http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTQ2ODY0JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDUxM19h\"}], \"logo\": \"http://img.iacstatic.co.kr/adbayimg/creative/20150316/a8c37a5970c961929e6257f29c15e2df.jpg\", \"height\": 50, \"size\": 70, \"logo_target\": \"_blank\", \"is_ep\": false, \"bg-color\": \"#ffffff\", \"width\": 100, \"ui\": \"sb\"}";
        dcpadbayadnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpadbayadnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, "
                        + "maximum-scale=1.0\"/><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0,"
                        + " maximum-scale=1.0\" /><style type=\"text/css\">body {margin: 0px;overflow: hidden;"
                        + "width:320px;height:50px;}.m_shoppingbox {min-width:320px;position:relative;"
                        + "overflow:hidden;width:100%;height:50px;background:#fff;border:1px solid #d9d9d9;"
                        + "font:11px/14px Dotum,ë\u008F\u008Bì\u009B\u0080,AppleGothic,sans-serif,Gulim,êµ´ë¦¼;"
                        + "box-sizing:border-box;}.ad_list__wrap {position:relative;padding:2px 0 2px 26px;}"
                        + ".ad_list__wrap h1.logo {position:absolute;left:-7px;top:19px;-ms-transform: rotate(90deg);"
                        + " /* IE 9 */-webkit-transform: rotate(90deg); /* Chrome, Safari, Opera */transform: rotate"
                        + "(90deg);}.ad_list__wrap h1.logo img {width:40px;height:10px;}.ad_list__wrap .ad_list:after"
                        + " {content:'';display:block;clear:both;}.ad_list__wrap .ad_list li {float:left;width:50%;}"
                        + ".ad_list__wrap .ad_list li a {display:block;height:44px;color:#000;letter-spacing:-1px;}"
                        + ".ad_list__wrap .ad_list li a:after {content:'';display:block;clear:both;}.ad_list__wrap "
                        + ".ad_list li span.img {float:left;}.ad_list__wrap .ad_list li span.img img {width:44px;"
                        + "height:44px;}.ad_list__wrap .ad_list li span.txt {display:block;height:24px;"
                        + "overflow:hidden;padding:10px 4px 0 0;line-height:12px;}.m_shoppingbox .iad "
                        + "{position:absolute;z-index:999;right:8px;top:4px;}.m_shoppingbox .iad p a {display:block;"
                        + "width:16px;height:16px;background:url(/combineAdBanner/html/slide/images/svn_info.png) 0 0"
                        + " no-repeat;font-family:'ë\u008F\u008Bì\u009B\u0080',Dotum,'êµ´ë¦¼',Gulim,Helvetica,"
                        + "sans-serif;font-weight:bold;color:#282095;text-indent:-10000px;}.m_shoppingbox .iad p "
                        + "a:hover {width:86px;background:url(/combineAdBanner/html/slide/images/svn_open.png) 0 0 "
                        + "no-repeat;text-indent:0px;}.m_shoppingbox .svgArea .svg_on, .svgArea.svgHover .svg_off "
                        + "{display:none;}.m_shoppingbox .svgArea.svgHover .svg_on {display:block;}.m_shoppingbox "
                        + ".svgArea.svgHover .svg_on a {text-decoration:none;}/* 320x50 */.view__320x50 "
                        + ".ad_list__wrap {padding:2px 0 2px 25px;}.view__320x50 h1.logo {position:static;"
                        + "-ms-transform: rotate(0deg); /* IE 9 */-webkit-transform: rotate(0deg); /* Chrome, Safari,"
                        + " Opera */transform: rotate(0deg);float:left;margin:14px 27px 0 0;}.view__320x50 h1.logo "
                        + "img {width:62px;height:16px;}.view__320x50 .ad_list {margin-left:89px;}.view__320x50 "
                        + ".ad_list li {width:90%;}.view__320x50 .ad_list li span.img {margin-right:10px;}\n"
                        + "</style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi"
                        + ".com/android/mraid.js\"></script>$<div id=\"adbaymobilebanner\"><meta "
                        + "http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"><meta name=\"viewport\" "
                        + "content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0,maximum-scale=1.0, "
                        + "user-scalable=no, target-densitydpi=medium-dpi\"><link rel=\"stylesheet\" "
                        + "type=\"text/css\" href=\"http://211.172.246.134/combineAdBanner/css/mobile_shoppingbox"
                        + ".css\"><script type=\"text/javascript\" src=\"http://script.about.co"
                        + ".kr/os2/common/js/jquery-1.8.2.min.js\"></script>\n"
                        + "<div class=\"m_shoppingbox view__320x50\"><div id=\"adbay1thumb\" "
                        + "class=\"ad_list__wrap\"><h1 class= \"logo\"><a href='http://adclk.about.co"
                        + ".kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM1MTA4JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X2xvZ29fbQ==' onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM1MTA4JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxMTQuYXBpLTAzLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTE0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X2xvZ29fbQ=='); return false;\" target=\"_blank\" class=\"thum-cont\"> <img src='http://img.iacstatic.co.kr/adbayimg/creative/20150316/a8c37a5970c961929e6257f29c15e2df.jpg' alt=\"\"></a></h1> <ul class=\"ad_list\"> <li class=\"ad_list__item\"> \n"
                        + "<a href='http://adclk.about.co"
                        + ".kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM3MDE5JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDMyNl9h' onclick=\"document.getElementById('click').src='clickUrl';imraid.openExternal('http://adclk.about.co.kr/click/InMobi/total01/bottom_middle?aW52X2lkeD0yNzEwJnByZF9pZHg9MTEmY2FtX2lkeD00MzkmYWRzX2lkeD04MjQmY3R2X2dycF9pZHg9Mjc5NyZjdHZfaWR4PTM3MDE5JnRtcF9pZHg9NjgmZGV2aWNlPTImdWk9c2ImaXNfdGFyZ2V0PTAmY2F0ZWdvcnk9JnB0eXBlPTEmc2hvcF9pZD1jb3VwYW5nMiZhY2NfaWR4PTQ4OSZnb29kc19pZD0maXNfaG91c2U9MCZjbGllbnRfaXA9MTAzMDY4MjU2OSZyZXNfdGltZT0xNDM2MjQzNTUwJnRhZz1qc29uJnN2bm09MTBtJnJlZGlyZWN0PWh0dHBzJTNBLy8xMDIxNTQuYXBpLTAxLmNvbS9zZXJ2ZSUzRmFjdGlvbiUzRGNsaWNrJTI2cHVibGlzaGVyX2lkJTNEMTAyMTU0JTI2c2l0ZV9pZCUzRDM1NDA0JTI2b2ZmZXJfaWQlM0QyNjM2MzIlMjZteV9jYW1wYWlnbiUzREFkYmF5X09fMDMyNl9h'); return false;\" target=\"_blank\" class=\"thum-cont\"> <span class=\"img\"><img src='http://img.iacstatic.co.kr/adbayimg/creative/20150326/5d26d67b5c5080f37e20434df20d4451.jpg' alt=\"\"></span><span class=\"txt\">완전 편해! 블루투스 헤드셋★</span></a></li></ul><img id=\"click\" width=\"1\" height=\"1\" style=\"display:none;\" /></div></div></div><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>\n",
                dcpadbayadnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPAdbayGetName() throws Exception {
        assertEquals(dcpadbayadnetwork.getName(), "adbayDCP");
    }
}
