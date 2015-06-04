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
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.configuration.Configuration;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.inmobi.adserve.adpool.ContentType;
import com.inmobi.adserve.channels.adnetworks.startmeapp.DCPStartMeAppAdnetwork;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest(BaseAdNetworkImpl.class)
public class DCPStartMeAppAdnetworkTest {
    private static final String debug = "debug";
    private static final String loggerConf = "/tmp/channel-server.properties";
    private static final Bootstrap clientBootstrap = null;
    private static final String StartMeAppStatus = "on";
    private static final String StartMeAppAdspace = "1234567";
    private static final String StartMeappAdvId = "StartMeAppadv1";
    private static final String StartMeAppTest = "1";
    private static final String pubId = "1q2w3e";
    private static final String fsPlacementId = "230";
    private static Configuration mockConfig = null;
    private static DCPStartMeAppAdnetwork dcpStartMeAppAdnetwork;
    private static String StartMeAppHost = "http://api.rtb.startmeapp.net/request";
    private static RepositoryHelper repositoryHelper;

    public static void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("startmeappdcp.host")).andReturn(StartMeAppHost).anyTimes();
        expect(mockConfig.getString("startmeappdcp.adspace")).andReturn(StartMeAppAdspace).anyTimes();
        expect(mockConfig.getString("startmeappdcp.status")).andReturn(StartMeAppStatus).anyTimes();
        expect(mockConfig.getString("startmeappdcp.test")).andReturn(StartMeAppTest).anyTimes();
        expect(mockConfig.getString("startmeappdcp.pubid")).andReturn(pubId).anyTimes();
        expect(mockConfig.getString("startmeappdcp.advertiserId")).andReturn(StartMeappAdvId).anyTimes();
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        replay(mockConfig);
    }

    @BeforeClass
    public static void setUp() throws Exception {
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

        dcpStartMeAppAdnetwork = new DCPStartMeAppAdnetwork(mockConfig, clientBootstrap, base, serverChannel);
        dcpStartMeAppAdnetwork.setName("StartMeApp");

        final Field ipRepositoryField = BaseAdNetworkImpl.class.getDeclaredField("ipRepository");
        ipRepositoryField.setAccessible(true);
        IPRepository ipRepository = new IPRepository();
        ipRepository.getUpdateTimer().cancel();
        ipRepositoryField.set(null, ipRepository);

        dcpStartMeAppAdnetwork.setHost(StartMeAppHost);
    }

    @Test
    public void testDCPStartMeAppConfigureParameters() {

        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        casInternalRequestParameters.setGpid("gpidtest123");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(StartMeappAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpStartMeAppAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity,
                (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPStartMeAppConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(StartMeappAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpStartMeAppAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testDCPStartMeAppConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(StartMeappAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpStartMeAppAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper));
    }

    @Test
    public void testStartMeAppRequestUri() throws Exception {
        setUp();
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("APP");
        sasParams.setOsId(3);
        sasParams.setOsMajorVersion("4.4");
        casInternalRequestParameters.setUidIFA("idfa202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setGpid("gpid202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(Arrays.asList(new Long[] {10l, 13l, 30l}));
        final String externalKey = "123qwe";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(StartMeappAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpStartMeAppAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper)) {
            final String actualUrl = dcpStartMeAppAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://api.rtb.startmeapp.net/request?apiver=1&adspace=123qwe&pubid=1q2w3e&response=JSON&source=APP&ip=206.29.182.240&ua=Mozilla&pdid=gpid202cb962ac59075b964b07152d234b70&height=50&width=300&keywords=Food+%26+Drink%2CAdventure%2CWord&os=Android&osv=4.4";
            assertEquals(new URI(expectedUrl).getQuery(), new URI(actualUrl).getQuery());
            assertEquals(new URI(expectedUrl).getPath(), new URI(actualUrl).getPath());
        }
    }

    @Test
    public void testDCPStartMeAppParseResponse() throws Exception {
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
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(StartMeappAdvId, null, null, null,
                        0, null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<>(), 0.0d, null, null, 32, new Integer[] {0}));
        AdapterTestHelper.setBeaconAndClickStubs();
        dcpStartMeAppAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, (short) 4, repositoryHelper);
        final String response =
                "{\"adm\":\"<img src=\\\"http:\\/\\/pixcount.rtb.startmeapp.net\\/resource\\/AAAAAhQFEQR1dWlkESRmNDE1ZjgxNS01NGIyLTRkNzUtODA5OS0yN2Q4NTEzZTJiMTYRBG1vZGUGAREFY2xhc3MRBVRyYWNrEQZtZXRob2QRCW5vdGlmeVdpbhEEZGF0YRQBEQlyZXBvcnRpbmcUFBELcHVibGlzaGVySWQRBzEyMzQ1NjcRD3B1Ymxpc2hlclN0YXR1cwARCGJpZGRlcklkBl4RBGlzbzIRA1VTQRELbW9iaWxlQnJhbmQRB0dlbmVyaWMRBm9zTmFtZREHQW5kcm9pZBELaWRPc1ZlcnNpb24RAzQuNBENbW9iaWxlQnJvd3NlchEOQW5kcm9pZCBXZWJraXQRCG1ha2VOYW1lDhARCW1vZGVsTmFtZRESQW5kcm9pZCA0LjQgVGFibGV0EQZoZWlnaHQIAUARBXdpZHRoCAHgEQdyZXZlbnVlDD-yLQ5WBBiTEQhjbGlja1VybAARBXRhZ2lkABEEbnVybBHvaHR0cDovL3VzcHJpY2V2Mi5tZHNwLmF2YXp1dHJhY2tpbmcubmV0L3ByaWNlL3N0YXJ0bWVhcHA_cnFpZD00ZmNhZTY4Ni0xMmExLTQzODMtYjk5MS03NTlmYzIzZjhlODUmYmlkaWQ9MzY0MDY3Njk2NzYyNTY2NDA5NiZhY2NpZD04NDUmcHJpY2U9MC4wNzEmYWRpZD0xMjU3MV83NjQzOCZ1aWQ9JmltcGlkPTc1MzA5MzQwLTVjOGUtNGNiZi1hZTZmLTBmMzU1N2FiYWEzNyZhZmNhcD1kNTAuMTIxLjg0LjIzNSZjZmNhcD0RCnJlc29sdXRpb24GABENZHNwTW9kdWxlbmFtZREFQXZhenURCmNhbXBhaWduSWQRBTEyNTcxEQpjcmVhdGl2ZUlkEQU3NjQzOA\\\" width=\\\"1\\\" height=\\\"1\\\" border=\\\"0\\\" \\/> <a href=\\\"http:\\/\\/mdsp.avazutracking.net\\/tracking\\/redirect.php?bid_id=3640676967625664096&ids=ZODQ1fjEyNTcxfjc2NDM4fjEwMTJ-LTF-MX4xfjB-MH5VU35hbmRyb2lkfi0xfmdlbmVyaWN-NC40fjExNjE4OH4tMX51cw&_m=%07creative_id%0676438%07os_version%064.4%07exchange%06startmeapp%07source_name%06&ext=\\\" target=\\\"_blank\\\"><img src=\\\"http:\\/\\/cdn.avazutracking.net\\/images\\/201503\\/010\\/6d0996de118c803eabb3af26db139dfd_320x480.gif\\\" \\/> <\\/a><img src=\\\"http:\\/\\/mdsp.avazutracking.net\\/tracking\\/impression?bid_id=3640676967625664096&ids=ZODQ1fjEyNTcxfjc2NDM4fjEwMTJ-LTF-MX4xfjB-MH5VU35hbmRyb2lkfi0xfmdlbmVyaWN-NC40fjExNjE4OH4tMX51cw&ext=\\\" width=\\\"1\\\" height=\\\"1\\\" style=\\\"display:none;\\\" \\/> <script type=\\\"text\\/javascript\\\">generateTracker=function(ad){var tracker=document.createElement(\\\"img\\\");tracker.setAttribute(\\\"src\\\",\\\"http:\\/\\/resources.rtb.startmeapp.net\\/resource\\/AAAAAhQFEQR1dWlkESQ1ODlmYzBkYi1hZWI5LTRhZWUtYTk0My0yMDhhMzg3NTM4MGURBG1vZGUGAREFY2xhc3MRBVRyYWNrEQZtZXRob2QRBWF1ZGl0EQRkYXRhFAERCXJlcG9ydGluZxQUEQtwdWJsaXNoZXJJZBEHMTIzNDU2NxEPcHVibGlzaGVyU3RhdHVzABEIYmlkZGVySWQGXhEEaXNvMhEDVVNBEQttb2JpbGVCcmFuZBEHR2VuZXJpYxEGb3NOYW1lEQdBbmRyb2lkEQtpZE9zVmVyc2lvbhEDNC40EQ1tb2JpbGVCcm93c2VyEQ5BbmRyb2lkIFdlYmtpdBEIbWFrZU5hbWUOEBEJbW9kZWxOYW1lERJBbmRyb2lkIDQuNCBUYWJsZXQRBmhlaWdodAgBQBEFd2lkdGgIAeARB3JldmVudWUMP7ItDlYEGJMRCGNsaWNrVXJsABEFdGFnaWQAEQRudXJsEe9odHRwOi8vdXNwcmljZXYyLm1kc3AuYXZhenV0cmFja2luZy5uZXQvcHJpY2Uvc3RhcnRtZWFwcD9ycWlkPTRmY2FlNjg2LTEyYTEtNDM4My1iOTkxLTc1OWZjMjNmOGU4NSZiaWRpZD0zNjQwNjc2OTY3NjI1NjY0MDk2JmFjY2lkPTg0NSZwcmljZT0wLjA3MSZhZGlkPTEyNTcxXzc2NDM4JnVpZD0maW1waWQ9NzUzMDkzNDAtNWM4ZS00Y2JmLWFlNmYtMGYzNTU3YWJhYTM3JmFmY2FwPWQ1MC4xMjEuODQuMjM1JmNmY2FwPREKcmVzb2x1dGlvbgYAEQ1kc3BNb2R1bGVuYW1lEQVBdmF6dREKY2FtcGFpZ25JZBEFMTI1NzERCmNyZWF0aXZlSWQRBTc2NDM4\\\");document.body.appendChild(tracker);};register=function(){try {audit=document.getElementsByTagName(\\\"a\\\")[0];audit.addEventListener(\\\"click\\\",function(e){generateTracker(this);},true);}catch(err) {console.log(err.message);}};setTimeout(register,1500);<\\/script>\"}";
        dcpStartMeAppAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpStartMeAppAdnetwork.getHttpResponseStatusCode(), 200);
        assertEquals(
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><img src=\"http://pixcount.rtb.startmeapp.net/resource/AAAAAhQFEQR1dWlkESRmNDE1ZjgxNS01NGIyLTRkNzUtODA5OS0yN2Q4NTEzZTJiMTYRBG1vZGUGAREFY2xhc3MRBVRyYWNrEQZtZXRob2QRCW5vdGlmeVdpbhEEZGF0YRQBEQlyZXBvcnRpbmcUFBELcHVibGlzaGVySWQRBzEyMzQ1NjcRD3B1Ymxpc2hlclN0YXR1cwARCGJpZGRlcklkBl4RBGlzbzIRA1VTQRELbW9iaWxlQnJhbmQRB0dlbmVyaWMRBm9zTmFtZREHQW5kcm9pZBELaWRPc1ZlcnNpb24RAzQuNBENbW9iaWxlQnJvd3NlchEOQW5kcm9pZCBXZWJraXQRCG1ha2VOYW1lDhARCW1vZGVsTmFtZRESQW5kcm9pZCA0LjQgVGFibGV0EQZoZWlnaHQIAUARBXdpZHRoCAHgEQdyZXZlbnVlDD-yLQ5WBBiTEQhjbGlja1VybAARBXRhZ2lkABEEbnVybBHvaHR0cDovL3VzcHJpY2V2Mi5tZHNwLmF2YXp1dHJhY2tpbmcubmV0L3ByaWNlL3N0YXJ0bWVhcHA_cnFpZD00ZmNhZTY4Ni0xMmExLTQzODMtYjk5MS03NTlmYzIzZjhlODUmYmlkaWQ9MzY0MDY3Njk2NzYyNTY2NDA5NiZhY2NpZD04NDUmcHJpY2U9MC4wNzEmYWRpZD0xMjU3MV83NjQzOCZ1aWQ9JmltcGlkPTc1MzA5MzQwLTVjOGUtNGNiZi1hZTZmLTBmMzU1N2FiYWEzNyZhZmNhcD1kNTAuMTIxLjg0LjIzNSZjZmNhcD0RCnJlc29sdXRpb24GABENZHNwTW9kdWxlbmFtZREFQXZhenURCmNhbXBhaWduSWQRBTEyNTcxEQpjcmVhdGl2ZUlkEQU3NjQzOA\" width=\"1\" height=\"1\" border=\"0\" /> <a href=\"http://mdsp.avazutracking.net/tracking/redirect.php?bid_id=3640676967625664096&ids=ZODQ1fjEyNTcxfjc2NDM4fjEwMTJ-LTF-MX4xfjB-MH5VU35hbmRyb2lkfi0xfmdlbmVyaWN-NC40fjExNjE4OH4tMX51cw&_m=%07creative_id%0676438%07os_version%064.4%07exchange%06startmeapp%07source_name%06&ext=\" target=\"_blank\"><img src=\"http://cdn.avazutracking.net/images/201503/010/6d0996de118c803eabb3af26db139dfd_320x480.gif\" /> </a><img src=\"http://mdsp.avazutracking.net/tracking/impression?bid_id=3640676967625664096&ids=ZODQ1fjEyNTcxfjc2NDM4fjEwMTJ-LTF-MX4xfjB-MH5VU35hbmRyb2lkfi0xfmdlbmVyaWN-NC40fjExNjE4OH4tMX51cw&ext=\" width=\"1\" height=\"1\" style=\"display:none;\" /> <script type=\"text/javascript\">generateTracker=function(ad){var tracker=document.createElement(\"img\");tracker.setAttribute(\"src\",\"http://resources.rtb.startmeapp.net/resource/AAAAAhQFEQR1dWlkESQ1ODlmYzBkYi1hZWI5LTRhZWUtYTk0My0yMDhhMzg3NTM4MGURBG1vZGUGAREFY2xhc3MRBVRyYWNrEQZtZXRob2QRBWF1ZGl0EQRkYXRhFAERCXJlcG9ydGluZxQUEQtwdWJsaXNoZXJJZBEHMTIzNDU2NxEPcHVibGlzaGVyU3RhdHVzABEIYmlkZGVySWQGXhEEaXNvMhEDVVNBEQttb2JpbGVCcmFuZBEHR2VuZXJpYxEGb3NOYW1lEQdBbmRyb2lkEQtpZE9zVmVyc2lvbhEDNC40EQ1tb2JpbGVCcm93c2VyEQ5BbmRyb2lkIFdlYmtpdBEIbWFrZU5hbWUOEBEJbW9kZWxOYW1lERJBbmRyb2lkIDQuNCBUYWJsZXQRBmhlaWdodAgBQBEFd2lkdGgIAeARB3JldmVudWUMP7ItDlYEGJMRCGNsaWNrVXJsABEFdGFnaWQAEQRudXJsEe9odHRwOi8vdXNwcmljZXYyLm1kc3AuYXZhenV0cmFja2luZy5uZXQvcHJpY2Uvc3RhcnRtZWFwcD9ycWlkPTRmY2FlNjg2LTEyYTEtNDM4My1iOTkxLTc1OWZjMjNmOGU4NSZiaWRpZD0zNjQwNjc2OTY3NjI1NjY0MDk2JmFjY2lkPTg0NSZwcmljZT0wLjA3MSZhZGlkPTEyNTcxXzc2NDM4JnVpZD0maW1waWQ9NzUzMDkzNDAtNWM4ZS00Y2JmLWFlNmYtMGYzNTU3YWJhYTM3JmFmY2FwPWQ1MC4xMjEuODQuMjM1JmNmY2FwPREKcmVzb2x1dGlvbgYAEQ1kc3BNb2R1bGVuYW1lEQVBdmF6dREKY2FtcGFpZ25JZBEFMTI1NzERCmNyZWF0aXZlSWQRBTc2NDM4\");document.body.appendChild(tracker);};register=function(){try {audit=document.getElementsByTagName(\"a\")[0];audit.addEventListener(\"click\",function(e){generateTracker(this);},true);}catch(err) {console.log(err.message);}};setTimeout(register,1500);</script><img src='beaconUrl' height=1 width=1 border=0 style=\"display:none;\"/></body></html>",
                dcpStartMeAppAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPStartMeAppGetName() throws Exception {
        assertEquals(dcpStartMeAppAdnetwork.getName(), "startmeappdcp");
    }
}
