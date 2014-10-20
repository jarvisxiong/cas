package com.inmobi.adserve.channels.adnetworks;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import io.netty.channel.Channel;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.inmobi.adserve.channels.adnetworks.ix.IXAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.api.config.ServerConfig;
import com.inmobi.adserve.channels.api.provider.AsyncHttpClientProvider;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.entity.WapSiteUACEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.casthrift.ix.Bid;
import com.inmobi.casthrift.ix.IXBidResponse;
import com.inmobi.casthrift.ix.SeatBid;


public class IXAdNetworkTest extends TestCase {

    private Configuration mockConfig = null;
    private final String debug = "debug";
    private final String loggerConf = "/tmp/channel-server.properties";
    private IXAdNetwork ixAdNetwork;
    private final SASRequestParameters sasParams = new SASRequestParameters();
    private final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    private final String ixAdvId = "id";
    IXBidResponse bidResponse;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        final String advertiserName = "ix";
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getString(advertiserName + ".ixMethod")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".userName")).andReturn("test").anyTimes();
        expect(mockConfig.getString(advertiserName + ".password")).andReturn("api").anyTimes();
        expect(mockConfig.getInt(advertiserName + ".accountId")).andReturn(11726).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".htmlSupported", true)).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".nativeSupported", false)).andReturn(false).anyTimes();
        expect(mockConfig.getStringArray("ix.blockedAdvertisers")).andReturn(
                new String[] {"king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com",
                        "supercell.com"}).anyTimes();
        expect(mockConfig.getList("ix.globalBlind")).andReturn(new ArrayList<String>(Arrays.asList("1", "2")))
                .anyTimes();
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
        sasParams.setSource("app");
        sasParams.setDst(8);
        final String urlBase = "";
        final CurrencyConversionEntity currencyConversionEntity = EasyMock.createMock(CurrencyConversionEntity.class);
        EasyMock.expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
        EasyMock.replay(currencyConversionEntity);
        final RepositoryHelper repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
                .andReturn(currencyConversionEntity).anyTimes();
        EasyMock.replay(repositoryHelper);

        ixAdNetwork =
                new IXAdNetwork(mockConfig, null, base, serverChannel, urlBase, "ix", 200, repositoryHelper, true);

        final Field asyncHttpClientProviderField = IXAdNetwork.class.getDeclaredField("asyncHttpClientProvider");
        asyncHttpClientProviderField.setAccessible(true);
        final ServerConfig serverConfig = createMock(ServerConfig.class);
        expect(serverConfig.getDcpRequestTimeoutInMillis()).andReturn(800).anyTimes();
        expect(serverConfig.getRtbRequestTimeoutInMillis()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxDcpOutGoingConnections()).andReturn(200).anyTimes();
        expect(serverConfig.getMaxRtbOutGoingConnections()).andReturn(200).anyTimes();
        replay(serverConfig);
        final AsyncHttpClientProvider asyncHttpClientProvider =
                new AsyncHttpClientProvider(serverConfig, Executors.newCachedThreadPool());
        asyncHttpClientProvider.setup();
        asyncHttpClientProviderField.set(null, asyncHttpClientProvider);

        final Bid bid2 = new Bid();
        bid2.id = "ab73dd4868a0bbadf8fd7527d95136b4";
        bid2.price = 2.4028260707855225;
        bid2.crid = "CRID";
        bid2.adm =
                "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>";
        bid2.impid = "impressionId";
        bid2.estimated = 0;
        bid2.pmptier = 3;
        bid2.aqid = "Test_AQID";
        final List<Bid> bidList = new ArrayList<Bid>();
        bidList.add(bid2);
        final SeatBid seatBid = new SeatBid();
        seatBid.seat = "TO-BE-DETERMINED";
        seatBid.bid = bidList;
        final List<SeatBid> seatBidList = new ArrayList<SeatBid>();
        seatBidList.add(seatBid);
        bidResponse = new IXBidResponse();
        bidResponse.setSeatbid(seatBidList);
        bidResponse.id = "SGu1Jpq1IO";
        bidResponse.bidid = "ac1a2c944cff0a176643079625b0cad4a1bbe4a3";

        ixAdNetwork.setBidResponse(bidResponse);
    }



    @Test
    public void testGetRequestUri() throws URISyntaxException {
        final URI uri = new URI("urlBase");
        ixAdNetwork.setUrlArg("urlArg");
        ixAdNetwork.setUrlBase("urlBase");
        assertEquals(uri, ixAdNetwork.getRequestUri());
    }

    /*
     * @Test public void testGetHttpRequestBidRequestNull() throws Exception { URI uri = new
     * URI("http://localhost:8800?urlArg="); HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
     * HttpMethod.POST, uri.toASCIIString()); httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
     * httpRequest.setHeader("x_openrtb_version", "2"); rtbAdNetwork.setUrlArg("urlArg");
     * rtbAdNetwork.setUrlBase("http://localhost:8800"); assertEquals(null, rtbAdNetwork.getHttpRequest()); }
     */
    /*
     * @Test public void testGetHttpRequestBidRequestNotNull() throws Exception { URI uri = new
     * URI("http://localhost:8800"); HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
     * HttpMethod.POST, uri.toASCIIString()); httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
     * httpRequest.setHeader("x-openrtb-version", "2.0"); httpRequest.setHeader(HttpHeaders.Names.CONNECTION,
     * HttpHeaders.Values.CLOSE); httpRequest.setHeader(HttpHeaders.Names.HOST, uri.getHost());
     * httpRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0"); rtbAdNetwork.setUrlArg("urlArg"); StringBuilder str
     * = new StringBuilder(); str .append(
     * "{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"imp\":[{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"banner\":{\"w\":120,\"h\":20,\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\"},\"bidfloorcur\":\"USD\",\"iframebuster\":[\"None\"]}],\"app\":{\"id\":\"0000000000\",\"cat\":[\"IAB1-1\",\"IAB24\",\"IAB5\"]},\"device\":{\"ua\":\"Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334\",\"ip\":\"206.29.182.240\",\"geo\":{\"lat\":37.442901611328125,\"lon\":-122.15139770507812,\"type\":2},\"connectiontype\":2},\"user\":{\"id\":\"1234\",\"buyerid\":\"1234\",\"yob\":1987,\"gender\":\"Male\"},\"at\":2,\"tmax\":200,\"cur\":[\"USD\"]}"
     * ); StringBuilder responseAdm = new StringBuilder();
     * responseAdm.append("<html><body style=\"margin:0;padding:0;\">"); responseAdm .append(
     * "<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>"
     * ); responseAdm.append("<img src=\'\' height=1 width=1 border=0 /></body></html>"); String clickUrl =
     * "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1"
     * ; String beaconUrl = ""; String externalSiteKey = "f6wqjq1r5v"; ChannelSegmentEntity entity = new
     * ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId, null, null, null, 0, null, null,
     * true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0, null, false, false, false, false, false,
     * false, false, false, false, false, null, new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
     * rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
     * TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
     * rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
     * assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent); rtbAdNetwork.setSecondBidPrice(0.23);
     * rtbAdNetwork.setEncryptedBid("abc"); String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
     * assertEquals(afterMacros, rtbAdNetwork.responseContent); rtbAdNetwork.parseResponse(str.toString(),
     * HttpResponseStatus.NOT_FOUND); assertEquals("", rtbAdNetwork.responseContent);
     * 
     * rtbAdNetwork.setBidRequest(new BidRequest()); rtbAdNetwork.setUrlBase("http://localhost:8800");
     * assertEquals(httpRequest.toString(), rtbAdNetwork.getHttpRequest().toString()); }
     */

    @Test
    public void testConfigureParameters() {
        final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("wap");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.setLatLong("37.4429,-122.1514");
        casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        final String clickUrl =
                "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        final String externalSiteKey = "f6wqjq1r5v";
        final String beaconUrl = "";
        final ChannelSegmentEntity entity =
                new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null, 0,
                        null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null,
                        0, null, false, false, false, false, false, false, false, false, false, false, null,
                        new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
        assertEquals(
                ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl),
                false);
    }

    @Test
    public void testShouldNotConfigureAdapterIfSiteIdNotSentInAdditionalParams() {
        boolean adapterCreated = true;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null,
                            0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                            new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");
            sasParams.setSource("wap");
            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            // builder.setAppType("Games");
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertFalse(adapterCreated);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertFalse(adapterCreated);
        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }

    public void testShouldNotConfigureAdapterIfZoneIdNotSentInAdditionalParams() {
        boolean adapterCreated = true;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null,
                            0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"site\":\"38132\"}"), new ArrayList<Integer>(), 0.0d, null, null, 0,
                            new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");
            sasParams.setSource("wap");
            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            // builder.setAppType("Games");
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertFalse(adapterCreated);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertFalse(adapterCreated);
        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }

    @Test
    public void testShouldSetRightBlindListIfTransparencyEnabled() {
        boolean adapterCreated = false;
        List<Integer> blindList;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null,
                            0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(), 0.0d,
                            null, null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");
            sasParams.setSource("wap");
            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            builder.setTransparencyEnabled(false);
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");

            // Test case for transparency false
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().blind, 1);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().blind, 1);

            // Test Cases for transparency=true
            blindList = new ArrayList<Integer>(Arrays.asList(1, 2));

            // Test case when site_blind_list and pub_blind_list are null, should take global blind list, set above.
            sasParams.setSource("wap");
            builder.setTransparencyEnabled(true);
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));

            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().blind, 0);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().blindbuyers, blindList);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().blind, 0);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().blindbuyers, blindList);

            // Test case when site_blind_list or pub_blind_list is present
            sasParams.setSource("wap");
            builder.setTransparencyEnabled(true);
            blindList = new ArrayList<Integer>(Arrays.asList(8, 2, 3));
            builder.setBlindList(blindList);
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));

            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().blind, 0);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getTransparency().blindbuyers, blindList);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().blind, 0);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getTransparency().blindbuyers, blindList);

        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }

    @Test
    public void testShouldSetSensitivityCorrectlyInAdQuality() {
        boolean adapterCreated = false;
        final String externalSiteKey = "f6wqjq1r5v";

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null,
                            0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(), 0.0d,
                            null, null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");

            final WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");

            sasParams.setSource("wap");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getAq().getSensitivity(), "high");


            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getAq().getSensitivity(), "high");

            // if site type is performance
            sasParams.setSiteType("PERFORMANCE");

            sasParams.setSource("wap");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getSite().getAq().getSensitivity(), "low");


            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertEquals(ixAdNetwork.getBidRequest().getApp().getAq().getSensitivity(), "low");


        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }


    @Test
    public void testShouldSetSiteOrAppNotBoth() {
        boolean adapterCreated = false;
        final String externalSiteKey = "f6wqjq1r5v";
        WapSiteUACEntity.Builder builder;

        try {
            final ChannelSegmentEntity entity =
                    new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId, null, null, null,
                            0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null,
                            null, 0, null, false, false, false, false, false, false, false, false, false, false,
                            new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(), 0.0d,
                            null, null, 0, new Integer[] {0}));

            final CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");
            sasParams.setSource("wap");
            sasParams.setSiteType("PERFORMANCE");
            sasParams.setSiteIncId(423);
            builder = WapSiteUACEntity.newBuilder();
            builder.setAppType("Games");
            builder.setSiteName("TESTSITE");
            builder.setSiteUrl("www.testSite.com");
            builder.setTransparencyEnabled(true);
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.setImpressionId("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getApp());
            assertNotNull(ixAdNetwork.getBidRequest().getSite());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getName(), "TESTSITE");
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPage(), "www.testSite.com");
            assertEquals(ixAdNetwork.getBidRequest().getSite().getBlocklists(),
                    Lists.newArrayList("blk423", "InMobiPERF"));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPublisher().getExt().getRp().getAccount_id(), 11726);

            // checking for blocked list if siteType is not PERFORMANCE, also if site is not transparent

            sasParams.setSiteType("FAMILYSAFE");
            sasParams.setSiteIncId(423);
            builder = WapSiteUACEntity.newBuilder();
            builder.setAppType("Games");
            builder.setSiteName("TESTSITE");
            builder.setSiteUrl("www.testSite.com");
            builder.setTransparencyEnabled(false);

            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));

            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getApp());
            assertNotNull(ixAdNetwork.getBidRequest().getSite());
            assertNotSame(ixAdNetwork.getBidRequest().getSite().getId(), "some_site_id");
            // assertEquals(ixAdNetwork.getBidRequest().getSite().getId(), getBlindedSiteId(sasParams.getSiteIncId(),
            // entity.getIncId(getCreativeType())));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getName(), "Games");
            assertNull(ixAdNetwork.getBidRequest().getSite().getPage());
            assertEquals(ixAdNetwork.getBidRequest().getSite().getBlocklists(),
                    Lists.newArrayList("blk423", "InMobiFS"));
            assertEquals(ixAdNetwork.getBidRequest().getSite().getPublisher().getExt().getRp().getAccount_id(), 11726);

            sasParams.setSource("app");
            adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertTrue(adapterCreated);
            assertNull(ixAdNetwork.getBidRequest().getSite());
            assertNotNull(ixAdNetwork.getBidRequest().getApp());

        } catch (final JSONException e) {
            System.out.println("JSON EXCEPtion in creating new channel segment entity");
        }

    }
    /*
        public void testShouldSetSiteObjectCorrectly() {
            boolean adapterCreated = false;
            String externalSiteKey = "f6wqjq1r5v";

            try {
                ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId,
                        null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[]{0L}, true, null, null, 0,
                        null, false, false, false, false, false, false, false, false, false, false, new JSONObject("{\"3\":\"160212\",\"site\":\"38132\"}"), new ArrayList<Integer>(),
                        0.0d, null, null, 0, new Integer[]{0}));

                CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
                sasParams.setRemoteHostIp("206.29.182.240");
                sasParams.setSiteId("some_site_id");
                sasParams.setSource("wap");
                WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
                //   builder.setAppType("Games");
                sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
                sasParams.setCategories(Lists.newArrayList(3L, 15L, 12L, 11L));
                sasParams.setUserAgent(
                        "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
                casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
                adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

                assertTrue(adapterCreated);

                assertNotNull(ixAdNetwork.getBidRequest().getSite());
                assertEquals(ixAdNetwork.getBidRequest().getSite().getId(),"some_site_id");
                assertEquals(ixAdNetwork.getBidRequest().getSite().getName(),"");

                sasParams.setSource("app");
                adapterCreated = ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

                assertTrue(adapterCreated);
                assertNull(ixAdNetwork.getBidRequest().getSite());
                assertNotNull(ixAdNetwork.getBidRequest().getApp());

            }
            catch (JSONException e){
                System.out.println("JSON EXCEPtion in creating new channel segment entity");
            }

        }

    /*

        @Test
        public void testShouldTestCategorySetForSiteNameOrAppName() {
            String externalSiteKey = "f6wqjq1r5v";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSiteId("some_site_id");
            sasParams.setSource("wap");
            WapSiteUACEntity.Builder builder = WapSiteUACEntity.newBuilder();
            builder.setAppType("Games");
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            sasParams.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            //First UAC Entity Category should be present as Site Name.
            assertEquals("Games", ixAdNetwork.getBidRequest().getSite().getName());

            //For App, First UAC Entity Category should be present as App Name.
            sasParams.setSource("app");
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
            assertEquals("Games", ixAdNetwork.getBidRequest().getApp().getName());

            //If WapSiteUACEntity is null, then it should fallback to InMobi categories.
            sasParams.setSource("app");
            sasParams.setWapSiteUACEntity(null);
            sasParams.setCategories(Lists.newArrayList(15L, 12L, 11L));
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
            // 15 mean board games. Refer to CategoryList
            assertEquals("Board", ixAdNetwork.getBidRequest().getApp().getName());

            //If WapSiteUACEntity is not null, then it should set primary category name from uac.
            sasParams.setSource("app");
            builder = WapSiteUACEntity.newBuilder();
            builder.setAppType("Social");
            sasParams.setWapSiteUACEntity(new WapSiteUACEntity(builder));
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
            // Setting primary category name from uac.
            assertEquals("Social", ixAdNetwork.getBidRequest().getApp().getName());

            //If WapSiteUACEntity is null, then it should fallback to InMobi categories.
            sasParams.setSource("wap");
            sasParams.setWapSiteUACEntity(null);
            sasParams.setCategories(Lists.newArrayList(11L, 12L, 15L));
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");
            // 11 mean Games. Refer to CategoryList
            assertEquals("Games", ixAdNetwork.getBidRequest().getSite().getName());

            //If WapSiteUACEntity and InMobi categories are null.
            sasParams.setSource("wap");
            sasParams.setWapSiteUACEntity(null);
            ArrayList<Long> list = new ArrayList<Long>();
            sasParams.setCategories(list);
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            assertEquals("miscellenous", ixAdNetwork.getBidRequest().getSite().getName());
        }


        @Test
        public void testShouldHaveFixedBlockedAdvertisers() {
            String externalSiteKey = "f6wqjq1r5v";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSource("wap");
            sasParams.setUserAgent(
                    "Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            //Expected Blocked Advertisers
            ArrayList<String> expectedBlockedAdvertisers = Lists.newArrayList("king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com");
            assertNull(casInternalRequestParameters.blockedAdvertisers);
            assertEquals(6, ixAdNetwork.getBidRequest().getBadv().size());
            assertTrue(ixAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
        }

        @Test
        public void testShouldAddFixedBlockedAdvertisersForExistingBlockedList() {
            String externalSiteKey = "f6wqjq1r5v";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            casInternalRequestParameters.blockedAdvertisers = Lists.newArrayList("abcd.com");
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams.setSource("wap");
            sasParams.setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, "", "");

            //Expected Blocked Advertisers
            ArrayList<String> expectedBlockedAdvertisers = Lists.newArrayList("abcd.com", "king.com", "supercell.net", "paps.com", "fhs.com", "china.supercell.com", "supercell.com");
            assertEquals(1, casInternalRequestParameters.blockedAdvertisers.size());
            assertEquals(7, ixAdNetwork.getBidRequest().getBadv().size());
            assertTrue(ixAdNetwork.getBidRequest().getBadv().containsAll(expectedBlockedAdvertisers));
        }

        @Test
        public void testConfigureParametersWithAllsasparams() {
            SASRequestParameters sasParams = new SASRequestParameters();
            CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
            sasParams.setSiteId("123");
            sasParams.setSource("app");
            sasParams.setSlot((short)1);
            Long[] catLong = new Long[2];
            catLong[0] = (long) 1;
            catLong[1] = (long) 2;
            sasParams.setCategories(Arrays.asList(catLong));
            sasParams.setLocSrc("wifi");
            sasParams.setGender("Male");
            casInternalRequestParameters.uid = "1234";
            sasParams.setAge((short)26);
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.latLong = "37.4429,-122.1514";
            casInternalRequestParameters.impressionId = ("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            casInternalRequestParameters.auctionId = ("4f8d98e2-4bbd-40bc-8795-22da170700f9");
            String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
            String externalSiteKey = "f6wqjq1r5v";
            String beaconUrl = "";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            assertEquals(
                    ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl),
                    true);
        }

        @Test
        public void testRtbGetName() throws Exception {
            assertEquals(ixAdNetwork.getName(), "rtb");
        }

        @Test
        public void testReplaceMacros() {
            String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
            String externalSiteKey = "f6wqjq1r5v";
            String beaconUrl = "";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
            ixAdNetwork.setCallbackUrl("http://ix:8970/${AUCTION_ID}/${AUCTION_BID_ID}");
            ixAdNetwork.setCallbackUrl(ixAdNetwork.replaceIXMacros(ixAdNetwork.getCallbackUrl()));
            assertEquals("http://ix:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3",
                    ixAdNetwork.getCallbackUrl());
        }

        @Test
        public void testReplaceMacrosAllPosibilities() {
            String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
            String externalSiteKey = "f6wqjq1r5v";
            String beaconUrl = "";
            sasParams.setSource("app");
            sasParams.setRemoteHostIp("206.29.182.240");
            sasParams
                    .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
            casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
            ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(ixAdvId,
                    null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                    null, false, false, false, false, false, false, false, false, false, false, null,
                    new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[] {0}));
            ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
            ixAdNetwork
                    .setCallbackUrl("http://ix:8970/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_IMP_ID}/${AUCTION_SEAT_ID}/${AUCTION_AD_ID}/${AUCTION_PRICE}/${AUCTION_CURRENCY}");
            ixAdNetwork.setEncryptedBid("abc");
            ixAdNetwork.setCallbackUrl(ixAdNetwork.replaceIXMacros(ixAdNetwork.getCallbackUrl()));
            assertEquals(
                    "http://ix:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3/4f8d98e2-4bbd-40bc-8795-22da170700f9/TO-BE-DETERMINED/1335571993285/0.0/USD",
                    ixAdNetwork.getCallbackUrl());
        }

        */



    /*
    @Test
    public void testParseResponseWithRMD() throws TException {
        bidResponse.setCur("RMD");
        bidResponse.getSeatbid().get(0).getBidIterator().next().setNurl("${AUCTION_PRICE}${AUCTION_CURRENCY}");
        StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm
                .append("<img src=\'?b=${WIN_BID}\' height=1 width=1 border=0 /><img src=\'${AUCTION_PRICE}${AUCTION_CURRENCY}\' height=1 width=1 border=0 />");
        responseAdm.append("</body></html>");
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String beaconUrl = "";
        String externalSiteKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
                null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[]{0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[]{0}));
        sasParams.setDst(2);
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent);
        rtbAdNetwork.setEncryptedBid("0.23");
        rtbAdNetwork.setSecondBidPrice(0.23);
        String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.responseContent);
    }

    @Test
    public void testConfigureParametersTransparency() {
        Long[] catLong = new Long[2];
        catLong[0] = (long) 1;
        catLong[1] = (long) 2;

        SASRequestParameters sasParams = new SASRequestParameters();
        sasParams.setSiteId("4028cba631b705570131d1bd19f201b2"); // Transparency to be included for this site ID.
        sasParams.setSource("app"); // App object.
        sasParams.setOsId(3); // Android OS.
        sasParams.setSlot((short) 1);
        sasParams.setCategories(Arrays.asList(catLong));
        sasParams.setLocSrc("wifi");
        sasParams.setGender("Male");
        sasParams.setAge((short) 26);
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");

        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        casInternalRequestParameters.uid = "1234";
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.impressionId = ("4f8d98e2-4bbd-40bc-8795-22da170700f9");
        casInternalRequestParameters.auctionId = ("4f8d98e2-4bbd-40bc-8795-22da170700f9");

        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "f6wqjq1r5v";
        String beaconUrl = "";

        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
                null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, new Long[] {0L}, true, null, null, 0,
                null, false, false, false, false, false, false, false, false, false, false, null,
                new ArrayList<Integer>(), 0.0d, null, null, 32, new Integer[]{0}));
        ixAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        String ActualBundle = ixAdNetwork.getBidRequest().app.bundle.toString();

        // Compare the bundle value.
        assertEquals(ActualBundle, "com.dreamstep.wBESTLOVEPOEMS");
    }
    */
}
