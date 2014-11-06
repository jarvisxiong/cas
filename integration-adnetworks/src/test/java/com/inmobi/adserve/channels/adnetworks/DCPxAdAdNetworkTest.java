package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.testng.annotations.Test;

import com.inmobi.adserve.channels.adnetworks.xad.DCPxAdAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;

public class DCPxAdAdNetworkTest extends TestCase {
    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";

    private DCPxAdAdNetwork dcpxAdAdnetwork;
    private final String xAdHost = "http://xad.com/rest/banner";
    private final String xAdStatus = "on";
    private final String xAdAdvId = "xadadv1";
    private final String xAdTest = "1";

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("xad.host")).andReturn(xAdHost).anyTimes();
        expect(mockConfig.getString("xad.status")).andReturn(xAdStatus).anyTimes();
        expect(mockConfig.getString("xad.test")).andReturn(xAdTest).anyTimes();
        expect(mockConfig.getString("xad.advertiserId")).andReturn(xAdAdvId).anyTimes();
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
        dcpxAdAdnetwork = new DCPxAdAdNetwork(mockConfig, null, base, serverChannel);
    }

    @Test
    public void testDCPxAdConfigureParameters() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9));
    }

    @Test
    public void testDCPxAdConfigureParametersWithNoUid() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9));
    }

    @Test
    public void testDCPxAdConfigureParametersBlankIP() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp(null);
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15),
                false);
    }

    @Test
    public void testxAdConfigureParametersBlankExtKey() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15),
                false);
    }

    @Test
    public void testDCPxAdConfigureParametersBlankUA() {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent(" ");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15),
                false);
    }

    @Test
    public void testDCPxAdRequestUri() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] {9l, 10l, 13l, 30l}));
        final Long[] segmentCategories = {10l};
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(3);
        sasParams.setSiteId("12345");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, false,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9)) {
            final String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://xad.com/rest/banner?v=1.2&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_IAB8&devid=Mozilla&lat=37.4429&long=-122.1514&uid=202cb962ac59075b964b07152d234b70&uid_type=Android_Id%7CMD5&size=320x48&cat=IAB8&os=Android&instl=0&pt=web&bcat=IAB25-5&bcat=IAB25-4&bcat=IAB25-7&bcat=IAB25-1&bcat=IAB25-3&bcat=IAB25-2&bcat=IAB9-9&bcat=IAB7-9&bcat=IAB7-8&bcat=IAB14-1&bcat=IAB14-2&bcat=IAB14-3&bcat=IAB5-2&bcat=IAB7-45&bcat=IAB7-44&bcat=IAB26&bcat=IAB8-5&bcat=IAB7-3&bcat=IAB25&bcat=IAB23-9&bcat=IAB7-2&bcat=IAB7-5&bcat=IAB23-2&bcat=IAB13-5&bcat=IAB7-10&bcat=IAB7-4&bcat=IAB13-7&bcat=IAB7-6&bcat=IAB7-11&bcat=IAB7-12&bcat=IAB6-7&bcat=IAB7-13&bcat=IAB7-14&bcat=IAB7-16&bcat=IAB7-18&bcat=IAB7-19&bcat=IAB7&bcat=IAB12&bcat=IAB7-21&bcat=IAB11&bcat=IAB7-20&bcat=IAB7-28&bcat=IAB7-29&bcat=IAB7-27&bcat=IAB7-24&bcat=IAB7-25&bcat=IAB7-22&bcat=IAB19-3&bcat=IAB17-18&bcat=IAB7-31&bcat=IAB7-30&bcat=IAB7-37&bcat=IAB11-1&bcat=IAB7-38&bcat=IAB11-2&bcat=IAB7-39&bcat=IAB7-34&bcat=IAB7-36&bcat=IAB23-10&bcat=IAB15-5&bcat=IAB12-1&bcat=IAB12-3&bcat=IAB26-3&bcat=IAB12-2&bcat=IAB26-4&bcat=IAB26-1&bcat=IAB26-2&bcat=IAB7-41&bcat=IAB7-40&bcat=IAB11-5&bcat=IAB11-4&bcat=IAB11-3";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriWithMoreCategories() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setCategories(Arrays.asList(new Long[] {9l, 10l, 13l, 30l}));
        final Long[] segmentCategories = {10l};
        final List<Long> siteCategories = new ArrayList<Long>();
        siteCategories.add(12l);
        siteCategories.add(13l);
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUidIFA("202cb962ac59075b964b07152d234b70");
        casInternalRequestParameters.setBlockedIabCategories(Arrays.asList("IAB10", "IAB21", "IAB12"));
        sasParams.setOsId(5);
        sasParams.setSiteId("12345");
        sasParams.setCategories(siteCategories);
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, true,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9)) {
            final String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://xad.com/rest/banner?v=1.2&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_IAB1&devid=Mozilla&lat=37.4429&long=-122.1514&uid=202cb962ac59075b964b07152d234b70&uid_type=IDFA%7CRAW&size=320x48&cat=IAB1&cat=IAB10-2&cat=IAB19-29&cat=IAB20-1&os=iOS&instl=0&pt=web&bcat=IAB25-5&bcat=IAB25-4&bcat=IAB25-7&bcat=IAB25-1&bcat=IAB25-3&bcat=IAB25-2&bcat=IAB9-9&bcat=IAB7-9&bcat=IAB7-8&bcat=IAB14-1&bcat=IAB14-2&bcat=IAB14-3&bcat=IAB5-2&bcat=IAB7-45&bcat=IAB7-44&bcat=IAB26&bcat=IAB8-5&bcat=IAB7-3&bcat=IAB25&bcat=IAB23-9&bcat=IAB7-2&bcat=IAB7-5&bcat=IAB23-2&bcat=IAB13-5&bcat=IAB7-10&bcat=IAB7-4&bcat=IAB13-7&bcat=IAB21&bcat=IAB7-6&bcat=IAB7-11&bcat=IAB7-12&bcat=IAB6-7&bcat=IAB7-13&bcat=IAB7-14&bcat=IAB7-16&bcat=IAB7-18&bcat=IAB7-19&bcat=IAB7&bcat=IAB10&bcat=IAB12&bcat=IAB7-21&bcat=IAB11&bcat=IAB7-20&bcat=IAB7-28&bcat=IAB7-29&bcat=IAB7-27&bcat=IAB7-24&bcat=IAB7-25&bcat=IAB7-22&bcat=IAB19-3&bcat=IAB17-18&bcat=IAB7-31&bcat=IAB7-30&bcat=IAB7-37&bcat=IAB11-1&bcat=IAB7-38&bcat=IAB11-2&bcat=IAB7-39&bcat=IAB7-34&bcat=IAB7-36&bcat=IAB23-10&bcat=IAB15-5&bcat=IAB12-1&bcat=IAB12-3&bcat=IAB26-3&bcat=IAB12-2&bcat=IAB26-4&bcat=IAB26-1&bcat=IAB26-2&bcat=IAB7-41&bcat=IAB7-40&bcat=IAB11-5&bcat=IAB11-4&bcat=IAB11-3";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriWithDerivedLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("iPhone");
        sasParams.setCategories(Arrays.asList(new Long[] {9l, 10l, 13l, 30l}));
        final Long[] segmentCategories = {10l};
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUidO1("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(5);
        sasParams.setSiteId("12345");
        sasParams.setLocSrc("derived-lat-lon");
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, segmentCategories, true, true, externalKey, null, null, null, new Long[] {0L}, false,
                        null, null, 0, null, false, false, false, false, false, false, false, false, false, false,
                        null, new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 9)) {
            final String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://xad.com/rest/banner?v=1.2&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_IAB8&devid=iPhone&lat=37.4429&long=-122.1514&uid=202cb962ac59075b964b07152d234b70&uid_type=UUID%7CSHA1&size=320x48&cat=IAB8&os=iOS&instl=0&pt=web&bcat=IAB25-5&bcat=IAB25-4&bcat=IAB25-7&bcat=IAB25-1&bcat=IAB25-3&bcat=IAB25-2&bcat=IAB9-9&bcat=IAB7-9&bcat=IAB7-8&bcat=IAB14-1&bcat=IAB14-2&bcat=IAB14-3&bcat=IAB5-2&bcat=IAB7-45&bcat=IAB7-44&bcat=IAB26&bcat=IAB8-5&bcat=IAB7-3&bcat=IAB25&bcat=IAB23-9&bcat=IAB7-2&bcat=IAB7-5&bcat=IAB23-2&bcat=IAB13-5&bcat=IAB7-10&bcat=IAB7-4&bcat=IAB13-7&bcat=IAB7-6&bcat=IAB7-11&bcat=IAB7-12&bcat=IAB6-7&bcat=IAB7-13&bcat=IAB7-14&bcat=IAB7-16&bcat=IAB7-18&bcat=IAB7-19&bcat=IAB7&bcat=IAB12&bcat=IAB7-21&bcat=IAB11&bcat=IAB7-20&bcat=IAB7-28&bcat=IAB7-29&bcat=IAB7-27&bcat=IAB7-24&bcat=IAB7-25&bcat=IAB7-22&bcat=IAB19-3&bcat=IAB17-18&bcat=IAB7-31&bcat=IAB7-30&bcat=IAB7-37&bcat=IAB11-1&bcat=IAB7-38&bcat=IAB11-2&bcat=IAB7-39&bcat=IAB7-34&bcat=IAB7-36&bcat=IAB23-10&bcat=IAB15-5&bcat=IAB12-1&bcat=IAB12-3&bcat=IAB26-3&bcat=IAB12-2&bcat=IAB26-4&bcat=IAB26-1&bcat=IAB26-2&bcat=IAB7-41&bcat=IAB7-40&bcat=IAB11-5&bcat=IAB11-4&bcat=IAB11-3";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriBlankLatLong() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setOsId(3);
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertTrue(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15));
    }

    @Test
    public void testDCPxAdRequestUriNoUdid() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("11.6,-11.87");
        sasParams.setCategories(Arrays.asList(new Long[] {9l, 10l, 13l, 30l}));
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        sasParams.setOsId(3);
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 0, new Integer[] {0}));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15)) {
            final String actualUrl = dcpxAdAdnetwork.getRequestUri().toString();
            final String expectedUrl =
                    "http://xad.com/rest/banner?v=1.2&o_fmt=html5&ip=206.29.182.240&k=1324&appid=00000000-0000-0000-0000-000000000000_&devid=Mozilla&lat=11.6&long=-11.87&uid=7a31d90b848f70062e6281b058cc52fc&uid_type=UUID%7CMD5&size=320x50&cat=&os=Android&instl=0&pt=web&bcat=IAB25-5&bcat=IAB25-4&bcat=IAB25-7&bcat=IAB25-1&bcat=IAB25-3&bcat=IAB25-2&bcat=IAB9-9&bcat=IAB7-9&bcat=IAB7-8&bcat=IAB14-1&bcat=IAB14-2&bcat=IAB14-3&bcat=IAB5-2&bcat=IAB7-45&bcat=IAB7-44&bcat=IAB26&bcat=IAB8-5&bcat=IAB7-3&bcat=IAB25&bcat=IAB23-9&bcat=IAB7-2&bcat=IAB7-5&bcat=IAB23-2&bcat=IAB13-5&bcat=IAB7-10&bcat=IAB7-4&bcat=IAB13-7&bcat=IAB7-6&bcat=IAB7-11&bcat=IAB7-12&bcat=IAB6-7&bcat=IAB7-13&bcat=IAB7-14&bcat=IAB7-16&bcat=IAB7-18&bcat=IAB7-19&bcat=IAB7&bcat=IAB12&bcat=IAB7-21&bcat=IAB11&bcat=IAB7-20&bcat=IAB7-28&bcat=IAB7-29&bcat=IAB7-27&bcat=IAB7-24&bcat=IAB7-25&bcat=IAB7-22&bcat=IAB19-3&bcat=IAB17-18&bcat=IAB7-31&bcat=IAB7-30&bcat=IAB7-37&bcat=IAB11-1&bcat=IAB7-38&bcat=IAB11-2&bcat=IAB7-39&bcat=IAB7-34&bcat=IAB7-36&bcat=IAB23-10&bcat=IAB15-5&bcat=IAB12-1&bcat=IAB12-3&bcat=IAB26-3&bcat=IAB12-2&bcat=IAB26-4&bcat=IAB26-1&bcat=IAB26-2&bcat=IAB7-41&bcat=IAB7-40&bcat=IAB11-5&bcat=IAB11-4&bcat=IAB11-3";
            assertEquals(expectedUrl, actualUrl);
        }
    }

    @Test
    public void testDCPxAdRequestUriBlankSlot() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.setUid("202cb962ac59075b964b07152d234b70");
        sasParams.setCategories(new ArrayList<Long>());
        final String externalKey = "1324";
        final String clurl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertFalse(dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, Short.MAX_VALUE));
    }

    @Test
    public void testDCPxAdParseResponseAd() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setOsId(3);
        sasParams.setSiteId("1234567");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0"
                        + "/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11"
                        + "?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        if (dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4)) {
            final String response =
                    "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
            dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
            assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
            final String expectedResponse =
                    "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
            assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
        }
    }

    @Test
    public void testDCPxAdParseResponseAdWAP() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        sasParams.setSource("wap");
        final String externalKey = "19100";
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4);

        final String response =
                "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseResponseAdApp() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        sasParams.setSource("app");
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4);

        final String response =
                "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseResponseAdAppIMAI() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla");
        final String externalKey = "19100";
        sasParams.setSource("app");
        sasParams.setSdkVersion("a370");
        sasParams.setImaiBaseUrl("http://cdn.inmobi.com/android/mraid.js");
        final String beaconUrl =
                "http://c2.w.inmobi.com/c"
                        + ".asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc"
                        + "-87e5-22da170600f9/-1/1/9cddca11?beacon=true";
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl, (short) 4);

        final String response =
                "<meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script>";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 200);
        final String expectedResponse =
                "<html><head><title></title><meta name=\"viewport\" content=\"user-scalable=0, minimum-scale=1.0, maximum-scale=1.0\"/><style type=\"text/css\">body {margin: 0px; overflow: hidden;} </style></head><body><script type=\"text/javascript\" src=\"http://cdn.inmobi.com/android/mraid.js\"></script><meta charset=\"utf-8\"/> <meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0;\"/> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/zepto.min.js\"></script> <link rel=\"stylesheet\" media=\"all\" href=\"http://test.xad.com/rest/xadbanner/layout/sb15/_ui/css/main_short.css\"/><script type=\"text/javascript\" src= \"http://test.xad.com/rest/xadbanner/layout/validation/validation.js\"> </script> <div id=\"xad-banner-wrapper_outer\"><div id=\"xad-banner-wrapper\"> <a href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\" style=\"text-decoration:none\"><div id=\"bus-holder\"> <img src=\"http://test.xad.com/rest/xadbanner/layout/sb15/template/map_small.png\" id=\"bus-logo\" alt=\"Gs\"/> </div> <p id=\"xad-banner-description\"> <span class=\"descr descr-height\">Fashionable Eyewear for Men &amp; Women in San Anselmo. Stop by Our Store!... </span> <span class=\"location\"> <span id=\"bus\">Focus Opticians</span> </span> </p> </a> <div id=\"banner-action\"> <a class=\"call-it\" href=\"http://test.xad.com/wap/?l_id=L3rwAq7nP7&appid=Dummy221\">call</a> <sub>xAd</sub></div> </div> <img src=\"http://test.xad.com/rest/notify?t=imp&tid=0&k=X3vZEQ0smjqdLj0sRi49wdtRkUulP9SP&v=1.1&uid=null&l_id=L3rwAq7nP7%7E9AYDW0evP7-0%7E1%7E1%7E1%7E3%7ECA%7Eus%7E9AYDW0evP7%7E1%7E0%7E0%7E%7E0%7E1&type=banner\" width=\"0\" height=\"0\"/> </div> <script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/main.js\"></script><script type=\"text/javascript\" src=\"http://test.xad.com/rest/xadbanner/layout/sb15/js/textSizer.js\"></script><script type=\"text/javascript\">$(window).bind('orientationchange', resizeText);$(window).resize(resizeText)</script><img src='http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?beacon=true' height=1 width=1 border=0 style=\"display:none;\"/></body></html>";
        assertEquals(expectedResponse, dcpxAdAdnetwork.getHttpResponseContent());
    }

    @Test
    public void testDCPxAdParseNoAd() throws Exception {
        final String response = "";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 500);
    }

    @Test
    public void testDCPxAdParseEmptyResponseCode() throws Exception {
        final String response = "";
        dcpxAdAdnetwork.parseResponse(response, HttpResponseStatus.OK);
        assertEquals(dcpxAdAdnetwork.getHttpResponseStatusCode(), 500);
        assertEquals(dcpxAdAdnetwork.getHttpResponseContent(), "");
    }

    @Test
    public void testDCPxAdGetId() throws Exception {
        assertEquals(dcpxAdAdnetwork.getId(), "xadadv1");
    }

    @Test
    public void testDCPxAdGetImpressionId() throws Exception {
        final SASRequestParameters sasParams = new SASRequestParameters();
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29"
                + "+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        final String clurl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        sasParams.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String externalKey = "f6wqjq1r5v";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(xAdAdvId, null, null, null, 0,
                        null, null, true, true, externalKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        dcpxAdAdnetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clurl, null, (short) 15);
        assertEquals(dcpxAdAdnetwork.getImpressionId(), "4f8d98e2-4bbd-40bc-8795-22da170700f9");
    }

    @Test
    public void testDCPxAdGetName() throws Exception {
        assertEquals(dcpxAdAdnetwork.getName(), "xad");
    }

    @Test
    public void testDCPxAdIsClickUrlReq() throws Exception {
        assertEquals(dcpxAdAdnetwork.isClickUrlRequired(), false);
    }

    @Test
    public void testDCPxAdIsBeaconUrlReq() throws Exception {
        assertEquals(dcpxAdAdnetwork.isBeaconUrlRequired(), true);
    }
}
