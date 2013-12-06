package com.inmobi.adserve.channels.adnetworks;

import com.inmobi.adserve.channels.adnetworks.rtb.ImpressionCallbackHelper;
import com.inmobi.adserve.channels.adnetworks.rtb.RtbAdNetwork;
import com.inmobi.adserve.channels.api.CasInternalRequestParameters;
import com.inmobi.adserve.channels.api.Formatter;
import com.inmobi.adserve.channels.api.HttpRequestHandlerBase;
import com.inmobi.adserve.channels.api.SASRequestParameters;
import com.inmobi.adserve.channels.entity.ChannelSegmentEntity;
import com.inmobi.adserve.channels.entity.CurrencyConversionEntity;
import com.inmobi.adserve.channels.repository.RepositoryHelper;
import com.inmobi.adserve.channels.util.DebugLogger;
import com.inmobi.casthrift.rtb.Bid;
import com.inmobi.casthrift.rtb.BidRequest;
import com.inmobi.casthrift.rtb.BidResponse;
import com.inmobi.casthrift.rtb.SeatBid;
import junit.framework.TestCase;
import org.apache.commons.configuration.Configuration;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.easymock.EasyMock;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.*;
import org.testng.annotations.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;


public class RtbAdnetworkTest extends TestCase {

    private Configuration                mockConfig                   = null;
    private final String                 debug                        = "debug";
    private final String                 loggerConf                   = "/tmp/channel-server.properties";
    private ClientBootstrap              clientBootstrap              = null;
    private DebugLogger                  logger;
    private RtbAdNetwork                 rtbAdNetwork;
    private SASRequestParameters         sasParams                    = new SASRequestParameters();
    private CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
    private String                       rtbAdvId                     = "id";
    BidResponse                          bidResponse;

    public void prepareMockConfig() {
        mockConfig = createMock(Configuration.class);
        expect(mockConfig.getString("debug")).andReturn(debug).anyTimes();
        expect(mockConfig.getString("slf4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/logger.xml");
        expect(mockConfig.getString("log4jLoggerConf")).andReturn("/opt/mkhoj/conf/cas/channel-server.properties");
        String advertiserName = "rtb";
        expect(mockConfig.getString(advertiserName + ".advertiserId")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".urlArg")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbVer", "2.0")).andReturn("2.0").anyTimes();
        expect(mockConfig.getString(advertiserName + ".wnUrlback")).andReturn("").anyTimes();
        expect(mockConfig.getString(advertiserName + ".rtbMethod")).andReturn("").anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWnRequired")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".isWinFromClient")).andReturn(true).anyTimes();
        expect(mockConfig.getBoolean(advertiserName + ".siteBlinded")).andReturn(true).anyTimes();
        replay(mockConfig);
    }

    @Override
    public void setUp() throws Exception {
        File f;
        f = new File(loggerConf);
        if (!f.exists()) {
            f.createNewFile();
        }
        MessageEvent serverEvent = createMock(MessageEvent.class);
        HttpRequestHandlerBase base = createMock(HttpRequestHandlerBase.class);
        clientBootstrap = createMock(ClientBootstrap.class);
        prepareMockConfig();
        DebugLogger.init(mockConfig);
        Formatter.init();
        sasParams.setSource("app");
        sasParams.setDst(2);
        logger = new DebugLogger();
        String urlBase = "";
        CurrencyConversionEntity currencyConversionEntity = EasyMock.createMock(CurrencyConversionEntity.class);
        EasyMock.expect(currencyConversionEntity.getConversionRate()).andReturn(10.0).anyTimes();
        EasyMock.replay(currencyConversionEntity);
        RepositoryHelper repositoryHelper = EasyMock.createMock(RepositoryHelper.class);
        EasyMock.expect(repositoryHelper.queryCurrencyConversionRepository(EasyMock.isA(String.class)))
                    .andReturn(currencyConversionEntity)
                    .anyTimes();
        EasyMock.replay(repositoryHelper);
        rtbAdNetwork = new RtbAdNetwork(logger, mockConfig, clientBootstrap, base, serverEvent, urlBase, "rtb", 200,
                repositoryHelper);
        Bid bid2 = new Bid();
        bid2.id = "ab73dd4868a0bbadf8fd7527d95136b4";
        bid2.adid = "1335571993285";
        bid2.price = 0.2;
        bid2.cid = "cid";
        bid2.crid = "crid";
        bid2.adm = "<style type='text/css'>body { margin:0;padding:0 }  </style> <p align='center'><a href='http://www.inmobi.com/' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>";
        bid2.impid = "impressionId";
        List<Bid> bidList = new ArrayList<Bid>();
        bidList.add(bid2);
        SeatBid seatBid = new SeatBid();
        seatBid.seat = "TO-BE-DETERMINED";
        seatBid.bid = bidList;
        List<SeatBid> seatBidList = new ArrayList<SeatBid>();
        seatBidList.add(seatBid);
        bidResponse = new BidResponse();
        bidResponse.setSeatbid(seatBidList);
        bidResponse.id = "SGu1Jpq1IO";
        bidResponse.bidid = "ac1a2c944cff0a176643079625b0cad4a1bbe4a3";
        bidResponse.cur = "USD";
        rtbAdNetwork.setBidResponse(bidResponse);
    }

    @Test
    public void testImpressionCallback() {
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String beaconUrl = "";
        String externalSiteKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        RtbAdNetwork.impressionCallbackHelper = createMock(ImpressionCallbackHelper.class);
        expect(
            RtbAdNetwork.impressionCallbackHelper.writeResponse(isA(ClientBootstrap.class), isA(DebugLogger.class),
                isA(URI.class), isA(DefaultHttpRequest.class))).andReturn(true).anyTimes();
        replay(RtbAdNetwork.impressionCallbackHelper);
        rtbAdNetwork.setBidResponse(bidResponse);
        rtbAdNetwork.impressionCallback();
    }

    @Test
    public void testGetRequestUri() throws URISyntaxException {
        URI uri = new URI("urlBase");
        rtbAdNetwork.setUrlArg("urlArg");
        rtbAdNetwork.setUrlBase("urlBase");
        assertEquals(uri, rtbAdNetwork.getRequestUri());
    }

    @Test
    public void testGetHttpRequestBidRequestNull() throws Exception {
        URI uri = new URI("http://localhost:8800?urlArg=");
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
        httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        httpRequest.setHeader("x_openrtb_version", "2");
        rtbAdNetwork.setUrlArg("urlArg");
        rtbAdNetwork.setUrlBase("http://localhost:8800");
        assertEquals(null, rtbAdNetwork.getHttpRequest());
    }

    @Test
    public void testGetHttpRequestBidRequestNotNull() throws Exception {
        URI uri = new URI("http://localhost:8800");
        HttpRequest httpRequest = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uri.toASCIIString());
        httpRequest.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        httpRequest.setHeader("x-openrtb-version", "2.0");
        httpRequest.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
        httpRequest.setHeader(HttpHeaders.Names.HOST, uri.getHost());
        httpRequest.setHeader(HttpHeaders.Names.CONTENT_LENGTH, "0");
        rtbAdNetwork.setUrlArg("urlArg");
        StringBuilder str = new StringBuilder();
        str
                .append("{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"imp\":[{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"banner\":{\"w\":120,\"h\":20,\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\"},\"bidfloorcur\":\"USD\",\"iframebuster\":[\"None\"]}],\"app\":{\"id\":\"0000000000\",\"cat\":[\"IAB1-1\",\"IAB24\",\"IAB5\"]},\"device\":{\"ua\":\"Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334\",\"ip\":\"206.29.182.240\",\"geo\":{\"lat\":37.442901611328125,\"lon\":-122.15139770507812,\"type\":2},\"connectiontype\":2},\"user\":{\"id\":\"1234\",\"buyerid\":\"1234\",\"yob\":1987,\"gender\":\"Male\"},\"at\":2,\"tmax\":200,\"cur\":[\"USD\"]}");
        StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm.append("<img src=\'\' height=1 width=1 border=0 /></body></html>");
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String beaconUrl = "";
        String externalSiteKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent);
        rtbAdNetwork.setSecondBidPrice(0.23);
        rtbAdNetwork.setEncryptedBid("abc");
        String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.responseContent);
        rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.NOT_FOUND);
        assertEquals("", rtbAdNetwork.responseContent);

        rtbAdNetwork.setBidRequest(new BidRequest());
        rtbAdNetwork.setUrlBase("http://localhost:8800");
        assertEquals(httpRequest.toString(), rtbAdNetwork.getHttpRequest().toString());
    }

    @Test
    public void testConfigureParameters() {
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setRemoteHostIp("206.29.182.240");
        sasParams.setSource("wap");
        sasParams
                .setUserAgent("Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334");
        casInternalRequestParameters.latLong = "37.4429,-122.1514";
        casInternalRequestParameters.impressionId = "4f8d98e2-4bbd-40bc-8795-22da170700f9";
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "f6wqjq1r5v";
        String beaconUrl = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        assertEquals(
            rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl),
            false);
    }

    @Test
    public void testConfigureParametersWithAllsasparams() {
        SASRequestParameters sasParams = new SASRequestParameters();
        CasInternalRequestParameters casInternalRequestParameters = new CasInternalRequestParameters();
        sasParams.setSiteId("123");
        sasParams.setSource("app");
        sasParams.setSlot("1");
        Long[] catLong = new Long[2];
        catLong[0] = (long) 1;
        catLong[1] = (long) 2;
        sasParams.setCategories(Arrays.asList(catLong));
        sasParams.setLocSrc("wifi");
        sasParams.setGenderOrig("Male");
        casInternalRequestParameters.uid = "1234";
        sasParams.setAge("26");
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
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        assertEquals(
            rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl),
            true);
    }

    @Test
    public void testRtbGetName() throws Exception {
        assertEquals(rtbAdNetwork.getName(), "rtb");
    }

    @Test
    public void testReplaceMacros() {
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String externalSiteKey = "f6wqjq1r5v";
        String beaconUrl = "";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        rtbAdNetwork.setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}");
        rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
        assertEquals("http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3",
            rtbAdNetwork.getCallbackUrl());
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
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        rtbAdNetwork
                .setCallbackUrl("http://rtb:8970/${AUCTION_ID}/${AUCTION_BID_ID}/${AUCTION_IMP_ID}/${AUCTION_SEAT_ID}/${AUCTION_AD_ID}/${AUCTION_PRICE}/${AUCTION_CURRENCY}");
        rtbAdNetwork.setEncryptedBid("abc");
        rtbAdNetwork.setCallbackUrl(rtbAdNetwork.replaceRTBMacros(rtbAdNetwork.getCallbackUrl()));
        assertEquals(
            "http://rtb:8970/SGu1Jpq1IO/ac1a2c944cff0a176643079625b0cad4a1bbe4a3/4f8d98e2-4bbd-40bc-8795-22da170700f9/TO-BE-DETERMINED/1335571993285/0.0/USD",
            rtbAdNetwork.getCallbackUrl());
    }

    @Test
    public void testParseResponse() throws TException {
        StringBuilder str = new StringBuilder();
        str
                .append("{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"imp\":[{\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\",\"banner\":{\"w\":120,\"h\":20,\"id\":\"4f8d98e2-4bbd-40bc-8795-22da170700f9\"},\"bidfloorcur\":\"USD\",\"iframebuster\":[\"None\"]}],\"app\":{\"id\":\"0000000000\",\"cat\":[\"IAB1-1\",\"IAB24\",\"IAB5\"]},\"device\":{\"ua\":\"Mozilla%2F5.0+%28iPhone%3B+CPU+iPhone+OS+5_0+like+Mac+OS+X%29+AppleWebKit%2F534.46+%28KHTML%2C+like+Gecko%29+Mobile%2F9A334\",\"ip\":\"206.29.182.240\",\"geo\":{\"lat\":37.442901611328125,\"lon\":-122.15139770507812,\"type\":2},\"connectiontype\":2},\"user\":{\"id\":\"1234\",\"buyerid\":\"1234\",\"yob\":1987,\"gender\":\"Male\"},\"at\":2,\"tmax\":200,\"cur\":[\"USD\"]}");
        StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm.append("<img src=\'\' height=1 width=1 border=0 />");
        responseAdm.append("</body></html>");
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String beaconUrl = "";
        String externalSiteKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent);
        rtbAdNetwork.setSecondBidPrice(0.23);
        String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.responseContent);
        rtbAdNetwork.parseResponse(str.toString(), HttpResponseStatus.NOT_FOUND);
        assertEquals("", rtbAdNetwork.responseContent);
    }

    @Test
    public void testParseResponseWithRMD() throws TException {
        bidResponse.setCur("RMD");
        bidResponse.getSeatbid().get(0).getBidIterator().next().setNurl("${AUCTION_PRICE}${AUCTION_CURRENCY}");
        StringBuilder responseAdm = new StringBuilder();
        responseAdm.append("<html><body style=\"margin:0;padding:0;\">");
        responseAdm
                .append("<script src=\"mraid.js\" ></script><style type=\'text/css\'>body { margin:0;padding:0 }  </style> <p align='center'><a href=\'http://www.inmobi.com/\' target='_blank'><img src='http://www.digitalmarket.asia/wp-content/uploads/2012/04/7a4cb5ba9e52331ae91aeee709cd3fe3.jpg' border='0'/></a></p>");
        responseAdm
                .append("<img src=\'\' height=1 width=1 border=0 /><img src=\'${AUCTION_PRICE}${AUCTION_CURRENCY}\' height=1 width=1 border=0 />");
        responseAdm.append("</body></html>");
        String clickUrl = "http://c2.w.inmobi.com/c.asm/4/b/bx5/yaz/2/b/a5/m/0/0/0/202cb962ac59075b964b07152d234b70/4f8d98e2-4bbd-40bc-87e5-22da170600f9/-1/1/9cddca11?ds=1";
        String beaconUrl = "";
        String externalSiteKey = "f6wqjq1r5v";
        ChannelSegmentEntity entity = new ChannelSegmentEntity(AdNetworksTest.getChannelSegmentEntityBuilder(rtbAdvId,
            null, null, null, 0, null, null, true, true, externalSiteKey, null, null, null, 0, true, null, null, 0,
            null, false, false, false, false, false, false, false, false, false, false, null, new ArrayList<Integer>(),
            0.0d, null, null, 32));
        sasParams.setDst(2);
        rtbAdNetwork.configureParameters(sasParams, casInternalRequestParameters, entity, clickUrl, beaconUrl);
        TSerializer serializer = new TSerializer(new TSimpleJSONProtocol.Factory());
        rtbAdNetwork.parseResponse(serializer.toString(bidResponse), HttpResponseStatus.OK);
        assertEquals(responseAdm.toString(), rtbAdNetwork.responseContent);
        rtbAdNetwork.setSecondBidPrice(0.23);
        String afterMacros = rtbAdNetwork.replaceRTBMacros(responseAdm.toString());
        assertEquals(afterMacros, rtbAdNetwork.responseContent);
    }
}
